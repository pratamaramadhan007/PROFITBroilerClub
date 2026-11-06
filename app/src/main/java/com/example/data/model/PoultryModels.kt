package com.example.data.model

enum class Floor(val id: String, val displayName: String, val shortName: String) {
    L1("L1", "Lantai 1", "Lt 1"),
    L2("L2", "Lantai 2", "Lt 2"),
    ALL("ALL", "Gabungan (L1 + L2)", "All")
}

data class FeedItem(
    val id: String,
    val name: String,
    val code: String,
    val initialStockKg: Double,
    val currentStockKg: Double,
    val pricePerKg: Double,
    val packagingType: String = "Sak (50kg)",
    val supplier: String = "PT Malindo Feedmill",
    val colorHex: Long = 0xFF00F5FF
) {
    val currentStockSak: Double get() = currentStockKg / 50.0
    val initialStockSak: Double get() = initialStockKg / 50.0
    val percentageRemaining: Float
        get() = if (initialStockKg > 0) (currentStockKg / initialStockKg).toFloat().coerceIn(0f, 1f) else 0f
    val isOutOfStock: Boolean get() = currentStockKg <= 0.05
    val isLowStock: Boolean get() = percentageRemaining < 0.20f && !isOutOfStock
}

enum class OvkCategory(val label: String) {
    VAKSIN("Vaksin"),
    ANTIBIOTIK("Antibiotik"),
    VITAMIN("Vitamin & Suplemen"),
    KIMIA_DISINFEKTAN("Kimia & Disinfektan");

    val displayName: String get() = label
}

data class OvkItem(
    val id: String,
    val name: String,
    val category: OvkCategory,
    val initialStock: Double,
    val currentStock: Double,
    val unit: String,
    val pricePerUnit: Double
) {
    val isOutOfStock: Boolean get() = currentStock <= 0.01
}

data class OvkUsageEntry(
    val ovkId: String,
    val ovkName: String,
    val amount: Double,
    val unit: String,
    val cost: Double
)

data class DailyRecording(
    val id: String,
    val date: String,
    val dayNumber: Int,
    val floor: Floor,
    val mortalityCount: Int,
    val cullingCount: Int = 0,
    val feedTypeId: String,
    val feedTypeName: String,
    val feedAmountKg: Double,
    val sampleWeightGram: Double,
    val temperatureC: Double = 30.5,
    val humidityPercent: Double = 65.0,
    val ovkUsages: List<OvkUsageEntry> = emptyList(),
    val notes: String = ""
)

enum class ExpenseCategory(
    val displayName: String,
    val shortName: String,
    val description: String,
    val colorHex: Long
) {
    KELISTRIKAN(
        "Kelistrikan & Daya",
        "Kelistrikan",
        "Token PLN, Genset Blower & Solar",
        0xFF00F5FF
    ),
    ATAL_ALAS_KANDANG(
        "Atal & Alas Kandang",
        "Alas/Atal",
        "Sekam Padi, Serbuk Kayu & Kalsium",
        0xFFF59E0B
    ),
    PEMANAS(
        "Pemanas (Brooder / Heater)",
        "Pemanas",
        "Gas LPG 12kg/50kg, Batu Bara & Heater",
        0xFFFF3366
    ),
    TRANSPORTASI(
        "Transportasi & Logistik",
        "Transport",
        "Armada Pakan, DOC, Tol & Ekspedisi",
        0xFF38BDF8
    ),
    BONGKAR_MUAT(
        "Jasa Bongkar Muat",
        "Bongkar Muat",
        "Bongkar Pakan, DOC, Tebar Sekam & Tangkap",
        0xFFA855F7
    ),
    BIOSECURITY_SANITASI(
        "Biosecurity & Sanitasi",
        "Sanitasi",
        "Disinfektan, Kaporit, Cuci Kandang & Hama",
        0xFF00FF87
    ),
    TENAGA_KERJA(
        "Tenaga Kerja & Operator",
        "Operator",
        "Upah Anak Kandang & Bonus Panen",
        0xFF10B981
    ),
    MAINTENANCE_SPAREPART(
        "Perawatan & Sparepart",
        "Maintenance",
        "Nipple, Fan Belt, Dinamo & Celdek",
        0xFF6366F1
    ),
    ATK_ADMINISTRASI(
        "ATK & Administrasi",
        "ATK/Admin",
        "Buku Recording, Map & Perizinan",
        0xFF94A3B8
    ),
    LAINNYA(
        "Operasional Lainnya",
        "Lainnya",
        "Kebutuhan Operasional Tak Terduga",
        0xFF64748B
    )
}

data class OperationalExpense(
    val id: String,
    val category: ExpenseCategory,
    val amount: Double,
    val date: String,
    val description: String,
    val floorScope: Floor = Floor.ALL,
    val quantity: Double = 1.0,
    val unit: String = "Paket",
    val vendor: String = ""
)

data class StandardGrowthPoint(
    val day: Int,
    val standardWeightGram: Double,
    val standardFcr: Double
)

