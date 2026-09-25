package com.example.blunt.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blunt.model.*
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.components.*
import com.example.blunt.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun date(time: Long) = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("en-IN")).format(Date(time))
@Composable fun OrdersScreen(vm: OrderViewModel) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val orders = snapshot.account?.orders.orEmpty().filter { when (tab) { 1 -> it.status == OrderStatus.DELIVERED; 2 -> it.status == OrderStatus.CANCELLED; else -> it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED } }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("My orders", false)
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Active", "Delivered", "Cancelled").forEachIndexed { index, label -> FilterChip(tab == index, { tab = index }, { Text(label) }) } }
        if (orders.isEmpty()) Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { EmptyState(Icons.Outlined.Inventory2, if (tab == 0) "Your first find is waiting." else if (tab == 1) "Nothing delivered yet." else "No cancelled orders.", if (tab == 0) "When you place an order, its whole journey will live here." else "You'll find ${if (tab == 1) "delivered" else "cancelled"} orders here when there are any.", "Explore the edit", { nav.go("listing/All") }) }
        else LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            items(orders, key = { it.id }) { order ->
                Column(Modifier.fillMaxWidth().clickable { nav.go("order/${order.id}") }, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(order.status.label, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelLarge); Text(date(order.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    CheckoutLine(order.lines.first())
                    if (order.lines.size > 1) Text("+ ${order.lines.size - 1} more ${if (order.lines.size == 2) "find" else "finds"}", style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(order.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(money(order.total), style = MaterialTheme.typography.titleMedium) }
                    HorizontalDivider()
                }
            }
        }
    }
}
@Composable fun OrderSuccessScreen(vm: OrderViewModel, id: String) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val order = snapshot.account?.orders?.find { it.id == id }
    val nav = LocalNavigator.current
    var appeared by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (appeared) 1f else .8f, spring(dampingRatio = 1f), label = "Order confirmed")
    LaunchedEffect(Unit) { appeared = true }
    if (order == null) { EmptyState(Icons.Outlined.Inventory2, "Order not found", "You can find your purchases in My Orders.", "My orders", { nav.go("orders") }); return }
    LazyColumn(contentPadding = PaddingValues(32.dp), verticalArrangement = Arrangement.spacedBy(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item { Spacer(Modifier.height(24.dp)); Box(Modifier.size(88.dp).scale(scale).background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Check, "Order confirmed", Modifier.size(44.dp), tint = MaterialTheme.colorScheme.tertiary) } }
        item { Text("A good choice.\nOrder confirmed.", style = MaterialTheme.typography.displaySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
        item { Text("${order.id}\nPlaced ${date(order.createdAt)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
        item { Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.large) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { DetailRow("Estimated delivery", "${date(order.createdAt + 3 * 86_400_000L)} –\n${date(order.createdAt + 5 * 86_400_000L)}"); DetailRow("Final amount", money(order.total)); HorizontalDivider(); Text(order.address.name, style = MaterialTheme.typography.titleMedium); Text(order.address.formatted) } } }
        item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { PrimaryButton("Track order", { nav.go("order/$id") }); SecondaryButton("Continue shopping", { nav.go("home") }); DemoNote("Your demo order is saved on this device. No real payment or delivery will take place.") } }
    }
}
@Composable fun OrderDetailsScreen(vm: OrderViewModel, id: String) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val order = snapshot.account?.orders?.find { it.id == id }
    val nav = LocalNavigator.current
    var cancel by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(false) }
    var stars by rememberSaveable { mutableIntStateOf(5) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Order details")
        if (order == null) { EmptyState(Icons.Outlined.Inventory2, "Order not found", "Try My Orders to see your saved purchases.", "My orders", { nav.go("orders") }); return@Column }
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item { Text(order.status.label, style = MaterialTheme.typography.headlineLarge); Text("${order.id} · ${date(order.createdAt)}", Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
            item { OrderStatusTimeline(order.status) }
            item { HorizontalDivider(); Text("Your finds", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleLarge) }
            items(order.lines) { CheckoutLine(it) }
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { HorizontalDivider(); Text("Delivery address", style = MaterialTheme.typography.titleLarge); Text(order.address.name, style = MaterialTheme.typography.titleMedium); Text(order.address.formatted); Text("+91 ${order.address.phone}"); DetailRow("Payment", "${order.payment.label} (demo)") } }
            item { PriceDetails(Totals(order.totalMrp, order.subtotal, order.deliveryFee)) }
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionFeedback(vm)
                if (order.status in listOf(OrderStatus.CONFIRMED, OrderStatus.PACKED)) SecondaryButton("Cancel order", { cancel = true })
                if (order.status == OrderStatus.DELIVERED) PrimaryButton(if (order.rating == 0) "Rate your finds" else "Your rating: ${order.rating}/5 · Edit", { stars = order.rating.takeIf { it > 0 } ?: 5; rating = true })
                SecondaryButton("Need help?", { nav.go("info/help") })
                if (order.status.ordinal < OrderStatus.DELIVERED.ordinal) TextButton({ vm.advance(id) }) { Icon(Icons.Outlined.FastForward, null); Spacer(Modifier.width(8.dp)); Text("Demo: advance delivery status") }
                DemoNote("Tracking is simulated. Use the demo control to explore packed, shipped, out for delivery and delivered states.")
            } }
        }
    }
    if (cancel) AlertDialog(onDismissRequest = { cancel = false }, title = { Text("Cancel this order?") }, text = { Text("The order will move to Cancelled. No real money was charged in this demo.") }, confirmButton = { TextButton({ cancel = false; vm.cancel(id) }) { Text("Cancel order") } }, dismissButton = { TextButton({ cancel = false }) { Text("Keep order") } })
    if (rating) AlertDialog(onDismissRequest = { rating = false }, title = { Text("How were your finds?") }, text = { Column { Text("Your rating is saved locally."); Row { (1..5).forEach { value -> IconToggleButton(stars >= value, { stars = value }) { Icon(Icons.Outlined.Star, "Rate $value stars", tint = if (stars >= value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline) } } } } }, confirmButton = { TextButton({ vm.rate(id, stars); rating = false }) { Text("Save rating") } }, dismissButton = { TextButton({ rating = false }) { Text("Later") } })
}
