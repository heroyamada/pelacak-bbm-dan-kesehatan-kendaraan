# Aturan Pengembangan & Eksekusi Otonom (Autonomous Execution)

1. **Eksekusi Mandiri & Tuntas (Autonomous Execution)**:
   - Lakukan analisis, modifikasi kode, penyesuaian file, hingga commit dan push secara langsung dan tuntas tanpa berhenti meminta konfirmasi atau persetujuan langkah per langkah.
   - Hindari pertanyaan trivial atau prompt konfirmasi yang memperlambat alur kerja user.

2. **Standar Kualitas & Bebas Error (Zero Compilation Error)**:
   - Periksa integritas sintaks, kesesuaian parameter Compose/Kotlin, dan kompatibilitas tipe data secara teliti sebelum melakukan commit.
   - Pastikan tidak ada unresolved reference, import yang terlewat, atau ketidakcocokan tipe antara model (`VehicleModels.kt`), penyimpanan (`VehicleStorage.kt`), dan UI (`VehicleAppUI.kt`).

3. **Otomatisasi Kompilasi & Verifikasi APK**:
   - Setelah kode selesai dan rapi, commit dan push ke GitHub secara otomatis.
   - Pantau status workflow GitHub Actions hingga proses build selesai dengan status `success` dan file APK siap diunduh.
