package com.example.blunt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.components.*

@Composable fun InformationScreen(page: String) {
    val nav = LocalNavigator.current
    val title = when (page) { "privacy" -> "Privacy, plainly."; "terms" -> "Demo terms"; "drop" -> "How the Daily Drop works"; else -> "A little help." }
    val sections = when (page) {
        "privacy" -> listOf(
            "What stays on this device" to "Your profile, hashed password, login session, wishlist, bag, addresses, searches, notifications and orders are saved in the app's private local storage. This version has no analytics or advertising SDK.",
            "Your password" to "Passwords are salted and hashed. The app does not save your raw password. This is demonstration authentication and is not a substitute for a production identity service.",
            "Images and sharing" to "Product photos are bundled in the app. Using Share hands the product text to the app you select through Android's share sheet.",
            "Remove your data" to "You can remove saved addresses, bag items, favorites and notifications inside the app. To remove all demo accounts and orders, use Android Settings → Apps → Blunt → Storage → Clear storage. This is permanent.",
            "About this notice" to "This notice describes this local prototype only. A real service would need an updated privacy notice, support details and appropriate controls before launch."
        )
        "terms" -> listOf(
            "A prototype, with no real purchases" to "Blunt is a local shopping demonstration. Catalogue entries, prices, savings, reviews, stock and delivery estimates are illustrative. No payment is charged, product dispatched or commercial agreement created.",
            "Daily Drop" to "One promotional unit can be claimed per demo drop on an installation. The first successful claim reserves it for 10 minutes. There is no lottery, random draw, entry fee or membership. An expired or cancelled claim does not automatically reopen the same drop.",
            "Demo controls" to "The demo can activate or reset a drop, decline a mock payment and advance order tracking. These controls do not represent a live shopping service.",
            "Catalogue photography" to "Photos illustrate product categories and may not match the named model, color or specification. Replace this material with verified, licensed SKU imagery before any public commercial launch."
        )
        "drop" -> listOf(
            "One unit. First successful claim." to "When the drop is live, tap Claim deal and confirm. The first completed claim on this installation reserves the one promotional unit. All other accounts see Sold out.",
            "Ten minutes to check out" to "After a successful claim, you get 10 minutes to choose a delivery address and complete mock checkout. Payment is requested only after your claim succeeds. Closing the app does not pause the timer.",
            "When the timer ends" to "The reservation expires and checkout is disabled. No payment is taken. The drop stays closed until a new demo drop is explicitly started from Profile.",
            "Try another run" to "Profile → Start a new demo Daily Drop schedules a fresh drop. You can also use Start drop now on the upcoming drop screen. This is a local demonstration, not shared live inventory across devices."
        )
        else -> listOf(
            "Where is my order?" to "Open My Orders, select your order and follow its timeline. Since this is a demo, the 'Advance delivery status' control moves it through packed, shipped, out for delivery and delivered.",
            "Can I cancel an order?" to "You can cancel while an order is confirmed or packed. Open the order and select Cancel order. A dispatched order cannot be cancelled in this demo. No real payment needs a refund.",
            "Why didn't my Daily Drop go through?" to "There is only one promotional unit. A second claim is rejected, and a successful reservation expires after 10 minutes. Check the Daily Drop screen for the current state.",
            "Is payment real?" to "No. UPI, card and cash on delivery are demonstration choices. We never ask for a card number, CVV, PIN, UPI ID or financial account access.",
            "What if I forget my password?" to "Email and SMS recovery aren't connected in this prototype. Use the built-in demo account (demo@example.com / password123), or create another local account. Logging out keeps your existing data."
        )
    }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(if (page == "help") "Help & support" else title)
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item { Text(title, style = MaterialTheme.typography.headlineLarge) }
            sections.forEach { (heading, body) -> item { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(heading, style = MaterialTheme.typography.titleMedium); Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            if (page == "help") item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { PrimaryButton("View my orders", { nav.go("orders") }); SecondaryButton("Open the Daily Drop", { nav.go("daily") }) } }
            item { DemoNote("Blunt · Local MVP · No live customer support service is connected.") }
        }
    }
}
