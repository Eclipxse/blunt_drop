package com.example.blunt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.components.*
import com.example.blunt.viewmodel.CartViewModel

@Composable fun CartScreen(vm: CartViewModel) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val totals by vm.totals.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val cart = snapshot.account?.cart.orEmpty()
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Your bag${if (cart.isEmpty()) "" else " (${cart.sumOf { it.quantity }})"}")
        if (cart.isEmpty()) Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { EmptyState(Icons.Outlined.ShoppingBag, "Room for a good find.", "Your bag is empty. Let's find something you'll love using every day.", "Explore the edit", { nav.go("listing/All") }) }
        else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                item { ActionFeedback(vm); Text("A few good choices.", style = MaterialTheme.typography.headlineMedium) }
                items(cart, key = { it.key }) { item ->
                    val product = vm.product(item.productId)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ProductImage(product.images.first(), product.name, Modifier.size(96.dp).clip(MaterialTheme.shapes.medium))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(product.brand, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(product.name, style = MaterialTheme.typography.titleMedium)
                                Text(item.variant, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                PriceDisplay(product.price, product.mrp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            QuantitySelector(item.quantity, { vm.quantity(item.key, it) })
                            Spacer(Modifier.weight(1f))
                            TextButton({ vm.remove(item.key, true) }) { Text("Save for later") }
                            IconButton({ vm.remove(item.key) }) { Icon(Icons.Outlined.DeleteOutline, "Remove ${product.name}") }
                        }
                        HorizontalDivider()
                    }
                }
                item { PriceDetails(totals) }
                item { DemoNote("Free delivery from ₹999. Below that, delivery is ₹49. Daily Drops always include free delivery.") }
            }
            Surface(shadowElevation = 2.dp) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { PrimaryButton("Proceed to checkout", { nav.go("checkout/cart") }) } }
        }
    }
}
