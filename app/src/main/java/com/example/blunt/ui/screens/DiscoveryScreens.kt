package com.example.blunt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blunt.data.Catalogue
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.components.*
import com.example.blunt.viewmodel.*

@Composable fun HomeScreen(home: HomeViewModel, products: ProductViewModel, drop: DailyDealViewModel) {
    val nav = LocalNavigator.current
    val snapshot by home.state.collectAsStateWithLifecycle()
    val now by drop.now.collectAsStateWithLifecycle()
    val account = snapshot.account
    val wishlist = account?.wishlist.orEmpty()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Wordmark(Modifier.weight(1f))
                    IconButton({ nav.go("notifications") }) { BadgedBox(badge = { if (account?.notifications?.any { !it.read } == true) Badge() }) { Icon(Icons.Outlined.NotificationsNone, "Notifications") } }
                    IconButton({ nav.go("cart") }) { BadgedBox(badge = { val count = account?.cart?.sumOf { it.quantity } ?: 0; if (count > 0) Badge { Text("$count") } }) { Icon(Icons.Outlined.ShoppingBag, "Shopping bag") } }
                }
                Column { Text("Hi, ${account?.user?.name?.substringBefore(' ') ?: "there"}.", style = MaterialTheme.typography.headlineSmall); Text("Something good is waiting.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Surface(onClick = { nav.go("search") }, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Search, null); Text("Search products, brands & more", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        item { PromotionalBanner(Modifier.padding(horizontal = 24.dp)) }
        item { Column(Modifier.padding(horizontal = 24.dp)) { DailyDealCard(drop.product, snapshot.deal, snapshot.sessionId, now) { nav.go("daily") } } }
        item {
            Box(Modifier.padding(horizontal = 24.dp)) { SectionTitle("Shop by category", "All", { nav.go("categories") }) }
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(Catalogue.categories) { category -> CategoryItem(category, { nav.go("listing/$category") }) } }
        }
        item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.padding(horizontal = 24.dp)) { SectionTitle("Trending now", "See all", { nav.go("listing/Trending") }) }
            ProductRail(home.products.take(6), wishlist, products::wishlist)
        } }
        item { Box(Modifier.padding(horizontal = 24.dp)) { SectionTitle("Picked for your everyday", "Explore", { nav.go("listing/All") }) } }
        items(home.products.drop(6).take(6).chunked(2)) { row ->
            Row(Modifier.padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) { row.forEach { p -> ProductCard(p, p.id in wishlist, { products.wishlist(p.id) }, Modifier.weight(1f)) } }
        }
        item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.padding(horizontal = 24.dp)) { SectionTitle("The repeat favorites", "Best sellers", { nav.go("listing/Bestsellers") }) }
            ProductRail(home.products.filter { it.stock > 0 }.sortedByDescending { it.rating }.take(6), wishlist, products::wishlist)
        } }
        item { Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { HorizontalDivider(); Text("Find a little better.", style = MaterialTheme.typography.headlineMedium); DemoNote(); ActionFeedback(products) } }
    }
}
@Composable private fun PromotionalBanner(modifier: Modifier) {
    val nav = LocalNavigator.current
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainer, contentColor = MaterialTheme.colorScheme.onSurface, shape = MaterialTheme.shapes.large) {
        Row(Modifier.heightIn(min = 206.dp)) {
            Column(Modifier.weight(1.05f).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Everyday,\nbut better.", style = MaterialTheme.typography.headlineMedium)
                Text("Up to 60% off the daily edit.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton({ nav.go("listing/All") }, contentPadding = PaddingValues(0.dp)) { Text("Shop now"); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.padding(start = 8.dp).size(18.dp)) }
            }
            ProductImage("file:///android_asset/products/sneakers.jpg", "Everyday sneaker edit", Modifier.weight(.95f).height(232.dp))
        }
    }
}
@Composable fun CategoriesScreen() {
    val nav = LocalNavigator.current
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Find your thing.", false) { IconButton({ nav.go("search") }) { Icon(Icons.Outlined.Search, "Search") }; IconButton({ nav.go("cart") }) { Icon(Icons.Outlined.ShoppingBag, "Shopping bag") } }
        Text("Good finds, in every corner.", Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyVerticalGrid(GridCells.Adaptive(150.dp), contentPadding = PaddingValues(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(Catalogue.categories) { name ->
                val p = Catalogue.products.find { it.category == name } ?: Catalogue.products.last()
                Column(Modifier.clickable { nav.go("listing/${if (name == "More") "All" else name}") }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProductImage(p.images.first(), null, Modifier.fillMaxWidth().aspectRatio(1.12f).clip(MaterialTheme.shapes.medium))
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(18.dp)) }
                    Text(if (name == "More") "Explore everything" else "${Catalogue.products.count { it.category == name }} considered finds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
@Composable fun ListingScreen(vm: ProductViewModel, category: String, search: Boolean = false) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val results by vm.results.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    var sorting by remember { mutableStateOf(false) }
    var filtering by remember { mutableStateOf(false) }
    LaunchedEffect(category, search) { vm.update(ProductQuery(category = if (category in Catalogue.categories) category else "All", sort = if (category == "Bestsellers") SortMode.RATING else SortMode.FEATURED)) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(if (search) "Find a good thing." else when (category) { "All" -> "The whole edit"; "Trending" -> "Trending deals"; "Bestsellers" -> "Best sellers"; else -> category }) { IconButton({ nav.go("cart") }) { Icon(Icons.Outlined.ShoppingBag, "Shopping bag") } }
        OutlinedTextField(query.text, { vm.update(query.copy(text = it)) }, Modifier.fillMaxWidth().padding(horizontal = 24.dp), singleLine = true,
            placeholder = { Text("Products, brands, categories", style = MaterialTheme.typography.bodyMedium) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = MaterialTheme.shapes.medium,
            trailingIcon = if (query.text.isEmpty()) null else ({ IconButton({ vm.update(query.copy(text = "")) }) { Icon(Icons.Outlined.Close, "Clear search") } }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { vm.searchSaved(query.text) }))
        if (search && query.text.isBlank()) {
            LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { SectionTitle("Recent searches", if (snapshot.account?.recentSearches.isNullOrEmpty()) null else "Clear", vm::clearSearches) }
                if (snapshot.account?.recentSearches.isNullOrEmpty()) item { Text("Your next good find starts here. Try a brand, product or category.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(snapshot.account?.recentSearches.orEmpty()) { term -> SearchSuggestion(term, true) { vm.update(query.copy(text = term)) } }
                item { Spacer(Modifier.height(12.dp)); Text("Popular right now", style = MaterialTheme.typography.titleLarge) }
                items(listOf("Sony", "headphones", "shoes", "gaming", "fashion")) { term -> SearchSuggestion(term, false) { vm.update(query.copy(text = term)); vm.searchSaved(term) } }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${results.size} finds", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton({ sorting = true }) { Icon(Icons.Outlined.Sort, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Sort") }
                TextButton({ filtering = true }) { Icon(Icons.Outlined.Tune, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Filter${if (query.inStock || query.under2000 || query.topRated) " •" else ""}") }
            }
            if (results.isEmpty()) EmptyState(Icons.Outlined.SearchOff, "No finds this time.", "Try another word or clear your filters. Your next favorite is out there.", "Clear search & filters", { vm.update(ProductQuery(category = query.category)) })
            else LazyVerticalGrid(GridCells.Adaptive(150.dp), Modifier.weight(1f), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                items(results, key = { it.id }) { p -> ProductCard(p, p.id in snapshot.account?.wishlist.orEmpty(), { vm.wishlist(p.id) }) }
                item(span = { GridItemSpan(maxLineSpan) }) { ActionFeedback(vm) }
            }
        }
    }
    if (sorting) AlertDialog(onDismissRequest = { sorting = false }, title = { Text("Sort your finds") }, text = { Column { SortMode.entries.forEach { mode -> Row(Modifier.fillMaxWidth().clickable { vm.update(query.copy(sort = mode)); sorting = false }, verticalAlignment = Alignment.CenterVertically) { RadioButton(query.sort == mode, { vm.update(query.copy(sort = mode)); sorting = false }); Text(mode.label) } } } }, confirmButton = { TextButton({ sorting = false }) { Text("Done") } })
    if (filtering) AlertDialog(onDismissRequest = { filtering = false }, title = { Text("A little more specific") }, text = {
        Column { FilterRow("In stock only", query.inStock) { vm.update(query.copy(inStock = it)) }; FilterRow("Under ₹2,000", query.under2000) { vm.update(query.copy(under2000 = it)) }; FilterRow("Rated 4.5 and above", query.topRated) { vm.update(query.copy(topRated = it)) } }
    }, confirmButton = { TextButton({ filtering = false }) { Text("Show ${results.size} finds") } }, dismissButton = { TextButton({ vm.update(query.copy(inStock = false, under2000 = false, topRated = false)) }) { Text("Reset") } })
}
@Composable private fun SearchSuggestion(term: String, recent: Boolean, action: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = action).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) { Icon(if (recent) Icons.Outlined.History else Icons.Outlined.TrendingUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Text(term, Modifier.weight(1f)); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(18.dp)) }
}
@Composable private fun FilterRow(text: String, selected: Boolean, onChange: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Checkbox(selected, onChange); Text(text) } }
