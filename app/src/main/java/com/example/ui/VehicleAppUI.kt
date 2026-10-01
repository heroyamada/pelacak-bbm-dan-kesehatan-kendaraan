package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FuelLog
import com.example.data.ServiceLog
import com.example.data.VehicleState
import com.example.data.VehicleStorage
import com.example.data.VehicleProfile
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

var isDarkMode by mutableStateOf(true)

val BentoBg: Color get() = if (isDarkMode) Color(0xFF09090B) else Color(0xFFF3F4F6)
val BentoCardBg: Color get() = if (isDarkMode) Color(0xFF141417) else Color(0xFFFFFFFF)
val BentoCardBorder: Color get() = if (isDarkMode) Color(0xFF27272A) else Color(0xFFE5E7EB)
val BentoAccentIndigo: Color get() = if (isDarkMode) Color(0xFF6366F1) else Color(0xFF4F46E5)
val BentoAccentIndigoLight: Color get() = if (isDarkMode) Color(0x186366F1) else Color(0x184F46E5)
val BentoTextPrimary: Color get() = if (isDarkMode) Color(0xFFF4F4F5) else Color(0xFF111827)
val BentoTextSecondary: Color get() = if (isDarkMode) Color(0xFFA1A1AA) else Color(0xFF4B5563)
val BentoTextMuted: Color get() = if (isDarkMode) Color(0xFF71717A) else Color(0xFF6B7280)

val BentoEmerald: Color get() = if (isDarkMode) Color(0xFF10B981) else Color(0xFF059669)
val BentoEmeraldBg: Color get() = if (isDarkMode) Color(0x1510B981) else Color(0x15059669)
val BentoAmber: Color get() = if (isDarkMode) Color(0xFFF59E0B) else Color(0xFFD97706)
val BentoAmberBg: Color get() = if (isDarkMode) Color(0x15F59E0B) else Color(0x15D97706)
val BentoRose: Color get() = if (isDarkMode) Color(0xFFEF4444) else Color(0xFFDC2626)
val BentoRoseBg: Color get() = if (isDarkMode) Color(0x15EF4444) else Color(0x15DC2626)

enum class AppTab {
    DASHBOARD,
    STATISTICS,
    FUEL_HISTORY,
    MAINTENANCE
}

