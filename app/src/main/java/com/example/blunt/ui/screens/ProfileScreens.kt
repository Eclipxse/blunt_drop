package com.example.blunt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.components.*
import com.example.blunt.viewmodel.AuthViewModel
import com.example.blunt.viewmodel.ProfileViewModel

@Composable fun ProfileScreen(vm: ProfileViewModel, auth: AuthViewModel) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val user = snapshot.account?.user
    var logout by remember { mutableStateOf(false) }
    var reset by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { ScreenHeader("Your corner.", false) }
        item { Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(72.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) { Text(user?.name?.split(' ')?.take(2)?.mapNotNull { it.firstOrNull()?.uppercaseChar() }?.joinToString("") ?: "B", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer) }
            Column { Text(user?.name.orEmpty(), style = MaterialTheme.typography.titleLarge); Text(user?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("+91 ${user?.phone.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } }
        item { Column(Modifier.padding(horizontal = 24.dp)) {
            ProfileRow("My orders", Icons.Outlined.Inventory2, "${snapshot.account?.orders?.size ?: 0} orders") { nav.go("orders") }
            ProfileRow("Saved addresses", Icons.Outlined.LocationOn) { nav.go("addresses") }
            ProfileRow("Wishlist", Icons.Outlined.FavoriteBorder, "${snapshot.account?.wishlist?.size ?: 0} saved") { nav.go("wishlist") }
            ProfileRow("Notifications", Icons.Outlined.NotificationsNone) { nav.go("notifications") }
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            ProfileRow("Help & support", Icons.Outlined.HelpOutline) { nav.go("info/help") }
            ProfileRow("Privacy policy", Icons.Outlined.PrivacyTip) { nav.go("info/privacy") }
            ProfileRow("Terms", Icons.Outlined.Description) { nav.go("info/terms") }
            ProfileRow("Log out", Icons.Outlined.Logout) { logout = true }
        } }
        item { Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HorizontalDivider(); Text("Made for a good demo.", style = MaterialTheme.typography.titleMedium)
            DemoNote("Blunt 1.0 · Local prototype\nAll catalogue offers, reviews, payments and delivery estimates are illustrative. Your account, bag and orders are saved only on this device.")
            TextButton({ reset = true }) { Icon(Icons.Outlined.RestartAlt, null); Spacer(Modifier.width(8.dp)); Text("Start a new demo Daily Drop") }
            ActionFeedback(vm)
        } }
    }
    if (logout) AlertDialog(onDismissRequest = { logout = false }, title = { Text("Log out of Blunt?") }, text = { Text("Your bag, wishlist, addresses and orders will be here when you return.") }, confirmButton = { TextButton({ logout = false; auth.logout() }) { Text("Log out") } }, dismissButton = { TextButton({ logout = false }) { Text("Stay") } })
    if (reset) AlertDialog(onDismissRequest = { reset = false }, title = { Text("Start a new demo drop?") }, text = { Text("This replaces the current drop and any unpaid reservation on this device. Existing orders are kept. The new drop starts in 2 minutes.") }, confirmButton = { TextButton({ reset = false; vm.resetDemo() }) { Text("Start new drop") } }, dismissButton = { TextButton({ reset = false }) { Text("Keep current drop") } })
}
@Composable private fun ProfileRow(title: String, icon: ImageVector, detail: String? = null, action: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = action).padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(icon, null, Modifier.size(22.dp)); Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(Icons.Outlined.ChevronRight, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable fun WishlistScreen(vm: ProfileViewModel) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val products = vm.products.filter { it.id in snapshot.account?.wishlist.orEmpty() }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Saved for a good day") { IconButton({ nav.go("cart") }) { Icon(Icons.Outlined.ShoppingBag, "Shopping bag") } }
        if (products.isEmpty()) Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { EmptyState(Icons.Outlined.FavoriteBorder, "Keep the good ones close.", "Tap the heart on a product to save it here for later.", "Find a favorite", { nav.go("listing/All") }) }
        else LazyVerticalGrid(GridCells.Adaptive(150.dp), contentPadding = PaddingValues(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            items(products, key = { it.id }) { p -> ProductCard(p, true, { vm.wishlist(p.id) }) }
            item(span = { GridItemSpan(maxLineSpan) }) { ActionFeedback(vm) }
        }
    }
}
@Composable fun NotificationsScreen(vm: ProfileViewModel) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val notices = snapshot.account?.notifications.orEmpty()
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Your updates") { if (notices.isNotEmpty()) TextButton(vm::clearNotices) { Text("Clear all") } }
        if (notices.isEmpty()) Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { EmptyState(Icons.Outlined.NotificationsNone, "All caught up.", "Drop reminders and order updates will find you here.") }
        else LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(notices, key = { it.id }) { notice ->
                Column(Modifier.fillMaxWidth().clickable { vm.readNotice(notice.id); nav.go(notice.target) }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(44.dp).background(if (notice.read) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) { Icon(if (notice.target.startsWith("order")) Icons.Outlined.LocalShipping else Icons.Outlined.NotificationsNone, null, Modifier.size(21.dp)) }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(notice.title, style = MaterialTheme.typography.titleMedium); Text(notice.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); if (!notice.read) Text("New", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                    }
                    HorizontalDivider()
                }
            }
            item { ActionFeedback(vm) }
        }
    }
}
