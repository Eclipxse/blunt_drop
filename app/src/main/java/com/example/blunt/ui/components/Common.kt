package com.example.blunt.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.blunt.model.*
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.viewmodel.ActionViewModel
import com.example.blunt.viewmodel.UiEvent
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable fun Wordmark(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("blunt", style = MaterialTheme.typography.headlineLarge)
        Box(Modifier.padding(start = 3.dp, top = 16.dp).size(7.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
    }
}
@Composable fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, busy: Boolean = false) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 54.dp), enabled = enabled && !busy, shape = MaterialTheme.shapes.medium, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
        if (busy) { CircularProgressIndicator(Modifier.size(18.dp), color = LocalContentColor.current, strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)) }
        Text(if (busy) "Please wait…" else text, style = MaterialTheme.typography.labelLarge)
    }
}
@Composable fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick, modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = enabled, shape = MaterialTheme.shapes.medium, contentPadding = PaddingValues(16.dp)) { Text(text) }
}
@Composable fun ScreenHeader(title: String, back: Boolean = true, action: @Composable RowScope.() -> Unit = {}) {
    val nav = LocalNavigator.current
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = if (back) 12.dp else 24.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back) IconButton(nav.back) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
        Text(title, Modifier.weight(1f).padding(start = if (back) 4.dp else 0.dp), style = MaterialTheme.typography.titleLarge)
        action()
    }
}
@Composable fun SectionTitle(title: String, link: String? = null, onClick: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        if (link != null) TextButton(onClick) { Text(link); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.padding(start = 4.dp).size(16.dp)) }
    }
}
@Composable fun PriceDisplay(price: Int, mrp: Int, large: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(money(price), style = if (large) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleMedium)
        if (mrp > price) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(money(mrp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
            Text("${((mrp - price) * 100.0 / mrp).toInt()}% off", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
@Composable fun RatingDisplay(rating: Double, reviews: Int? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(Icons.Outlined.Star, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
        Text("%.1f".format(rating), style = MaterialTheme.typography.labelMedium)
        if (reviews != null) Text("($reviews reviews)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable fun QuantitySelector(quantity: Int, onChange: (Int) -> Unit, maximum: Int = 10) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton({ onChange(quantity - 1) }, enabled = quantity > 1) { Icon(Icons.Outlined.Remove, "Decrease quantity") }
        Text("$quantity", Modifier.widthIn(min = 24.dp).semantics { contentDescription = "Quantity $quantity" }, style = MaterialTheme.typography.titleMedium)
        IconButton({ onChange(quantity + 1) }, enabled = quantity < maximum) { Icon(Icons.Outlined.Add, "Increase quantity") }
    }
}
@Composable fun EmptyState(icon: ImageVector, title: String, body: String, action: String? = null, onAction: () -> Unit = {}, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.size(88.dp).background(MaterialTheme.colorScheme.surfaceContainer, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (action != null) PrimaryButton(action, onAction, Modifier.widthIn(max = 300.dp))
    }
}
@Composable fun ErrorBanner(text: String?, modifier: Modifier = Modifier) {
    if (text != null) Surface(modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(Icons.Outlined.ErrorOutline, null, Modifier.size(20.dp)); Text(text, style = MaterialTheme.typography.bodyMedium) }
    }
}
@Composable fun LoadingSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "Loading")
    val alpha by transition.animateFloat(0.3f, 0.65f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "Loading pulse")
    Box(modifier.clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * .16f)).semantics { contentDescription = "Loading content" })
}
@Composable fun ActionFeedback(vm: ActionViewModel) { val action by vm.action.collectAsStateWithLifecycle(); ErrorBanner(action.error) }
@Composable fun ActionEvents(vm: ActionViewModel, handle: (UiEvent) -> Unit) {
    val latest by rememberUpdatedState(handle)
    LaunchedEffect(vm) { vm.events.collect { latest(it) } }
}
@Composable fun FormField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, keyboard: KeyboardType = KeyboardType.Text, password: Boolean = false, error: String? = null) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(value, onValueChange, modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        shape = MaterialTheme.shapes.medium, isError = error != null, supportingText = if (error == null) null else ({ Text(error) }),
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard, imeAction = ImeAction.Next),
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (!password) null else ({ IconButton({ visible = !visible }) { Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (visible) "Hide password" else "Show password") } }))
}
@Composable fun PriceDetails(totals: Totals, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Price details", style = MaterialTheme.typography.titleLarge)
        DetailRow("Total MRP", money(totals.mrp)); DetailRow("Discount", "−${money(totals.discount)}", positive = true)
        DetailRow("Delivery", if (totals.delivery == 0) "Free" else money(totals.delivery), positive = totals.delivery == 0)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total amount", style = MaterialTheme.typography.titleMedium); Text(money(totals.total), style = MaterialTheme.typography.titleLarge) }
    }
}
@Composable fun DetailRow(label: String, value: String, positive: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = if (positive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
    }
}
@Composable fun DemoNote(text: String = "A local shopping demo. Products, offers and payments are illustrative.") { Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
