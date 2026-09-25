package com.example.blunt.data

import android.content.Context
import com.example.blunt.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val store = LocalStore(FileSnapshotStorage(context.applicationContext), scope)
    val products: ProductRepository = FakeProductRepository()
    val auth: AuthRepository = FakeAuthRepository(store)
    val cart: CartRepository = FakeCartRepository(store, products)
    val orders: OrderRepository = FakeOrderRepository(store, products, cart)
    val deal: DailyDealRepository = FakeDailyDealRepository(store)
    val profile: ProfileRepository = FakeProfileRepository(store)
}