@Composable
fun VehicleAppUI() {
    val context = LocalContext.current
    val storage = remember { VehicleStorage(context) }
    
    var profiles by remember { mutableStateOf(storage.loadProfiles()) }
    var activeProfileId by remember { mutableStateOf(profiles.firstOrNull()?.id ?: "") }
    val activeProfile = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull()
    var state = activeProfile?.state ?: VehicleState()
    
    fun saveActiveState(newState: VehicleState) {
        val updatedProfile = activeProfile?.copy(state = newState) ?: return
        profiles = profiles.map { if (it.id == updatedProfile.id) updatedProfile else it }
        storage.saveProfiles(profiles)
    }

    var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    
    // Modals visibility
    var showAddFuelDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showEditVehicleDialog by remember { mutableStateOf(false) }
    var showDeleteVehicleDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var showAdjustOdoDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BentoBg,
        topBar = {
            HeaderSection(
                activeProfile = activeProfile,
                profiles = profiles,
                onProfileSelected = { activeProfileId = it },
                onAddProfile = { showAddVehicleDialog = true },
                onEditProfile = { showEditVehicleDialog = true },
                onDeleteProfile = { showDeleteVehicleDialog = true },
                onAdjustOdo = { showAdjustOdoDialog = true },
                onToggleTheme = { isDarkMode = !isDarkMode }
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedTab) { selectedTab = it }
        },
        floatingActionButton = {
            if (selectedTab == AppTab.FUEL_HISTORY || selectedTab == AppTab.DASHBOARD) {
                FloatingActionButton(
                    onClick = { showAddFuelDialog = true },
                    containerColor = BentoAccentIndigo,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Catat BBM"
                    )
                }
            } else if (selectedTab == AppTab.MAINTENANCE) {
                FloatingActionButton(
                    onClick = { showAddServiceDialog = true },
                    containerColor = BentoAccentIndigo,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Catat Servis"
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BentoBg)
        ) {
            when (selectedTab) {
                AppTab.DASHBOARD -> DashboardTab(
                    state = state,
                    onNavigateToTab = { selectedTab = it },
                    onAddFuelClick = { showAddFuelDialog = true },
                    onConfigureVisibility = { showVisibilityDialog = true }
                )
                AppTab.STATISTICS -> StatisticsTab(state = state)
                AppTab.FUEL_HISTORY -> FuelHistoryTab(
                    state = state,
                    onDeleteLog = { logId ->
                        val updatedLogs = state.fuelLogs.filter { it.id != logId }
                        val newMaxOdo = updatedLogs.maxOfOrNull { it.odometer } ?: 0.0
                        val oldMaxServiceOdo = state.serviceLogs.maxOfOrNull { it.odometer } ?: 0.0
                        val finalOdo = maxOf(newMaxOdo, oldMaxServiceOdo)
                        
                        val newState = state.copy(
                            fuelLogs = updatedLogs,
                            currentOdometer = maxOf(finalOdo, state.oilLastChangeOdo, state.tires.maxOfOrNull { it.installOdo } ?: 0.0, state.generalServiceLastOdo)
                        )
                        saveActiveState(newState)
                    }
                )
                AppTab.MAINTENANCE -> MaintenanceTab(
                    state = state,
                    onSaveConfig = { updatedState ->
                        saveActiveState(updatedState)
                    },
                    onDeleteServiceLog = { serviceId ->
                        val updatedLogs = state.serviceLogs.filter { it.id != serviceId }
                        val newState = state.copy(serviceLogs = updatedLogs)
                        saveActiveState(newState)
                    }
                )
            }

            if (showAddVehicleDialog) {
                AddVehicleDialog(
                    onDismiss = { showAddVehicleDialog = false },
                    onSave = { name, type ->
                        val initialTires = if (type == "Roda 4") {
                            listOf(
                                com.example.data.TireState("FL", "Ban Depan Kiri"),
                                com.example.data.TireState("FR", "Ban Depan Kanan"),
                                com.example.data.TireState("RL", "Ban Belakang Kiri"),
                                com.example.data.TireState("RR", "Ban Belakang Kanan")
                            )
                        } else {
                            listOf(
                                com.example.data.TireState("F", "Ban Depan"),
                                com.example.data.TireState("R", "Ban Belakang")
                            )
                        }
                        val newProfile = VehicleProfile(name = name, type = type, state = com.example.data.VehicleState(tires = initialTires))
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
                    text = { Text("Yakin hapus profil '${activeProfile.name}'? Semua data servis dan BBM akan hilang.", color = BentoTextSecondary) },
                    confirmButton = {
                        TextButton(onClick = {
                            val newProfiles = profiles.filter { it.id != activeProfile.id }
                            profiles = newProfiles.ifEmpty { 
                                val defaultTires = listOf(com.example.data.TireState("F", "Ban Depan"), com.example.data.TireState("R", "Ban Belakang"))
                                listOf(com.example.data.VehicleProfile(name = "Kendaraan Baru", type = "Roda 2", state = com.example.data.VehicleState(tires = defaultTires)))
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
            if (showAddFuelDialog) {
                AddFuelDialog(
                    currentOdo = state.currentOdometer,
                    onDismiss = { showAddFuelDialog = false },
                    onSave = { date, odo, liters, price, fuelType ->
                        val newLog = FuelLog(
                            date = date,
                            odometer = odo,
                            liters = liters,
                            pricePerLiter = price,
                            fuelType = fuelType
                        )
                        val updatedLogs = (state.fuelLogs + newLog).sortedBy { it.odometer }
                        val finalOdo = maxOf(state.currentOdometer, odo)
                        
                        val newState = state.copy(
                            fuelLogs = updatedLogs,
                            currentOdometer = finalOdo
                        )
                        saveActiveState(newState)
                        showAddFuelDialog = false
                    }
                )
            }

            if (showAddServiceDialog) {
                AddServiceDialog(
                    currentOdo = state.currentOdometer,
                    onDismiss = { showAddServiceDialog = false },
                    onSave = { date, type, odo, cost, notes ->
                        val newLog = ServiceLog(
                            date = date,
                            type = type,
                            odometer = odo,
                            cost = cost,
                            notes = notes
                        )
                        
                        // Automatically update last service odometers if the user adds corresponding service log type
                        val updatedState = when (type) {
                            "Ganti Oli" -> state.copy(
                                oilLastChangeOdo = odo,
                                currentOdometer = maxOf(state.currentOdometer, odo)
                            )
                            "Ganti Ban" -> state.copy(
                                currentOdometer = maxOf(state.currentOdometer, odo)
                            )
                            "Servis Umum" -> state.copy(
                                generalServiceLastOdo = odo,
                                currentOdometer = maxOf(state.currentOdometer, odo)
                            )
                            else -> state.copy(
                                currentOdometer = maxOf(state.currentOdometer, odo)
                            )
                        }

                        val updatedLogs = (updatedState.serviceLogs + newLog).sortedByDescending { it.date }
                        saveActiveState(updatedState.copy(serviceLogs = updatedLogs))
                        showAddServiceDialog = false
                    }
                )
            }
        }
    }
}

// Formatters
fun formatIdr(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace("Rp", "Rp ").substringBefore(",")
}

fun formatDecimal(num: Double): String {
    val df = DecimalFormat("#,##0.0")
    return df.format(num)
}

fun formatOdo(odo: Double): String {
    val df = DecimalFormat("#,###")
    return df.format(odo) + " km"
}

// Prepopulate initial realistic sample data
fun getSampleData(): VehicleState {
    return VehicleState(
        fuelLogs = listOf(
            FuelLog(id = "1", date = "2026-05-10", odometer = 10000.0, liters = 35.0, pricePerLiter = 10000.0, fuelType = "Pertalite"),
            FuelLog(id = "2", date = "2026-05-25", odometer = 10450.0, liters = 32.0, pricePerLiter = 12500.0, fuelType = "Pertamax"),
            FuelLog(id = "3", date = "2026-06-12", odometer = 10920.0, liters = 34.0, pricePerLiter = 12500.0, fuelType = "Pertamax"),
            FuelLog(id = "4", date = "2026-06-28", odometer = 11390.0, liters = 33.0, pricePerLiter = 12500.0, fuelType = "Pertamax"),
            FuelLog(id = "5", date = "2026-07-15", odometer = 11870.0, liters = 35.0, pricePerLiter = 14500.0, fuelType = "Pertamax Turbo")
        ),
        serviceLogs = listOf(
            ServiceLog(id = "s1", date = "2026-05-10", type = "Ganti Oli", odometer = 10000.0, cost = 350000.0, notes = "Ganti oli mesin Shell Helix Ultra 10W-40"),
            ServiceLog(id = "s2", date = "2026-05-10", type = "Ganti Ban", odometer = 10000.0, cost = 1200000.0, notes = "Ganti ban depan Bridgestone Ecopia Medium Compound")
        ),
        oilBrand = "Shell Helix Ultra 10W-40",
        oilLastChangeOdo = 10000.0,
        oilIntervalKm = 5000.0,
        tires = listOf(
            com.example.data.TireState(id = "depan", name = "Ban Depan", brand = "Bridgestone Ecopia", compound = "Medium", installOdo = 10000.0, intervalKm = 40000.0),
            com.example.data.TireState(id = "belakang", name = "Ban Belakang", brand = "Bridgestone Ecopia", compound = "Medium", installOdo = 10000.0, intervalKm = 40000.0)
        ),
        generalServiceLastOdo = 10000.0,
        generalServiceIntervalKm = 10000.0,
        currentOdometer = 11870.0
    )
}

// Calculating fuel efficiencies safely
fun calculateEfficiencies(fuelLogs: List<FuelLog>): List<Double> {
    val efficiencies = mutableListOf<Double>()
    for (i in fuelLogs.indices) {
        if (i == 0) {
            efficiencies.add(0.0) // No previous log to compare
        } else {
            val dist = fuelLogs[i].odometer - fuelLogs[i-1].odometer
            val eff = if (fuelLogs[i].liters > 0) dist / fuelLogs[i].liters else 0.0
            efficiencies.add(eff)
        }
    }
    return efficiencies
}

@Composable
fun HeaderSection(
    activeProfile: VehicleProfile?,
    profiles: List<VehicleProfile>,
    onProfileSelected: (String) -> Unit,
    onAddProfile: () -> Unit,
    onEditProfile: () -> Unit,
    onDeleteProfile: () -> Unit,
    onAdjustOdo: () -> Unit,
    onToggleTheme: () -> Unit
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
                Row {
                    IconButton(onClick = onToggleTheme) {
                        Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = "Toggle Theme", tint = BentoTextSecondary)
                    }
                    IconButton(onClick = onAdjustOdo) {
                        Icon(Icons.Default.Speed, contentDescription = "Adjust Odometer", tint = BentoTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    NavigationBar(
        containerColor = BentoCardBg,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == AppTab.DASHBOARD,
            onClick = { onTabSelected(AppTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Ringkasan") },
            label = { Text("Ringkasan", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BentoAccentIndigo,
                selectedTextColor = BentoAccentIndigo,
                unselectedIconColor = BentoTextMuted,
                unselectedTextColor = BentoTextMuted,
                indicatorColor = BentoAccentIndigoLight
            )
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.STATISTICS,
            onClick = { onTabSelected(AppTab.STATISTICS) },
            icon = { Icon(Icons.Default.ShowChart, contentDescription = "Statistik") },
            label = { Text("Statistik", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BentoAccentIndigo,
                selectedTextColor = BentoAccentIndigo,
                unselectedIconColor = BentoTextMuted,
                unselectedTextColor = BentoTextMuted,
                indicatorColor = BentoAccentIndigoLight
            )
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.FUEL_HISTORY,
            onClick = { onTabSelected(AppTab.FUEL_HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
            label = { Text("Riwayat BBM", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BentoAccentIndigo,
                selectedTextColor = BentoAccentIndigo,
                unselectedIconColor = BentoTextMuted,
                unselectedTextColor = BentoTextMuted,
                indicatorColor = BentoAccentIndigoLight
            )
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.MAINTENANCE,
            onClick = { onTabSelected(AppTab.MAINTENANCE) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Servis") },
            label = { Text("Servis", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BentoAccentIndigo,
                selectedTextColor = BentoAccentIndigo,
                unselectedIconColor = BentoTextMuted,
                unselectedTextColor = BentoTextMuted,
                indicatorColor = BentoAccentIndigoLight
            )
        )
    }
}

// -------------------- TAB 1: DASHBOARD (RINGKASAN) --------------------
@Composable
fun DashboardTab(
    state: VehicleState,
    onNavigateToTab: (AppTab) -> Unit,
    onAddFuelClick: () -> Unit,
    onConfigureVisibility: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Calculate core stats
    val fuelLogs = state.fuelLogs
    val totalExpense = fuelLogs.sumOf { it.totalCost }
    
    val efficiencies = calculateEfficiencies(fuelLogs)
    val validEfficiencies = efficiencies.filter { it > 0.0 }
    val avgEfficiency = if (validEfficiencies.isNotEmpty()) validEfficiencies.average() else 0.0

    // Component Alerts & Statuses
    val oilElapsed = state.currentOdometer - state.oilLastChangeOdo
    val oilRemaining = maxOf(0.0, state.oilIntervalKm - oilElapsed)
    val oilProgress = (oilElapsed / state.oilIntervalKm).coerceIn(0.0, 1.0).toFloat()
    val oilDue = oilRemaining <= 500.0

    val serviceElapsed = state.currentOdometer - state.generalServiceLastOdo
    val serviceRemaining = maxOf(0.0, state.generalServiceIntervalKm - serviceElapsed)
    val serviceProgress = (serviceElapsed / state.generalServiceIntervalKm).coerceIn(0.0, 1.0).toFloat()
    val serviceDue = serviceRemaining <= 1000.0

    val tireAlerts = state.tires.count { tire -> (state.currentOdometer - tire.installOdo) >= (tire.intervalKm - 3000.0) }
    val alertCount = (if (oilDue) 1 else 0) + tireAlerts + (if (serviceDue) 1 else 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Alert Cards if service is due
        if (alertCount > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BentoRoseBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoRose.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = BentoRose,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Butuh Perhatian Pemeliharaan!",
                            fontWeight = FontWeight.Bold,
                            color = BentoRose,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Ada $alertCount komponen kendaraan yang sudah mendekati atau melewati batas waktu servis berkala.",
                            fontSize = 13.sp,
                            color = BentoTextSecondary
                        )
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = BentoEmeraldBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoEmerald.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Sehat",
                        tint = BentoEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Kondisi kendaraan Anda dalam keadaan optimal & aman!",
                        fontWeight = FontWeight.Medium,
                        color = BentoEmerald,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Stats Row Card
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ikhtisar Kendaraan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BentoTextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Odometer", fontSize = 12.sp, color = BentoTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatOdo(state.currentOdometer), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BentoTextPrimary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(BentoCardBorder).align(Alignment.CenterVertically))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Rata-rata BBM", fontSize = 12.sp, color = BentoTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (avgEfficiency > 0.0) "${formatDecimal(avgEfficiency)} km/L" else "-",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoTextPrimary
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(BentoCardBorder).align(Alignment.CenterVertically))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Biaya", fontSize = 12.sp, color = BentoTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            formatIdr(totalExpense),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BentoTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Component Health Title
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
        }

        // Oil Health Card
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
        }

        // Quick Actions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddFuelClick,
                colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Isi Bensin", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { onNavigateToTab(AppTab.MAINTENANCE) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BentoCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoAccentIndigo),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atur Servis", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ComponentHealthCard(
    title: String,
    subtitle: String,
    elapsed: Double,
    interval: Double,
    remaining: Double,
    progress: Float,
    isDue: Boolean,
    lastLoggedMessage: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    barColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = if (isDue) BentoRoseBg else BentoAccentIndigoLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDue) BentoRose else BentoAccentIndigo
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = BentoTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isDue) {
                    Surface(
                        color = BentoRoseBg,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "PERLU SERVIS",
                            color = BentoRose,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            
            // Linear Progress Indicator representing usage
            LinearProgressIndicator(
                progress = { progress },
                color = barColor,
                trackColor = BentoCardBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.Transparent, RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${formatDecimal(elapsed)} / ${formatDecimal(interval)} km",
                    fontSize = 12.sp,
                    color = BentoTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isDue) "Lewat ${formatDecimal(elapsed - interval)} km" else "Sisa ${formatDecimal(remaining)} km",
                    fontSize = 12.sp,
                    color = if (isDue) BentoRose else BentoEmerald,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = BentoCardBorder)

            Text(
                text = lastLoggedMessage,
                fontSize = 11.sp,
                color = BentoTextMuted,
                textAlign = TextAlign.Start
            )
        }
    }
}


// -------------------- TAB 2: STATISTICS & GRAPHS --------------------
@Composable
fun StatisticsTab(state: VehicleState) {
    val scrollState = rememberScrollState()
    
    // Group fuel logs by month
    val fuelLogsByMonth = remember(state.fuelLogs) {
        val groups = LinkedHashMap<String, MutableList<FuelLog>>()
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("MMM yy", Locale.getDefault())
        
        for (log in state.fuelLogs) {
            try {
                val date = sdfIn.parse(log.date)
                val monthStr = if (date != null) sdfOut.format(date) else "Unknown"
                if (!groups.containsKey(monthStr)) {
                    groups[monthStr] = mutableListOf()
                }
                groups[monthStr]?.add(log)
            } catch (e: Exception) {
                if (!groups.containsKey("Unknown")) {
                    groups["Unknown"] = mutableListOf()
                }
                groups["Unknown"]?.add(log)
            }
        }
        groups
    }

    // Efficiencies list for line graph
    val efficiencies = remember(state.fuelLogs) {
        calculateEfficiencies(state.fuelLogs)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Analisis Penggunaan & Efisiensi",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = BentoTextPrimary
        )

        // 1. Efficiency line graph
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tren Efisiensi Bahan Bakar (km/L)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BentoTextPrimary
                )
                Text(
                    text = "Menunjukkan tingkat keiritan bbm dari pengisian ke pengisian",
                    fontSize = 12.sp,
                    color = BentoTextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val validEffData = efficiencies.zip(state.fuelLogs).filterIndexed { index, _ -> index > 0 }
                if (validEffData.size < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Butuh minimal 3 catatan isi bensin untuk menggambar grafik tren efisiensi harian.",
                            fontSize = 13.sp,
                            color = BentoTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    EfficiencyLineChart(validEffData)
                }
            }
        }

        // 2. Fuel expense by month chart
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pengeluaran Bulanan BBM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BentoTextPrimary
                )
                Text(
                    text = "Total rupiah yang dihabiskan setiap bulannya",
                    fontSize = 12.sp,
                    color = BentoTextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (fuelLogsByMonth.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada pengeluaran bbm untuk dianalisis.",
                            fontSize = 13.sp,
                            color = BentoTextSecondary
                        )
                    }
                } else {
                    MonthlyExpenseBarChart(fuelLogsByMonth)
                }
            }
        }

        // 3. Fuel Type breakdown distribution
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Distribusi Penggunaan Jenis BBM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BentoTextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val fuelTypeCostMap = remember(state.fuelLogs) {
                    val map = mutableMapOf<String, Double>()
                    for (log in state.fuelLogs) {
                        map[log.fuelType] = (map[log.fuelType] ?: 0.0) + log.totalCost
                    }
                    map.entries.sortedByDescending { it.value }
                }

                val totalAllFuelCost = fuelTypeCostMap.sumOf { it.value }

                if (totalAllFuelCost == 0.0) {
                    Text(
                        text = "Belum ada catatan biaya bbm.",
                        fontSize = 13.sp,
                        color = BentoTextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        fuelTypeCostMap.forEach { (fuelType, cost) ->
                            val percent = (cost / totalAllFuelCost).toFloat()
                            val color = when (fuelType) {
                                "Pertalite" -> BentoEmerald
                                "Pertamax" -> Color(0xFF3B82F6)
                                "Pertamax Turbo" -> Color(0xFF8B5CF6)
                                "Solar" -> BentoAmber
                                "Dexlite" -> Color(0xFFEC4899)
                                else -> BentoTextSecondary
                            }
                            
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = fuelType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BentoTextPrimary
                                    )
                                    Text(
                                        text = "${formatIdr(cost)} (${(percent * 100).toInt()}%)",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = BentoTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { percent },
                                    color = color,
                                    trackColor = BentoCardBorder,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .background(Color.Transparent, RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Line graph for efficiency
@Composable
fun EfficiencyLineChart(data: List<Pair<Double, FuelLog>>) {
    val maxVal = remember(data) {
        val maxEff = data.maxOf { it.first }
        (maxEff * 1.2).coerceAtLeast(15.0) // default max y scale helper
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 10.dp)
    ) {
        val width = size.width
        val height = size.height
        val marginX = 40.dp.toPx()
        val marginY = 20.dp.toPx()
        
        val chartW = width - marginX - 10.dp.toPx()
        val chartH = height - marginY * 2

        // Draw horizontal grid lines & text
        val gridLines = 4
        for (i in 0..gridLines) {
            val yFactor = i.toFloat() / gridLines
            val y = height - marginY - (yFactor * chartH)
            
            // Grid line
            drawLine(
                color = BentoCardBorder,
                start = Offset(marginX, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )

            // Grid text Label (km/L)
            val valText = (yFactor * maxVal)
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.1f", valText),
                10.dp.toPx(),
                y + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextSecondary.toArgb()
                    textSize = 10.dp.toPx()
                    isAntiAlias = true
                }
            )
        }

        // Draw data line
        val stepX = chartW / (data.size - 1).coerceAtLeast(1)
        val points = data.mapIndexed { index, pair ->
            val eff = pair.first
            val x = marginX + (index * stepX)
            val y = height - marginY - ((eff / maxVal).toFloat() * chartH)
            Offset(x, y)
        }

        // Draw background gradient under line
        val fillPath = Path().apply {
            moveTo(points.first().x, height - marginY)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, height - marginY)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(BentoAccentIndigo.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = height - marginY
            )
        )

        // Draw line connection
        for (i in 0 until points.size - 1) {
            drawLine(
                color = BentoAccentIndigo,
                start = points[i],
                end = points[i+1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw data points & values
        points.forEachIndexed { idx, point ->
            drawCircle(
                color = BentoAccentIndigo,
                radius = 5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = BentoBg,
                radius = 2.5.dp.toPx(),
                center = point
            )

            // Draw text value above point
            val textValue = String.format("%.1f", data[idx].first)
            drawContext.canvas.nativeCanvas.drawText(
                textValue,
                point.x - 12.dp.toPx(),
                point.y - 8.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextPrimary.toArgb()
                    textSize = 10.dp.toPx()
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    isAntiAlias = true
                }
            )

            // X-axis label (date)
            val fullDateStr = data[idx].second.date
            val shortDate = try {
                val parts = fullDateStr.split("-")
                if (parts.size == 3) "${parts[2]}/${parts[1]}" else fullDateStr
            } catch (e: Exception) {
                fullDateStr
            }

            drawContext.canvas.nativeCanvas.drawText(
                shortDate,
                point.x - 14.dp.toPx(),
                height - 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextSecondary.toArgb()
                    textSize = 9.dp.toPx()
                    isAntiAlias = true
                }
            )
        }
    }
}

// Bar chart for monthly expense
@Composable
fun MonthlyExpenseBarChart(monthlyLogs: Map<String, List<FuelLog>>) {
    val monthData = remember(monthlyLogs) {
        monthlyLogs.entries.map { (month, logs) ->
            month to logs.sumOf { it.totalCost }
        }.takeLast(6) // display at most last 6 months
    }

    val maxVal = remember(monthData) {
        val maxCost = monthData.maxOfOrNull { it.second } ?: 100000.0
        (maxCost * 1.2).coerceAtLeast(100000.0)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 10.dp)
    ) {
        val width = size.width
        val height = size.height
        val marginX = 50.dp.toPx()
        val marginY = 20.dp.toPx()
        
        val chartW = width - marginX - 10.dp.toPx()
        val chartH = height - marginY * 2

        // Draw horizontal grid lines & labels
        val gridLines = 4
        for (i in 0..gridLines) {
            val yFactor = i.toFloat() / gridLines
            val y = height - marginY - (yFactor * chartH)
            
            drawLine(
                color = BentoCardBorder,
                start = Offset(marginX, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )

            // Grid text Label (in IDR thousands)
            val valText = (yFactor * maxVal) / 1000.0
            val formattedLabel = if (valText >= 1000.0) String.format("%.1fM", valText/1000.0) else String.format("%.0fK", valText)
            drawContext.canvas.nativeCanvas.drawText(
                formattedLabel,
                5.dp.toPx(),
                y + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextSecondary.toArgb()
                    textSize = 10.dp.toPx()
                    isAntiAlias = true
                }
            )
        }

        // Draw bars
        val numMonths = monthData.size
        val colWidth = (chartW / numMonths) * 0.55f
        val gap = (chartW / numMonths) * 0.45f

        monthData.forEachIndexed { index, (monthName, totalCost) ->
            val barHeight = ((totalCost / maxVal).toFloat() * chartH)
            val x = marginX + gap / 2 + index * (colWidth + gap)
            val y = height - marginY - barHeight

            // Draw Rounded Bar
            drawRoundRect(
                color = BentoAccentIndigo,
                topLeft = Offset(x, y),
                size = Size(colWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw Value label above bar
            val formattedValue = if (totalCost >= 1000000.0) String.format("%.2f Jt", totalCost / 1000000.0) else String.format("%.0fK", totalCost / 1000.0)
            drawContext.canvas.nativeCanvas.drawText(
                formattedValue,
                x + colWidth / 2 - 16.dp.toPx(),
                y - 6.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextPrimary.toArgb()
                    textSize = 9.dp.toPx()
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    isAntiAlias = true
                }
            )

            // Draw X label (Month)
            drawContext.canvas.nativeCanvas.drawText(
                monthName,
                x + colWidth / 2 - 18.dp.toPx(),
                height - 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextSecondary.toArgb()
                    textSize = 10.dp.toPx()
                    isAntiAlias = true
                }
            )
        }
    }
}


// -------------------- TAB 3: FUEL LOG HISTORY --------------------
@Composable
fun FuelHistoryTab(
    state: VehicleState,
    onDeleteLog: (String) -> Unit
) {
    val efficiencies = remember(state.fuelLogs) {
        calculateEfficiencies(state.fuelLogs)
    }

    if (state.fuelLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = BentoTextMuted,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Belum Ada Catatan Pengisian BBM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BentoTextSecondary
                )
                Text(
                    text = "Gunakan tombol + di kanan bawah untuk mencatat pembelian bbm harian Anda.",
                    fontSize = 13.sp,
                    color = BentoTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }
        }
    } else {
        // Display newest logs first in history list
        val reversedLogs = state.fuelLogs.zip(efficiencies).reversed()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Pengisian BBM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "${state.fuelLogs.size} Catatan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = BentoTextSecondary
                    )
                }
            }

            items(reversedLogs) { (log, eff) ->
                FuelLogItemCard(log = log, efficiency = eff, onDelete = { onDeleteLog(log.id) })
            }
        }
    }
}

@Composable
fun FuelLogItemCard(
    log: FuelLog,
    efficiency: Double,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                color = when (log.fuelType) {
                    "Pertalite" -> BentoEmeraldBg
                    "Pertamax" -> BentoAccentIndigoLight
                    "Solar" -> BentoAmberBg
                    else -> BentoCardBorder.copy(alpha = 0.3f)
                },
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = when (log.fuelType) {
                            "Pertalite" -> BentoEmerald
                            "Pertamax" -> BentoAccentIndigo
                            "Solar" -> BentoAmber
                            else -> BentoTextSecondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatIdr(log.totalCost),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BentoTextPrimary
                    )
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = BentoTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${formatDecimal(log.liters)} L • ${log.fuelType}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = BentoTextSecondary
                    )
                    Text(
                        text = formatOdo(log.odometer),
                        fontSize = 13.sp,
                        color = BentoTextSecondary
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = BentoCardBorder)

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.date,
                        fontSize = 11.sp,
                        color = BentoTextMuted
                    )
                    
                    if (efficiency > 0.0) {
                        Surface(
                            color = if (efficiency >= 14.0) BentoEmeraldBg else if (efficiency >= 10.0) BentoAmberBg else BentoRoseBg,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "${formatDecimal(efficiency)} km/L",
                                color = if (efficiency >= 14.0) BentoEmerald else if (efficiency >= 10.0) BentoAmber else BentoRose,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "First log (Awal)",
                            fontSize = 11.sp,
                            color = BentoTextMuted
                        )
                    }
                }
            }
        }
    }
}


