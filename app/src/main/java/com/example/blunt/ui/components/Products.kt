package com.example.blunt.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.blunt.model.*
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.theme.DropOrange
import com.example.blunt.ui.theme.StudioInk

@Composable fun ProductImage(image: String, description: String?, modifier: Modifier = Modifier, scale: ContentScale = ContentScale.Crop) {
    var loading by remember(image) { mutableStateOf(true) }
    var failed by remember(image) { mutableStateOf(false) }
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainer), contentAlignment = Alignment.Center) {
        if (loading) LoadingSkeleton(Modifier.matchParentSize())
        AsyncImage(image, description, Modifier.fillMaxSize(), contentScale = scale,
            onSuccess = { loading = false; failed = false }, onError = { loading = false; failed = true })
        if (failed) Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.ImageNotSupported, null); Text("Image unavailable", style = MaterialTheme.typography.labelSmall)
        }
    }
}
@Composable fun WishlistButton(saved: Boolean, onClick: () -> Unit, name: String, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(if (saved) 1.06f else 1f, spring(dampingRatio = 1f, stiffness = 600f), label = "Saved")
    IconToggleButton(saved, { onClick() }, modifier.size(48.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = .96f), CircleShape)) {
        Icon(if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, if (saved) "Remove $name from wishlist" else "Save $name to wishlist", Modifier.size(21.dp).scale(scale), tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}
@Composable fun ProductCard(product: Product, saved: Boolean, onWishlist: () -> Unit, modifier: Modifier = Modifier) {
    val nav = LocalNavigator.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .98f else 1f, spring(dampingRatio = 1f), label = "Product press")
    Column(modifier.scale(scale).clickable(source, indication = null, onClickLabel = "View ${product.name}") { nav.go("product/${product.id}") }, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box {
            ProductImage(product.images.first(), product.name, Modifier.fillMaxWidth().aspectRatio(.92f).clip(MaterialTheme.shapes.medium))
            WishlistButton(saved, onWishlist, product.name, Modifier.align(Alignment.TopEnd).padding(6.dp))
            if (product.stock == 0) Surface(Modifier.align(Alignment.BottomStart).padding(8.dp), color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small) { Text("Sold out", Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall) }
        }
        Text(product.brand, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(product.name, maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2, style = MaterialTheme.typography.titleSmall)
        RatingDisplay(product.rating)
        PriceDisplay(product.price, product.mrp)
    }
}
@Composable fun ProductRail(products: List<Product>, wishlist: Set<String>, toggle: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(products, key = { it.id }) { ProductCard(it, it.id in wishlist, { toggle(it.id) }, Modifier.width(168.dp)) }
    }
}
fun categoryIcon(category: String) = when (category) {
    "Electronics" -> Icons.Outlined.Headphones
    "Fashion" -> Icons.Outlined.Checkroom
    "Beauty" -> Icons.Outlined.Spa
    "Home" -> Icons.Outlined.Chair
    "Shoes" -> Icons.Outlined.DirectionsRun
    "Accessories" -> Icons.Outlined.Watch
    "Gaming" -> Icons.Outlined.SportsEsports
    else -> Icons.Outlined.GridView
}
@Composable fun CategoryItem(name: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.widthIn(min = 72.dp).clickable(onClick = onClick).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(60.dp).background(MaterialTheme.colorScheme.surfaceContainer, CircleShape), contentAlignment = Alignment.Center) { Icon(categoryIcon(name), null, Modifier.size(24.dp)) }
        Text(name, style = MaterialTheme.typography.labelMedium)
    }
}
@Composable fun DailyDealCard(product: Product, deal: DealState, userId: String?, now: Long, onClick: () -> Unit) {
    val phase = deal.phase(now)
    val owned = phase == DealPhase.RESERVED && deal.claimedBy == userId
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), color = StudioInk, contentColor = Color.White, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("The Daily Drop.", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                Surface(color = DropOrange, contentColor = StudioInk, shape = CircleShape) { Text(if (phase == DealPhase.UPCOMING) "COMING UP" else if (phase == DealPhase.LIVE) "1 UNIT ONLY" else "SOLD OUT", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sony WH-CH520", style = MaterialTheme.typography.titleMedium)
                    Text(money(deal.price), style = MaterialTheme.typography.displayMedium, color = DropOrange)
                    Text("Usually ${money(product.mrp)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCFD0CA))
                }
                ProductImage(product.images.first(), product.name, Modifier.size(116.dp).clip(MaterialTheme.shapes.medium))
            }
            HorizontalDivider(color = Color.White.copy(alpha = .18f))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (owned) "Your reservation ends in" else if (phase == DealPhase.UPCOMING) "Drops in" else if (phase == DealPhase.LIVE) "Ends in" else "One drop. One lucky find.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCFD0CA))
                    if (owned || phase in listOf(DealPhase.UPCOMING, DealPhase.LIVE)) Text(countdown((if (owned) deal.reservedUntil else if (phase == DealPhase.UPCOMING) deal.startsAt else deal.endsAt) - now), style = MaterialTheme.typography.titleMedium)
                }
                FilledTonalButton(onClick, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White, contentColor = StudioInk), shape = MaterialTheme.shapes.small) {
                    Text(if (owned) "Checkout" else if (phase == DealPhase.UPCOMING) "Remind me" else if (phase == DealPhase.LIVE) "Claim ₹99" else "View drop")
                }
            }
        }
    }
}
