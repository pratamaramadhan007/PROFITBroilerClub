package com.example.data.mock

import com.example.data.model.*

object MockDataProvider {

    val periodCurrent = PeriodData(
        id = "P14",
        title = "Periode 14 (Aktif - Panen Dekat)",
        startDate = "07 Okt 2026",
        docEntryTime = "02:30 WIB",
        currentAgeDays = 28,
        initialPopulationL1 = 10000,
        initialPopulationL2 = 10000,
        docPricePerBird = 7800.0,
        targetFcr = 1.48,
        targetWeightKg = 1.90,
        docStrain = "Cobb 500",
        docHatchery = "PT Japfa Comfeed",
        isCurrent = true
    )

    val initialArchivedPeriods = listOf(
        ArchivedPeriodRecord(
            id = "P13",
            title = "Periode 13 (Panen Sebelumnya)",
            startDate = "15 Agu 2026",
            closeDate = "18 Sep 2026",
            harvestAgeDays = 34,
            initialPopulation = 20000,
            harvestedPopulation = 19240,
            totalMortality = 760,
            depletionRatePercent = 3.80,
            livabilityPercent = 96.20,
            avgWeightKg = 1.88,
            totalTonaseKg = 36171.2,
            totalFeedUsedKg = 56427.0,
            fcr = 1.56,
            performanceIndex = 341.0,
            ipCategory = "Baik",
            totalProductionCost = 622144640.0,
            hppPerKg = 17200.0,
            totalRevenue = 690544640.0,
            netProfit = 68400000.0
        ),
        ArchivedPeriodRecord(
            id = "P12",
            title = "Periode 12 (Panen Lalu)",
            startDate = "20 Jun 2026",
            closeDate = "25 Jul 2026",
            harvestAgeDays = 35,
            initialPopulation = 20000,
            harvestedPopulation = 19100,
            totalMortality = 900,
            depletionRatePercent = 4.50,
            livabilityPercent = 95.50,
            avgWeightKg = 1.85,
            totalTonaseKg = 35335.0,
            totalFeedUsedKg = 57242.0,
            fcr = 1.62,
            performanceIndex = 312.0,
            ipCategory = "Sedang",
            totalProductionCost = 618362500.0,
            hppPerKg = 17500.0,
            totalRevenue = 671365000.0,
            netProfit = 53002500.0
        )
    )

    val periodPrevious = initialArchivedPeriods.first()

    val initialFeeds = listOf(
        FeedItem(
            id = "feed_1",
            name = "BR-1 Pre-Starter Crumble",
            code = "BR-1",
            initialStockKg = 4000.0, // 80 sak
            currentStockKg = 650.0, // Used mostly in brooding
            pricePerKg = 9800.0,
            packagingType = "Sak (50kg)",
            supplier = "PT Charoen Pokphand",
            colorHex = 0xFF00F5FF
        ),
        FeedItem(
            id = "feed_2",
            name = "BR-2 Starter Pellet",
            code = "BR-2",
            initialStockKg = 18000.0, // 360 sak
            currentStockKg = 2400.0,
            pricePerKg = 9200.0,
            packagingType = "Sak (50kg)",
            supplier = "PT Japfa Comfeed",
            colorHex = 0xFF00FF87
        ),
        FeedItem(
            id = "feed_3",
            name = "BR-3 Finisher Pellet High-Pro",
            code = "BR-3",
            initialStockKg = 16000.0, // 320 sak
            currentStockKg = 9850.0,
            pricePerKg = 8900.0,
            packagingType = "Sak (50kg)",
            supplier = "PT Japfa Comfeed",
            colorHex = 0xFF38BDF8
        ),
        FeedItem(
            id = "feed_4",
            name = "Konsentrat Broiler Mix",
            code = "K-MIX",
            initialStockKg = 500.0, // 10 sak
            currentStockKg = 0.0, // Emulate out-of-stock for testing guard logic!
            pricePerKg = 11500.0,
            packagingType = "Sak (50kg)",
            supplier = "PT Malindo",
            colorHex = 0xFFFF3366
        )
    )

