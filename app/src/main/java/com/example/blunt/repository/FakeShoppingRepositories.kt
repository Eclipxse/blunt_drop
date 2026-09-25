package com.example.blunt.repository

import com.example.blunt.data.*
import com.example.blunt.model.*

class FakeProductRepository : ProductRepository {
    override val products = Catalogue.products
    override fun find(id: String) = products.find { it.id == id } ?: error("This product is no longer available.")
    override fun search(query: String, category: String): List<Product> {
        val terms = query.trim().lowercase().split(Regex("\\s+")).filter(String::isNotBlank)
        return products.filter { product ->
            (category == "All" || category == "More" || product.category == category) &&
                terms.all { it in "${product.name} ${product.brand} ${product.category} ${product.description}".lowercase() }
        }
    }
}
class FakeCartRepository(private val store: LocalStore, private val products: ProductRepository) : CartRepository {
    override val state = store.state
    override suspend fun add(item: CartItem) = store.transaction { snapshot ->
        val account = snapshot.requireAccount()
        val product = products.find(item.productId)
        require(item.quantity > 0) { "Choose at least one item." }
        require(item.color in product.colors && (product.sizes.isEmpty() || item.size in product.sizes)) { "Choose an available color and size." }
        val existing = account.cart.find { it.key == item.key }
        val newItem = item.copy(quantity = item.quantity + (existing?.quantity ?: 0))
        val cart = account.cart.filterNot { it.key == item.key } + newItem
        validateStock(snapshot, cart, products)
        snapshot.withAccount(account.copy(cart = cart)) to Unit
    }
    override suspend fun quantity(key: String, quantity: Int) = store.transaction { snapshot ->
        require(quantity in 1..10) { "Choose a quantity from 1 to 10." }
        val account = snapshot.requireAccount()
        val cart = account.cart.map { if (it.key == key) it.copy(quantity = quantity) else it }
        validateStock(snapshot, cart, products)
        snapshot.withAccount(account.copy(cart = cart)) to Unit
    }
    override suspend fun remove(key: String, saveForLater: Boolean) = store.transaction { snapshot ->
        val account = snapshot.requireAccount()
        val item = account.cart.find { it.key == key }
        snapshot.withAccount(account.copy(cart = account.cart.filterNot { it.key == key }, wishlist = if (saveForLater && item != null) account.wishlist + item.productId else account.wishlist)) to Unit
    }
    override fun totals(items: List<CartItem>): Totals {
        val subtotal = items.sumOf { products.find(it.productId).price * it.quantity }
        return Totals(items.sumOf { products.find(it.productId).mrp * it.quantity }, subtotal, if (subtotal == 0 || subtotal >= 999) 0 else 49)
    }
}
internal fun validateStock(snapshot: AppSnapshot, cart: List<CartItem>, products: ProductRepository) {
    cart.groupBy { it.productId }.forEach { (id, items) ->
        val product = products.find(id)
        val available = (product.stock - (snapshot.purchasedStock[id] ?: 0)).coerceAtLeast(0)
        require(items.sumOf { it.quantity } <= minOf(10, available)) { "Only $available ${product.name} available. Update your bag to continue." }
    }
}
class FakeProfileRepository(private val store: LocalStore) : ProfileRepository {
    override val state = store.state
    private suspend fun change(block: (LocalAccount) -> LocalAccount) = store.transaction { it.withAccount(block(it.requireAccount())) to Unit }
    override suspend fun toggleWishlist(productId: String) = change { it.copy(wishlist = if (productId in it.wishlist) it.wishlist - productId else it.wishlist + productId) }
    override suspend fun saveAddress(address: Address) = change {
        Validation.address(address)
        it.copy(addresses = it.addresses.filterNot { old -> old.id == address.id } + address, selectedAddressId = address.id)
    }
    override suspend fun selectAddress(id: String) = change {
        require(it.addresses.any { address -> address.id == id }) { "Choose a saved address." }
        it.copy(selectedAddressId = id)
    }
    override suspend fun deleteAddress(id: String) = change { account ->
        val addresses = account.addresses.filterNot { it.id == id }
        account.copy(addresses = addresses, selectedAddressId = if (account.selectedAddressId == id) addresses.firstOrNull()?.id else account.selectedAddressId)
    }
    override suspend fun rememberSearch(query: String) = change {
        val term = query.trim().take(80)
        if (term.isEmpty()) it else it.copy(recentSearches = (listOf(term) + it.recentSearches.filterNot { old -> old.equals(term, true) }).take(6))
    }
    override suspend fun clearSearches() = change { it.copy(recentSearches = emptyList()) }
    override suspend fun readNotice(id: String) = change { it.copy(notifications = it.notifications.map { notice -> if (notice.id == id) notice.copy(read = true) else notice }) }
    override suspend fun clearNotices() = change { it.copy(notifications = emptyList()) }
}
