package com.example.blunt.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blunt.model.*
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.components.*
import com.example.blunt.viewmodel.ProductViewModel

@Composable fun ProductScreen(id: String, vm: ProductViewModel) {
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val action by vm.action.collectAsStateWithLifecycle()
    val product = vm.product(id)
    if (product == null) { Column { ScreenHeader("Product"); EmptyState(Icons.Outlined.SearchOff, "This find has moved.", "Browse the edit for something else to love.", "Explore products", { nav.go("listing/All") }) }; return }
    var color by rememberSaveable(id) { mutableStateOf(product.colors.first()) }
    var size by rememberSaveable(id) { mutableStateOf(product.sizes.firstOrNull().orEmpty()) }
    var quantity by rememberSaveable(id) { mutableIntStateOf(1) }
    val stock = vm.available(product, snapshot)
    val saved = id in snapshot.account?.wishlist.orEmpty()
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("The details") {
            IconButton({
                val share = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Found on Blunt: ${product.brand} ${product.name} — ${money(product.price)}. Illustrative local demo offer.") }
                runCatching { context.startActivity(Intent.createChooser(share, "Share this find")) }.onFailure { nav.message("No sharing app is available on this device.") }
            }) { Icon(Icons.Outlined.Share, "Share product") }
            IconButton({ nav.go("cart") }) { Icon(Icons.Outlined.ShoppingBag, "Shopping bag") }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Box(Modifier.padding(horizontal = 24.dp)) {
                    val pager = rememberPagerState { product.images.size + 1 }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalPager(pager) { page ->
                            ProductImage(product.images[page.coerceAtMost(product.images.lastIndex)], product.name + if (page == product.images.size) " detail crop" else " full view",
                                Modifier.fillMaxWidth().heightIn(max = 410.dp).aspectRatio(1.03f).clip(MaterialTheme.shapes.large), if (page == product.images.size) ContentScale.Crop else ContentScale.Fit)
                        }
                        Text("${pager.currentPage + 1} / ${product.images.size + 1}   ·   ${if (pager.currentPage == product.images.size) "Detail crop" else "Full view"}   ·   Swipe to explore", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    WishlistButton(saved, { vm.wishlist(id) }, product.name, Modifier.align(Alignment.TopEnd).padding(10.dp))
                }
            }
            item {
                Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(product.brand, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(product.name, style = MaterialTheme.typography.headlineLarge)
                    RatingDisplay(product.rating, product.reviews)
                    PriceDisplay(product.price, product.mrp, true)
                    Text("Inclusive of all taxes · Demo price", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) { Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(Icons.Outlined.LocalOffer, null, Modifier.size(20.dp)); Text("Save ${money(product.mrp - product.price)} on this find", style = MaterialTheme.typography.bodyMedium) } }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Color / option: $color", style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(product.colors) { option -> FilterChip(color == option, { color = option }, { Text(option) }) } }
                    if (product.sizes.isNotEmpty()) { Text("Size (UK): $size", style = MaterialTheme.typography.titleMedium); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(product.sizes) { option -> FilterChip(size == option, { size = option }, { Text(option) }) } } }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Quantity", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium); QuantitySelector(quantity, { quantity = it }, minOf(10, stock).coerceAtLeast(1)) }
                    Text(if (stock > 0) "In stock · $stock available" else "Currently out of stock", style = MaterialTheme.typography.labelLarge, color = if (stock > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(Icons.Outlined.LocalShipping, null, Modifier.size(20.dp)); Text("Estimated delivery in 3–5 days. Free delivery on orders of ₹999 or more.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    ActionFeedback(vm)
                }
            }
            item { Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { HorizontalDivider(); Text("A closer look", style = MaterialTheme.typography.titleLarge); Text(product.description, style = MaterialTheme.typography.bodyLarge) } }
            item { Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text("The particulars", style = MaterialTheme.typography.titleLarge); product.specifications.forEach { (label, value) -> DetailRow(label, value) }; DemoNote("Illustrative photography. Model, color, specifications and reviews are demonstration data.") } }
            item { Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ratings at a glance", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) { Text("${product.rating}", style = MaterialTheme.typography.displayMedium); Column { RatingDisplay(product.rating); Text("${product.reviews} illustrative reviews", style = MaterialTheme.typography.bodySmall) } }
                LinearProgressIndicator(progress = { (product.rating / 5).toFloat() }, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.tertiary)
            } }
            if (vm.related(product).isNotEmpty()) item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("You might also like", Modifier.padding(horizontal = 24.dp), style = MaterialTheme.typography.titleLarge); ProductRail(vm.related(product), snapshot.account?.wishlist.orEmpty(), vm::wishlist) } }
        }
        Surface(shadowElevation = 2.dp) {
            Row(Modifier.padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton("Add to bag", { vm.add(product, color, size, quantity) }, Modifier.weight(1f), enabled = stock > 0 && !action.busy)
                PrimaryButton(if (stock > 0) "Buy now" else "Sold out", { vm.add(product, color, size, quantity, true) }, Modifier.weight(1f), enabled = stock > 0, busy = action.busy)
            }
        }
    }
}
