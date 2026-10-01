import re

ui_path = "app/src/main/java/com/example/ui/VehicleAppUI.kt"
with open(ui_path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Update AdjustOdoDialog to add the text
odo_pattern = re.compile(r"""Text\("Sesuaikan Odometer",.*?\n(.*?)OutlinedTextField\(""", re.DOTALL)
def odo_replacement(match):
    return """Text("Sesuaikan Odometer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                Text("Masukkan angka odometer terkini dalam kilometer (km) untuk menyelaraskan indikator kendaraan.", color = BentoTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField("""
content = odo_pattern.sub(odo_replacement, content)

# 2. Fix AddFuelDialog dropdown color
# Look for ExposedDropdownMenuBox and DropdownMenu
dropdown_pattern = re.compile(r"""ExposedDropdownMenu\(\n.*?expanded = expanded,\n.*?onDismissRequest = \{ expanded = false \}\n\s*\) \{""", re.DOTALL)
def dropdown_replacement(match):
    return """ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(BentoCardBg)
                        ) {"""
content = dropdown_pattern.sub(dropdown_replacement, content)

# 3. Add management buttons in HeaderSection
header_pattern = re.compile(r"""DropdownMenuItem\(\n.*?text = \{ Text\("\+ Tambah Kendaraan Baru", color = BentoAccentIndigo\) \},""", re.DOTALL)
def header_replacement(match):
    return """DropdownMenuItem(
                                text = { Text("Edit Profil Saat Ini", color = BentoTextSecondary) },
                                onClick = { 
                                    onEditProfile()
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus Profil Saat Ini", color = BentoRose) },
                                onClick = { 
                                    onDeleteProfile()
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("+ Tambah Kendaraan Baru", color = BentoAccentIndigo) },"""
content = header_pattern.sub(header_replacement, content)

# Update HeaderSection signature
content = content.replace("onAddProfile: () -> Unit,", "onAddProfile: () -> Unit,\n    onEditProfile: () -> Unit,\n    onDeleteProfile: () -> Unit,")

# Update Scaffold topBar call
content = content.replace("onAddProfile = { showAddVehicleDialog = true },", """onAddProfile = { showAddVehicleDialog = true },
                onEditProfile = { showEditVehicleDialog = true },
                onDeleteProfile = { showDeleteVehicleDialog = true },""")

# Add Dialog variables
content = content.replace("var showAddVehicleDialog by remember { mutableStateOf(false) }", """var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showEditVehicleDialog by remember { mutableStateOf(false) }
    var showDeleteVehicleDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }""")

# Add Dialog Handlers
dialog_handlers = """
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
                    text = { Text("Apakah Anda yakin ingin menghapus profil '${activeProfile.name}'? Semua data servis dan BBM akan hilang.", color = BentoTextSecondary) },
                    confirmButton = {
                        TextButton(onClick = {
                            val newProfiles = profiles.filter { it.id != activeProfile.id }
                            if (newProfiles.isEmpty()) {
                                profiles = listOf(com.example.data.VehicleProfile(name = "Kendaraan Baru", type = "Roda 2"))
                            } else {
                                profiles = newProfiles
                            }
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
"""
content = content.replace("            // Dialogs", dialog_handlers + "            // Dialogs")

# Update AddVehicleDialog signature
content = content.replace("fun AddVehicleDialog(", "fun AddVehicleDialog(\n    initialName: String = \"\",\n    initialType: String = \"Roda 2\",")
content = content.replace('var name by remember { mutableStateOf("") }', 'var name by remember { mutableStateOf(initialName) }')
content = content.replace('var type by remember { mutableStateOf("Roda 2") }', 'var type by remember { mutableStateOf(initialType) }')

# 4. Modify DashboardTab and tires
# Add Configure Visibility button
dashboard_header = """Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = "Kesehatan", tint = BentoAccentIndigo, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kesehatan Kendaraan", color = BentoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = onConfigureVisibility) {
                Text("Atur Card", color = BentoAccentIndigo, fontSize = 12.sp)
            }
        }"""
content = content.replace("""Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = "Kesehatan", tint = BentoAccentIndigo, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kesehatan Kendaraan", color = BentoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }""", dashboard_header)

content = content.replace("fun DashboardTab(", "fun DashboardTab(\n    onConfigureVisibility: () -> Unit,")
content = content.replace("DashboardTab(\n                    state = state,\n                    onNavigateToTab = { selectedTab = it },\n                    onAddFuelClick = { showAddFuelDialog = true }\n                )", "DashboardTab(\n                    state = state,\n                    onNavigateToTab = { selectedTab = it },\n                    onAddFuelClick = { showAddFuelDialog = true },\n                    onConfigureVisibility = { showVisibilityDialog = true }\n                )")

# Replace Oil card visibility
content = re.sub(
    r"""(HealthCard\(\n\s*title = "Oli Mesin",.*?\n\s*\))""",
    r"""if (state.visibility.showOil) { \1 }""",
    content, flags=re.DOTALL
)
# Replace Service card visibility
content = re.sub(
    r"""(HealthCard\(\n\s*title = "Servis Umum Berkala",.*?\n\s*\))""",
    r"""if (state.visibility.showGeneralService) { \1 }""",
    content, flags=re.DOTALL
)

# Replace Tire card with dynamic tires
tire_card_pattern = re.compile(r"""HealthCard\(\n\s*title = "Ban Kendaraan",\n\s*subtitle = "\$\{state\.tireBrand\} \(\$\{state\.tireCompound\} Compound\)",\n\s*icon = Icons\.Default\.TireRepair,\n\s*currentOdo = state\.currentOdometer,\n\s*lastActionOdo = state\.tireInstallOdo,\n\s*intervalKm = state\.tireIntervalKm\n\s*\)\n\s*Spacer\(modifier = Modifier\.height\((.*?)\)\)""", re.DOTALL)
def tire_replacement(match):
    return """state.tires.filter { it.isVisible }.forEach { tire ->
            HealthCard(
                title = tire.name,
                subtitle = "${tire.brand} (${tire.compound} Compound)",
                icon = Icons.Default.DirectionsCar,
                currentOdo = state.currentOdometer,
                lastActionOdo = tire.installOdo,
                intervalKm = tire.intervalKm
            )
            Spacer(modifier = Modifier.height(12.dp))
        }"""
content = tire_card_pattern.sub(tire_replacement, content)

# 5. Add DashboardVisibilityDialog and update TireConfigDialog
visibility_dialog = """
@Composable
fun DashboardVisibilityDialog(
    state: VehicleState,
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

# We must also replace the single Tire configuration in MaintenanceTab with a list of Tire configs.
# In MaintenanceTab:
maintenance_tire_pattern = re.compile(r"""MaintenanceConfigItem\(\n\s*title = "Ban Kendaraan",\n\s*subtitle = "\$\{state\.tireBrand\}.*?\)",\n\s*icon = Icons\.Default\.TireRepair,\n\s*onClick = \{ showTireConfigDialog = true \}\n\s*\)""", re.DOTALL)
def maintenance_tire_replacement(match):
    return """state.tires.forEach { tire ->
                MaintenanceConfigItem(
                    title = tire.name,
                    subtitle = "${tire.brand} (${tire.compound}) - Ganti setiap ${formatOdo(tire.intervalKm)} km",
                    icon = Icons.Default.DirectionsCar,
                    onClick = { activeTireIdForConfig = tire.id; showTireConfigDialog = true }
                )
            }"""
content = maintenance_tire_pattern.sub(maintenance_tire_replacement, content)

# Add activeTireIdForConfig variable to MaintenanceTab
content = content.replace("var showTireConfigDialog by remember { mutableStateOf(false) }", "var showTireConfigDialog by remember { mutableStateOf(false) }\n    var activeTireIdForConfig by remember { mutableStateOf(\"\") }")

# Update TireConfigDialog call
tire_dialog_call_pattern = re.compile(r"""if \(showTireConfigDialog\) \{\n\s*TireConfigDialog\(.*?onSave = \{ b, c, o, i ->.*?\}\n\s*\)""", re.DOTALL)
def tire_dialog_call_replacement(match):
    return """if (showTireConfigDialog) {
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
content = tire_dialog_call_pattern.sub(tire_dialog_call_replacement, content)

with open(ui_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Update UI V2 complete")
