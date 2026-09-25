package com.example.blunt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.repository.Validation
import com.example.blunt.ui.components.*
import com.example.blunt.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable fun SplashScreen(vm: AuthViewModel) {
    val error by vm.loadError.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Wordmark(); Spacer(Modifier.height(12.dp)); Text("Good finds. Better prices.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(32.dp))
        if (error != null) { ErrorBanner(error); SecondaryButton("Retry", vm::retry) }
        else LoadingSkeleton(Modifier.width(100.dp).height(4.dp))
    }
}
@Composable fun OnboardingScreen(vm: AuthViewModel) {
    val pager = rememberPagerState { 3 }
    val scope = rememberCoroutineScope()
    val titles = listOf("Good things.\nGreat finds.", "Your everyday,\nupgraded.", "One drop.\nAn unreal price.")
    val bodies = listOf("Shop everything you love, thoughtfully picked and priced for your everyday.", "Discover fresh deals on the things that make life a little better.", "One product. One promotional unit. The first successful claim gets the ₹99 Daily Drop.")
    val images = listOf("sneakers", "watch", "headphones")
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Wordmark(); TextButton(vm::finishOnboarding) { Text("Skip") } }
        HorizontalPager(pager, Modifier.weight(1f)) { page ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                ProductImage("file:///android_asset/products/${images[page]}.jpg", null, Modifier.fillMaxWidth().heightIn(max = 340.dp).aspectRatio(1.05f).clip(MaterialTheme.shapes.large))
                Text(titles[page], style = MaterialTheme.typography.displaySmall)
                Text(bodies[page], style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (page == 2) DemoNote("A demo offer, with no lottery, random draw or real payment.")
            }
        }
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { repeat(3) { Box(Modifier.width(if (pager.currentPage == it) 28.dp else 7.dp).height(7.dp).background(if (pager.currentPage == it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape)) } }
            PrimaryButton(if (pager.currentPage == 2) "Get started" else "Next", { if (pager.currentPage == 2) vm.finishOnboarding() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } })
        }
    }
}
@Composable fun LoginScreen(vm: AuthViewModel, created: Boolean) {
    val nav = LocalNavigator.current
    val action by vm.action.collectAsStateWithLifecycle()
    var identity by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Wordmark(Modifier.padding(top = 20.dp, bottom = 28.dp))
        Text(if (created) "You're in.\nMake yourself at home." else "Good to\nsee you again.", style = MaterialTheme.typography.displaySmall)
        Text(if (created) "Account created. Log in with your new details." else "Log in for good finds, saved favorites and your next Daily Drop.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        FormField(identity, { identity = it; vm.clearError() }, "Email or phone", keyboard = KeyboardType.Email)
        FormField(password, { password = it; vm.clearError() }, "Password", password = true)
        TextButton({ nav.go("forgot") }, Modifier.align(Alignment.End)) { Text("Forgot password?") }
        ActionFeedback(vm)
        PrimaryButton("Log in", { vm.login(identity, password) }, busy = action.busy)
        SecondaryButton("Try the demo account", { vm.login("demo@example.com", "password123") }, enabled = !action.busy)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Text("New to Blunt?", style = MaterialTheme.typography.bodyMedium); TextButton({ nav.go("register") }) { Text("Create account") } }
        DemoNote("Demo login: demo@example.com / password123. Accounts and orders stay on this device.")
    }
}
@Composable fun RegisterScreen(vm: AuthViewModel) {
    val nav = LocalNavigator.current
    val action by vm.action.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }; var phone by rememberSaveable { mutableStateOf("") }; var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }; var confirm by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader("Create account")
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Find your\nnext favorite.", style = MaterialTheme.typography.displaySmall)
            Text("A few details, and you're all set.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FormField(name, { name = it; vm.clearError() }, "Full name")
            FormField(phone, { phone = it.filter(Char::isDigit).take(10); vm.clearError() }, "Phone number", keyboard = KeyboardType.Phone, error = if (phone.length == 10 && !Validation.phone(phone)) "Use a valid Indian mobile number" else null)
            FormField(email, { email = it; vm.clearError() }, "Email", keyboard = KeyboardType.Email, error = if (email.contains(' ') || (email.contains('.') && !Validation.email(email))) "Enter a valid email" else null)
            FormField(password, { password = it; vm.clearError() }, "Password", password = true)
            Text("8+ characters, with a letter and a number", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FormField(confirm, { confirm = it; vm.clearError() }, "Confirm password", password = true, error = if (confirm.isNotEmpty() && confirm != password) "Passwords don't match" else null)
            ActionFeedback(vm)
            PrimaryButton("Create account", { vm.register(name, phone, email, password, confirm) }, busy = action.busy)
            TextButton({ nav.go("login") }, Modifier.align(Alignment.CenterHorizontally)) { Text("Already have an account? Log in") }
            DemoNote("Use demonstration details. This is local registration; no verification SMS or email is sent.")
        }
    }
}
@Composable fun ForgotScreen(vm: AuthViewModel) {
    val nav = LocalNavigator.current
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Account help")
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Icon(Icons.Outlined.LockReset, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Let's get you\nback to browsing.", style = MaterialTheme.typography.headlineLarge)
            Text("Blunt stores demo accounts only on this device, so email and SMS password recovery aren't connected. You can use the built-in demo account or create another local account.")
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium) { Column(Modifier.padding(20.dp)) { Text("Demo account", style = MaterialTheme.typography.titleMedium); Text("demo@example.com\npassword123") } }
            ActionFeedback(vm)
            PrimaryButton("Log in to demo account", { vm.login("demo@example.com", "password123") })
            SecondaryButton("Create a new account", { nav.go("register") })
        }
    }
}
