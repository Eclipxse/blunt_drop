package com.example.blunt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blunt.model.*
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.components.*
import com.example.blunt.viewmodel.OrderViewModel

@Composable fun CheckoutScreen(vm: OrderViewModel, drop: Boolean) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val action by vm.action.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    var payment by rememberSaveable { mutableStateOf(PaymentMethod.UPI) }
    val request = rememberSaveable { vm.newRequestId() }
    val account = snapshot.account
    val address = account?.addresses?.find { it.id == account.selectedAddressId }
    val lines = vm.checkoutLines(snapshot, drop)
    val totals = Totals(lines.sumOf { it.mrp * it.quantity }, lines.sumOf { it.price * it.quantity }, vm.delivery(snapshot, drop))
    val validDrop = !drop || (snapshot.deal.claimedBy == snapshot.sessionId && snapshot.deal.phase(now) == DealPhase.RESERVED)
    var demoOptions by rememberSaveable { mutableStateOf(false) }
    var failNext by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(if (drop) "Daily Drop checkout" else "Checkout")
        if (!validDrop) { EmptyState(Icons.Outlined.TimerOff, "This reservation has closed.", "No payment was taken. Open the Daily Drop for its latest status.", "View Daily Drop", { nav.go("daily") }); return@Column }
        if (lines.isEmpty()) { EmptyState(Icons.Outlined.ShoppingBag, "Your bag is empty.", "Add a good find before heading to checkout.", "Explore products", { nav.go("listing/All") }); return@Column }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item { Text("Nearly yours.", style = MaterialTheme.typography.headlineLarge); if (drop) Text("Reserved for ${countdown(snapshot.deal.reservedUntil - now)}", Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium) }
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Delivery address", if (address == null) "Add" else "Change", { nav.go("addresses?select=true") })
                if (address == null) Surface(onClick = { nav.go("addresses?select=true") }, color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium) { Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Outlined.AddLocationAlt, null); Text("Add a delivery address to continue") } }
                else { Text(address.name, style = MaterialTheme.typography.titleMedium); Text(address.formatted); Text("+91 ${address.phone}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } }
            item { HorizontalDivider(); Text("Your good finds", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleLarge) }
            items(lines) { line -> CheckoutLine(line) }
            item { HorizontalDivider(); Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Payment method", style = MaterialTheme.typography.titleLarge)
                Text("Demo payment — no money will be charged.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                PaymentMethod.entries.forEach { method -> Row(Modifier.fillMaxWidth().clickable { payment = method }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(when (method) { PaymentMethod.UPI -> Icons.Outlined.QrCode2; PaymentMethod.CARD -> Icons.Outlined.CreditCard; PaymentMethod.COD -> Icons.Outlined.Payments }, null)
                    Text(method.label, Modifier.weight(1f).padding(start = 14.dp), style = MaterialTheme.typography.bodyLarge)
                    RadioButton(payment == method, { payment = method })
                } }
            } }
            item { PriceDetails(totals) }
            item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionFeedback(vm)
                DemoNote("This checkout creates a local demonstration order. No card details, UPI ID or real payment are collected.")
                TextButton({ demoOptions = !demoOptions }) { Text(if (demoOptions) "Hide demo payment options" else "Demo payment options") }
                if (demoOptions) Row(Modifier.fillMaxWidth().clickable { failNext = !failNext }, verticalAlignment = Alignment.CenterVertically) { Switch(failNext, { failNext = it }); Text("Decline the next mock payment", Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodySmall) }
            } }
        }
        Surface(shadowElevation = 2.dp) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (action.error != null) Text(action.error!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                PrimaryButton("Place order · ${money(totals.total)}", { vm.place(address?.id, payment, drop, request, failNext); failNext = false }, busy = action.busy)
            }
        }
    }
}
@Composable fun CheckoutLine(line: OrderLine) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        ProductImage(line.image, null, Modifier.size(76.dp).clip(MaterialTheme.shapes.small))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(line.name, style = MaterialTheme.typography.titleSmall); Text("${line.variant} · Qty ${line.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(money(line.price * line.quantity), style = MaterialTheme.typography.titleMedium) }
    }
}
