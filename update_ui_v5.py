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
old_dash_header = """        // Component Health Title
        Text(
            text = "Kesehatan & Jadwal Servis Komponen",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = BentoTextPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )"""
new_dash_header = """        // Component Health Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            Text(
                text = "Kesehatan & Jadwal Servis Komponen",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = BentoTextPrimary,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onConfigureVisibility) { Text("Atur Card", color = BentoAccentIndigo, fontSize = 12.sp) }
        }"""
replace_exact(old_dash_header, new_dash_header)


# 12. Dashboard Variables
old_dash_vars = """    val tireElapsed = state.currentOdometer - state.tireInstallOdo
    val tireRemaining = maxOf(0.0, state.tireIntervalKm - tireElapsed)
    val tireProgress = (tireElapsed / state.tireIntervalKm).coerceIn(0.0, 1.0).toFloat()
    val tireDue = tireRemaining <= 3000.0

    val serviceElapsed = state.currentOdometer - state.generalServiceLastOdo
    val serviceRemaining = maxOf(0.0, state.generalServiceIntervalKm - serviceElapsed)
    val serviceProgress = (serviceElapsed / state.generalServiceIntervalKm).coerceIn(0.0, 1.0).toFloat()
    val serviceDue = serviceRemaining <= 1000.0

    val alertCount = (if (oilDue) 1 else 0) + (if (tireDue) 1 else 0) + (if (serviceDue) 1 else 0)"""
new_dash_vars = """    val serviceElapsed = state.currentOdometer - state.generalServiceLastOdo
    val serviceRemaining = maxOf(0.0, state.generalServiceIntervalKm - serviceElapsed)
    val serviceProgress = (serviceElapsed / state.generalServiceIntervalKm).coerceIn(0.0, 1.0).toFloat()
    val serviceDue = serviceRemaining <= 1000.0

    val tireAlerts = state.tires.count { tire -> (state.currentOdometer - tire.installOdo) >= (tire.intervalKm - 3000.0) }
    val alertCount = (if (oilDue) 1 else 0) + tireAlerts + (if (serviceDue) 1 else 0)"""
replace_exact(old_dash_vars, new_dash_vars)

# 13. Dashboard Cards Replacement
old_dashboard_cards = """        // Oil Health Card
        ComponentHealthCard(
            title = "Oli Mesin",
            subtitle = state.oilBrand,
            elapsed = oilElapsed,
            interval = state.oilIntervalKm,
            remaining = oilRemaining,
            progress = oilProgress,
            isDue = oilDue,
            lastLoggedMessage = "Terakhir diganti pada ${formatOdo(state.oilLastChangeOdo)}",
            icon = Icons.Default.Settings,
            barColor = if (oilDue) BentoRose else if (oilRemaining <= 1500) BentoAmber else BentoEmerald
        )

        // Tires Health Card
        ComponentHealthCard(
            title = "Pemeliharaan Ban",
            subtitle = "${state.tireBrand} (${state.tireCompound} Compound)",
            elapsed = tireElapsed,
            interval = state.tireIntervalKm,
            remaining = tireRemaining,
            progress = tireProgress,
            isDue = tireDue,
            lastLoggedMessage = "Pasang baru pada ${formatOdo(state.tireInstallOdo)}",
            icon = Icons.Default.DirectionsCar,
            barColor = if (tireDue) BentoRose else if (tireRemaining <= 8000) BentoAmber else BentoEmerald
        )

        // General Service Card
        ComponentHealthCard(
            title = "Servis Umum Berkala",
            subtitle = "Sistem Engine, Rem & Elektrikal",
            elapsed = serviceElapsed,
            interval = state.generalServiceIntervalKm,
            remaining = serviceRemaining,
            progress = serviceProgress,
            isDue = serviceDue,
            lastLoggedMessage = "Servis terakhir pada ${formatOdo(state.generalServiceLastOdo)}",
            icon = Icons.Default.Build,
            barColor = if (serviceDue) BentoRose else if (serviceRemaining <= 2000) BentoAmber else BentoEmerald
        )"""