// -------------------- TAB 4: COMPONENT MAINTENANCE & CONFIG --------------------
@Composable
fun MaintenanceTab(
    state: VehicleState,
    onSaveConfig: (VehicleState) -> Unit,
    onDeleteServiceLog: (String) -> Unit
) {
    var showOilConfigDialog by remember { mutableStateOf(false) }
    var showTireConfigDialog by remember { mutableStateOf(false) }
    var activeTireIdForConfig by remember { mutableStateOf("") }
    var showServiceConfigDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Component Settings Title
        item {
            Text(
                text = "Pengaturan & Konfigurasi Kendaraan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BentoTextPrimary
            )
        }

        // 1. Oil change settings config card
        item {
            MaintenanceConfigCard(
                title = "Oli Mesin",
                subtitle = "Ubah merek oli & interval penggantian harian",
                details = listOf(
                    "Merek Oli" to state.oilBrand,
                    "Interval Odo Ganti Terakhir" to formatOdo(state.oilLastChangeOdo),
                    "Target Interval Penggantian" to formatOdo(state.oilIntervalKm)
                ),
                onClickEdit = { showOilConfigDialog = true }
            )
        }

        // 2. Tire settings config card
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
        }

        // 3. General Service Settings
        item {
            MaintenanceConfigCard(
                title = "Servis Umum Berkala",
                subtitle = "Ubah jadwal pengecekan mesin & suspensi",
                details = listOf(
                    "Odometer Servis Terakhir" to formatOdo(state.generalServiceLastOdo),
                    "Target Interval Servis" to formatOdo(state.generalServiceIntervalKm)
                ),
                onClickEdit = { showServiceConfigDialog = true }
            )
        }

        // Service History Title
        item {
            Text(
                text = "Riwayat Pemeliharaan & Servis",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BentoTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (state.serviceLogs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BentoCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat pemeliharaan tambahan yang dicatat.",
                            fontSize = 13.sp,
                            color = BentoTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(state.serviceLogs) { log ->
                ServiceHistoryCard(log = log, onDelete = { onDeleteServiceLog(log.id) })
            }
        }
    }

    // Configuration Dialogs
    if (showOilConfigDialog) {
        OilConfigDialog(
            currentBrand = state.oilBrand,
            currentLastOdo = state.oilLastChangeOdo,
            currentInterval = state.oilIntervalKm,
            onDismiss = { showOilConfigDialog = false },
            onSave = { brand, lastOdo, interval ->
                val updated = state.copy(
                    oilBrand = brand,
                    oilLastChangeOdo = lastOdo,
                    oilIntervalKm = interval,
                    currentOdometer = maxOf(state.currentOdometer, lastOdo)
                )
                onSaveConfig(updated)
                showOilConfigDialog = false
            }
        )
    }

    if (showTireConfigDialog) {
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
    }

    if (showServiceConfigDialog) {
        ServiceConfigDialog(
            currentLastOdo = state.generalServiceLastOdo,
            currentInterval = state.generalServiceIntervalKm,
            onDismiss = { showServiceConfigDialog = false },
            onSave = { lastOdo, interval ->
                val updated = state.copy(
                    generalServiceLastOdo = lastOdo,
                    generalServiceIntervalKm = interval,
                    currentOdometer = maxOf(state.currentOdometer, lastOdo)
                )
                onSaveConfig(updated)
                showServiceConfigDialog = false
            }
        )
    }
}

