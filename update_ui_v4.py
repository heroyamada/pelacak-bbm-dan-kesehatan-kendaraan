import os
import sys

file_path = r"app\src\main\java\com\example\ui\VehicleAppUI.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

def replace_exact(old_str, new_str, expected_count=1):
    global content
    count = content.count(old_str)
    if count != expected_count:
        print(f"Error: expected {expected_count} occurrences of '{old_str.strip()[:30]}...', found {count}")
        sys.exit(1)
    content = content.replace(old_str, new_str)

# 1. Dropdown menus color fix
replace_exact("modifier = Modifier.background(Color.White)", "modifier = Modifier.background(BentoCardBg)", expected_count=3)

# 2. AdjustOdoDialog Text
old_odo_text = 'Text("Sesuaikan Odometer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 16.dp))'
new_odo_text = 'Text("Sesuaikan Odometer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 8.dp))\n                Text("Masukkan angka odometer terkini dalam kilometer (km) untuk menyelaraskan indikator kendaraan.", color = BentoTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))'
replace_exact(old_odo_text, new_odo_text)

# 3. HeaderSection Dropdown Items
old_header_items = """                            DropdownMenuItem(
                                text = { Text("+ Tambah Kendaraan Baru", color = BentoAccentIndigo) },
                                onClick = { 
                                    onAddProfile()
                                    expanded = false
                                }
                            )"""
new_header_items = """                            DropdownMenuItem(
                                text = { Text("Edit Profil Saat Ini", color = BentoTextSecondary) },
                                onClick = { onEditProfile(); expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus Profil Saat Ini", color = BentoRose) },
                                onClick = { onDeleteProfile(); expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("+ Tambah Kendaraan Baru", color = BentoAccentIndigo) },
                                onClick = { 
                                    onAddProfile()
                                    expanded = false
                                }
                            )"""
replace_exact(old_header_items, new_header_items)

# 4. HeaderSection signature
old_header_sig = """fun HeaderSection(
    activeProfile: VehicleProfile?,
    profiles: List<VehicleProfile>,
    onProfileSelected: (String) -> Unit,
    onAddProfile: () -> Unit,
    onAdjustOdo: () -> Unit
) {"""
new_header_sig = """fun HeaderSection(
    activeProfile: VehicleProfile?,
    profiles: List<VehicleProfile>,
    onProfileSelected: (String) -> Unit,
    onAddProfile: () -> Unit,
    onEditProfile: () -> Unit,
    onDeleteProfile: () -> Unit,
    onAdjustOdo: () -> Unit
) {"""
replace_exact(old_header_sig, new_header_sig)

# 5. Scaffold topBar usage
old_scaffold_usage = """                onAddProfile = { showAddVehicleDialog = true },
                onAdjustOdo = { showAdjustOdoDialog = true }"""
new_scaffold_usage = """                onAddProfile = { showAddVehicleDialog = true },
                onEditProfile = { showEditVehicleDialog = true },
                onDeleteProfile = { showDeleteVehicleDialog = true },
                onAdjustOdo = { showAdjustOdoDialog = true }"""
replace_exact(old_scaffold_usage, new_scaffold_usage)

# 6. Dialog variables
old_dialog_vars = """    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showAdjustOdoDialog by remember { mutableStateOf(false) }"""
new_dialog_vars = """    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showEditVehicleDialog by remember { mutableStateOf(false) }
    var showDeleteVehicleDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var showAdjustOdoDialog by remember { mutableStateOf(false) }"""
replace_exact(old_dialog_vars, new_dialog_vars)

# 7. AddVehicleDialog initial values
old_add_dialog_sig = """fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Roda 2") }"""
new_add_dialog_sig = """fun AddVehicleDialog(
    initialName: String = "",
    initialType: String = "Roda 2",
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }"""
replace_exact(old_add_dialog_sig, new_add_dialog_sig)

# 8. Dialog handlers in VehicleAppUI
old_dialogs = """            // Dialogs
            if (showAddFuelDialog) {"""
