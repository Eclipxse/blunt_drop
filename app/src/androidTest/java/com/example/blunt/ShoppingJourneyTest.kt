package com.example.blunt

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Runs against a clean installation. Uses only public UI controls for the purchase journeys. */
@RunWith(AndroidJUnit4::class)
class ShoppingJourneyTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun waitText(text: String) { ui.waitUntil(15_000) { ui.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() } }
    private fun reveal(text: String) {
        val matcher = hasText(text)
        if (ui.onAllNodes(matcher).fetchSemanticsNodes().isEmpty()) ui.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(matcher)
        runCatching { ui.onAllNodes(matcher).onFirst().performScrollTo() }
    }
    private fun tap(text: String) {
        reveal(text)
        val clickables = ui.onAllNodes(hasText(text) and hasClickAction())
        if (clickables.fetchSemanticsNodes().isNotEmpty()) clickables.onLast().performClick() else ui.onAllNodesWithText(text).onFirst().performClick()
        ui.waitForIdle()
    }
    private fun input(label: String, text: String) { reveal(label); ui.onNode(hasText(label) and hasSetTextAction()).performTextReplacement(text) }
    private fun tab(title: String) { ui.onNodeWithContentDescription("$title tab").performClick(); ui.waitForIdle() }
    private fun back() { Espresso.pressBack(); ui.waitForIdle() }
    private fun shot(name: String) {
        ui.waitForIdle()
        Thread.sleep(500) // Let the emulator compositor present the settled frame.
        val file = File(ui.activity.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @After fun saveLastState() {
        runCatching { shot("last-screen"); File(ui.activity.getExternalFilesDir(null), "last-semantics.txt").writeText(ui.onRoot().printToString()) }
    }

    @Test fun completeShoppingAndDailyDropJourneys() {
        waitText("Skip"); shot("onboarding"); tap("Next"); tap("Next"); tap("Get started")
        waitText("Try the demo account")
        input("Email or phone", "wrong@example.com"); input("Password", "wrongpass"); Espresso.closeSoftKeyboard(); tap("Log in")
        waitText("Email, phone or password is incorrect")
        tap("Create account")
        input("Full name", "Jamie Sharma"); input("Phone number", "9123456789"); input("Email", "jamie@example.com")
        input("Password", "shopping123"); input("Confirm password", "shopping123"); Espresso.closeSoftKeyboard(); tap("Create account")
        waitText("Account created")
        input("Email or phone", "jamie@example.com"); input("Password", "shopping123"); Espresso.closeSoftKeyboard(); tap("Log in")
        waitText("Something good is waiting."); shot("phone")
        ui.onNodeWithContentDescription("Shopping bag").performClick(); waitText("Room for a good find."); shot("empty-cart"); back()
        tab("Categories"); shot("categories"); tap("Gaming"); waitText("Gaming"); tap("Sort"); tap("Price: low to high"); tap("Filter"); tap("In stock only"); tap("Show 2 finds"); shot("listing"); back(); tab("Home")
        tap("Search products, brands & more"); tap("Sony"); waitText("WH-CH520 Wireless Headphones"); tap("WH-CH520 Wireless Headphones")
        waitText("The details"); shot("product")
        ui.onNodeWithContentDescription("Save WH-CH520 Wireless Headphones to wishlist").performClick()
        reveal("Blue"); tap("Blue"); ui.onNodeWithContentDescription("Increase quantity").performClick(); tap("Add to bag")
        waitText("Added to your bag"); ui.onNodeWithContentDescription("Shopping bag").performClick(); waitText("Your bag (2)"); shot("cart")
        tap("Proceed to checkout"); waitText("Nearly yours."); tap("Place order · ₹5,980"); waitText("Select a delivery address")
        tap("Add"); tap("Add an address")
        input("House / flat", "12B"); input("Street", "Sample Street"); input("Area", "Indiranagar"); input("City", "Bengaluru"); input("State", "Karnataka"); input("PIN code", "560038"); Espresso.closeSoftKeyboard(); tap("Save address")
        waitText("Use this address"); tap("Use this address"); tap("Credit / Debit Card"); shot("checkout")
        tap("Demo payment options"); tap("Decline the next mock payment"); tap("Place order · ₹5,980"); waitText("mock payment was declined")
        tap("Place order · ₹5,980"); waitText("Order confirmed."); shot("success"); tap("Track order"); shot("order")
        repeat(4) { index ->
            tap("Demo: advance delivery status")
            ui.waitUntil(5000) { (ui.activity.application as BluntApplication).container.store.state.value.account?.orders?.firstOrNull()?.status?.ordinal == index + 1 }
        }
        tap("Rate your finds"); ui.onNodeWithContentDescription("Rate 4 stars").performClick(); tap("Save rating"); waitText("Your rating: 4/5")
        tap("Need help?"); reveal("Is payment real?"); back(); back(); tap("Continue shopping")
        tab("Daily Deal"); waitText("One good thing.")
        if (ui.onAllNodesWithText("Demo preview: start drop now").fetchSemanticsNodes().isNotEmpty()) tap("Demo preview: start drop now")
        else { runCatching { reveal("Demo preview: start drop now"); tap("Demo preview: start drop now") } }
        tap("Claim deal for ₹99"); tap("Confirm claim"); waitText("Nice catch."); shot("reservation")
        tap("Proceed to checkout · ₹99"); tap("Cash on Delivery"); tap("Place order · ₹99"); waitText("Order confirmed."); shot("drop-success")
        tap("Track order"); tap("Cancel order"); tap("Cancel order"); waitText("Order cancelled. No real payment was taken."); shot("cancelled-order"); back(); tap("Continue shopping")
        tab("Daily Deal"); reveal("Sold out · this drop has been claimed"); ui.onNodeWithText("Sold out · this drop has been claimed").assertExists(); shot("sold-out")
        tab("Orders"); tap("Delivered"); waitText("Delivered"); shot("orders")
        tab("Profile"); tap("Wishlist"); waitText("WH-CH520 Wireless Headphones"); shot("wishlist"); ui.onNodeWithContentDescription("Remove WH-CH520 Wireless Headphones from wishlist").performClick(); waitText("Keep the good ones close."); back()
        tap("Notifications"); shot("notifications"); tap("Clear all"); waitText("All caught up."); back()
        tap("Saved addresses"); tap("Edit"); input("House / flat", "14A"); Espresso.closeSoftKeyboard(); tap("Save address"); waitText("14A"); back()
        tap("Privacy policy"); shot("privacy"); back(); tap("Terms"); waitText("A prototype, with no real purchases"); back()
        tap("Log out"); tap("Log out"); waitText("Try the demo account"); tap("Try the demo account"); waitText("Hi, Alex.")
        tab("Daily Deal"); reveal("Sold out · this drop has been claimed"); ui.onNodeWithText("Sold out · this drop has been claimed").assertExists()
        tab("Home"); shot("final-home")
        tap("Search products, brands & more"); ui.onNode(hasSetTextAction()).performTextReplacement("zzzzzz"); Espresso.closeSoftKeyboard(); waitText("No finds this time."); shot("empty-search")
        tap("Clear search & filters"); ui.onNode(hasSetTextAction()).performTextReplacement("Casio"); Espresso.closeSoftKeyboard(); tap("Vintage Digital Watch"); waitText("The details"); ui.onNode(hasText("Sold out") and hasClickAction()).assertIsNotEnabled(); back(); back()
        tab("Profile"); tap("Start a new demo Daily Drop"); tap("Start new drop"); tab("Daily Deal"); tap("Remind me"); waitText("Reminder set · tap to remove"); shot("upcoming-drop"); tap("Reminder set · tap to remove"); tab("Home")
    }
}