    val initialOvks = listOf(
        OvkItem(
            id = "ovk_1",
            name = "Vita Stress Multivitamin",
            category = OvkCategory.VITAMIN,
            initialStock = 30.0,
            currentStock = 14.0,
            unit = "Sachet (100g)",
            pricePerUnit = 35000.0
        ),
        OvkItem(
            id = "ovk_2",
            name = "Medivac ND-IB Clone",
            category = OvkCategory.VAKSIN,
            initialStock = 20.0,
            currentStock = 8.0,
            unit = "Vial (1000 Dosis)",
            pricePerUnit = 72000.0
        ),
        OvkItem(
            id = "ovk_3",
            name = "Medivac Gumboro A",
            category = OvkCategory.VAKSIN,
            initialStock = 20.0,
            currentStock = 6.0,
            unit = "Vial (1000 Dosis)",
            pricePerUnit = 85000.0
        ),
        OvkItem(
            id = "ovk_4",
            name = "Enrofloxacin 10% Soluble",
            category = OvkCategory.ANTIBIOTIK,
            initialStock = 12.0,
            currentStock = 5.0,
            unit = "Botol (1 Liter)",
            pricePerUnit = 145000.0
        ),
        OvkItem(
            id = "ovk_5",
            name = "Antisep Disinfektan Kandang",
            category = OvkCategory.KIMIA_DISINFEKTAN,
            initialStock = 10.0,
            currentStock = 4.0,
            unit = "Jerigen (5 Liter)",
            pricePerUnit = 135000.0
        )
    )

    val initialExpenses = listOf(
        OperationalExpense("exp_1", ExpenseCategory.PEMANAS, 4200000.0, "08 Okt 2026", "Gas LPG 50kg Brooding Masa Awal (L1 & L2)", Floor.ALL, 8.0, "Tabung 50kg", "Agen Gas Pertamina"),
        OperationalExpense("exp_2", ExpenseCategory.ATAL_ALAS_KANDANG, 3600000.0, "05 Okt 2026", "Sekam Padi (Atal) Alas Tebal 2 Lantai", Floor.ALL, 3.0, "Truk Tronton", "UD Sekam Berkah"),
        OperationalExpense("exp_3", ExpenseCategory.ATAL_ALAS_KANDANG, 450000.0, "06 Okt 2026", "Kapur Tohor & Kalsium Tabur Sterilisasi Lantai", Floor.ALL, 15.0, "Sak (25kg)", "Toko Pertanian"),
        OperationalExpense("exp_4", ExpenseCategory.KELISTRIKAN, 2850000.0, "20 Okt 2026", "Token Listrik PLN Exhaust Fan, Blower & Inverter", Floor.ALL, 1.0, "Bulan", "PLN Pascabayar"),
        OperationalExpense("exp_5", ExpenseCategory.KELISTRIKAN, 1800000.0, "22 Okt 2026", "Solar Industri Genset Otomatis Cadangan", Floor.ALL, 120.0, "Liter", "SPBU"),
        OperationalExpense("exp_6", ExpenseCategory.BONGKAR_MUAT, 1200000.0, "07 Okt 2026", "Jasa Bongkar Pakan Masuk 800 Sak (@Rp 1.500)", Floor.ALL, 800.0, "Sak", "Tenaga Bongkar"),
        OperationalExpense("exp_7", ExpenseCategory.BONGKAR_MUAT, 500000.0, "07 Okt 2026", "Jasa Bongkar & Tebar Box DOC 20.000 Ekor", Floor.ALL, 200.0, "Box", "Tenaga Muat"),
        OperationalExpense("exp_8", ExpenseCategory.BONGKAR_MUAT, 700000.0, "06 Okt 2026", "Jasa Angkut & Tebar Atal Sekam ke Lantai 1 & 2", Floor.ALL, 2.0, "Lantai", "Pekerja Lepas"),
        OperationalExpense("exp_9", ExpenseCategory.TRANSPORTASI, 1250000.0, "07 Okt 2026", "Sewa Armada Truk Angkut DOC Ekspedisi Khusus", Floor.ALL, 1.0, "Trip", "Ekspedisi Unggas"),
        OperationalExpense("exp_10", ExpenseCategory.TRANSPORTASI, 450000.0, "15 Okt 2026", "BBM & Biaya Tol Pengambilan Tambahan Suplemen", Floor.ALL, 1.0, "Ritase", "Armada Operasional"),
        OperationalExpense("exp_11", ExpenseCategory.BIOSECURITY_SANITASI, 850000.0, "05 Okt 2026", "Desinfektan Footbath & Sprayer Sterilisasi Kandang", Floor.ALL, 5.0, "Jerigen (5L)", "Poultry Shop"),
        OperationalExpense("exp_12", ExpenseCategory.TENAGA_KERJA, 4500000.0, "25 Okt 2026", "Upah Petugas Operator Closed House (Termin 1)", Floor.ALL, 2.0, "Orang", "Tim Kandang"),
        OperationalExpense("exp_13", ExpenseCategory.MAINTENANCE_SPAREPART, 350000.0, "18 Okt 2026", "Perbaikan Nipple Drinker & Penggantian Karet Valve", Floor.L1, 20.0, "Pcs", "Sparepart Farm"),
        OperationalExpense("exp_14", ExpenseCategory.ATK_ADMINISTRASI, 250000.0, "07 Okt 2026", "Buku Recording Harian, Spidol & Map Arsip", Floor.ALL, 1.0, "Paket", "Toko ATK")
    )

