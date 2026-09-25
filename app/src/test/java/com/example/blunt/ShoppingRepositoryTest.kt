package com.example.blunt

import com.example.blunt.data.*
import com.example.blunt.model.*
import com.example.blunt.repository.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class ShoppingRepositoryTest {
    private class MemoryStorage : SnapshotStorage {
        var json: String? = null
        var fail = false
        override fun read(): AppSnapshot? = json?.let { Gson().fromJson(it, AppSnapshot::class.java) }
        override fun write(snapshot: AppSnapshot) { if (fail) error("disk full"); json = Gson().toJson(snapshot) }
    }
    private class Fixture {
        val storage = MemoryStorage()
        var now = 1_800_000_000_000L
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val store = LocalStore(storage, scope) { now }
        val products = FakeProductRepository()
        val auth = FakeAuthRepository(store)
        val cart = FakeCartRepository(store, products)
        val profile = FakeProfileRepository(store)
        val drop = FakeDailyDealRepository(store)
        val orders = FakeOrderRepository(store, products, cart)
        val address = Address("home", "Demo User", "9876543210", "12", "Sample Street", "Indiranagar", "Bengaluru", "Karnataka", "560038")
        suspend fun login() { auth.login("demo@example.com", "password123") }
        fun close() { scope.cancel() }
    }
    private fun test(block: suspend Fixture.() -> Unit) = runBlocking { val f = Fixture(); try { f.block() } finally { f.close() } }
    private suspend fun rejected(block: suspend () -> Unit) { try { block(); fail("Expected rejection") } catch (e: IllegalArgumentException) { assertFalse(e.message.isNullOrBlank()) } catch (e: IllegalStateException) { assertFalse(e.message.isNullOrBlank()) } }

    @Test fun searchFindsBrandCategoryAndProductAndSortDataIsComplete() = test {
        assertEquals(18, products.products.size)
        listOf("Sony", "headphones", "shoes", "gaming", "fashion").forEach { assertTrue(products.search(it, "All").isNotEmpty()) }
        assertTrue(products.search("not-a-product", "All").isEmpty())
        assertTrue(products.products.all { it.mrp >= it.price && it.images.isNotEmpty() && it.colors.isNotEmpty() && it.specifications.isNotEmpty() })
    }
    @Test fun registrationValidatesAndNeverStoresPlaintextPasswords() = test {
        rejected { auth.register("A", "123", "bad", "short", "short") }
        auth.register("Test Shopper", "9123456789", "shopper@example.com", "pass12345", "pass12345")
        assertNull(store.state.value.sessionId)
        rejected { auth.login("shopper@example.com", "wrong") }
        auth.login("9123456789", "pass12345")
        assertEquals("Test Shopper", store.state.value.account?.user?.name)
        assertFalse(storage.json!!.contains("pass12345"))
        rejected { auth.register("Other Shopper", "9123456789", "other@example.com", "pass12345", "pass12345") }
    }
    @Test fun variantsMergeAndTotalStockCannotBeBypassedWithAnotherColor() = test {
        login(); cart.add(CartItem("sony-ch520", "Black", quantity = 3)); cart.add(CartItem("sony-ch520", "Black", quantity = 2))
        assertEquals(5, store.state.value.account!!.cart.single().quantity)
        rejected { cart.add(CartItem("sony-ch520", "Blue", quantity = 6)) }
        rejected { cart.add(CartItem("sony-ch520", "Pink")) }
        rejected { cart.add(CartItem("nike-court", "White", "99")) }
        rejected { cart.add(CartItem("casio-watch", "Silver")) }
    }
    @Test fun cartWishlistAndAddressesAreIsolatedByAccountAndPersisted() = test {
        login(); cart.add(CartItem("sony-ch520", "Black")); profile.toggleWishlist("nike-court"); profile.saveAddress(address)
        auth.logout(); auth.register("Second User", "9123456789", "second@example.com", "password1", "password1"); auth.login("second@example.com", "password1")
        assertTrue(store.state.value.account!!.cart.isEmpty()); assertTrue(store.state.value.account!!.addresses.isEmpty()); assertTrue(store.state.value.account!!.wishlist.isEmpty())
        auth.logout(); login()
        val loaded = storage.read()!!
        assertEquals(1, loaded.account!!.cart.size); assertEquals(setOf("nike-court"), loaded.account!!.wishlist); assertEquals(address, loaded.account!!.addresses.single())
    }
    @Test fun normalCheckoutIsAtomicAndIdempotentAndCancellationRestoresStock() = test {
        login(); cart.add(CartItem("sony-ch520", "Black", quantity = 2))
        rejected { orders.place(null, PaymentMethod.UPI, false, "test-order") }
        assertTrue(store.state.value.account!!.orders.isEmpty()); assertEquals(2, store.state.value.account!!.cart.single().quantity)
        profile.saveAddress(address)
        assertEquals("test-order", orders.place(address.id, PaymentMethod.CARD, false, "test-order"))
        orders.place(address.id, PaymentMethod.CARD, false, "test-order")
        assertEquals(1, store.state.value.account!!.orders.size); assertTrue(store.state.value.account!!.cart.isEmpty()); assertEquals(2, store.state.value.purchasedStock["sony-ch520"])
        orders.cancel("test-order"); assertEquals(OrderStatus.CANCELLED, store.state.value.account!!.orders.single().status); assertEquals(0, store.state.value.purchasedStock["sony-ch520"])
        rejected { orders.cancel("test-order") }
    }
    @Test fun deliveryThresholdAndSaveForLaterWork() = test {
        login(); cart.add(CartItem("minimalist-serum", "30 ml"))
        assertEquals(49, cart.totals(store.state.value.account!!.cart).delivery)
        cart.quantity(store.state.value.account!!.cart.single().key, 3)
        assertEquals(0, cart.totals(store.state.value.account!!.cart).delivery)
        cart.remove(store.state.value.account!!.cart.single().key, true)
        assertTrue(store.state.value.account!!.cart.isEmpty()); assertTrue("minimalist-serum" in store.state.value.account!!.wishlist)
    }
    @Test fun onlyOneConcurrentDropClaimCanSucceed() = test {
        login(); rejected { drop.claim() }; drop.activateDemo()
        val results = coroutineScope { (1..12).map { async(Dispatchers.Default) { runCatching { drop.claim() }.isSuccess } }.awaitAll() }
        assertEquals(1, results.count { it }); assertEquals("demo", store.state.value.deal.claimedBy)
        assertEquals(600_000L, store.state.value.deal.reservedUntil - now)
    }
    @Test fun secondUserCannotClaimOrCheckoutAnotherUsersReservation() = test {
        login(); drop.activateDemo(); drop.claim(); auth.logout()
        auth.register("Other User", "9123456789", "other@example.com", "password1", "password1"); auth.login("other@example.com", "password1"); profile.saveAddress(address)
        rejected { drop.claim() }; rejected { orders.place(address.id, PaymentMethod.UPI, true, "wrong-owner") }
        assertTrue(store.state.value.account!!.orders.isEmpty())
    }
    @Test fun expiredReservationCannotBePurchasedOrReclaimed() = test {
        login(); profile.saveAddress(address); drop.activateDemo(); drop.claim(); now += 600_001
        assertEquals(DealPhase.EXPIRED, store.state.value.deal.phase(now))
        rejected { orders.place(address.id, PaymentMethod.UPI, true, "expired") }; rejected { drop.claim() }
        assertNull(store.state.value.deal.orderId)
    }
    @Test fun dropCheckoutCosts99AndKeepsOrdinaryCart() = test {
        login(); profile.saveAddress(address); cart.add(CartItem("sony-ch520", "Blue")); drop.activateDemo(); drop.claim()
        orders.place(address.id, PaymentMethod.COD, true, "drop-order")
        val order = store.state.value.account!!.orders.single()
        assertEquals(99, order.total); assertEquals(0, order.deliveryFee); assertEquals(1, store.state.value.account!!.cart.size); assertEquals(DealPhase.SOLD_OUT, store.state.value.deal.phase(now))
        rejected { orders.place(address.id, PaymentMethod.UPI, true, "second-drop-order") }
    }
    @Test fun shippedOrdersCannotBeCancelledAndDeliveredOrdersCanBeRated() = test {
        login(); profile.saveAddress(address); cart.add(CartItem("sony-ch520", "Black")); orders.place(address.id, PaymentMethod.UPI, false, "tracking")
        rejected { orders.rate("tracking", 5) }
        repeat(2) { orders.advanceDemo("tracking") }; rejected { orders.cancel("tracking") }
        repeat(2) { orders.advanceDemo("tracking") }; orders.rate("tracking", 4)
        assertEquals(OrderStatus.DELIVERED, store.state.value.account!!.orders.single().status); assertEquals(4, store.state.value.account!!.orders.single().rating)
        assertTrue(store.state.value.account!!.notifications.any { it.title == "Your order has shipped" })
    }
    @Test fun failedPersistenceDoesNotPublishOrConsumeState() = test {
        login(); profile.saveAddress(address); cart.add(CartItem("sony-ch520", "Black")); storage.fail = true
        rejected { orders.place(address.id, PaymentMethod.UPI, false, "disk-fail") }
        assertTrue(store.state.value.account!!.orders.isEmpty()); assertEquals(1, store.state.value.account!!.cart.size)
        storage.fail = false; orders.place(address.id, PaymentMethod.UPI, false, "disk-fail"); assertEquals(1, store.state.value.account!!.orders.size)
    }
    @Test fun addressValidationRejectsMalformedPhoneAndPin() = test {
        login(); rejected { profile.saveAddress(address.copy(pin = "000000")) }; rejected { profile.saveAddress(address.copy(phone = "123")) }; rejected { profile.saveAddress(address.copy(street = "")) }
        assertTrue(store.state.value.account!!.addresses.isEmpty())
    }
    @Test fun remindersAndReservationWarningsArePersistedOnlyOnce() = test {
        login(); drop.remind(); now += 120_001; drop.refreshClock(); drop.refreshClock()
        assertEquals(1, store.state.value.account!!.notifications.count { it.id.startsWith("live-") })
        drop.claim(); now += 480_001; drop.refreshClock(); drop.refreshClock()
        assertEquals(1, store.state.value.account!!.notifications.count { it.id.startsWith("expiring-") })
    }
}
