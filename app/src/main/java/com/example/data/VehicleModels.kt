package com.example.data

import java.util.UUID

data class FuelLog(
    val id: String = UUID.randomUUID().toString(),
    val date: String,             // Format: YYYY-MM-DD
    val odometer: Double,          // km
    val liters: Double,            // L
    val pricePerLiter: Double,     // Rp per liter
    val fuelType: String          // e.g. "Pertalite", "Pertamax", "Solar", "Pertamax Turbo"
) {
    val totalCost: Double
        get() = liters * pricePerLiter
}

data class ServiceLog(
    val id: String = UUID.randomUUID().toString(),
    val date: String,             // Format: YYYY-MM-DD
    val type: String,             // e.g., "Ganti Oli", "Ganti Ban", "Servis Umum", "Lainnya"
    val odometer: Double,          // km
    val cost: Double,             // Rp
    val notes: String             // Detail service
)

data class VehicleState(
    val fuelLogs: List<FuelLog> = emptyList(),
    val serviceLogs: List<ServiceLog> = emptyList(),
    val oilBrand: String = "Pertamina Fastron",
    val oilLastChangeOdo: Double = 0.0,
    val oilIntervalKm: Double = 5000.0,
    val tireBrand: String = "Bridgestone",
    val tireCompound: String = "Medium", // "Soft", "Medium", "Hard", "Eco"
    val tireInstallOdo: Double = 0.0,
    val tireIntervalKm: Double = 40000.0,
    val generalServiceLastOdo: Double = 0.0,
    val generalServiceIntervalKm: Double = 10000.0,
    val currentOdometer: Double = 0.0
)

data class VehicleProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "Roda 2" or "Roda 4"
    val state: VehicleState = VehicleState()
)
