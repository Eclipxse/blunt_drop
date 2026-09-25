package com.example.blunt.model

import java.text.NumberFormat
import java.util.Locale

data class Product(
    val id: String, val name: String, val brand: String, val category: String,
    val description: String, val mrp: Int, val price: Int, val rating: Double,
    val reviews: Int, val stock: Int, val images: List<String>,
    val specifications: Map<String, String>, val colors: List<String>,
    val sizes: List<String> = emptyList(), val tag: String = ""
) {
    val discount: Int get() = ((mrp - price) * 100.0 / mrp).toInt()
}

data class User(val id: String, val name: String, val email: String, val phone: String)
data class CartItem(val productId: String, val color: String, val size: String = "", val quantity: Int = 1) {
    val key: String get() = "$productId|$color|$size"
    val variant: String get() = listOf(color, size).filter { it.isNotBlank() }.joinToString(" / ")
}
data class Address(
    val id: String, val name: String, val phone: String, val house: String,
    val street: String, val area: String, val city: String, val state: String,
    val pin: String, val type: String = "Home"
) {
    val formatted: String get() = "$house, $street, $area\n$city, $state — $pin"
}
enum class OrderStatus(val label: String) {
    CONFIRMED("Order confirmed"), PACKED("Packed"), SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for delivery"), DELIVERED("Delivered"), CANCELLED("Cancelled")
}
enum class PaymentMethod(val label: String) { UPI("UPI"), CARD("Credit / Debit Card"), COD("Cash on Delivery") }
data class OrderLine(val productId: String, val name: String, val image: String, val variant: String, val quantity: Int, val price: Int, val mrp: Int)
data class Order(
    val id: String, val createdAt: Long, val lines: List<OrderLine>, val address: Address,
    val payment: PaymentMethod, val status: OrderStatus = OrderStatus.CONFIRMED,
    val deliveryFee: Int, val dailyDrop: Boolean = false, val rating: Int = 0
) {
    val subtotal: Int get() = lines.sumOf { it.price * it.quantity }
    val totalMrp: Int get() = lines.sumOf { it.mrp * it.quantity }
    val total: Int get() = subtotal + deliveryFee
}
data class Notice(val id: String, val title: String, val body: String, val target: String, val read: Boolean = false)
data class LocalAccount(
    val user: User, val salt: String, val passwordHash: String,
    val cart: List<CartItem> = emptyList(), val wishlist: Set<String> = emptySet(),
    val addresses: List<Address> = emptyList(), val selectedAddressId: String? = null,
    val orders: List<Order> = emptyList(), val recentSearches: List<String> = emptyList(),
    val notifications: List<Notice> = emptyList(), val reminder: Boolean = false
)
data class DealState(
    val id: String = "drop-1", val productId: String = "sony-ch520", val price: Int = 99,
    val startsAt: Long = 0L, val endsAt: Long = Long.MAX_VALUE,
    val claimedBy: String? = null, val reservedUntil: Long = 0L, val orderId: String? = null
) {
    fun phase(now: Long): DealPhase = when {
        orderId != null -> DealPhase.SOLD_OUT
        claimedBy != null && now >= reservedUntil -> DealPhase.EXPIRED
        claimedBy != null -> DealPhase.RESERVED
        now < startsAt -> DealPhase.UPCOMING
        now >= endsAt -> DealPhase.ENDED
        else -> DealPhase.LIVE
    }
}
enum class DealPhase { UPCOMING, LIVE, RESERVED, SOLD_OUT, EXPIRED, ENDED }
data class AppSnapshot(
    val onboardingCompleted: Boolean = false, val sessionId: String? = null,
    val accounts: List<LocalAccount> = emptyList(), val deal: DealState = DealState(),
    val purchasedStock: Map<String, Int> = emptyMap()
) {
    val account: LocalAccount? get() = accounts.find { it.user.id == sessionId }
}
data class Totals(val mrp: Int, val subtotal: Int, val delivery: Int) {
    val discount: Int get() = mrp - subtotal
    val total: Int get() = subtotal + delivery
}
fun money(value: Int): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply { maximumFractionDigits = 0 }.format(value)
fun countdown(millis: Long): String {
    val seconds = (millis.coerceAtLeast(0) / 1000)
    return "%02d : %02d : %02d".format(Locale.US, seconds / 3600, seconds / 60 % 60, seconds % 60)
}