    val standardGrowth = listOf(
        StandardGrowthPoint(1, 42.0, 0.85),
        StandardGrowthPoint(3, 78.0, 0.90),
        StandardGrowthPoint(7, 190.0, 0.98),
        StandardGrowthPoint(10, 310.0, 1.05),
        StandardGrowthPoint(14, 500.0, 1.15),
        StandardGrowthPoint(17, 720.0, 1.23),
        StandardGrowthPoint(21, 1010.0, 1.32),
        StandardGrowthPoint(24, 1340.0, 1.40),
        StandardGrowthPoint(28, 1750.0, 1.49),
        StandardGrowthPoint(32, 2150.0, 1.58),
        StandardGrowthPoint(35, 2450.0, 1.65)
    )

    fun generateInitialDailyRecordings(): List<DailyRecording> {
        val records = mutableListOf<DailyRecording>()

        // Generate day 1 to day 28 for Lantai 1 and Lantai 2
        for (day in 1..28) {
            val dateStr = "${day + 6} Okt 2026"

            // Feed type: BR-1 (Day 1-10), BR-2 (Day 11-21), BR-3 (Day 22-28)
            val feedId = when {
                day <= 10 -> "feed_1"
                day <= 21 -> "feed_2"
                else -> "feed_3"
            }
            val feedName = when {
                day <= 10 -> "BR-1 Pre-Starter Crumble"
                day <= 21 -> "BR-2 Starter Pellet"
                else -> "BR-3 Finisher Pellet High-Pro"
            }

            // Standard daily consumption per bird in grams
            val gramPerBirdL1 = (18.0 + day * 4.6).coerceAtMost(165.0)
            val gramPerBirdL2 = (17.8 + day * 4.5).coerceAtMost(162.0)

            // Approximate floor populations at that time
            val aliveBirdsL1 = (10000 - day * 9).coerceAtLeast(9600)
            val aliveBirdsL2 = (10000 - day * 11).coerceAtLeast(9500)

            val feedKgL1 = Math.round((aliveBirdsL1 * gramPerBirdL1 / 1000.0) * 10.0) / 10.0
            val feedKgL2 = Math.round((aliveBirdsL2 * gramPerBirdL2 / 1000.0) * 10.0) / 10.0

            // Mortality
            val mortL1 = when (day) {
                1 -> 18
                2 -> 14
                3 -> 12
                7 -> 8
                14 -> 6
                21 -> 7
                28 -> 9
                else -> (4..8).random()
            }
            val mortL2 = when (day) {
                1 -> 22
                2 -> 16
                3 -> 15
                7 -> 10
                14 -> 9
                21 -> 11
                28 -> 12
                else -> (6..10).random()
            }

            // Sample weight
            val weightL1 = (42.0 + (day * day * 2.15)).coerceAtMost(1820.0)
            val weightL2 = (42.0 + (day * day * 2.08)).coerceAtMost(1760.0)

            // Lantai 1 recording
            records.add(
                DailyRecording(
                    id = "rec_L1_D$day",
                    date = dateStr,
                    dayNumber = day,
                    floor = Floor.L1,
                    mortalityCount = mortL1,
                    cullingCount = if (day % 7 == 0) 2 else 0,
                    feedTypeId = feedId,
                    feedTypeName = feedName,
                    feedAmountKg = feedKgL1,
                    sampleWeightGram = Math.round(weightL1 * 10.0) / 10.0,
                    temperatureC = (32.5 - (day * 0.2)).coerceAtLeast(26.5),
                    humidityPercent = 62.0 + (day % 5),
                    notes = if (day == 1) "DOC Masuk kondisi sehat, lincah" else if (day == 7) "Vaksin ND-IB tetes mata" else ""
                )
            )

            // Lantai 2 recording
            records.add(
                DailyRecording(
                    id = "rec_L2_D$day",
                    date = dateStr,
                    dayNumber = day,
                    floor = Floor.L2,
                    mortalityCount = mortL2,
                    cullingCount = if (day % 7 == 0) 3 else 0,
                    feedTypeId = feedId,
                    feedTypeName = feedName,
                    feedAmountKg = feedKgL2,
                    sampleWeightGram = Math.round(weightL2 * 10.0) / 10.0,
                    temperatureC = (32.8 - (day * 0.2)).coerceAtLeast(26.8),
                    humidityPercent = 64.0 + (day % 5),
                    notes = if (day == 14) "Vaksin Gumboro air minum" else ""
                )
            )
        }

        return records
    }

