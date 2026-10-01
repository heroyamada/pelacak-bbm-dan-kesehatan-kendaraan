package com.example.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class VehicleStorage(context: Context) {
    private val fileName = "vehicle_profiles_v1.json"
    private val file = File(context.filesDir, fileName)

    fun saveProfiles(profiles: List<VehicleProfile>) {
        try {
            val profilesArray = JSONArray()
            for (profile in profiles) {
                val pJson = JSONObject()
                pJson.put("id", profile.id)
                pJson.put("name", profile.name)
                pJson.put("type", profile.type)

                val state = profile.state
                val sJson = JSONObject()
                sJson.put("oilBrand", state.oilBrand)
                sJson.put("oilLastChangeOdo", state.oilLastChangeOdo)
                sJson.put("oilIntervalKm", state.oilIntervalKm)
                sJson.put("tireBrand", state.tireBrand)
                sJson.put("tireCompound", state.tireCompound)
                sJson.put("tireInstallOdo", state.tireInstallOdo)
                sJson.put("tireIntervalKm", state.tireIntervalKm)
                sJson.put("generalServiceLastOdo", state.generalServiceLastOdo)
                sJson.put("generalServiceIntervalKm", state.generalServiceIntervalKm)
                sJson.put("currentOdometer", state.currentOdometer)

                val fuelArray = JSONArray()
                for (log in state.fuelLogs) {
                    val fJson = JSONObject()
                    fJson.put("id", log.id)
                    fJson.put("date", log.date)
                    fJson.put("odometer", log.odometer)
                    fJson.put("liters", log.liters)
                    fJson.put("pricePerLiter", log.pricePerLiter)
                    fJson.put("fuelType", log.fuelType)
                    fuelArray.put(fJson)
                }
                sJson.put("fuelLogs", fuelArray)

                val serviceArray = JSONArray()
                for (log in state.serviceLogs) {
                    val sJson = JSONObject()
                    sJson.put("id", log.id)
                    sJson.put("date", log.date)
                    sJson.put("type", log.type)
                    sJson.put("odometer", log.odometer)
                    sJson.put("cost", log.cost)
                    sJson.put("notes", log.notes)
                    serviceArray.put(sJson)
                }
                sJson.put("serviceLogs", serviceArray)

                pJson.put("state", sJson)
                profilesArray.put(pJson)
            }

            file.writeText(profilesArray.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadProfiles(): List<VehicleProfile> {
        if (!file.exists()) {
            return listOf(VehicleProfile(name = "Kendaraan Utamaku", type = "Roda 2"))
        }
        return try {
            val content = file.readText()
            val profilesArray = JSONArray(content)
            val result = mutableListOf<VehicleProfile>()

            for (p in 0 until profilesArray.length()) {
                val pJson = profilesArray.getJSONObject(p)
                val id = pJson.optString("id", UUID.randomUUID().toString())
                val name = pJson.optString("name", "Kendaraan")
                val type = pJson.optString("type", "Roda 2")
                
                var state = VehicleState()
                if (pJson.has("state")) {
                    val json = pJson.getJSONObject("state")
                    val oilBrand = json.optString("oilBrand", "Pertamina Fastron")
                    val oilLastChangeOdo = json.optDouble("oilLastChangeOdo", 0.0)
                    val oilIntervalKm = json.optDouble("oilIntervalKm", 5000.0)
                    val tireBrand = json.optString("tireBrand", "Bridgestone")
                    val tireCompound = json.optString("tireCompound", "Medium")
                    val tireInstallOdo = json.optDouble("tireInstallOdo", 0.0)
                    val tireIntervalKm = json.optDouble("tireIntervalKm", 40000.0)
                    val generalServiceLastOdo = json.optDouble("generalServiceLastOdo", 0.0)
                    val generalServiceIntervalKm = json.optDouble("generalServiceIntervalKm", 10000.0)
                    val currentOdometer = json.optDouble("currentOdometer", 0.0)

                    val fuelLogs = mutableListOf<FuelLog>()
                    if (json.has("fuelLogs")) {
                        val fuelArray = json.getJSONArray("fuelLogs")
                        for (i in 0 until fuelArray.length()) {
                            val fJson = fuelArray.getJSONObject(i)
                            fuelLogs.add(
                                FuelLog(
                                    id = fJson.optString("id", UUID.randomUUID().toString()),
                                    date = fJson.optString("date", "2026-07-21"),
                                    odometer = fJson.optDouble("odometer", 0.0),
                                    liters = fJson.optDouble("liters", 0.0),
                                    pricePerLiter = fJson.optDouble("pricePerLiter", 0.0),
                                    fuelType = fJson.optString("fuelType", "Pertalite")
                                )
                            )
                        }
                    }

                    val serviceLogs = mutableListOf<ServiceLog>()
                    if (json.has("serviceLogs")) {
                        val serviceArray = json.getJSONArray("serviceLogs")
                        for (i in 0 until serviceArray.length()) {
                            val sJson = serviceArray.getJSONObject(i)
                            serviceLogs.add(
                                ServiceLog(
                                    id = sJson.optString("id", UUID.randomUUID().toString()),
                                    date = sJson.optString("date", "2026-07-21"),
                                    type = sJson.optString("type", "Ganti Oli"),
                                    odometer = sJson.optDouble("odometer", 0.0),
                                    cost = sJson.optDouble("cost", 0.0),
                                    notes = sJson.optString("notes", "")
                                )
                            )
                        }
                    }

                    val sortedFuelLogs = fuelLogs.sortedBy { it.odometer }
                    val sortedServiceLogs = serviceLogs.sortedByDescending { it.date }

                    state = VehicleState(
                        fuelLogs = sortedFuelLogs,
                        serviceLogs = sortedServiceLogs,
                        oilBrand = oilBrand,
                        oilLastChangeOdo = oilLastChangeOdo,
                        oilIntervalKm = oilIntervalKm,
                        tireBrand = tireBrand,
                        tireCompound = tireCompound,
                        tireInstallOdo = tireInstallOdo,
                        tireIntervalKm = tireIntervalKm,
                        generalServiceLastOdo = generalServiceLastOdo,
                        generalServiceIntervalKm = generalServiceIntervalKm,
                        currentOdometer = currentOdometer
                    )
                }
                
                result.add(VehicleProfile(id = id, name = name, type = type, state = state))
            }
            if (result.isEmpty()) listOf(VehicleProfile(name = "Kendaraan Utamaku", type = "Roda 2")) else result
        } catch (e: Exception) {
            e.printStackTrace()
            listOf(VehicleProfile(name = "Kendaraan Utamaku", type = "Roda 2"))
        }
    }
}
