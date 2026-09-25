package com.example.blunt.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.blunt.data.AppContainer
import com.example.blunt.ui.components.ActionEvents
import com.example.blunt.ui.screens.*
import com.example.blunt.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class Destination(val route: String, val title: String, val icon: ImageVector)
private val destinations = listOf(Destination("home", "Home", Icons.Outlined.Home), Destination("categories", "Categories", Icons.Outlined.GridView), Destination("daily", "Daily Deal", Icons.Outlined.Bolt), Destination("orders", "Orders", Icons.Outlined.Inventory2), Destination("profile", "Profile", Icons.Outlined.PersonOutline))

@Composable fun BluntApp(app: AppContainer) {
    val controller = rememberNavController()
    val factory = remember { BluntViewModelFactory(app) }
    val auth: AuthViewModel = viewModel(factory = factory)
    val ready by auth.ready.collectAsStateWithLifecycle()
    val snapshot by auth.state.collectAsStateWithLifecycle()
    val entry by controller.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "splash"
    val showNav = destinations.any { it.route == route }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun go(target: String) {
        snackbar.currentSnackbarData?.dismiss()
        if (target == "back") { controller.popBackStack(); return }
        if (target.startsWith("success/")) {
            controller.navigate("home") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
            controller.navigate(target); return
        }
        controller.navigate(target) {
            launchSingleTop = true
            when {
                target.startsWith("login") || (target == "home" && route in listOf("splash", "login?created={created}", "register", "forgot")) -> popUpTo(0) { inclusive = true }
                target == "onboarding" -> popUpTo("splash") { inclusive = true }
                destinations.any { it.route == target } -> { popUpTo("home") { saveState = true }; restoreState = true }
            }
        }
    }
    val navigator = Navigator(::go, { if (!controller.popBackStack()) go("home") }, { message -> scope.launch { snackbar.showSnackbar(message) } })
    fun event(event: UiEvent) { when (event) { is UiEvent.Navigate -> go(event.route); is UiEvent.Message -> navigator.message(event.text) } }
    ActionEvents(auth, ::event)
    LaunchedEffect(ready) {
        if (ready && controller.currentDestination?.route == "splash") {
            delay(600)
            go(if (!snapshot.onboardingCompleted) "onboarding" else if (snapshot.sessionId != null) "home" else "login")
        }
    }
    CompositionLocalProvider(LocalNavigator provides navigator) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val expanded = maxWidth >= 700.dp
            Row(Modifier.fillMaxSize()) {
                if (showNav && expanded) NavigationRail(Modifier.fillMaxHeight(), containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Spacer(Modifier.height(24.dp))
                    destinations.forEach { destination -> NavigationRailItem(route == destination.route, { go(destination.route) }, { Icon(destination.icon, null) }, modifier = Modifier.semantics { contentDescription = "${destination.title} tab" }, label = { Text(destination.title) }) }
                }
                Scaffold(Modifier.weight(1f), snackbarHost = { SnackbarHost(snackbar, Modifier.padding(bottom = if (route.startsWith("product/") || route == "cart" || route.startsWith("checkout/")) 104.dp else 0.dp)) }, contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = { if (showNav && !expanded) NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                        destinations.forEach { destination -> NavigationBarItem(route == destination.route, { go(destination.route) }, icon = { Icon(destination.icon, null) }, modifier = Modifier.semantics { contentDescription = "${destination.title} tab" }, label = { Text(destination.title, style = MaterialTheme.typography.labelSmall, maxLines = 1) }, colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer, selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary)) }
                    } }
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                        NavHost(controller, "splash", Modifier.widthIn(max = 1040.dp).fillMaxSize(), enterTransition = { fadeIn(tween(160)) }, exitTransition = { fadeOut(tween(120)) }) {
                            composable("splash") { SplashScreen(auth) }
                            composable("onboarding") { OnboardingScreen(auth) }
                            composable("login?created={created}", arguments = listOf(navArgument("created") { type = NavType.BoolType; defaultValue = false })) { LoginScreen(auth, it.arguments?.getBoolean("created") == true) }
                            composable("register") { RegisterScreen(auth) }
                            composable("forgot") { ForgotScreen(auth) }
                            composable("home") {
                                val home: HomeViewModel = viewModel(factory = factory); val products: ProductViewModel = viewModel(factory = factory); val drop: DailyDealViewModel = viewModel(factory = factory)
                                ActionEvents(products, ::event); HomeScreen(home, products, drop)
                            }
                            composable("categories") { CategoriesScreen() }
                            composable("listing/{category}") { backStack -> val vm: ProductViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); ListingScreen(vm, backStack.arguments?.getString("category") ?: "All") }
                            composable("search") { val vm: ProductViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); ListingScreen(vm, "All", true) }
                            composable("product/{productId}") { backStack -> val vm: ProductViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); ProductScreen(backStack.arguments?.getString("productId").orEmpty(), vm) }
                            composable("cart") { val vm: CartViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); CartScreen(vm) }
                            composable("addresses?select={select}", arguments = listOf(navArgument("select") { type = NavType.BoolType; defaultValue = false })) { backStack -> val vm: ProfileViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); AddressesScreen(vm, backStack.arguments?.getBoolean("select") == true) }
                            composable("address/{id}") { backStack -> val vm: ProfileViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); AddressFormScreen(vm, backStack.arguments?.getString("id") ?: "new") }
                            composable("checkout/{mode}") { backStack -> val vm: OrderViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); CheckoutScreen(vm, backStack.arguments?.getString("mode") == "drop") }
                            composable("success/{orderId}") { backStack -> val vm: OrderViewModel = viewModel(factory = factory); OrderSuccessScreen(vm, backStack.arguments?.getString("orderId").orEmpty()) }
                            composable("orders") { val vm: OrderViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); OrdersScreen(vm) }
                            composable("order/{orderId}") { backStack -> val vm: OrderViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); OrderDetailsScreen(vm, backStack.arguments?.getString("orderId").orEmpty()) }
                            composable("daily") { val vm: DailyDealViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); DailyDealScreen(vm) }
                            composable("profile") { val vm: ProfileViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); ProfileScreen(vm, auth) }
                            composable("wishlist") { val vm: ProfileViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); WishlistScreen(vm) }
                            composable("notifications") { val vm: ProfileViewModel = viewModel(factory = factory); ActionEvents(vm, ::event); NotificationsScreen(vm) }
                            composable("info/{page}") { backStack -> InformationScreen(backStack.arguments?.getString("page") ?: "help") }
                        }
                    }
                }
            }
        }
    }
}