    val initialHarvestTickets = listOf(
        HarvestTicket(
            id = "tkt_1",
            ticketNumber = "TKT-P14-001",
            date = "04 Nov 2026",
            time = "08:30 WIB",
            dayName = "Rabu",
            farmerName = "Ika Ayu Wandira",
            buyerName = "H. Ahmad (CV Berkah Unggas)",
            driverName = "Sugeng Waluyo",
            vehiclePlate = "B 9481 UYT",
            floor = Floor.L1,
            pricePerKg = 21500.0,
            status = "Selesai",
            notes = "Panen sekat depan Lantai 1, ayam sehat padat",
            entries = listOf(
                ScaleEntry(1, 15, 31.5, 36.5, 5.0),
                ScaleEntry(2, 16, 33.6, 38.6, 5.0),
                ScaleEntry(3, 15, 31.8, 36.8, 5.0),
                ScaleEntry(4, 15, 31.2, 36.2, 5.0),
                ScaleEntry(5, 16, 33.9, 38.9, 5.0),
                ScaleEntry(6, 15, 31.5, 36.5, 5.0),
                ScaleEntry(7, 16, 34.1, 39.1, 5.0),
                ScaleEntry(8, 15, 31.7, 36.7, 5.0),
                ScaleEntry(9, 16, 33.4, 38.4, 5.0),
                ScaleEntry(10, 15, 31.5, 36.5, 5.0)
            )
        ),
        HarvestTicket(
            id = "tkt_2",
            ticketNumber = "TKT-P14-002",
            date = "04 Nov 2026",
            time = "14:15 WIB",
            dayName = "Rabu",
            farmerName = "Ika Ayu Wandira",
            buyerName = "PT Sumber Rejeki Broiler",
            driverName = "Mulyono",
            vehiclePlate = "AD 8214 CP",
            floor = Floor.L2,
            pricePerKg = 21500.0,
            status = "Selesai",
            notes = "Panen sekat tengah Lantai 2",
            entries = listOf(
                ScaleEntry(1, 16, 33.2, 38.2, 5.0),
                ScaleEntry(2, 15, 31.4, 36.4, 5.0),
                ScaleEntry(3, 16, 33.8, 38.8, 5.0),
                ScaleEntry(4, 15, 31.2, 36.2, 5.0),
                ScaleEntry(5, 16, 33.5, 38.5, 5.0),
                ScaleEntry(6, 15, 31.8, 36.8, 5.0),
                ScaleEntry(7, 16, 33.6, 38.6, 5.0),
                ScaleEntry(8, 15, 31.3, 36.3, 5.0)
            )
        )
    )
}

