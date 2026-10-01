import re

ui_path = "app/src/main/java/com/example/ui/VehicleAppUI.kt"
with open(ui_path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. AdjustOdoDialog Text
content = content.replace(
    'Text("Sesuaikan Odometer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 16.dp))',
    'Text("Sesuaikan Odometer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 8.dp))\\n                Text("Masukkan angka odometer terkini dalam kilometer (km) untuk menyelaraskan indikator kendaraan.", color = BentoTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))'
)

# 2. AddFuelDialog dropdown color
content = content.replace(
    '''ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {''',
    '''ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(BentoCardBg)
                        ) {'''
)

# 3. HeaderSection signature & body
content = content.replace(
    '''fun HeaderSection(
    activeProfile: VehicleProfile?,
    profiles: List<VehicleProfile>,
    onProfileSelected: (String) -> Unit,
    onAddProfile: () -> Unit,
    onAdjustOdo: () -> Unit
) {''',
    '''fun HeaderSection(
    activeProfile: VehicleProfile?,
    profiles: List<VehicleProfile>,
    onProfileSelected: (String) -> Unit,
    onAddProfile: () -> Unit,
    onEditProfile: () -> Unit,
    onDeleteProfile: () -> Unit,
    onAdjustOdo: () -> Unit
) {'''
)

content = content.replace(
    '''DropdownMenuItem(
                                text = { Text("+ Tambah Kendaraan Baru", color = BentoAccentIndigo) },
                                onClick = { 
                                    onAddProfile()
                                    expanded = false
                                }
                            )''',
    '''DropdownMenuItem(
                                text = { Text("Edit Profil Saat Ini", color = BentoTextSecondary) },
                                onClick = { onEditProfile(); expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus Profil Saat Ini", color = BentoRose) },
                                onClick = { onDeleteProfile(); expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("+ Tambah Kendaraan Baru", color = BentoAccentIndigo) },
                                onClick = { onAddProfile(); expanded = false }
                            )'''
)

# 4. Scaffold usage in VehicleAppUI
content = content.replace(
    '''onAddProfile = { showAddVehicleDialog = true },
                onAdjustOdo = { showAdjustOdoDialog = true }''',
    '''onAddProfile = { showAddVehicleDialog = true },
                onEditProfile = { showEditVehicleDialog = true },
                onDeleteProfile = { showDeleteVehicleDialog = true },
                onAdjustOdo = { showAdjustOdoDialog = true }'''
)

content = content.replace(
    'var showAddVehicleDialog by remember { mutableStateOf(false) }',
    '''var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showEditVehicleDialog by remember { mutableStateOf(false) }
    var showDeleteVehicleDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }'''
)

dialog_handlers = '''
            // Dialogs
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
                    text = { Text("Yakin hapus profil '${activeProfile.name}'?", color = BentoTextSecondary) },
                    confirmButton = {
                        TextButton(onClick = {
                            val newProfiles = profiles.filter { it.id != activeProfile.id }
                            profiles = newProfiles.ifEmpty { listOf(com.example.data.VehicleProfile(name = "Baru", type = "Roda 2")) }
                            storage.saveProfiles(profiles)
                            activeProfileId = profiles.first().id
                            showDeleteVehicleDialog = false
                        }) { Text("Hapus", color = BentoRose) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteVehicleDialog = false }) { Text("Batal") }
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
'''
content = content.replace('            // Dialogs', dialog_handlers)

# 5. AddVehicleDialog initial values
content = content.replace(
    'fun AddVehicleDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {',
    'fun AddVehicleDialog(initialName: String = "", initialType: String = "Roda 2", onDismiss: () -> Unit, onSave: (String, String) -> Unit) {'
)
content = content.replace('var name by remember { mutableStateOf("") }', 'var name by remember { mutableStateOf(initialName) }')
content = content.replace('var type by remember { mutableStateOf("Roda 2") }', 'var type by remember { mutableStateOf(initialType) }')

# 6. DashboardTab
content = content.replace(
    '''fun DashboardTab(
    state: VehicleState,
    onNavigateToTab: (Int) -> Unit,
    onAddFuelClick: () -> Unit
) {''',
    '''fun DashboardTab(
    state: com.example.data.VehicleState,
    onNavigateToTab: (Int) -> Unit,
    onAddFuelClick: () -> Unit,
    onConfigureVisibility: () -> Unit
) {'''
)

content = content.replace(
    '''DashboardTab(
                    state = state,
                    onNavigateToTab = { selectedTab = it },
                    onAddFuelClick = { showAddFuelDialog = true }
                )''',
    '''DashboardTab(
                    state = state,
                    onNavigateToTab = { selectedTab = it },
                    onAddFuelClick = { showAddFuelDialog = true },
                    onConfigureVisibility = { showVisibilityDialog = true }
                )'''
)

content = content.replace(
    '''Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = "Kesehatan", tint = BentoAccentIndigo, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kesehatan Kendaraan", color = BentoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }''',
    '''Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = "Kesehatan", tint = BentoAccentIndigo, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kesehatan Kendaraan", color = BentoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = onConfigureVisibility) { Text("Atur Card", color = BentoAccentIndigo) }
        }'''
)

# 7. Tires and visibility
oil_card = '''        HealthCard(
            title = "Oli Mesin",
            subtitle = state.oilBrand,
            icon = Icons.Default.OilBarrel,
            currentOdo = state.currentOdometer,
            lastActionOdo = state.oilLastChangeOdo,
            intervalKm = state.oilIntervalKm
        )
        Spacer(modifier = Modifier.height(12.dp))'''
new_oil_card = '''        if (state.visibility.showOil) {
            HealthCard(
                title = "Oli Mesin",
                subtitle = state.oilBrand,
                icon = Icons.Default.OilBarrel,
                currentOdo = state.currentOdometer,
                lastActionOdo = state.oilLastChangeOdo,
                intervalKm = state.oilIntervalKm
            )
            Spacer(modifier = Modifier.height(12.dp))
        }'''
content = content.replace(oil_card, new_oil_card)

service_card = '''        HealthCard(
            title = "Servis Umum Berkala",
            subtitle = "Sistem Engine, Rem & Elektrikal",
            icon = Icons.Default.Build,
            currentOdo = state.currentOdometer,
            lastActionOdo = state.generalServiceLastOdo,
            intervalKm = state.generalServiceIntervalKm
        )'''
new_service_card = '''        if (state.visibility.showGeneralService) {
            HealthCard(
                title = "Servis Umum Berkala",
                subtitle = "Sistem Engine, Rem & Elektrikal",
                icon = Icons.Default.Build,
                currentOdo = state.currentOdometer,
                lastActionOdo = state.generalServiceLastOdo,
                intervalKm = state.generalServiceIntervalKm
            )
        }'''
content = content.replace(service_card, new_service_card)

old_tire_card = '''        HealthCard(
            title = "Ban Kendaraan",
            subtitle = "${state.tireBrand} (${state.tireCompound} Compound)",
            icon = Icons.Default.TireRepair,
            currentOdo = state.currentOdometer,
            lastActionOdo = state.tireInstallOdo,
            intervalKm = state.tireIntervalKm
        )
        Spacer(modifier = Modifier.height(12.dp))'''
new_tire_cards = '''        state.tires.filter { it.isVisible }.forEach { tire ->
            HealthCard(
                title = tire.name,
                subtitle = "${tire.brand} (${tire.compound} Compound)",
                icon = Icons.Default.DirectionsCar,
                currentOdo = state.currentOdometer,
                lastActionOdo = tire.installOdo,
                intervalKm = tire.intervalKm
            )
            Spacer(modifier = Modifier.height(12.dp))
        }'''
content = content.replace(old_tire_card, new_tire_cards)

# 8. MaintenanceTab
old_maint_tire = '''        MaintenanceConfigItem(
            title = "Ban Kendaraan",
            subtitle = "${state.tireBrand} (${state.tireCompound}) - Ganti setiap ${formatOdo(state.tireIntervalKm)} km",
            icon = Icons.Default.TireRepair,
            onClick = { showTireConfigDialog = true }
        )'''
new_maint_tire = '''        state.tires.forEach { tire ->
            MaintenanceConfigItem(
                title = tire.name,
                subtitle = "${tire.brand} (${tire.compound}) - Ganti setiap ${formatOdo(tire.intervalKm)} km",
                icon = Icons.Default.DirectionsCar,
                onClick = { activeTireIdForConfig = tire.id; showTireConfigDialog = true }
            )
        }'''
content = content.replace(old_maint_tire, new_maint_tire)

content = content.replace('var showTireConfigDialog by remember { mutableStateOf(false) }', 'var showTireConfigDialog by remember { mutableStateOf(false) }\\n    var activeTireIdForConfig by remember { mutableStateOf("") }')

old_tire_dialog_call = '''    if (showTireConfigDialog) {
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
    }'''
new_tire_dialog_call = '''    if (showTireConfigDialog) {
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
    }'''
content = content.replace(old_tire_dialog_call, new_tire_dialog_call)


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

with open(ui_path, "w", encoding="utf-8") as f:
    f.write(content)
print("done v3")
