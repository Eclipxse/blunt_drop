package com.example.blunt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blunt.model.Address
import com.example.blunt.navigation.LocalNavigator
import com.example.blunt.ui.components.*
import com.example.blunt.viewmodel.ProfileViewModel
import java.util.UUID

@Composable fun AddressesScreen(vm: ProfileViewModel, selecting: Boolean) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val account = snapshot.account
    var deleting by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(if (selecting) "Deliver to" else "Saved addresses")
        if (account?.addresses.isNullOrEmpty()) EmptyState(Icons.Outlined.LocationOn, "Where should it go?", "Save a delivery address so your next checkout feels effortless.", "Add an address", { nav.go("address/new") }, Modifier.weight(1f))
        else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { ActionFeedback(vm) }
            items(account!!.addresses, key = { it.id }) { address ->
                Surface(Modifier.fillMaxWidth(), color = if (account.selectedAddressId == address.id) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.medium) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth().clickable { vm.select(address.id) }, verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(account.selectedAddressId == address.id, { vm.select(address.id) })
                            Column(Modifier.weight(1f)) { Text(address.name, style = MaterialTheme.typography.titleMedium); Text(address.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Icon(if (address.type == "Home") Icons.Outlined.Home else Icons.Outlined.Business, null)
                        }
                        Text(address.formatted, style = MaterialTheme.typography.bodyMedium)
                        Text("+91 ${address.phone}", style = MaterialTheme.typography.bodyMedium)
                        Row { TextButton({ nav.go("address/${address.id}") }) { Text("Edit") }; TextButton({ deleting = address.id }) { Text("Remove") } }
                    }
                }
            }
            item { SecondaryButton("Add another address", { nav.go("address/new") }) }
        }
        if (selecting && !account?.addresses.isNullOrEmpty()) Column(Modifier.padding(24.dp)) { PrimaryButton("Use this address", nav.back, enabled = account?.selectedAddressId != null) }
    }
    if (deleting != null) AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Remove this address?") }, text = { Text("Your existing orders keep their delivery details.") }, confirmButton = { TextButton({ vm.delete(deleting!!); deleting = null }) { Text("Remove") } }, dismissButton = { TextButton({ deleting = null }) { Text("Keep address") } })
}
@Composable fun AddressFormScreen(vm: ProfileViewModel, id: String) {
    val snapshot by vm.state.collectAsStateWithLifecycle()
    val action by vm.action.collectAsStateWithLifecycle()
    val original = snapshot.account?.addresses?.find { it.id == id }
    val newId = rememberSaveable(id) { if (id == "new") UUID.randomUUID().toString() else id }
    var name by rememberSaveable(id) { mutableStateOf(original?.name ?: snapshot.account?.user?.name.orEmpty()) }
    var phone by rememberSaveable(id) { mutableStateOf(original?.phone ?: snapshot.account?.user?.phone.orEmpty()) }
    var house by rememberSaveable(id) { mutableStateOf(original?.house.orEmpty()) }; var street by rememberSaveable(id) { mutableStateOf(original?.street.orEmpty()) }
    var area by rememberSaveable(id) { mutableStateOf(original?.area.orEmpty()) }; var city by rememberSaveable(id) { mutableStateOf(original?.city.orEmpty()) }
    var state by rememberSaveable(id) { mutableStateOf(original?.state.orEmpty()) }; var pin by rememberSaveable(id) { mutableStateOf(original?.pin.orEmpty()) }
    var type by rememberSaveable(id) { mutableStateOf(original?.type ?: "Home") }
    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader(if (id == "new") "Add address" else "Edit address")
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Your next good find\nis coming home.", style = MaterialTheme.typography.headlineMedium)
            FormField(name, { name = it; vm.clearError() }, "Full name")
            FormField(phone, { phone = it.filter(Char::isDigit).take(10); vm.clearError() }, "Phone number", keyboard = KeyboardType.Phone)
            FormField(house, { house = it; vm.clearError() }, "House / flat")
            FormField(street, { street = it; vm.clearError() }, "Street")
            FormField(area, { area = it; vm.clearError() }, "Area")
            FormField(city, { city = it; vm.clearError() }, "City")
            FormField(state, { state = it; vm.clearError() }, "State")
            FormField(pin, { pin = it.filter(Char::isDigit).take(6); vm.clearError() }, "PIN code", keyboard = KeyboardType.Number, error = if (pin.length == 6 && pin.startsWith('0')) "PIN cannot start with zero" else null)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { listOf("Home", "Work").forEach { option -> FilterChip(type == option, { type = option }, { Text(option) }, leadingIcon = { Icon(if (option == "Home") Icons.Outlined.Home else Icons.Outlined.Business, null, Modifier.size(18.dp)) }) } }
            ActionFeedback(vm)
            PrimaryButton("Save address", { vm.save(Address(newId, name.trim(), phone, house.trim(), street.trim(), area.trim(), city.trim(), state.trim(), pin, type)) }, busy = action.busy)
            DemoNote("Use a sample address for this local demo.")
        }
    }
}