@Composable
fun MaintenanceConfigCard(
    title: String,
    subtitle: String,
    details: List<Pair<String, String>>,
    onClickEdit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = BentoTextSecondary
                    )
                }
                IconButton(
                    onClick = onClickEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = BentoAccentIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                details.forEach { (label, value) ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = BentoTextSecondary
                        )
                        Text(
                            text = value,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceHistoryCard(
    log: ServiceLog,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BentoCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = when (log.type) {
                        "Ganti Oli" -> BentoAccentIndigoLight
                        "Ganti Ban" -> BentoEmeraldBg
                        "Servis Umum" -> BentoAmberBg
                        else -> BentoCardBorder.copy(alpha = 0.3f)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (log.type) {
                                "Ganti Oli" -> Icons.Default.Settings
                                "Ganti Ban" -> Icons.Default.DirectionsCar
                                "Servis Umum" -> Icons.Default.Build
                                else -> Icons.Default.Build
                            },
                            contentDescription = null,
                            tint = when (log.type) {
                                "Ganti Oli" -> BentoAccentIndigo
                                "Ganti Ban" -> BentoEmerald
                                "Servis Umum" -> BentoAmber
                                else -> BentoTextSecondary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.type,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "Odometer: ${formatOdo(log.odometer)}",
                        fontSize = 11.sp,
                        color = BentoTextSecondary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = BentoTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (log.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = BentoBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = log.notes,
                        fontSize = 12.sp,
                        color = BentoTextSecondary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.date,
                    fontSize = 11.sp,
                    color = BentoTextMuted
                )
                Text(
                    text = formatIdr(log.cost),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BentoTextPrimary
                )
            }
        }
    }
}


// -------------------- DIALOGS / FORMS --------------------

@Composable
fun AddFuelDialog(
    currentOdo: Double,
    onDismiss: () -> Unit,
    onSave: (date: String, odo: Double, liters: Double, price: Double, fuelType: String) -> Unit
) {
    var odoInput by remember { mutableStateOf(if (currentOdo > 0.0) String.format(Locale.US, "%.0f", currentOdo + 350.0) else "") }
    var litersInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var selectedFuelType by remember { mutableStateOf("Pertalite") }
    var dateInput by remember {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        mutableStateOf(today)
    }

    var fuelDropdownExpanded by remember { mutableStateOf(false) }
    val fuelOptions = listOf("Pertalite", "Pertamax", "Pertamax Turbo", "Solar", "Dexlite", "Pertamina Dex")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Catat Pengisian BBM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Date
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Odometer
                OutlinedTextField(
                    value = odoInput,
                    onValueChange = { odoInput = it },
                    label = { Text("Odometer Terkini (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Liters
                OutlinedTextField(
                    value = litersInput,
                    onValueChange = { litersInput = it },
                    label = { Text("Jumlah Liter (L)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Price per liter
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Harga per Liter (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Fuel Type dropdown selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedFuelType,
                        onValueChange = {},
                        label = { Text("Jenis BBM") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { fuelDropdownExpanded = true }
                            ) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { fuelDropdownExpanded = true },
                        shape = RoundedCornerShape(8.dp)
                    )
                    DropdownMenu(
                        expanded = fuelDropdownExpanded,
                        onDismissRequest = { fuelDropdownExpanded = false },
                        modifier = Modifier.background(BentoCardBg)
                    ) {
                        fuelOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedFuelType = option
                                    fuelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val odo = odoInput.toDoubleOrNull() ?: 0.0
                            val liters = litersInput.toDoubleOrNull() ?: 0.0
                            val price = priceInput.toDoubleOrNull() ?: 0.0
                            
                            if (odo > 0 && liters > 0 && price > 0 && dateInput.isNotEmpty()) {
                                onSave(dateInput, odo, liters, price, selectedFuelType)
                            }
                        },
                        enabled = odoInput.isNotEmpty() && litersInput.isNotEmpty() && priceInput.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun AddServiceDialog(
    currentOdo: Double,
    onDismiss: () -> Unit,
    onSave: (date: String, type: String, odo: Double, cost: Double, notes: String) -> Unit
) {
    var typeInput by remember { mutableStateOf("Ganti Oli") }
    var odoInput by remember { mutableStateOf(if (currentOdo > 0.0) String.format(Locale.US, "%.0f", currentOdo) else "") }
    var costInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var dateInput by remember {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        mutableStateOf(today)
    }

    var dropdownExpanded by remember { mutableStateOf(false) }
    val serviceOptions = listOf("Ganti Oli", "Ganti Ban", "Servis Umum", "Lainnya")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Catat Pemeliharaan / Servis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Date
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Service Type Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = typeInput,
                        onValueChange = {},
                        label = { Text("Jenis Servis") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { dropdownExpanded = true }
                            ) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true },
                        shape = RoundedCornerShape(8.dp)
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(BentoCardBg)
                    ) {
                        serviceOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    typeInput = option
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Odometer
                OutlinedTextField(
                    value = odoInput,
                    onValueChange = { odoInput = it },
                    label = { Text("Odometer Pengerjaan (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Cost
                OutlinedTextField(
                    value = costInput,
                    onValueChange = { costInput = it },
                    label = { Text("Biaya Servis (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Notes
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Keterangan Tambahan") },
                    placeholder = { Text("e.g. Merk oli, part yang diganti, bengkel dll") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 3
                )

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val odo = odoInput.toDoubleOrNull() ?: 0.0
                            val cost = costInput.toDoubleOrNull() ?: 0.0
                            
                            if (odo > 0 && dateInput.isNotEmpty()) {
                                onSave(dateInput, typeInput, odo, cost, notesInput)
                            }
                        },
                        enabled = odoInput.isNotEmpty() && dateInput.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun OilConfigDialog(
    currentBrand: String,
    currentLastOdo: Double,
    currentInterval: Double,
    onDismiss: () -> Unit,
    onSave: (brand: String, lastOdo: Double, interval: Double) -> Unit
) {
    var brandInput by remember { mutableStateOf(currentBrand) }
    var lastOdoInput by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentLastOdo)) }
    var intervalInput by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentInterval)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Konfigurasi Oli Mesin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )

                OutlinedTextField(
                    value = brandInput,
                    onValueChange = { brandInput = it },
                    label = { Text("Merek Oli") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = lastOdoInput,
                    onValueChange = { lastOdoInput = it },
                    label = { Text("Odometer Ganti Oli Terakhir") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = intervalInput,
                    onValueChange = { intervalInput = it },
                    label = { Text("Interval Ganti Oli (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val lastOdo = lastOdoInput.toDoubleOrNull() ?: 0.0
                            val interval = intervalInput.toDoubleOrNull() ?: 5000.0
                            onSave(brandInput, lastOdo, interval)
                        },
                        enabled = brandInput.isNotEmpty() && lastOdoInput.isNotEmpty() && intervalInput.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun TireConfigDialog(
    currentBrand: String,
    currentCompound: String,
    currentInstallOdo: Double,
    currentInterval: Double,
    onDismiss: () -> Unit,
    onSave: (brand: String, compound: String, installOdo: Double, interval: Double) -> Unit
) {
    var brandInput by remember { mutableStateOf(currentBrand) }
    var compoundInput by remember { mutableStateOf(currentCompound) }
    var installOdoInput by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentInstallOdo)) }
    var intervalInput by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentInterval)) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    val compoundOptions = listOf("Soft", "Medium", "Hard", "Eco")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Konfigurasi Ban Kendaraan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )

                OutlinedTextField(
                    value = brandInput,
                    onValueChange = { brandInput = it },
                    label = { Text("Merek Ban") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Compound Type Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = compoundInput,
                        onValueChange = {},
                        label = { Text("Jenis Kompound") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { dropdownExpanded = true }
                            ) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true },
                        shape = RoundedCornerShape(8.dp)
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(BentoCardBg)
                    ) {
                        compoundOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text("$option Compound") },
                                onClick = {
                                    compoundInput = option
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = installOdoInput,
                    onValueChange = { installOdoInput = it },
                    label = { Text("Odometer Pasang Ban") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = intervalInput,
                    onValueChange = { intervalInput = it },
                    label = { Text("Interval Umur Ban (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val installOdo = installOdoInput.toDoubleOrNull() ?: 0.0
                            val interval = intervalInput.toDoubleOrNull() ?: 40000.0
                            onSave(brandInput, compoundInput, installOdo, interval)
                        },
                        enabled = brandInput.isNotEmpty() && installOdoInput.isNotEmpty() && intervalInput.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceConfigDialog(
    currentLastOdo: Double,
    currentInterval: Double,
    onDismiss: () -> Unit,
    onSave: (lastOdo: Double, interval: Double) -> Unit
) {
    var lastOdoInput by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentLastOdo)) }
    var intervalInput by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentInterval)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Konfigurasi Servis Umum",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )

                OutlinedTextField(
                    value = lastOdoInput,
                    onValueChange = { lastOdoInput = it },
                    label = { Text("Odometer Servis Terakhir") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = intervalInput,
                    onValueChange = { intervalInput = it },
                    label = { Text("Interval Servis (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val lastOdo = lastOdoInput.toDoubleOrNull() ?: 0.0
                            val interval = intervalInput.toDoubleOrNull() ?: 10000.0
                            onSave(lastOdo, interval)
                        },
                        enabled = lastOdoInput.isNotEmpty() && intervalInput.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun AddVehicleDialog(
    initialName: String = "",
    initialType: String = "Roda 2",
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    
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
                Text("Sesuaikan Odometer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                Text("Masukkan angka odometer terkini dalam kilometer (km) untuk menyelaraskan indikator kendaraan.", color = BentoTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
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
