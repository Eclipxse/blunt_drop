package com.example.blunt.repository

import com.example.blunt.data.*
import com.example.blunt.model.*

class FakeOrderRepository(private val store: LocalStore, private val products: ProductRepository, private val cart: CartRepository) : OrderRepository {
    override val state = store.state
    override suspend fun place(addressId: String?, payment: PaymentMethod, drop: Boolean, requestId: String): String = store.transaction { snapshot ->
        val account = snapshot.requireAccount()
        // A checkout request can safely be retried without buying twice.
        val existing = account.orders.find { it.id == requestId }
        if (existing != null) return@transaction snapshot to existing.id
        val address = account.addresses.find { it.id == addressId } ?: error("Select a delivery address before placing your order.")
        Validation.address(address)
        val deal = snapshot.deal
        val lines: List<OrderLine>
        val fee: Int
        var stock = snapshot.purchasedStock
        if (drop) {
            require(deal.claimedBy == account.user.id && deal.phase(store.now()) == DealPhase.RESERVED) { "Your reservation has expired or is unavailable. Open Daily Drop to check its status." }
            val product = products.find(deal.productId)
            lines = listOf(OrderLine(product.id, product.name, product.images.first(), product.colors.first(), 1, deal.price, product.mrp))
            fee = 0
        } else {
            require(account.cart.isNotEmpty()) { "Your bag is empty. Add a product to continue." }
            validateStock(snapshot, account.cart, products)
            lines = account.cart.map { item ->
                val p = products.find(item.productId)
                OrderLine(p.id, p.name, p.images.first(), item.variant, item.quantity, p.price, p.mrp)
            }
            fee = cart.totals(account.cart).delivery
            stock = stock.toMutableMap().apply { lines.forEach { this[it.productId] = (this[it.productId] ?: 0) + it.quantity } }
        }
        val order = Order(requestId, store.now(), lines, address, payment, deliveryFee = fee, dailyDrop = drop)
        val notice = Notice("order-$requestId", "Order confirmed", "Your ${if (drop) "Daily Drop" else "Blunt order"} is in. Track its journey from My Orders.", "order/$requestId")
        val next = snapshot.withAccount(account.copy(orders = listOf(order) + account.orders, cart = if (drop) account.cart else emptyList(), notifications = listOf(notice) + account.notifications))
        next.copy(purchasedStock = stock, deal = if (drop) deal.copy(orderId = requestId) else deal) to requestId
    }
    override suspend fun cancel(id: String) = store.transaction { snapshot ->
        val account = snapshot.requireAccount()
        val order = account.orders.find { it.id == id } ?: error("Order not found.")
        require(order.status == OrderStatus.CONFIRMED || order.status == OrderStatus.PACKED) { "This order is already on its way and cannot be cancelled." }
        val stock = snapshot.purchasedStock.toMutableMap()
        if (!order.dailyDrop) order.lines.forEach { stock[it.productId] = ((stock[it.productId] ?: 0) - it.quantity).coerceAtLeast(0) }
        snapshot.withAccount(account.copy(orders = account.orders.map { if (it.id == id) it.copy(status = OrderStatus.CANCELLED) else it })).copy(purchasedStock = stock) to Unit
    }
    override suspend fun advanceDemo(id: String) = store.transaction { snapshot ->
        val account = snapshot.requireAccount()
        val order = account.orders.find { it.id == id } ?: error("Order not found.")
        require(order.status.ordinal < OrderStatus.DELIVERED.ordinal) { "This order has already finished." }
        val status = OrderStatus.entries[order.status.ordinal + 1]
        val notice = Notice("$id-${status.name}", if (status == OrderStatus.SHIPPED) "Your order has shipped" else status.label, "Open your order for the latest demo tracking update.", "order/$id")
        snapshot.withAccount(account.copy(orders = account.orders.map { if (it.id == id) it.copy(status = status) else it }, notifications = listOf(notice) + account.notifications)) to Unit
    }
    override suspend fun rate(id: String, rating: Int) = store.transaction { snapshot ->
        val account = snapshot.requireAccount()
        require(rating in 1..5) { "Choose a rating from 1 to 5." }
        require(account.orders.any { it.id == id && it.status == OrderStatus.DELIVERED }) { "You can rate an order after delivery." }
        snapshot.withAccount(account.copy(orders = account.orders.map { if (it.id == id) it.copy(rating = rating) else it })) to Unit
    }
}
