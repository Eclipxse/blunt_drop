package com.example.blunt.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blunt.model.*
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.components.*
import com.example.blunt.viewmodel.DailyDealViewModel

@Composable fun DailyDealScreen(vm: DailyDealViewModel) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val action by vm.action.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val deal = snapshot.deal
    val phase = deal.phase(now)
    val mine = deal.claimedBy == snapshot.sessionId && snapshot.sessionId != null
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("The Daily Drop", false) { IconButton({ nav.go("info/drop") }) { Icon(Icons.Outlined.Info, "Daily Drop rules") } }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            AnimatedContent(mine && phase == DealPhase.RESERVED, label = "Reservation state") { reserved ->
                if (reserved) Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Check, null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.tertiary) }
                    Text("Nice catch.\nIt's yours to claim.", style = MaterialTheme.typography.displaySmall)
                    Text("Congratulations — you claimed today's Daily Drop. Complete checkout before your 10-minute reservation ends.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("One good thing.\nOne unreal price.", style = MaterialTheme.typography.displaySmall)
                    Text("One promotional unit. First successful claim gets it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            ProductImage(vm.product.images.first(), vm.product.name, Modifier.fillMaxWidth().heightIn(max = 340.dp).aspectRatio(1.25f).clip(MaterialTheme.shapes.large))
            Text("Sony WH-CH520", style = MaterialTheme.typography.headlineSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(money(deal.price), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                Column { Text(money(vm.product.mrp), textDecoration = TextDecoration.LineThrough, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Save ${money(vm.product.mrp - deal.price)}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary) }
            }
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(when (phase) { DealPhase.UPCOMING -> "The drop starts in"; DealPhase.LIVE -> "Live now · 1 unit available"; DealPhase.RESERVED -> if (mine) "Reserved for you · time remaining" else "Sold out · reserved by another shopper"; DealPhase.EXPIRED -> if (mine) "Your reservation has expired" else "Sold out"; DealPhase.SOLD_OUT -> "Sold out · this drop has been claimed"; DealPhase.ENDED -> "This drop has ended" }, style = MaterialTheme.typography.titleMedium)
                    if (phase in listOf(DealPhase.UPCOMING, DealPhase.LIVE) || (mine && phase == DealPhase.RESERVED)) Text(countdown((if (phase == DealPhase.UPCOMING) deal.startsAt else if (phase == DealPhase.RESERVED) deal.reservedUntil else deal.endsAt) - now), style = MaterialTheme.typography.headlineMedium)
                    if (phase == DealPhase.EXPIRED && mine) Text("The checkout window has closed. No payment was taken.", style = MaterialTheme.typography.bodyMedium)
                }
            }
            ActionFeedback(vm)
            when {
                phase == DealPhase.UPCOMING -> {
                    PrimaryButton(if (snapshot.account?.reminder == true) "Reminder set · tap to remove" else "Remind me", vm::remind, busy = action.busy)
                    Text("Reminders appear in this app's notification inbox.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SecondaryButton("Demo preview: start drop now", vm::activate, enabled = !action.busy)
                }
                phase == DealPhase.LIVE -> PrimaryButton("Claim deal for ₹99", { confirm = true }, busy = action.busy)
                mine && phase == DealPhase.RESERVED -> PrimaryButton("Proceed to checkout · ₹99", { nav.go("checkout/drop") })
                mine && deal.orderId != null -> PrimaryButton("View your Daily Drop order", { nav.go("order/${deal.orderId}") })
                else -> { PrimaryButton("Sold out", {}, enabled = false); SecondaryButton("Explore more good finds", { nav.go("listing/All") }) }
            }
            HorizontalDivider()
            Text("A fair shot. A simple rule.", style = MaterialTheme.typography.titleLarge)
            listOf("One promotional unit is available.", "The first eligible, successful claim reserves it.", "One claim per account per drop. No random selection.", "Payment is requested only after a successful claim.", "Complete checkout within 10 minutes.").forEachIndexed { index, text ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) { Text("${index + 1}.", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary); Text(text, style = MaterialTheme.typography.bodyMedium) }
            }
            DemoNote("Local demonstration: availability is shared between accounts on this installation. No real purchase or cross-device reservation is made. Photography is illustrative.")
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, icon = { Icon(Icons.Outlined.Bolt, null) }, title = { Text("Make it your Daily Drop?") }, text = { Text("If your claim succeeds, one Sony WH-CH520 is reserved for ₹99. You'll have 10 minutes to complete mock checkout. No payment is taken now.") }, confirmButton = { TextButton({ confirm = false; vm.claim() }) { Text("Confirm claim") } }, dismissButton = { TextButton({ confirm = false }) { Text("Not now") } })
}