data class PeriodData(
    val id: String,
    val title: String,
    val startDate: String, // Tanggal Masuk DOC
    val docEntryTime: String = "02:30 WIB", // Waktu/Jam Masuk DOC
    val currentAgeDays: Int,
    val initialPopulationL1: Int, // Jumlah DOC Masuk Lantai 1
    val initialPopulationL2: Int, // Jumlah DOC Masuk Lantai 2
    val docPricePerBird: Double = 7500.0,
    val targetFcr: Double = 1.50,
    val targetWeightKg: Double = 1.95,
    val docStrain: String = "Cobb 500",
    val docHatchery: String = "PT Japfa Comfeed",
    val isCurrent: Boolean = true
) {
    val totalInitialPopulation: Int get() = initialPopulationL1 + initialPopulationL2
}

data class FloorMetricSummary(
    val floor: Floor,
    val initialPopulation: Int,
    val activePopulation: Int,
    val totalMortality: Int,
    val totalCulling: Int,
    val depletionRatePercent: Double,
    val livabilityPercent: Double,
    val totalFeedKg: Double,
    val latestAvgWeightKg: Double,
    val totalTonaseKg: Double,
    val fcr: Double,
    val performanceIndex: Double,
    val ipCategory: String,
    val ageDays: Int = 1
)

data class ArchivedPeriodRecord(
    val id: String,
    val title: String,
    val startDate: String,
    val closeDate: String,
    val harvestAgeDays: Int,
    val initialPopulation: Int,
    val harvestedPopulation: Int,
    val totalMortality: Int,
    val depletionRatePercent: Double,
    val livabilityPercent: Double,
    val avgWeightKg: Double,
    val totalTonaseKg: Double,
    val totalFeedUsedKg: Double,
    val fcr: Double,
    val performanceIndex: Double,
    val ipCategory: String,
    val totalProductionCost: Double,
    val hppPerKg: Double,
    val totalRevenue: Double,
    val netProfit: Double
)

data class ScaleEntry(
    val scaleNumber: Int,
    val birdCount: Int,
    val netWeightKg: Double,
    val grossWeightKg: Double = netWeightKg,
    val tareWeightKg: Double = 0.0
) {
    val avgWeightKg: Double get() = if (birdCount > 0) netWeightKg / birdCount else 0.0
}

data class HarvestTicket(
    val id: String,
    val ticketNumber: String,
    val date: String,
    val time: String,
    val dayName: String = "Hari ini",
    val farmerName: String = "Ika Ayu Wandira",
    val buyerName: String,
    val driverName: String,
    val vehiclePlate: String,
    val floor: Floor = Floor.ALL,
    val pricePerKg: Double = 21500.0,
    val entries: List<ScaleEntry> = emptyList(),
    val notes: String = "",
    val status: String = "Selesai" // "Proses" or "Selesai"
) {
    val totalBirdCount: Int get() = entries.sumOf { it.birdCount }
    val totalWeightKg: Double get() = entries.sumOf { it.netWeightKg }
    val overallAvgWeightKg: Double get() = if (totalBirdCount > 0) totalWeightKg / totalBirdCount else 0.0
    val totalTonaseTon: Double get() = totalWeightKg / 1000.0
    val totalAmount: Double get() = totalWeightKg * pricePerKg

    fun generateWhatsAppText(): String {
        val sb = StringBuilder()
        sb.append("📋 *NOTA TIMBANGAN PANEN AYAM BROILER*\n")
        sb.append("🏢 *PROFIT BROILER CLUB*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🎫 *No Tiket*     : $ticketNumber\n")
        sb.append("📅 *Hari / Tgl*   : $dayName, $date\n")
        sb.append("⏰ *Waktu*        : $time\n")
        sb.append("👨‍🌾 *Nama Peternak*: $farmerName\n")
        sb.append("🏪 *Nama Pembeli* : $buyerName\n")
        sb.append("🚛 *Nama Driver*  : $driverName\n")
        sb.append("🔢 *No Plat Kend* : $vehiclePlate\n")
        sb.append("🏢 *Asal Kandang* : ${floor.displayName}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("⚖️ *RINCIAN PENIMBANGAN ($totalBirdCount Ekor / ${entries.size} Kali Timbang):*\n")
        entries.forEach { entry ->
            val avg = "%.2f".format(entry.avgWeightKg)
            val net = "%.1f".format(entry.netWeightKg)
            sb.append("• Timb #${entry.scaleNumber}: ${entry.birdCount} ekor = ${net} kg (rata² ${avg} kg)\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📊 *RINGKASAN REKAP TOTAL:* \n")
        sb.append("🐔 *Jumlah Ekor*  : $totalBirdCount Ekor\n")
        sb.append("⚖️ *Jumlah Tonase*: ${"%.2f".format(totalWeightKg)} Kg (${"%.3f".format(totalTonaseTon)} Ton)\n")
        sb.append("⚖️ *Bobot Rata²*  : ${"%.2f".format(overallAvgWeightKg)} Kg / ekor (${(overallAvgWeightKg * 1000).toInt()} gram)\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        if (notes.isNotBlank()) {
            sb.append("📝 *Catatan Tambahan*: $notes\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
        sb.append("✅ *Hasil timbangan valid & tersinkronisasi otomatis.*\n")
        sb.append("Terima kasih atas kerjasamanya!")
        return sb.toString()
    }
}