new_dashboard_cards = """        // Oil Health Card
        if (state.visibility.showOil) {
            ComponentHealthCard(
                title = "Oli Mesin",
                subtitle = state.oilBrand,
                elapsed = oilElapsed,
                interval = state.oilIntervalKm,
                remaining = oilRemaining,
                progress = oilProgress,
                isDue = oilDue,
                lastLoggedMessage = "Terakhir diganti pada ${formatOdo(state.oilLastChangeOdo)}",
                icon = Icons.Default.Settings,
                barColor = if (oilDue) BentoRose else if (oilRemaining <= 1500) BentoAmber else BentoEmerald
            )
        }

        // Tires Health Cards
        state.tires.filter { it.isVisible }.forEach { tire ->
            val tElapsed = state.currentOdometer - tire.installOdo
            val tRemaining = maxOf(0.0, tire.intervalKm - tElapsed)
            val tProgress = (tElapsed / tire.intervalKm).coerceIn(0.0, 1.0).toFloat()
            val tDue = tRemaining <= 3000.0
            
            ComponentHealthCard(
                title = tire.name,
                subtitle = "${tire.brand} (${tire.compound} Compound)",
                elapsed = tElapsed,
                interval = tire.intervalKm,
                remaining = tRemaining,
                progress = tProgress,
                isDue = tDue,
                lastLoggedMessage = "Pasang baru pada ${formatOdo(tire.installOdo)}",
                icon = Icons.Default.DirectionsCar,
                barColor = if (tDue) BentoRose else if (tRemaining <= 8000) BentoAmber else BentoEmerald
            )
        }

        // General Service Card
        if (state.visibility.showGeneralService) {
            ComponentHealthCard(
                title = "Servis Umum Berkala",
                subtitle = "Sistem Engine, Rem & Elektrikal",
                elapsed = serviceElapsed,
                interval = state.generalServiceIntervalKm,
                remaining = serviceRemaining,
                progress = serviceProgress,
                isDue = serviceDue,
                lastLoggedMessage = "Servis terakhir pada ${formatOdo(state.generalServiceLastOdo)}",
                icon = Icons.Default.Build,
                barColor = if (serviceDue) BentoRose else if (serviceRemaining <= 2000) BentoAmber else BentoEmerald
            )
        }"""
replace_exact(old_dashboard_cards, new_dashboard_cards)

# 14. MaintenanceTab Tires configuration
old_maint_tire = """        // 2. Tire settings config card
        item {
            MaintenanceConfigCard(
                title = "Pemeliharaan Ban",
                subtitle = "Ubah merek ban & jenis kompound yang terpasang",
                details = listOf(
                    "Merek Ban" to state.tireBrand,
                    "Jenis Kompound" to "${state.tireCompound} Compound",
                    "Odometer Pemasangan" to formatOdo(state.tireInstallOdo),
                    "Target Interval Umur" to formatOdo(state.tireIntervalKm)
                ),
                onClickEdit = { showTireConfigDialog = true }
            )
        }"""
new_maint_tire = """        // 2. Tire settings config card
        items(state.tires) { tire ->
            MaintenanceConfigCard(
                title = tire.name,
                subtitle = "Ubah merek ban & jenis kompound yang terpasang",
                details = listOf(
                    "Merek Ban" to tire.brand,
                    "Jenis Kompound" to "${tire.compound} Compound",
                    "Odometer Pemasangan" to formatOdo(tire.installOdo),
                    "Target Interval Umur" to formatOdo(tire.intervalKm)
                ),
                onClickEdit = { activeTireIdForConfig = tire.id; showTireConfigDialog = true }
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
            onSave = { brand, compound, installOdo, interval ->
                val updated = state.copy(
                    tireBrand = brand,
                    tireCompound = compound,
                    tireInstallOdo = installOdo,
                    tireIntervalKm = interval,
                    currentOdometer = maxOf(state.currentOdometer, installOdo)
                )
                onSaveConfig(updated)
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
                onSave = { brand, compound, installOdo, interval ->
                    val newTires = state.tires.map { 
                        if (it.id == currentTire.id) it.copy(brand = brand, compound = compound, installOdo = installOdo, intervalKm = interval) 
                        else it 
                    }
                    val updated = state.copy(
                        tires = newTires,
                        currentOdometer = maxOf(state.currentOdometer, installOdo)
                    )
                    onSaveConfig(updated)
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