new_dialogs = """            // Dialogs
            if (showEditVehicleDialog && activeProfile != null) {
                AddVehicleDialog(
                    initialName = activeProfile.name,
                    initialType = activeProfile.type,
                    onDismiss = { showEditVehicleDialog = false },
                    onSave = { name, type ->
                        val updated = activeProfile.copy(name = name, type = type)
                        profiles = profiles.map { if (it.id == updated.id) updated else it }
                        storage.saveProfiles(profiles)
                        showEditVehicleDialog = false
                    }
                )
            }
            if (showDeleteVehicleDialog && activeProfile != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteVehicleDialog = false },
                    containerColor = BentoCardBg,
                    title = { Text("Hapus Kendaraan", color = BentoTextPrimary) },
                    text = { Text("Yakin hapus profil '${activeProfile.name}'? Semua data servis dan BBM akan hilang.", color = BentoTextSecondary) },
                    confirmButton = {
                        TextButton(onClick = {
                            val newProfiles = profiles.filter { it.id != activeProfile.id }
                            profiles = newProfiles.ifEmpty { listOf(com.example.data.VehicleProfile(name = "Kendaraan Baru", type = "Roda 2")) }
                            storage.saveProfiles(profiles)
                            activeProfileId = profiles.first().id
                            showDeleteVehicleDialog = false
                        }) { Text("Hapus", color = BentoRose) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteVehicleDialog = false }) { Text("Batal", color = BentoTextPrimary) }
                    }
                )
            }
            if (showVisibilityDialog) {
                DashboardVisibilityDialog(
                    state = state,
                    onDismiss = { showVisibilityDialog = false },
                    onSave = { newVisibility, newTires ->
                        saveActiveState(state.copy(visibility = newVisibility, tires = newTires))
                        showVisibilityDialog = false
                    }
                )
            }
            if (showAddFuelDialog) {"""
replace_exact(old_dialogs, new_dialogs)

# 9. DashboardTab signature
old_dash_sig = """fun DashboardTab(
    state: VehicleState,
    onNavigateToTab: (AppTab) -> Unit,
    onAddFuelClick: () -> Unit
) {"""
new_dash_sig = """fun DashboardTab(
    state: VehicleState,
    onNavigateToTab: (AppTab) -> Unit,
    onAddFuelClick: () -> Unit,
    onConfigureVisibility: () -> Unit
) {"""
replace_exact(old_dash_sig, new_dash_sig)

# 10. DashboardTab call
old_dash_call = """                AppTab.DASHBOARD -> DashboardTab(
                    state = state,
                    onNavigateToTab = { selectedTab = it },
                    onAddFuelClick = { showAddFuelDialog = true }
                )"""
new_dash_call = """                AppTab.DASHBOARD -> DashboardTab(
                    state = state,
                    onNavigateToTab = { selectedTab = it },
                    onAddFuelClick = { showAddFuelDialog = true },
                    onConfigureVisibility = { showVisibilityDialog = true }
                )"""
replace_exact(old_dash_call, new_dash_call)

# 11. Dashboard header
old_dash_header = """        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = "Kesehatan", tint = BentoAccentIndigo, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kesehatan Kendaraan", color = BentoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }"""
new_dash_header = """        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = "Kesehatan", tint = BentoAccentIndigo, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kesehatan Kendaraan", color = BentoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = onConfigureVisibility) { Text("Atur Card", color = BentoAccentIndigo, fontSize = 12.sp) }
        }"""
replace_exact(old_dash_header, new_dash_header)

# 12. Dashboard Cards Replacement
old_dashboard_cards = """        HealthCard(
            title = "Oli Mesin",
            subtitle = state.oilBrand,
            icon = Icons.Default.OilBarrel,
            currentOdo = state.currentOdometer,
            lastActionOdo = state.oilLastChangeOdo,
            intervalKm = state.oilIntervalKm
        )
        Spacer(modifier = Modifier.height(12.dp))

        HealthCard(
            title = "Ban Kendaraan",
            subtitle = "${state.tireBrand} (${state.tireCompound} Compound)",
            icon = Icons.Default.TireRepair,
            currentOdo = state.currentOdometer,
            lastActionOdo = state.tireInstallOdo,
            intervalKm = state.tireIntervalKm
        )
        Spacer(modifier = Modifier.height(12.dp))

        HealthCard(
            title = "Servis Umum Berkala",
            subtitle = "Sistem Engine, Rem & Elektrikal",
            icon = Icons.Default.Build,
            currentOdo = state.currentOdometer,
            lastActionOdo = state.generalServiceLastOdo,
            intervalKm = state.generalServiceIntervalKm
        )"""
new_dashboard_cards = """        if (state.visibility.showOil) {
            HealthCard(
                title = "Oli Mesin",
                subtitle = state.oilBrand,
                icon = Icons.Default.OilBarrel,
                currentOdo = state.currentOdometer,
                lastActionOdo = state.oilLastChangeOdo,
                intervalKm = state.oilIntervalKm
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.tires.filter { it.isVisible }.forEach { tire ->
            HealthCard(
                title = tire.name,
                subtitle = "${tire.brand} (${tire.compound} Compound)",
                icon = Icons.Default.TireRepair,
                currentOdo = state.currentOdometer,
                lastActionOdo = tire.installOdo,
                intervalKm = tire.intervalKm
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.visibility.showGeneralService) {
            HealthCard(
                title = "Servis Umum Berkala",
                subtitle = "Sistem Engine, Rem & Elektrikal",
                icon = Icons.Default.Build,
                currentOdo = state.currentOdometer,
                lastActionOdo = state.generalServiceLastOdo,
                intervalKm = state.generalServiceIntervalKm
            )
        }"""
