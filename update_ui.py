import re

def update_ui():
    ui_path = "app/src/main/java/com/example/ui/VehicleAppUI.kt"
    with open(ui_path, "r", encoding="utf-8") as f:
        content = f.read()

    # 1. Replace Color.White containerColor in Dialogs with BentoCardBg
    content = content.replace("containerColor = Color.White", "containerColor = BentoCardBg")

    # 2. Update imports (if any missing)
    if "com.example.data.VehicleProfile" not in content:
        content = content.replace("import com.example.data.VehicleStorage", "import com.example.data.VehicleStorage\nimport com.example.data.VehicleProfile")
    
    # We will just write a new file for the entire HeaderSection logic.
    # Actually, let's use the Python script just for the complex replace in VehicleAppUI
    
    # 3. Replace state logic in VehicleAppUI
    old_state_logic = """    // Load initial state, if empty pre-populate with realistic mock data for preview
    var state by remember {
        val loaded = storage.loadState()
        mutableStateOf(
            if (loaded.fuelLogs.isEmpty() && loaded.currentOdometer == 0.0) {
                val initialState = getSampleData()
                storage.saveState(initialState)
                initialState
            } else {
                loaded
            }
        )
    }"""
    
    new_state_logic = """    var profiles by remember { mutableStateOf(storage.loadProfiles()) }
    var activeProfileId by remember { mutableStateOf(profiles.firstOrNull()?.id ?: "") }
    val activeProfile = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull()
    var state = activeProfile?.state ?: VehicleState()
    
    fun saveActiveState(newState: VehicleState) {
        val updatedProfile = activeProfile?.copy(state = newState) ?: return
        profiles = profiles.map { if (it.id == updatedProfile.id) updatedProfile else it }
        storage.saveProfiles(profiles)
    }"""
    
    content = content.replace(old_state_logic, new_state_logic)
    
    # 4. Replace saveState(state) calls inside VehicleAppUI()
    # First we need to find all `storage.saveState(state)` and `state = state.copy(...)`
    # We'll use regex for these.
    
    content = re.sub(
        r"state = state\.copy\((.*?)\)\n\s*storage\.saveState\(state\)",
        r"val newState = state.copy(\1)\n                        saveActiveState(newState)",
        content, flags=re.DOTALL
    )
    # Also for MaintenanceTab onSaveConfig
    content = content.replace(
        "state = updatedState\n                        storage.saveState(state)",
        "saveActiveState(updatedState)"
    )

    # 5. HeaderSection replacement
    # We'll replace the HeaderSection composable completely
    # We find where HeaderSection starts and ends
    header_pattern = re.compile(r"@Composable\nfun HeaderSection\(state: VehicleState\).*?\n}\n", re.DOTALL)
    
    new_header = """@Composable
fun HeaderSection(
    activeProfile: VehicleProfile?,
    profiles: List<VehicleProfile>,
    onProfileSelected: (String) -> Unit,
    onAddProfile: () -> Unit,
    onAdjustOdo: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Surface(
        color = BentoBg,
        contentColor = BentoTextPrimary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (activeProfile?.type == "Roda 2") Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                    contentDescription = "Vehicle",
                    tint = BentoAccentIndigo,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { expanded = true }
                        ) {
                            Text(
                                text = activeProfile?.name ?: "Pilih Kendaraan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih", tint = BentoTextPrimary)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(BentoCardBg)
                        ) {
                            profiles.forEach { profile ->
                                DropdownMenuItem(
                                    text = { Text(profile.name, color = BentoTextPrimary) },
                                    onClick = { 
                                        onProfileSelected(profile.id)
                                        expanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("+ Tambah Kendaraan Baru", color = BentoAccentIndigo) },
                                onClick = { 
                                    onAddProfile()
                                    expanded = false
                                }
                            )
                        }
                    }
                    Text(
                        text = "Odo: ${formatOdo(activeProfile?.state?.currentOdometer ?: 0.0)} km",
                        fontSize = 13.sp,
                        color = BentoTextSecondary,
                        modifier = Modifier.clickable { onAdjustOdo() }
                    )
                }
                IconButton(onClick = onAdjustOdo) {
                    Icon(Icons.Default.Speed, contentDescription = "Adjust Odometer", tint = BentoTextSecondary)
                }
            }
        }
    }
}
"""
    content = header_pattern.sub(new_header, content)
    
    # 6. Update topBar call in Scaffold
    content = content.replace("HeaderSection(state)", """HeaderSection(
                activeProfile = activeProfile,
                profiles = profiles,
                onProfileSelected = { activeProfileId = it },
                onAddProfile = { showAddVehicleDialog = true },
                onAdjustOdo = { showAdjustOdoDialog = true }
            )""")
            
    # Add Dialog variables
    content = content.replace("var showAddServiceDialog by remember { mutableStateOf(false) }", """var showAddServiceDialog by remember { mutableStateOf(false) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showAdjustOdoDialog by remember { mutableStateOf(false) }""")

    # 7. Add AddVehicleDialog and AdjustOdoDialog and new Icons imports
    if "Icons.Default.TwoWheeler" not in content:
        content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.rounded.*")

    new_dialogs = """
@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Roda 2") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Tambah Kendaraan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kendaraan (Contoh: Beat, Avanza)", color = BentoTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoTextPrimary,
                        unfocusedTextColor = BentoTextPrimary,
                        focusedBorderColor = BentoAccentIndigo,
                        unfocusedBorderColor = BentoCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Jenis Kendaraan", color = BentoTextSecondary, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "Roda 2",
                        onClick = { type = "Roda 2" },
                        label = { Text("Roda 2") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BentoAccentIndigo, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = type == "Roda 4",
                        onClick = { type = "Roda 4" },
                        label = { Text("Roda 4") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BentoAccentIndigo, selectedLabelColor = Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Batal", color = BentoTextSecondary) }
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name, type) },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo)
                    ) { Text("Simpan") }
                }
            }
        }
    }
}

@Composable
fun AdjustOdoDialog(
    currentOdo: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var odoStr by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentOdo)) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Sesuaikan Odometer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(
                    value = odoStr,
                    onValueChange = { odoStr = it },
                    label = { Text("Odometer Terkini (km)", color = BentoTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoTextPrimary,
                        unfocusedTextColor = BentoTextPrimary,
                        focusedBorderColor = BentoAccentIndigo,
                        unfocusedBorderColor = BentoCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Batal", color = BentoTextSecondary) }
                    Button(
                        onClick = { 
                            val o = odoStr.toDoubleOrNull()
                            if (o != null) onSave(o)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo)
                    ) { Text("Simpan") }
                }
            }
        }
    }
}
"""
    content += new_dialogs
    
    # 8. Add logic for handling these dialogs at the end of Scaffold Box
    dialog_handling = """            if (showAddVehicleDialog) {
                AddVehicleDialog(
                    onDismiss = { showAddVehicleDialog = false },
                    onSave = { name, type ->
                        val newProfile = VehicleProfile(name = name, type = type)
                        profiles = profiles + newProfile
                        storage.saveProfiles(profiles)
                        activeProfileId = newProfile.id
                        showAddVehicleDialog = false
                    }
                )
            }
            if (showAdjustOdoDialog) {
                AdjustOdoDialog(
                    currentOdo = state.currentOdometer,
                    onDismiss = { showAdjustOdoDialog = false },
                    onSave = { newOdo ->
                        val newState = state.copy(currentOdometer = newOdo)
                        saveActiveState(newState)
                        showAdjustOdoDialog = false
                    }
                )
            }
"""
    # Insert before the last brace of the Box content (around Dialogs)
    content = content.replace("            // Dialogs", dialog_handling + "            // Dialogs")
    
    # Also I need to change TextFields in existing Modals to use BentoTextPrimary instead of the default dark text, since the modal is now BentoCardBg (dark).
    content = content.replace(
        "OutlinedTextFieldDefaults.colors(\n                        focusedBorderColor = BentoAccentIndigo\n                    )", 
        "OutlinedTextFieldDefaults.colors(\n                        focusedTextColor = BentoTextPrimary,\n                        unfocusedTextColor = BentoTextPrimary,\n                        focusedBorderColor = BentoAccentIndigo,\n                        unfocusedBorderColor = BentoCardBorder\n                    )"
    )

    with open(ui_path, "w", encoding="utf-8") as f:
        f.write(content)
        
update_ui()
