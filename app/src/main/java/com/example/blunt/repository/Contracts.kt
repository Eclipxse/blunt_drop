package com.example.blunt.repository

import com.example.blunt.model.*
import kotlinx.coroutines.flow.StateFlow

interface ProductRepository {
    val products: List<Product>
    fun find(id: String): Product
    fun search(query: String, category: String = "All"): List<Product>
}
interface AuthRepository {
    val state: StateFlow<AppSnapshot>
    suspend fun login(identity: String, password: String)
    suspend fun register(name: String, phone: String, email: String, password: String, confirm: String)
    suspend fun logout()
    suspend fun completeOnboarding()
}
interface CartRepository {
    val state: StateFlow<AppSnapshot>
    suspend fun add(item: CartItem)
    suspend fun quantity(key: String, quantity: Int)
    suspend fun remove(key: String, saveForLater: Boolean = false)
    fun totals(items: List<CartItem>): Totals
}
interface OrderRepository {
    val state: StateFlow<AppSnapshot>
    suspend fun place(addressId: String?, payment: PaymentMethod, drop: Boolean, requestId: String): String
    suspend fun cancel(id: String)
    suspend fun advanceDemo(id: String)
    suspend fun rate(id: String, rating: Int)
}
interface DailyDealRepository {
    val state: StateFlow<AppSnapshot>
    suspend fun claim()
    suspend fun remind()
    suspend fun activateDemo()
    suspend fun resetDemo()
    suspend fun refreshClock()
}
interface ProfileRepository {
    val state: StateFlow<AppSnapshot>
    suspend fun toggleWishlist(productId: String)
    suspend fun saveAddress(address: Address)
    suspend fun selectAddress(id: String)
    suspend fun deleteAddress(id: String)
    suspend fun rememberSearch(query: String)
    suspend fun clearSearches()
    suspend fun readNotice(id: String)
    suspend fun clearNotices()
}
