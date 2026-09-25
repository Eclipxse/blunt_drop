package com.example.blunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blunt.data.AppContainer
import com.example.blunt.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
    data class Navigate(val route: String, val clear: Boolean = false) : UiEvent
}
data class ActionState(val busy: Boolean = false, val error: String? = null)
open class ActionViewModel : ViewModel() {
    protected val _action = MutableStateFlow(ActionState())
    val action = _action.asStateFlow()
    private val channel = Channel<UiEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    fun clearError() { _action.value = _action.value.copy(error = null) }
    protected fun perform(success: String? = null, block: suspend () -> UiEvent?) {
        if (_action.value.busy) return
        viewModelScope.launch {
            _action.value = ActionState(busy = true)
            try {
                val event = block()
                _action.value = ActionState()
                if (event != null) channel.send(event)
                else if (success != null) channel.send(UiEvent.Message(success))
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _action.value = ActionState(error = e.message ?: "Something went wrong. Please try again.") }
        }
    }
}
class AuthViewModel(private val app: AppContainer) : ActionViewModel() {
    val state = app.auth.state
    val ready = app.store.ready
    val loadError = app.store.error
    fun retry() { viewModelScope.launch { app.store.initialize() } }
    fun finishOnboarding() = perform { app.auth.completeOnboarding(); UiEvent.Navigate("login", true) }
    fun login(identity: String, password: String) = perform { app.auth.login(identity, password); UiEvent.Navigate("home", true) }
    fun register(name: String, phone: String, email: String, password: String, confirm: String) = perform {
        app.auth.register(name, phone, email, password, confirm); UiEvent.Navigate("login?created=true", true)
    }
    fun logout() = perform { app.auth.logout(); UiEvent.Navigate("login", true) }
}
class HomeViewModel(app: AppContainer) : ViewModel() {
    val state = app.store.state
    val products = app.products.products
}
enum class SortMode(val label: String) { FEATURED("Featured"), PRICE_LOW("Price: low to high"), PRICE_HIGH("Price: high to low"), RATING("Top rated") }
data class ProductQuery(val text: String = "", val category: String = "All", val sort: SortMode = SortMode.FEATURED, val inStock: Boolean = false, val under2000: Boolean = false, val topRated: Boolean = false)
class ProductViewModel(private val app: AppContainer) : ActionViewModel() {
    val state = app.store.state
    private val _query = MutableStateFlow(ProductQuery())
    val query = _query.asStateFlow()
    val results = combine(_query, state) { q, snapshot ->
        val filtered = app.products.search(q.text, q.category).filter {
            (!q.inStock || available(it, snapshot) > 0) && (!q.under2000 || it.price <= 2000) && (!q.topRated || it.rating >= 4.5)
        }
        when (q.sort) {
            SortMode.FEATURED -> filtered
            SortMode.PRICE_LOW -> filtered.sortedBy { it.price }
            SortMode.PRICE_HIGH -> filtered.sortedByDescending { it.price }
            SortMode.RATING -> filtered.sortedByDescending { it.rating }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), app.products.products)
    fun update(query: ProductQuery) { _query.value = query }
    fun product(id: String) = app.products.products.find { it.id == id }
    fun related(product: Product) = app.products.products.filter { it.category == product.category && it.id != product.id }
    fun available(product: Product, snapshot: AppSnapshot) = (product.stock - (snapshot.purchasedStock[product.id] ?: 0)).coerceAtLeast(0)
    fun wishlist(id: String) = perform { app.profile.toggleWishlist(id); null }
    fun searchSaved(text: String) = perform { app.profile.rememberSearch(text); null }
    fun clearSearches() = perform { app.profile.clearSearches(); null }
    fun add(product: Product, color: String, size: String, quantity: Int, buy: Boolean = false) = perform(if (buy) null else "Added to your bag") {
        app.cart.add(CartItem(product.id, color, size, quantity)); if (buy) UiEvent.Navigate("checkout/cart") else null
    }
}
class CartViewModel(private val app: AppContainer) : ActionViewModel() {
    val state = app.cart.state
    val totals = state.map { app.cart.totals(it.account?.cart.orEmpty()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Totals(0, 0, 0))
    fun product(id: String) = app.products.find(id)
    fun quantity(key: String, amount: Int) = perform { app.cart.quantity(key, amount); null }
    fun remove(key: String, save: Boolean = false) = perform(if (save) "Saved to your wishlist" else "Removed from your bag") { app.cart.remove(key, save); null }
}
class ProfileViewModel(private val app: AppContainer) : ActionViewModel() {
    val state = app.profile.state
    val products = app.products.products
    fun wishlist(id: String) = perform { app.profile.toggleWishlist(id); null }
    fun save(address: Address) = perform { app.profile.saveAddress(address); UiEvent.Navigate("back") }
    fun select(id: String) = perform { app.profile.selectAddress(id); null }
    fun delete(id: String) = perform("Address removed") { app.profile.deleteAddress(id); null }
    fun readNotice(id: String) = perform { app.profile.readNotice(id); null }
    fun clearNotices() = perform("Notifications cleared") { app.profile.clearNotices(); null }
    fun resetDemo() = perform("New demo drop scheduled in 2 minutes") { app.deal.resetDemo(); null }
}
class DailyDealViewModel(private val app: AppContainer) : ActionViewModel() {
    val state = app.deal.state
    val now = flow {
        while (true) {
            emit(app.store.now())
            try { app.deal.refreshClock() } catch (e: CancellationException) { throw e } catch (_: Exception) { /* A notification write must not stop the reservation clock. */ }
            delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), app.store.now())
    val product = app.products.find("sony-ch520")
    fun claim() = perform { app.deal.claim(); null }
    fun remind() = perform { app.deal.remind(); null }
    fun activate() = perform { app.deal.activateDemo(); null }
}
class OrderViewModel(private val app: AppContainer) : ActionViewModel() {
    val state = app.orders.state
    val now = flow { while (true) { emit(app.store.now()); delay(1000) } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), app.store.now())
    fun checkoutLines(snapshot: AppSnapshot, drop: Boolean): List<OrderLine> = if (drop) {
        val p = app.products.find(snapshot.deal.productId)
        listOf(OrderLine(p.id, p.name, p.images.first(), p.colors.first(), 1, snapshot.deal.price, p.mrp))
    } else snapshot.account?.cart.orEmpty().map { val p = app.products.find(it.productId); OrderLine(p.id, p.name, p.images.first(), it.variant, it.quantity, p.price, p.mrp) }
    fun delivery(snapshot: AppSnapshot, drop: Boolean) = if (drop) 0 else app.cart.totals(snapshot.account?.cart.orEmpty()).delivery
    fun place(address: String?, payment: PaymentMethod, drop: Boolean, requestId: String, simulateFailure: Boolean = false) = perform {
        check(!simulateFailure) { "The mock payment was declined. Nothing was charged; your bag is safe. Try placing the order again." }
        val id = app.orders.place(address, payment, drop, requestId); UiEvent.Navigate("success/$id", true)
    }
    fun cancel(id: String) = perform("Order cancelled. No real payment was taken.") { app.orders.cancel(id); null }
    fun advance(id: String) = perform("Demo tracking updated") { app.orders.advanceDemo(id); null }
    fun rate(id: String, rating: Int) = perform("Thanks for your rating") { app.orders.rate(id, rating); null }
    fun newRequestId() = "BL-${UUID.randomUUID().toString().take(8).uppercase()}"
}
class BluntViewModelFactory(private val app: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AuthViewModel::class.java -> AuthViewModel(app)
        HomeViewModel::class.java -> HomeViewModel(app)
        ProductViewModel::class.java -> ProductViewModel(app)
        CartViewModel::class.java -> CartViewModel(app)
        ProfileViewModel::class.java -> ProfileViewModel(app)
        DailyDealViewModel::class.java -> DailyDealViewModel(app)
        OrderViewModel::class.java -> OrderViewModel(app)
        else -> error("Unknown ViewModel ${modelClass.name}")
    } as T
}
