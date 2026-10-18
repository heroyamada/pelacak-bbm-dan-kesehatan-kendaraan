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
import androidx.compose.foundation.horizontalScroll
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
                    profile = activeProfile,
                    onNavigateToTab = { selectedTab = it },
                    onAddFuelClick = { showAddFuelDialog = true },
                    onAdjustOdo = { showAdjustOdoDialog = true },
                    onConfigureVisibility = { showVisibilityDialog = true },
                    onEditProfile = { showEditVehicleDialog = true }
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
                    profile = activeProfile,
                    onEditProfile = { showEditVehicleDialog = true },
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
                    title = "Tambah Kendaraan Baru",
                    onDismiss = { showAddVehicleDialog = false },
                    onSave = { name, type, tankCapacity ->
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
                        val newProfile = VehicleProfile(
                            name = name,
                            type = type,
                            tankCapacity = tankCapacity,
                            state = com.example.data.VehicleState(tires = initialTires)
                        )
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
                    title = "Edit Profil Kendaraan",
                    initialName = activeProfile.name,
                    initialType = activeProfile.type,
                    initialTankCapacity = activeProfile.tankCapacity,
                    onDismiss = { showEditVehicleDialog = false },
                    onSave = { name, type, tankCapacity ->
                        val updated = activeProfile.copy(name = name, type = type, tankCapacity = tankCapacity)
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
                                listOf(com.example.data.VehicleProfile(name = "Kendaraan Baru", type = "Roda 2", tankCapacity = 0.0, state = com.example.data.VehicleState(tires = defaultTires)))
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
                    tankCapacity = activeProfile?.tankCapacity ?: 0.0,
                    onDismiss = { showAddFuelDialog = false },
                    onSave = { date, odo, liters, price, fuelType, isFullTank ->
                        val newLog = FuelLog(
                            date = date,
                            odometer = odo,
                            liters = liters,
                            pricePerLiter = price,
                            fuelType = fuelType,
                            isFullTank = isFullTank
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

fun formatDateIndo(dateStr: String): String {
    return try {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdfIn.parse(dateStr)
        if (date != null) {
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(date)
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

fun formatMonthIndo(monthStr: String): String {
    return try {
        val sdfIn = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = sdfIn.parse(monthStr)
        if (date != null) {
            SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(date)
        } else {
            monthStr
        }
    } catch (e: Exception) {
        monthStr
    }
}

fun getPreviousMonth(monthKey: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val cal = Calendar.getInstance()
        val date = sdf.parse(monthKey)
        if (date != null) cal.time = date
        cal.add(Calendar.MONTH, -1)
        sdf.format(cal.time)
    } catch (e: Exception) {
        monthKey
    }
}

fun getNextMonth(monthKey: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val cal = Calendar.getInstance()
        val date = sdf.parse(monthKey)
        if (date != null) cal.time = date
        cal.add(Calendar.MONTH, 1)
        sdf.format(cal.time)
    } catch (e: Exception) {
        monthKey
    }
}

enum class StatsFilterMode(val label: String) {
    MONTH("Bulan"),
    YEAR("Tahun"),
    DATE_RANGE("Rentang Tanggal"),
    ALL("Semua")
}

val INDO_MONTHS = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)

val INDO_MONTHS_SHORT = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
    "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
)

fun parseYearFromMonthKey(monthKey: String): Int {
    return try {
        monthKey.split("-")[0].toInt()
    } catch (e: Exception) {
        Calendar.getInstance().get(Calendar.YEAR)
    }
}

fun parseMonthIndexFromMonthKey(monthKey: String): Int {
    return try {
        monthKey.split("-")[1].toInt()
    } catch (e: Exception) {
        Calendar.getInstance().get(Calendar.MONTH) + 1
    }
}

fun buildMonthKey(year: Int, monthIndex: Int): String {
    return String.format(Locale.US, "%04d-%02d", year, monthIndex.coerceIn(1, 12))
}

fun getDaysAgoDate(days: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}

fun getStartOfMonthDate(monthKey: String): String = "$monthKey-01"

fun getEndOfMonthDate(monthKey: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val cal = Calendar.getInstance()
        val date = sdf.parse(monthKey)
        if (date != null) cal.time = date
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        String.format(Locale.US, "%s-%02d", monthKey, maxDay)
    } catch (e: Exception) {
        "$monthKey-28"
    }
}

// Prepopulate initial realistic sample data
fun getSampleData(): VehicleState {
    return VehicleState(
        fuelLogs = listOf(
            FuelLog(id = "1", date = "2026-05-10", odometer = 10000.0, liters = 35.0, pricePerLiter = 10000.0, fuelType = "Pertalite", isFullTank = true),
            FuelLog(id = "2", date = "2026-05-25", odometer = 10450.0, liters = 32.0, pricePerLiter = 12500.0, fuelType = "Pertamax", isFullTank = true),
            FuelLog(id = "3", date = "2026-06-12", odometer = 10920.0, liters = 34.0, pricePerLiter = 12500.0, fuelType = "Pertamax", isFullTank = true),
            FuelLog(id = "4", date = "2026-06-28", odometer = 11390.0, liters = 33.0, pricePerLiter = 12500.0, fuelType = "Pertamax", isFullTank = true),
            FuelLog(id = "5", date = "2026-07-15", odometer = 11870.0, liters = 35.0, pricePerLiter = 14500.0, fuelType = "Pertamax Turbo", isFullTank = true)
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

data class FuelTankStatus(
    val tankCapacity: Double,
    val remainingLiters: Double,
    val percentage: Float,
    val estimatedRangeKm: Double,
    val distanceSinceLastRefuel: Double,
    val consumedSinceLast: Double,
    val avgEfficiency: Double,
    val isLowFuel: Boolean,
    val hasLogs: Boolean
)

fun calculateFuelTankStatus(profile: VehicleProfile): FuelTankStatus {
    val capacity = profile.tankCapacity
    val fuelLogs = profile.state.fuelLogs.sortedBy { it.odometer }
    val currentOdo = profile.state.currentOdometer

    val efficiencies = calculateEfficiencies(fuelLogs).filter { it > 0.0 }
    val defaultEff = if (profile.type == "Roda 2") 40.0 else 12.0
    val eff = if (efficiencies.isNotEmpty()) efficiencies.average() else defaultEff

    if (capacity <= 0.0) {
        return FuelTankStatus(
            tankCapacity = 0.0,
            remainingLiters = 0.0,
            percentage = 0.0f,
            estimatedRangeKm = 0.0,
            distanceSinceLastRefuel = 0.0,
            consumedSinceLast = 0.0,
            avgEfficiency = eff,
            isLowFuel = false,
            hasLogs = fuelLogs.isNotEmpty()
        )
    }

    if (fuelLogs.isEmpty()) {
        return FuelTankStatus(
            tankCapacity = capacity,
            remainingLiters = capacity,
            percentage = 1.0f,
            estimatedRangeKm = capacity * eff,
            distanceSinceLastRefuel = 0.0,
            consumedSinceLast = 0.0,
            avgEfficiency = eff,
            isLowFuel = false,
            hasLogs = false
        )
    }

    var currentLevel = if (fuelLogs.first().isFullTank) capacity else minOf(capacity, fuelLogs.first().liters)
    for (i in 1 until fuelLogs.size) {
        val dist = maxOf(0.0, fuelLogs[i].odometer - fuelLogs[i - 1].odometer)
        val consumed = if (eff > 0.0) dist / eff else 0.0
        currentLevel = maxOf(0.0, currentLevel - consumed)
        currentLevel = if (fuelLogs[i].isFullTank) capacity else minOf(capacity, currentLevel + fuelLogs[i].liters)
    }

    val lastLog = fuelLogs.last()
    val distSinceLast = maxOf(0.0, currentOdo - lastLog.odometer)
    val consumedSinceLast = if (eff > 0.0) distSinceLast / eff else 0.0
    val remainingLiters = maxOf(0.0, minOf(capacity, currentLevel - consumedSinceLast))
    val percentage = if (capacity > 0.0) (remainingLiters / capacity).toFloat().coerceIn(0f, 1f) else 0f
    val estimatedRangeKm = remainingLiters * eff
    val isLowFuel = percentage <= 0.20f || remainingLiters <= (if (profile.type == "Roda 2") 1.0 else 7.0)

    return FuelTankStatus(
        tankCapacity = capacity,
        remainingLiters = remainingLiters,
        percentage = percentage,
        estimatedRangeKm = estimatedRangeKm,
        distanceSinceLastRefuel = distSinceLast,
        consumedSinceLast = consumedSinceLast,
        avgEfficiency = eff,
        isLowFuel = isLowFuel,
        hasLogs = true
    )
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Odo: ${formatOdo(activeProfile?.state?.currentOdometer ?: 0.0)}",
                            fontSize = 13.sp,
                            color = BentoTextSecondary,
                            modifier = Modifier.clickable { onAdjustOdo() }
                        )
                        if (activeProfile != null) {
                            if (activeProfile.tankCapacity > 0.0) {
                                val fuelStatus = calculateFuelTankStatus(activeProfile)
                                Text(" • ", fontSize = 13.sp, color = BentoTextMuted)
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = if (fuelStatus.isLowFuel) BentoRose else BentoEmerald,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${formatDecimal(fuelStatus.remainingLiters)} L (${(fuelStatus.percentage * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (fuelStatus.isLowFuel) BentoRose else BentoEmerald
                                )
                            } else {
                                Text(" • ", fontSize = 13.sp, color = BentoTextMuted)
                                Text(
                                    text = "Atur Tangki",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BentoAmber,
                                    modifier = Modifier.clickable { onEditProfile() }
                                )
                            }
                        }
                    }
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
    profile: VehicleProfile?,
    onNavigateToTab: (AppTab) -> Unit,
    onAddFuelClick: () -> Unit,
    onAdjustOdo: () -> Unit,
    onConfigureVisibility: () -> Unit,
    onEditProfile: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val currentProfile = profile ?: VehicleProfile(name = "Kendaraan", type = "Roda 2", state = state)
    val fuelTankStatus = calculateFuelTankStatus(currentProfile)

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

        // Fuel Tank Realtime Monitoring Card
        if (state.visibility.showFuelTank) {
            FuelTankMonitorCard(
                status = fuelTankStatus,
                onAddFuelClick = onAddFuelClick,
                onAdjustOdo = onAdjustOdo,
                onEditProfile = onEditProfile
            )
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
fun FuelTankMonitorCard(
    status: FuelTankStatus,
    onAddFuelClick: () -> Unit,
    onAdjustOdo: () -> Unit,
    onEditProfile: () -> Unit = {}
) {
    if (status.tankCapacity <= 0.0) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoAmber.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = BentoAmberBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = "Fuel Tank",
                                tint = BentoAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kapasitas Tangki Belum Ditentukan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Setiap kendaraan memiliki kapasitas berbeda. Tentukan kapasitas tangki Anda agar sisa BBM dapat dipantau.",
                            fontSize = 12.sp,
                            color = BentoTextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onEditProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tentukan Kapasitas Tangki Sekarang", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    val barColor = when {
        status.isLowFuel -> BentoRose
        status.percentage <= 0.40f -> BentoAmber
        else -> BentoEmerald
    }
    val badgeBg = when {
        status.isLowFuel -> BentoRoseBg
        status.percentage <= 0.40f -> BentoAmberBg
        else -> BentoEmeraldBg
    }
    val badgeText = when {
        status.isLowFuel -> "BBM Kritis (${(status.percentage * 100).toInt()}%)"
        status.percentage <= 0.40f -> "Perhatian (${(status.percentage * 100).toInt()}%)"
        else -> "Aman (${(status.percentage * 100).toInt()}%)"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (status.isLowFuel) BentoRose.copy(alpha = 0.5f) else BentoCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = "Fuel Tank",
                            tint = barColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sisa Bahan Bakar di Tangki",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = if (status.hasLogs) "Jarak sejak isi: ${formatOdo(status.distanceSinceLastRefuel)}" else "Belum ada catatan BBM",
                        fontSize = 12.sp,
                        color = BentoTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = barColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Liters Display & Estimation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatDecimal(status.remainingLiters),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = " / ${formatDecimal(status.tankCapacity)} L",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = BentoTextSecondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Estimasi Jarak",
                        fontSize = 11.sp,
                        color = BentoTextMuted
                    )
                    Text(
                        text = "~${formatDecimal(status.estimatedRangeKm)} km",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (status.isLowFuel) BentoRose else BentoAccentIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual Tank Bar with Gauge notches (E, 1/4, 1/2, 3/4, F)
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { status.percentage },
                    color = barColor,
                    trackColor = BentoCardBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.Transparent, RoundedCornerShape(5.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("E (Kosong)", fontSize = 10.sp, color = BentoTextMuted)
                    Text("1/4", fontSize = 10.sp, color = BentoTextMuted)
                    Text("1/2", fontSize = 10.sp, color = BentoTextMuted)
                    Text("3/4", fontSize = 10.sp, color = BentoTextMuted)
                    Text("F (Penuh)", fontSize = 10.sp, color = BentoTextMuted)
                }
            }

            if (status.isLowFuel) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = BentoRoseBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BentoRose.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = BentoRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BBM menipis! Sisa estimasi tempuh ${formatDecimal(status.estimatedRangeKm)} km. Segera isi bensin.",
                            fontSize = 12.sp,
                            color = BentoRose,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 14.dp), color = BentoCardBorder)

            // Micro stats summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = BentoTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Terpakai: ${formatDecimal(status.consumedSinceLast)} L",
                        fontSize = 12.sp,
                        color = BentoTextSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = BentoTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Konsumsi: ${formatDecimal(status.avgEfficiency)} km/L",
                        fontSize = 12.sp,
                        color = BentoTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Update Odo & Isi BBM with equal weight & proper height
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAdjustOdo,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BentoCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoTextPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoAccentIndigo)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Update Odo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = onAddFuelClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Isi BBM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
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

    // Filter states
    var filterMode by remember { mutableStateOf(StatsFilterMode.MONTH) }
    val currentCal = remember { Calendar.getInstance() }
    val currentYear = remember { currentCal.get(Calendar.YEAR) }
    val currentMonthKey = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCal.time) }
    val currentToday = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentCal.time) }

    var selectedMonthKey by remember { mutableStateOf(currentMonthKey) }
    var selectedYear by remember { mutableStateOf(currentYear) }
    var startDateInput by remember { mutableStateOf(getDaysAgoDate(30)) }
    var endDateInput by remember { mutableStateOf(currentToday) }

    var showMonthYearDialog by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }

    // Toggle inside cards
    var showAllTimeEff by remember { mutableStateOf(false) }
    var expenseViewMode by remember { mutableStateOf("HARIAN") } // "HARIAN" vs "BULANAN"

    // Available months & years from data
    val availableMonths = remember(state.fuelLogs, currentMonthKey) {
        val months = state.fuelLogs.mapNotNull {
            if (it.date.length >= 7) it.date.substring(0, 7) else null
        }.distinct().sortedDescending()
        if (months.contains(currentMonthKey)) months else (listOf(currentMonthKey) + months)
    }

    val availableYears = remember(state.fuelLogs, currentYear) {
        val years = state.fuelLogs.mapNotNull {
            if (it.date.length >= 4) it.date.substring(0, 4).toIntOrNull() else null
        }.distinct().sortedDescending()
        if (years.contains(currentYear)) years else (listOf(currentYear) + years).distinct().sortedDescending()
    }

    // Active period label
    val activePeriodLabel = remember(filterMode, selectedMonthKey, selectedYear, startDateInput, endDateInput) {
        when (filterMode) {
            StatsFilterMode.MONTH -> formatMonthIndo(selectedMonthKey)
            StatsFilterMode.YEAR -> "Tahun $selectedYear"
            StatsFilterMode.DATE_RANGE -> {
                if (startDateInput.isNotBlank() && endDateInput.isNotBlank()) {
                    "${formatDateIndo(startDateInput)} - ${formatDateIndo(endDateInput)}"
                } else if (startDateInput.isNotBlank()) {
                    "Mulai ${formatDateIndo(startDateInput)}"
                } else if (endDateInput.isNotBlank()) {
                    "Sampai ${formatDateIndo(endDateInput)}"
                } else {
                    "Rentang Tanggal"
                }
            }
            StatsFilterMode.ALL -> "Semua Waktu"
        }
    }

    // Filtered fuel logs based on active mode
    val filteredFuelLogs = remember(state.fuelLogs, filterMode, selectedMonthKey, selectedYear, startDateInput, endDateInput) {
        when (filterMode) {
            StatsFilterMode.MONTH -> state.fuelLogs.filter { it.date.startsWith(selectedMonthKey) }
            StatsFilterMode.YEAR -> state.fuelLogs.filter { it.date.startsWith("$selectedYear-") }
            StatsFilterMode.DATE_RANGE -> state.fuelLogs.filter {
                val startOk = startDateInput.isBlank() || it.date >= startDateInput
                val endOk = endDateInput.isBlank() || it.date <= endDateInput
                startOk && endOk
            }
            StatsFilterMode.ALL -> state.fuelLogs
        }.sortedByDescending { it.date }
    }

    // Efficiencies calculated in full historical chronological order
    val allEfficienciesWithLogs = remember(state.fuelLogs) {
        val sorted = state.fuelLogs.sortedBy { it.odometer }
        val effs = calculateEfficiencies(sorted)
        effs.zip(sorted).filterIndexed { index, _ -> index > 0 }
    }

    // Efficiency data to display in chart
    val displayedEffData = remember(allEfficienciesWithLogs, filteredFuelLogs, showAllTimeEff, filterMode) {
        if (showAllTimeEff || filterMode == StatsFilterMode.ALL) {
            allEfficienciesWithLogs
        } else {
            val filteredIds = filteredFuelLogs.map { it.id }.toSet()
            allEfficienciesWithLogs.filter { filteredIds.contains(it.second.id) }
        }
    }

    // Financial & consumption summaries
    val totalCostInPeriod = remember(filteredFuelLogs) { filteredFuelLogs.sumOf { it.totalCost } }
    val totalLitersInPeriod = remember(filteredFuelLogs) { filteredFuelLogs.sumOf { it.liters } }
    val avgCostPerFill = remember(filteredFuelLogs, totalCostInPeriod) {
        if (filteredFuelLogs.isNotEmpty()) totalCostInPeriod / filteredFuelLogs.size else 0.0
    }

    // Daily expense data for current filtered month
    val dailyExpenseData = remember(filteredFuelLogs) {
        filteredFuelLogs
            .groupBy { it.date }
            .entries
            .sortedBy { it.key }
            .map { (date, logs) -> date to logs.sumOf { it.totalCost } }
    }

    // Monthly expense data for year view
    val yearlyMonthsData = remember(state.fuelLogs, selectedYear) {
        (1..12).map { mIdx ->
            val mKey = buildMonthKey(selectedYear, mIdx)
            val shortName = INDO_MONTHS_SHORT[mIdx - 1]
            val cost = state.fuelLogs.filter { it.date.startsWith(mKey) }.sumOf { it.totalCost }
            shortName to cost
        }
    }

    // All months summary for comparison
    val allMonthsData = remember(state.fuelLogs) {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("MMM yy", Locale.getDefault())
        val map = LinkedHashMap<String, Double>()
        state.fuelLogs.sortedBy { it.date }.forEach { log ->
            try {
                val d = sdfIn.parse(log.date)
                val key = if (d != null) sdfOut.format(d) else "Lain"
                map[key] = (map[key] ?: 0.0) + log.totalCost
            } catch (e: Exception) {
                map["Lain"] = (map["Lain"] ?: 0.0) + log.totalCost
            }
        }
        map.toList().takeLast(6)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Analisis Penggunaan & Efisiensi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BentoTextPrimary
                )
                Text(
                    text = "Periode: $activePeriodLabel",
                    fontSize = 12.sp,
                    color = BentoAccentIndigo,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                color = BentoAccentIndigoLight,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = filterMode.label,
                    color = BentoAccentIndigo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // ==================== MASTER FILTER PERIODE FLEKSIBEL CARD ====================
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pengaturan Periode Fleksibel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "Bulan • Tanggal • Tahun",
                        fontSize = 11.sp,
                        color = BentoTextMuted
                    )
                }

                // Mode Selector Segmented Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatsFilterMode.values().forEach { mode ->
                        val isSelected = filterMode == mode
                        val iconVector = when (mode) {
                            StatsFilterMode.MONTH -> Icons.Default.DateRange
                            StatsFilterMode.YEAR -> Icons.Default.Today
                            StatsFilterMode.DATE_RANGE -> Icons.Default.Tune
                            StatsFilterMode.ALL -> Icons.Default.History
                        }
                        Surface(
                            color = if (isSelected) BentoAccentIndigo else BentoCardBorder.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) BentoAccentIndigo else BentoCardBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable {
                                filterMode = mode
                                showAllTimeEff = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else BentoTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = mode.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else BentoTextPrimary
                                )
                            }
                        }
                    }
                }

                // Dynamic Controls based on selected mode
                when (filterMode) {
                    StatsFilterMode.MONTH -> {
                        Surface(
                            color = BentoCardBorder.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BentoCardBorder.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { selectedMonthKey = getPreviousMonth(selectedMonthKey) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Bulan Sebelumnya",
                                        tint = BentoTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { showMonthYearDialog = true }
                                ) {
                                    Text(
                                        text = formatMonthIndo(selectedMonthKey),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = BentoTextPrimary
                                    )
                                    Text(
                                        text = if (selectedMonthKey == currentMonthKey) "Bulan Berjalan (Klik ubah)" else "Ketuk untuk pilih bulan & tahun",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedMonthKey == currentMonthKey) BentoEmerald else BentoAccentIndigo
                                    )
                                }

                                IconButton(
                                    onClick = { selectedMonthKey = getNextMonth(selectedMonthKey) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Bulan Berikutnya",
                                        tint = BentoTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Quick Month Chips
                        if (availableMonths.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableMonths.forEach { monthKey ->
                                    val isSelected = monthKey == selectedMonthKey
                                    val labelText = if (monthKey == currentMonthKey) "${formatMonthIndo(monthKey)} (Bulan Ini)" else formatMonthIndo(monthKey)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedMonthKey = monthKey },
                                        label = { Text(labelText, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BentoAccentIndigo,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    StatsFilterMode.YEAR -> {
                        Surface(
                            color = BentoCardBorder.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BentoCardBorder.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { selectedYear -= 1 },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Tahun Lalu",
                                        tint = BentoTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Tahun $selectedYear",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = BentoTextPrimary
                                    )
                                    Text(
                                        text = if (selectedYear == currentYear) "Tahun Berjalan" else "Riwayat Tahun",
                                        fontSize = 10.sp,
                                        color = if (selectedYear == currentYear) BentoEmerald else BentoTextSecondary
                                    )
                                }

                                IconButton(
                                    onClick = { selectedYear += 1 },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Tahun Depan",
                                        tint = BentoTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Year quick chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableYears.forEach { yr ->
                                FilterChip(
                                    selected = selectedYear == yr,
                                    onClick = { selectedYear = yr },
                                    label = { Text("Tahun $yr", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoAccentIndigo,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    StatsFilterMode.DATE_RANGE -> {
                        Surface(
                            color = BentoCardBorder.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BentoCardBorder.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Rentang Tanggal Aktif",
                                        fontSize = 11.sp,
                                        color = BentoTextMuted
                                    )
                                    Text(
                                        text = "${formatDateIndo(startDateInput)} s/d ${formatDateIndo(endDateInput)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BentoTextPrimary
                                    )
                                }
                                Button(
                                    onClick = { showDateRangeDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ubah", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Preset Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = BentoCardBorder.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    startDateInput = getDaysAgoDate(7)
                                    endDateInput = currentToday
                                }
                            ) {
                                Text("7 Hari", fontSize = 11.sp, color = BentoTextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Surface(
                                color = BentoCardBorder.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    startDateInput = getDaysAgoDate(30)
                                    endDateInput = currentToday
                                }
                            ) {
                                Text("30 Hari", fontSize = 11.sp, color = BentoTextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Surface(
                                color = BentoCardBorder.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    startDateInput = "$currentMonthKey-01"
                                    endDateInput = currentToday
                                }
                            ) {
                                Text("Bulan Ini", fontSize = 11.sp, color = BentoTextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Surface(
                                color = BentoCardBorder.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    val lastM = getPreviousMonth(currentMonthKey)
                                    startDateInput = "$lastM-01"
                                    endDateInput = getEndOfMonthDate(lastM)
                                }
                            ) {
                                Text("Bulan Lalu", fontSize = 11.sp, color = BentoTextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Surface(
                                color = BentoCardBorder.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    startDateInput = "$currentYear-01-01"
                                    endDateInput = currentToday
                                }
                            ) {
                                Text("Tahun Ini", fontSize = 11.sp, color = BentoTextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    StatsFilterMode.ALL -> {
                        Surface(
                            color = BentoEmeraldBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoEmerald, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Menampilkan semua data riwayat dari awal tanpa batasan tanggal.",
                                    fontSize = 12.sp,
                                    color = BentoEmerald,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==================== 1. EFFICIENCY LINE GRAPH CARD ====================
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tren Efisiensi Bahan Bakar (km/L)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Menunjukkan tingkat keiritan bbm dari pengisian ke pengisian",
                            fontSize = 12.sp,
                            color = BentoTextSecondary
                        )
                    }

                    // Scope switch chip (Sesuai Filter vs Semua Riwayat)
                    Surface(
                        color = if (showAllTimeEff) BentoAccentIndigo else BentoCardBorder.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { showAllTimeEff = !showAllTimeEff }
                    ) {
                        Text(
                            text = if (showAllTimeEff) "Semua Riwayat" else "Sesuai Filter",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showAllTimeEff) Color.White else BentoTextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Highlight metrics if efficiency data exists
                if (displayedEffData.isNotEmpty()) {
                    val effVals = displayedEffData.map { it.first }
                    val avgEff = effVals.average()
                    val maxEff = effVals.maxOrNull() ?: 0.0
                    val minEff = effVals.minOrNull() ?: 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = BentoAccentIndigoLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Rata-rata", fontSize = 10.sp, color = BentoTextMuted)
                                Text(
                                    text = String.format(Locale.US, "%.1f km/L", avgEff),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoAccentIndigo
                                )
                            }
                        }
                        Surface(
                            color = BentoEmeraldBg,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Paling Irit", fontSize = 10.sp, color = BentoTextMuted)
                                Text(
                                    text = String.format(Locale.US, "%.1f km/L", maxEff),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoEmerald
                                )
                            }
                        }
                        Surface(
                            color = BentoAmberBg,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Paling Boros", fontSize = 10.sp, color = BentoTextMuted)
                                Text(
                                    text = String.format(Locale.US, "%.1f km/L", minEff),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoAmber
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (displayedEffData.size < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = BentoTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Tercatat ${displayedEffData.size} titik efisiensi pada ${if (showAllTimeEff) "semua riwayat" else activePeriodLabel}.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Grafik garis tren membutuhkan minimal 2 titik perbandingan (3 pengisian bensin).",
                                fontSize = 11.sp,
                                color = BentoTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            if (!showAllTimeEff && allEfficienciesWithLogs.size >= 2) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { showAllTimeEff = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Lihat Tren Semua Riwayat (${allEfficienciesWithLogs.size} Titik)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    EfficiencyLineChart(displayedEffData)
                }
            }
        }

        // ==================== 2. FUEL EXPENSE BAR CHART CARD ====================
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pengeluaran BBM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Total rupiah yang dihabiskan pada $activePeriodLabel",
                            fontSize = 12.sp,
                            color = BentoTextSecondary
                        )
                    }

                    // If in MONTH mode, provide Harian vs Bulanan toggle
                    if (filterMode == StatsFilterMode.MONTH) {
                        Surface(
                            color = BentoCardBorder.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                Surface(
                                    color = if (expenseViewMode == "HARIAN") BentoAccentIndigo else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { expenseViewMode = "HARIAN" }
                                ) {
                                    Text(
                                        text = "Harian",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (expenseViewMode == "HARIAN") Color.White else BentoTextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = if (expenseViewMode == "BULANAN") BentoAccentIndigo else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { expenseViewMode = "BULANAN" }
                                ) {
                                    Text(
                                        text = "Bulanan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (expenseViewMode == "BULANAN") Color.White else BentoTextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary stats pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = BentoCardBorder.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Biaya", fontSize = 10.sp, color = BentoTextMuted)
                            Text(
                                text = formatIdr(totalCostInPeriod),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }
                    }

                    Surface(
                        color = BentoCardBorder.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Liter", fontSize = 10.sp, color = BentoTextMuted)
                            Text(
                                text = "${formatDecimal(totalLitersInPeriod)} L",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }
                    }

                    Surface(
                        color = BentoCardBorder.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Rata-rata/Isi", fontSize = 10.sp, color = BentoTextMuted)
                            Text(
                                text = formatIdr(avgCostPerFill),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Render Bar Chart based on mode
                when {
                    filterMode == StatsFilterMode.MONTH && expenseViewMode == "HARIAN" -> {
                        if (dailyExpenseData.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Belum ada transaksi pada ${formatMonthIndo(selectedMonthKey)}.",
                                        fontSize = 13.sp,
                                        color = BentoTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { filterMode = StatsFilterMode.ALL }) {
                                        Text("Lihat Pengeluaran Semua Bulan", color = BentoAccentIndigo, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            DailyExpenseBarChart(dailyExpenseData)
                        }
                    }

                    filterMode == StatsFilterMode.YEAR -> {
                        val hasYearData = yearlyMonthsData.any { it.second > 0 }
                        if (!hasYearData) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada catatan pengeluaran di tahun $selectedYear.",
                                    fontSize = 13.sp,
                                    color = BentoTextSecondary
                                )
                            }
                        } else {
                            MonthlyExpenseBarChart(yearlyMonthsData)
                        }
                    }

                    else -> {
                        // Month with Bulanan view or Date Range or All Time
                        if (allMonthsData.isEmpty()) {
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
                            MonthlyExpenseBarChart(allMonthsData)
                        }
                    }
                }
            }
        }

        // ==================== 3. DAILY FUEL TRANSACTIONS BREAKDOWN CARD ====================
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rincian Transaksi per Tanggal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Catatan pengisian BBM pada $activePeriodLabel",
                            fontSize = 12.sp,
                            color = BentoTextSecondary
                        )
                    }
                    Surface(
                        color = BentoAccentIndigoLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${filteredFuelLogs.size}x Isi",
                            color = BentoAccentIndigo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (filteredFuelLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = BentoTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada transaksi pengisian BBM pada periode $activePeriodLabel.",
                                fontSize = 13.sp,
                                color = BentoTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (filterMode != StatsFilterMode.MONTH || selectedMonthKey != currentMonthKey) {
                                    Button(
                                        onClick = {
                                            filterMode = StatsFilterMode.MONTH
                                            selectedMonthKey = currentMonthKey
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Kembali ke Bulan Ini", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                                Button(
                                    onClick = { filterMode = StatsFilterMode.ALL },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoCardBorder),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Lihat Semua Riwayat", fontSize = 11.sp, color = BentoTextPrimary)
                                }
                            }
                        }
                    }
                } else {
                    val logsByDate = filteredFuelLogs.groupBy { it.date }.entries.sortedByDescending { it.key }

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        logsByDate.forEach { (date, logsForDate) ->
                            val dayTotalCost = logsForDate.sumOf { it.totalCost }
                            val dayTotalLiters = logsForDate.sumOf { it.liters }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Date Header with Subtotal
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = BentoAccentIndigo.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = formatDateIndo(date),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoAccentIndigo,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "${formatIdr(dayTotalCost)} (${formatDecimal(dayTotalLiters)} L)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BentoTextSecondary
                                    )
                                }

                                // Transactions on this date
                                logsForDate.forEach { log ->
                                    val fuelBadgeColor = when (log.fuelType) {
                                        "Pertalite" -> BentoEmerald
                                        "Pertamax" -> Color(0xFF3B82F6)
                                        "Pertamax Turbo" -> Color(0xFF8B5CF6)
                                        "Solar" -> BentoAmber
                                        "Dexlite" -> Color(0xFFEC4899)
                                        else -> BentoTextSecondary
                                    }
                                    val fuelBgColor = when (log.fuelType) {
                                        "Pertalite" -> BentoEmeraldBg
                                        "Pertamax" -> Color(0x183B82F6)
                                        "Pertamax Turbo" -> Color(0x188B5CF6)
                                        "Solar" -> BentoAmberBg
                                        "Dexlite" -> Color(0x18EC4899)
                                        else -> BentoCardBorder.copy(alpha = 0.2f)
                                    }

                                    Surface(
                                        color = BentoCardBorder.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, BentoCardBorder.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    color = fuelBgColor,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.LocalGasStation,
                                                            contentDescription = null,
                                                            tint = fuelBadgeColor,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = log.fuelType,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = BentoTextPrimary
                                                        )
                                                        if (log.isFullTank) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Surface(
                                                                color = BentoEmeraldBg,
                                                                shape = RoundedCornerShape(4.dp)
                                                            ) {
                                                                Text(
                                                                    text = "Full",
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = BentoEmerald,
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "${formatDecimal(log.liters)} L • ${formatIdr(log.pricePerLiter)}/L • Odo: ${formatOdo(log.odometer)}",
                                                        fontSize = 11.sp,
                                                        color = BentoTextSecondary
                                                    )
                                                }
                                            }
                                            Text(
                                                text = formatIdr(log.totalCost),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = BentoTextPrimary,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== 4. FUEL TYPE BREAKDOWN CARD ====================
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

                val fuelTypeCostMap = remember(filteredFuelLogs) {
                    val map = mutableMapOf<String, Double>()
                    for (log in filteredFuelLogs) {
                        map[log.fuelType] = (map[log.fuelType] ?: 0.0) + log.totalCost
                    }
                    map.entries.sortedByDescending { it.value }
                }

                val totalAllFuelCost = fuelTypeCostMap.sumOf { it.value }

                if (totalAllFuelCost == 0.0) {
                    Text(
                        text = "Belum ada catatan biaya bbm pada periode ini.",
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

    // Modal Dialogs for Flexible Picking
    if (showMonthYearDialog) {
        MonthYearPickerDialog(
            initialMonthKey = selectedMonthKey,
            onDismiss = { showMonthYearDialog = false },
            onConfirm = { newKey ->
                selectedMonthKey = newKey
                filterMode = StatsFilterMode.MONTH
            }
        )
    }

    if (showDateRangeDialog) {
        DateRangePickerDialog(
            initialStartDate = startDateInput,
            initialEndDate = endDateInput,
            onDismiss = { showDateRangeDialog = false },
            onConfirm = { sDate, eDate ->
                startDateInput = sDate
                endDateInput = eDate
                filterMode = StatsFilterMode.DATE_RANGE
            }
        )
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

// Daily Expense Bar Chart (for viewing individual days in a month)
@Composable
fun DailyExpenseBarChart(dailyData: List<Pair<String, Double>>) {
    val maxVal = remember(dailyData) {
        val maxCost = dailyData.maxOfOrNull { it.second } ?: 100000.0
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
        val marginX = 45.dp.toPx()
        val marginY = 22.dp.toPx()

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

            val valText = (yFactor * maxVal) / 1000.0
            val formattedLabel = if (valText >= 1000.0) String.format(Locale.US, "%.1fM", valText/1000.0) else String.format(Locale.US, "%.0fK", valText)
            drawContext.canvas.nativeCanvas.drawText(
                formattedLabel,
                4.dp.toPx(),
                y + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextSecondary.toArgb()
                    textSize = 10.dp.toPx()
                    isAntiAlias = true
                }
            )
        }

        // Draw bars
        val numItems = dailyData.size.coerceAtLeast(1)
        val colWidth = ((chartW / numItems) * 0.6f).coerceAtMost(36.dp.toPx())
        val totalColsWidth = colWidth * numItems
        val spacing = if (numItems > 1) (chartW - totalColsWidth) / (numItems + 1) else (chartW - colWidth) / 2

        dailyData.forEachIndexed { index, (dateStr, totalCost) ->
            val barHeight = ((totalCost / maxVal).toFloat() * chartH).coerceAtLeast(4.dp.toPx())
            val x = marginX + spacing + index * (colWidth + spacing)
            val y = height - marginY - barHeight

            // Draw Rounded Bar
            drawRoundRect(
                color = BentoAccentIndigo,
                topLeft = Offset(x, y),
                size = Size(colWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw Value label above bar
            val formattedValue = if (totalCost >= 1000000.0) String.format(Locale.US, "%.1f Jt", totalCost / 1000000.0) else String.format(Locale.US, "%.0fK", totalCost / 1000.0)
            drawContext.canvas.nativeCanvas.drawText(
                formattedValue,
                x + colWidth / 2 - 12.dp.toPx(),
                y - 6.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextPrimary.toArgb()
                    textSize = 9.dp.toPx()
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    isAntiAlias = true
                }
            )

            // Draw X label (e.g. "05/10")
            val label = try {
                val parts = dateStr.split("-")
                if (parts.size == 3) "${parts[2]}/${parts[1]}" else dateStr
            } catch (e: Exception) {
                dateStr
            }

            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + colWidth / 2 - 12.dp.toPx(),
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

// Monthly Expense Bar Chart (for 12 months in a year or last 6 months)
@Composable
fun MonthlyExpenseBarChart(
    data: List<Pair<String, Double>>
) {
    val maxVal = remember(data) {
        val maxCost = data.maxOfOrNull { it.second } ?: 100000.0
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
        val marginX = 45.dp.toPx()
        val marginY = 22.dp.toPx()

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

            val valText = (yFactor * maxVal) / 1000.0
            val formattedLabel = if (valText >= 1000.0) String.format(Locale.US, "%.1fM", valText/1000.0) else String.format(Locale.US, "%.0fK", valText)
            drawContext.canvas.nativeCanvas.drawText(
                formattedLabel,
                4.dp.toPx(),
                y + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextSecondary.toArgb()
                    textSize = 10.dp.toPx()
                    isAntiAlias = true
                }
            )
        }

        // Draw bars
        val numMonths = data.size.coerceAtLeast(1)
        val colWidth = (chartW / numMonths) * 0.55f
        val gap = (chartW / numMonths) * 0.45f

        data.forEachIndexed { index, (monthName, totalCost) ->
            val barHeight = ((totalCost / maxVal).toFloat() * chartH).coerceAtLeast(if (totalCost > 0) 4.dp.toPx() else 0f)
            val x = marginX + gap / 2 + index * (colWidth + gap)
            val y = height - marginY - barHeight

            // Draw Rounded Bar
            if (barHeight > 0f) {
                drawRoundRect(
                    color = BentoAccentIndigo,
                    topLeft = Offset(x, y),
                    size = Size(colWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            } else {
                drawCircle(
                    color = BentoCardBorder,
                    radius = 2.dp.toPx(),
                    center = Offset(x + colWidth / 2, height - marginY - 2.dp.toPx())
                )
            }

            // Draw Value label above bar
            if (totalCost > 0) {
                val formattedValue = if (totalCost >= 1000000.0) String.format(Locale.US, "%.1f Jt", totalCost / 1000000.0) else String.format(Locale.US, "%.0fK", totalCost / 1000.0)
                drawContext.canvas.nativeCanvas.drawText(
                    formattedValue,
                    x + colWidth / 2 - 12.dp.toPx(),
                    y - 6.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = BentoTextPrimary.toArgb()
                        textSize = 9.dp.toPx()
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        isAntiAlias = true
                    }
                )
            }

            // Draw X label (Month)
            drawContext.canvas.nativeCanvas.drawText(
                monthName,
                x + colWidth / 2 - 12.dp.toPx(),
                height - 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = BentoTextSecondary.toArgb()
                    textSize = 9.5.dp.toPx()
                    isAntiAlias = true
                }
            )
        }
    }
}

// Dialog for visually selecting Month and Year
@Composable
fun MonthYearPickerDialog(
    initialMonthKey: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var year by remember { mutableStateOf(parseYearFromMonthKey(initialMonthKey)) }
    var selectedMonthIndex by remember { mutableStateOf(parseMonthIndexFromMonthKey(initialMonthKey)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pilih Bulan & Tahun",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = BentoTextPrimary
                )

                // Year Stepper
                Surface(
                    color = BentoCardBorder.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { year -= 1 }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Tahun Lalu", tint = BentoTextPrimary)
                        }
                        Text(
                            text = "Tahun $year",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BentoTextPrimary
                        )
                        IconButton(onClick = { year += 1 }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Tahun Depan", tint = BentoTextPrimary)
                        }
                    }
                }

                // Quick year chips
                val curYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(curYear - 2, curYear - 1, curYear, curYear + 1).forEach { yr ->
                        FilterChip(
                            selected = year == yr,
                            onClick = { year = yr },
                            label = { Text("$yr", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoAccentIndigo,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 12 Months Grid (4 rows x 3 columns)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 3) {
                                val monthIdx = row * 3 + col + 1
                                val monthName = INDO_MONTHS_SHORT[monthIdx - 1]
                                val isSelected = monthIdx == selectedMonthIndex
                                Surface(
                                    color = if (isSelected) BentoAccentIndigo else BentoCardBorder.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isSelected) BentoAccentIndigo else BentoCardBorder.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMonthIndex = monthIdx }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = monthName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) Color.White else BentoTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = BentoTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newKey = buildMonthKey(year, selectedMonthIndex)
                            onConfirm(newKey)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Terapkan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Dialog for setting custom Date Range
@Composable
fun DateRangePickerDialog(
    initialStartDate: String,
    initialEndDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var startInput by remember { mutableStateOf(if (initialStartDate.isNotBlank()) initialStartDate else getDaysAgoDate(30)) }
    var endInput by remember { mutableStateOf(if (initialEndDate.isNotBlank()) initialEndDate else today) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BentoCardBorder),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Filter Rentang Tanggal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = BentoTextPrimary
                )

                Text(
                    text = "Pilih preset cepat atau masukkan tanggal spesifik (YYYY-MM-DD):",
                    fontSize = 12.sp,
                    color = BentoTextSecondary
                )

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = BentoCardBorder.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            startInput = getDaysAgoDate(7)
                            endInput = today
                        }
                    ) {
                        Text("7 Hari", fontSize = 11.sp, color = BentoTextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Surface(
                        color = BentoCardBorder.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            startInput = getDaysAgoDate(30)
                            endInput = today
                        }
                    ) {
                        Text("30 Hari", fontSize = 11.sp, color = BentoTextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Surface(
                        color = BentoCardBorder.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            val curM = today.substring(0, 7)
                            startInput = "$curM-01"
                            endInput = today
                        }
                    ) {
                        Text("Bulan Ini", fontSize = 11.sp, color = BentoTextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Surface(
                        color = BentoCardBorder.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            val lastM = getPreviousMonth(today.substring(0, 7))
                            startInput = "$lastM-01"
                            endInput = getEndOfMonthDate(lastM)
                        }
                    ) {
                        Text("Bulan Lalu", fontSize = 11.sp, color = BentoTextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                OutlinedTextField(
                    value = startInput,
                    onValueChange = { startInput = it },
                    label = { Text("Tanggal Mulai (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoTextPrimary,
                        unfocusedTextColor = BentoTextPrimary,
                        focusedBorderColor = BentoAccentIndigo,
                        unfocusedBorderColor = BentoCardBorder
                    )
                )

                OutlinedTextField(
                    value = endInput,
                    onValueChange = { endInput = it },
                    label = { Text("Tanggal Selesai (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoTextPrimary,
                        unfocusedTextColor = BentoTextPrimary,
                        focusedBorderColor = BentoAccentIndigo,
                        unfocusedBorderColor = BentoCardBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = BentoTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(startInput.trim(), endInput.trim())
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Terapkan Rentang", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${formatDecimal(log.liters)} L • ${log.fuelType}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoTextSecondary
                        )
                        if (log.isFullTank) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = BentoAccentIndigoLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Full Tank",
                                    fontSize = 10.sp,
                                    color = BentoAccentIndigo,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
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
    profile: VehicleProfile? = null,
    onEditProfile: () -> Unit = {},
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

        // Fuel Tank Config
        item {
            val cap = profile?.tankCapacity ?: 0.0
            MaintenanceConfigCard(
                title = "Kapasitas Tangki BBM",
                subtitle = "Ditentukan oleh Anda sendiri untuk memonitor sisa BBM secara presisi",
                details = listOf(
                    "Jenis Kendaraan" to (profile?.type ?: "Roda 2"),
                    "Kapasitas Tangki" to if (cap > 0.0) "${formatDecimal(cap)} Liter" else "Belum ditentukan (Ketuk Edit)",
                    "Status Monitoring" to if (state.visibility.showFuelTank) "Aktif di Dashboard" else "Nonaktif"
                ),
                onClickEdit = onEditProfile
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
    tankCapacity: Double = 0.0,
    onDismiss: () -> Unit,
    onSave: (date: String, odo: Double, liters: Double, price: Double, fuelType: String, isFullTank: Boolean) -> Unit
) {
    var odoInput by remember { mutableStateOf(if (currentOdo > 0.0) String.format(Locale.US, "%.0f", currentOdo + 350.0) else "") }
    var litersInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var selectedFuelType by remember { mutableStateOf("Pertalite") }
    var isFullTank by remember { mutableStateOf(true) }
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
                    color = BentoTextPrimary,
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

                // Full Tank Switch/Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFullTank = !isFullTank }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isFullTank,
                        onCheckedChange = { isFullTank = it },
                        colors = CheckboxDefaults.colors(checkedColor = BentoAccentIndigo, uncheckedColor = BentoTextSecondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Isi Penuh Tangki (Full Tank)", color = BentoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (isFullTank) {
                                if (tankCapacity > 0.0) "Tangki terisi penuh (~${formatDecimal(tankCapacity)} L). Akurasi monitor sisa BBM maksimal."
                                else "Tangki terisi penuh. Akurasi monitor sisa BBM maksimal."
                            } else "Pengisian sebagian (hanya menambah liter)",
                            color = BentoTextSecondary,
                            fontSize = 11.sp
                        )
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
                                onSave(dateInput, odo, liters, price, selectedFuelType, isFullTank)
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
                    color = BentoTextPrimary,
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
                    color = BentoTextPrimary
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
                    color = BentoTextPrimary
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
                    color = BentoTextPrimary
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
    title: String = "Tambah Kendaraan",
    initialName: String = "",
    initialType: String = "Roda 2",
    initialTankCapacity: Double = 0.0,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, tankCapacity: Double) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var tankCapacityStr by remember { 
        mutableStateOf(if (initialTankCapacity > 0.0) String.format(Locale.US, "%.1f", initialTankCapacity).trimEnd('0').trimEnd('.') else "") 
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, modifier = Modifier.padding(bottom = 16.dp))
                
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Jenis Kendaraan", color = BentoTextSecondary, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "Roda 2",
                        onClick = { type = "Roda 2" },
                        label = { Text("Roda 2 (Motor)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BentoAccentIndigo, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = type == "Roda 4",
                        onClick = { type = "Roda 4" },
                        label = { Text("Roda 4 (Mobil)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BentoAccentIndigo, selectedLabelColor = Color.White)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = tankCapacityStr,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                            tankCapacityStr = input
                        }
                    },
                    label = { Text("Kapasitas Tangki (Liter) *Wajib", color = BentoTextSecondary) },
                    placeholder = { Text("Masukkan kapasitas tangki Anda (L)") },
                    supportingText = {
                        val capVal = tankCapacityStr.toDoubleOrNull()
                        if (tankCapacityStr.isBlank()) {
                            Text("Wajib diisi sendiri karena setiap kendaraan berbeda-beda kapasitasnya.", fontSize = 11.sp, color = BentoAmber)
                        } else if (capVal == null || capVal <= 0.0) {
                            Text("Kapasitas tangki harus lebih besar dari 0 liter.", fontSize = 11.sp, color = BentoRose)
                        } else {
                            Text("Kapasitas: ${formatDecimal(capVal)} Liter (digunakan untuk perhitungan sisa BBM).", fontSize = 11.sp, color = BentoEmerald)
                        }
                    },
                    isError = tankCapacityStr.isNotBlank() && (tankCapacityStr.toDoubleOrNull() ?: 0.0) <= 0.0,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoTextPrimary,
                        unfocusedTextColor = BentoTextPrimary,
                        focusedBorderColor = BentoAccentIndigo,
                        unfocusedBorderColor = BentoCardBorder
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Batal", color = BentoTextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            val cap = tankCapacityStr.toDoubleOrNull()
                            if (name.isNotBlank() && cap != null && cap > 0.0) {
                                onSave(name.trim(), type, cap)
                            }
                        },
                        enabled = name.isNotBlank() && (tankCapacityStr.toDoubleOrNull() ?: 0.0) > 0.0,
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
    var showFuelTank by remember { mutableStateOf(state.visibility.showFuelTank) }
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
                            Checkbox(checked = showFuelTank, onCheckedChange = { showFuelTank = it }, colors = CheckboxDefaults.colors(checkedColor = BentoAccentIndigo, uncheckedColor = BentoTextSecondary))
                            Text("Monitoring Sisa Tangki BBM", color = BentoTextPrimary)
                        }
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
                        onClick = { onSave(com.example.data.DashboardVisibility(showOil, showGeneralService, showFuelTank), tires) },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentIndigo)
                    ) { Text("Simpan") }
                }
            }
        }
    }
}