replace_exact(old_dashboard_cards, new_dashboard_cards)

# 13. MaintenanceTab Tires configuration
old_maint_tire = """        MaintenanceConfigItem(
            title = "Ban Kendaraan",
            subtitle = "${state.tireBrand} (${state.tireCompound}) - Ganti setiap ${formatOdo(state.tireIntervalKm)} km",
            icon = Icons.Default.TireRepair,
            onClick = { showTireConfigDialog = true }
        )"""
new_maint_tire = """        state.tires.forEach { tire ->
            MaintenanceConfigItem(
                title = tire.name,
                subtitle = "${tire.brand} (${tire.compound}) - Ganti setiap ${formatOdo(tire.intervalKm)} km",
                icon = Icons.Default.TireRepair,
                onClick = { activeTireIdForConfig = tire.id; showTireConfigDialog = true }
            )
        }"""
replace_exact(old_maint_tire, new_maint_tire)

old_maint_dialog_var = """    var showTireConfigDialog by remember { mutableStateOf(false) }"""
new_maint_dialog_var = """    var showTireConfigDialog by remember { mutableStateOf(false) }
    var activeTireIdForConfig by remember { mutableStateOf("") }"""
replace_exact(old_maint_dialog_var, new_maint_dialog_var)

old_tire_dialog_call = """    if (showTireConfigDialog) {
        TireConfigDialog(
            currentBrand = state.tireBrand,
            currentCompound = state.tireCompound,
            currentInstallOdo = state.tireInstallOdo,
            currentInterval = state.tireIntervalKm,
            onDismiss = { showTireConfigDialog = false },
            onSave = { b, c, o, i ->
                onSaveConfig(state.copy(tireBrand = b, tireCompound = c, tireInstallOdo = o, tireIntervalKm = i))
                showTireConfigDialog = false
            }
        )
    }"""
new_tire_dialog_call = """    if (showTireConfigDialog) {
        val currentTire = state.tires.find { it.id == activeTireIdForConfig }
        if (currentTire != null) {
            TireConfigDialog(
                currentBrand = currentTire.brand,
                currentCompound = currentTire.compound,
                currentInstallOdo = currentTire.installOdo,
                currentInterval = currentTire.intervalKm,
                onDismiss = { showTireConfigDialog = false },
                onSave = { b, c, o, i ->
                    val newTires = state.tires.map { if (it.id == currentTire.id) it.copy(brand = b, compound = c, installOdo = o, intervalKm = i) else it }
                    onSaveConfig(state.copy(tires = newTires))
                    showTireConfigDialog = false
                }
            )
        }
    }"""
replace_exact(old_tire_dialog_call, new_tire_dialog_call)

# Finally, append DashboardVisibilityDialog at the bottom
visibility_dialog = """

@Composable
fun DashboardVisibilityDialog(
    state: com.example.data.VehicleState,
    onDismiss: () -> Unit,
    onSave: (com.example.data.DashboardVisibility, List<com.example.data.TireState>) -> Unit
) {
    var showOil by remember { mutableStateOf(state.visibility.showOil) }
    var showGeneralService by remember { mutableStateOf(state.visibility.showGeneralService) }
    var tires by remember { mutableStateOf(state.tires) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Pilih Card yang Ditampilkan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showOil, onCheckedChange = { showOil = it }, colors = CheckboxDefaults.colors(checkedColor = BentoAccentIndigo, uncheckedColor = BentoTextSecondary))
                            Text("Oli Mesin", color = BentoTextPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showGeneralService, onCheckedChange = { showGeneralService = it }, colors = CheckboxDefaults.colors(checkedColor = BentoAccentIndigo, uncheckedColor = BentoTextSecondary))
                            Text("Servis Umum", color = BentoTextPrimary)
                        }
                    }
                    items(tires) { tire ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = tire.isVisible, onCheckedChange = { isChecked ->
                                tires = tires.map { if (it.id == tire.id) it.copy(isVisible = isChecked) else it }
                            }, colors = CheckboxDefaults.colors(checkedColor = BentoAccentIndigo, uncheckedColor = BentoTextSecondary))
                            Text(tire.name, color = BentoTextPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Batal", color = BentoTextSecondary) }
                    Button(
                        onClick = { onSave(com.example.data.DashboardVisibility(showOil, showGeneralService), tires) },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo)
                    ) { Text("Simpan") }
                }
            }
        }
    }
}
"""
content += visibility_dialog

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)
print("SUCCESS: UI Updated!")
