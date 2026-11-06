package com.example.data.storage

import android.content.Context
import com.example.data.mock.MockDataProvider
import com.example.data.model.*
import com.example.ui.viewmodel.BroilerUiState
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Manajer penyimpanan persisten lokal untuk PROFIT Broiler Club.
 * Menyimpan seluruh data kandang (DOC masuk, recording harian, inventaris, panen, biaya,
 * arsip periode, dan autentikasi) ke berkas JSON internal di context.filesDir.
 */
object BroilerStorageManager {

    private const val FILE_NAME = "broiler_club_persisted_data.json"

    fun saveState(context: Context, state: BroilerUiState) {
        try {
            val root = JSONObject()

            // 1. Current Period
            val curPeriodObj = JSONObject().apply {
                put("id", state.currentPeriod.id)
                put("title", state.currentPeriod.title)
                put("startDate", state.currentPeriod.startDate)
                put("docEntryTime", state.currentPeriod.docEntryTime)
                put("currentAgeDays", state.currentPeriod.currentAgeDays)
                put("initialPopulationL1", state.currentPeriod.initialPopulationL1)
                put("initialPopulationL2", state.currentPeriod.initialPopulationL2)
                put("docPricePerBird", state.currentPeriod.docPricePerBird)
                put("targetFcr", state.currentPeriod.targetFcr)
                put("targetWeightKg", state.currentPeriod.targetWeightKg)
                put("docStrain", state.currentPeriod.docStrain)
                put("docHatchery", state.currentPeriod.docHatchery)
                put("isCurrent", state.currentPeriod.isCurrent)
            }
            root.put("currentPeriod", curPeriodObj)

            // 2. Previous Period
            root.put("previousPeriod", archivedPeriodToJson(state.previousPeriod))

            // 3. Archived Periods
            val archArray = JSONArray()
            state.archivedPeriods.forEach { arch ->
                archArray.put(archivedPeriodToJson(arch))
            }
            root.put("archivedPeriods", archArray)
            root.put("selectedComparisonPeriodId", state.selectedComparisonPeriodId)

            // 4. Feeds
            val feedArray = JSONArray()
            state.feeds.forEach { f ->
                val fObj = JSONObject().apply {
                    put("id", f.id)
                    put("name", f.name)
                    put("code", f.code)
                    put("initialStockKg", f.initialStockKg)
                    put("currentStockKg", f.currentStockKg)
                    put("pricePerKg", f.pricePerKg)
                    put("packagingType", f.packagingType)
                    put("supplier", f.supplier)
                    put("colorHex", f.colorHex)
                }
                feedArray.put(fObj)
            }
            root.put("feeds", feedArray)

            // 5. OVKs
            val ovkArray = JSONArray()
            state.ovks.forEach { o ->
                val oObj = JSONObject().apply {
                    put("id", o.id)
                    put("name", o.name)
                    put("category", o.category.name)
                    put("initialStock", o.initialStock)
                    put("currentStock", o.currentStock)
                    put("unit", o.unit)
                    put("pricePerUnit", o.pricePerUnit)
                }
                ovkArray.put(oObj)
            }
            root.put("ovks", ovkArray)

            // 6. Daily Recordings
            val recArray = JSONArray()
            state.dailyRecordings.forEach { r ->
                val rObj = JSONObject().apply {
                    put("id", r.id)
                    put("date", r.date)
                    put("dayNumber", r.dayNumber)
                    put("floor", r.floor.name)
                    put("mortalityCount", r.mortalityCount)
                    put("cullingCount", r.cullingCount)
                    put("feedTypeId", r.feedTypeId)
                    put("feedTypeName", r.feedTypeName)
                    put("feedAmountKg", r.feedAmountKg)
                    put("sampleWeightGram", r.sampleWeightGram)
                    put("temperatureC", r.temperatureC)
                    put("humidityPercent", r.humidityPercent)
                    put("notes", r.notes)

                    val usagesArr = JSONArray()
                    r.ovkUsages.forEach { u ->
                        val uObj = JSONObject().apply {
                            put("ovkId", u.ovkId)
                            put("ovkName", u.ovkName)
                            put("amount", u.amount)
                            put("unit", u.unit)
                            put("cost", u.cost)
                        }
                        usagesArr.put(uObj)
                    }
                    put("ovkUsages", usagesArr)
                }
                recArray.put(rObj)
            }
            root.put("dailyRecordings", recArray)

            // 7. Operational Expenses
            val expArray = JSONArray()
            state.operationalExpenses.forEach { exp ->
                val eObj = JSONObject().apply {
                    put("id", exp.id)
                    put("category", exp.category.name)
                    put("amount", exp.amount)
                    put("date", exp.date)
                    put("description", exp.description)
                    put("floorScope", exp.floorScope.name)
                    put("quantity", exp.quantity)
                    put("unit", exp.unit)
                    put("vendor", exp.vendor)
                }
                expArray.put(eObj)
            }
            root.put("operationalExpenses", expArray)

            // 8. Harvest Tickets
            val tickArray = JSONArray()
            state.harvestTickets.forEach { t ->
                val tObj = JSONObject().apply {
                    put("id", t.id)
                    put("ticketNumber", t.ticketNumber)
                    put("date", t.date)
                    put("time", t.time)
                    put("dayName", t.dayName)
                    put("farmerName", t.farmerName)
                    put("buyerName", t.buyerName)
                    put("driverName", t.driverName)
                    put("vehiclePlate", t.vehiclePlate)
                    put("floor", t.floor.name)
                    put("pricePerKg", t.pricePerKg)
                    put("notes", t.notes)
                    put("status", t.status)

                    val entArr = JSONArray()
                    t.entries.forEach { e ->
                        val entObj = JSONObject().apply {
                            put("scaleNumber", e.scaleNumber)
                            put("birdCount", e.birdCount)
                            put("netWeightKg", e.netWeightKg)
                            put("grossWeightKg", e.grossWeightKg)
                            put("tareWeightKg", e.tareWeightKg)
                        }
                        entArr.put(entObj)
                    }
                    put("entries", entArr)
                }
                tickArray.put(tObj)
            }
            root.put("harvestTickets", tickArray)

            // 9. Auth & Farmer Settings
            // Selalu set isLoggedIn = false agar setiap kali aplikasi dibuka kembali wajib memasukkan user & password
            root.put("isLoggedIn", false)
            root.put("userId", state.userId)
            root.put("password", state.password)
            root.put("recoveryEmail", state.recoveryEmail)
            root.put("defaultFarmerName", state.defaultFarmerName)

            // Write atomically via temporary file
            val targetFile = File(context.filesDir, FILE_NAME)
            val tempFile = File(context.filesDir, "$FILE_NAME.tmp")
            tempFile.writeText(root.toString(2))
            if (tempFile.exists()) {
                tempFile.renameTo(targetFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSavedState(context: Context): BroilerUiState? {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return null

        return try {
            val content = file.readText()
            if (content.isBlank()) return null
            val root = JSONObject(content)

            // 1. Current Period
            val curPeriod = if (root.has("currentPeriod")) {
                val obj = root.getJSONObject("currentPeriod")
                PeriodData(
                    id = obj.optString("id", "P14"),
                    title = obj.optString("title", "Periode 14 (Aktif - Panen Dekat)"),
                    startDate = obj.optString("startDate", "07 Okt 2026"),
                    docEntryTime = obj.optString("docEntryTime", "02:30 WIB"),
                    currentAgeDays = obj.optInt("currentAgeDays", 28),
                    initialPopulationL1 = obj.optInt("initialPopulationL1", 10000),
                    initialPopulationL2 = obj.optInt("initialPopulationL2", 10000),
                    docPricePerBird = obj.optDouble("docPricePerBird", 7800.0),
                    targetFcr = obj.optDouble("targetFcr", 1.48),
                    targetWeightKg = obj.optDouble("targetWeightKg", 1.90),
                    docStrain = obj.optString("docStrain", "Cobb 500"),
                    docHatchery = obj.optString("docHatchery", "PT Japfa Comfeed"),
                    isCurrent = obj.optBoolean("isCurrent", true)
                )
            } else MockDataProvider.periodCurrent

            // 2. Previous Period
            val prevPeriod = if (root.has("previousPeriod")) {
                jsonToArchivedPeriod(root.getJSONObject("previousPeriod"))
            } else MockDataProvider.periodPrevious

            // 3. Archived Periods
            val archivedList = mutableListOf<ArchivedPeriodRecord>()
            if (root.has("archivedPeriods")) {
                val arr = root.getJSONArray("archivedPeriods")
                for (i in 0 until arr.length()) {
                    archivedList.add(jsonToArchivedPeriod(arr.getJSONObject(i)))
                }
            } else {
                archivedList.addAll(MockDataProvider.initialArchivedPeriods)
            }

            val selCompId = root.optString("selectedComparisonPeriodId", prevPeriod.id)

            // 4. Feeds
            val feedsList = mutableListOf<FeedItem>()
            if (root.has("feeds")) {
                val arr = root.getJSONArray("feeds")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    feedsList.add(
                        FeedItem(
                            id = obj.optString("id", "feed_$i"),
                            name = obj.optString("name", "Pakan"),
                            code = obj.optString("code", "P-$i"),
                            initialStockKg = obj.optDouble("initialStockKg", 4000.0),
                            currentStockKg = obj.optDouble("currentStockKg", 1000.0),
                            pricePerKg = obj.optDouble("pricePerKg", 9800.0),
                            packagingType = obj.optString("packagingType", "Sak (50kg)"),
                            supplier = obj.optString("supplier", "PT Malindo Feedmill"),
                            colorHex = obj.optLong("colorHex", 0xFF00F5FF)
                        )
                    )
                }
            } else {
                feedsList.addAll(MockDataProvider.initialFeeds)
            }

            // 5. OVKs
            val ovkList = mutableListOf<OvkItem>()
            if (root.has("ovks")) {
                val arr = root.getJSONArray("ovks")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val catName = obj.optString("category", OvkCategory.VITAMIN.name)
                    val category = try {
                        OvkCategory.valueOf(catName)
                    } catch (e: Exception) {
                        OvkCategory.VITAMIN
                    }
                    ovkList.add(
                        OvkItem(
                            id = obj.optString("id", "ovk_$i"),
                            name = obj.optString("name", "OVK"),
                            category = category,
                            initialStock = obj.optDouble("initialStock", 100.0),
                            currentStock = obj.optDouble("currentStock", 50.0),
                            unit = obj.optString("unit", "Botol"),
                            pricePerUnit = obj.optDouble("pricePerUnit", 45000.0)
                        )
                    )
                }
            } else {
                ovkList.addAll(MockDataProvider.initialOvks)
            }

            // 6. Daily Recordings
            val recList = mutableListOf<DailyRecording>()
            if (root.has("dailyRecordings")) {
                val arr = root.getJSONArray("dailyRecordings")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val floorName = obj.optString("floor", Floor.L1.name)
                    val floor = try {
                        Floor.valueOf(floorName)
                    } catch (e: Exception) {
                        Floor.L1
                    }

                    val usages = mutableListOf<OvkUsageEntry>()
                    if (obj.has("ovkUsages")) {
                        val uArr = obj.getJSONArray("ovkUsages")
                        for (j in 0 until uArr.length()) {
                            val uObj = uArr.getJSONObject(j)
                            usages.add(
                                OvkUsageEntry(
                                    ovkId = uObj.optString("ovkId", ""),
                                    ovkName = uObj.optString("ovkName", ""),
                                    amount = uObj.optDouble("amount", 0.0),
                                    unit = uObj.optString("unit", ""),
                                    cost = uObj.optDouble("cost", 0.0)
                                )
                            )
                        }
                    }

                    recList.add(
                        DailyRecording(
                            id = obj.optString("id", "rec_$i"),
                            date = obj.optString("date", "Hari ini"),
                            dayNumber = obj.optInt("dayNumber", 1),
                            floor = floor,
                            mortalityCount = obj.optInt("mortalityCount", 0),
                            cullingCount = obj.optInt("cullingCount", 0),
                            feedTypeId = obj.optString("feedTypeId", "feed_1"),
                            feedTypeName = obj.optString("feedTypeName", "BR-1"),
                            feedAmountKg = obj.optDouble("feedAmountKg", 0.0),
                            sampleWeightGram = obj.optDouble("sampleWeightGram", 0.0),
                            temperatureC = obj.optDouble("temperatureC", 30.5),
                            humidityPercent = obj.optDouble("humidityPercent", 65.0),
                            ovkUsages = usages,
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            } else {
                recList.addAll(MockDataProvider.generateInitialDailyRecordings())
            }

            // 7. Operational Expenses
            val expList = mutableListOf<OperationalExpense>()
            if (root.has("operationalExpenses")) {
                val arr = root.getJSONArray("operationalExpenses")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val catName = obj.optString("category", ExpenseCategory.LAINNYA.name)
                    val category = try {
                        ExpenseCategory.valueOf(catName)
                    } catch (e: Exception) {
                        ExpenseCategory.LAINNYA
                    }
                    val floorName = obj.optString("floorScope", Floor.ALL.name)
                    val floorScope = try {
                        Floor.valueOf(floorName)
                    } catch (e: Exception) {
                        Floor.ALL
                    }

                    expList.add(
                        OperationalExpense(
                            id = obj.optString("id", "exp_$i"),
                            category = category,
                            amount = obj.optDouble("amount", 0.0),
                            date = obj.optString("date", "Hari ini"),
                            description = obj.optString("description", ""),
                            floorScope = floorScope,
                            quantity = obj.optDouble("quantity", 1.0),
                            unit = obj.optString("unit", "Paket"),
                            vendor = obj.optString("vendor", "")
                        )
                    )
                }
            } else {
                expList.addAll(MockDataProvider.initialExpenses)
            }

            // 8. Harvest Tickets
            val ticketList = mutableListOf<HarvestTicket>()
            if (root.has("harvestTickets")) {
                val arr = root.getJSONArray("harvestTickets")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val floorName = obj.optString("floor", Floor.ALL.name)
                    val floor = try {
                        Floor.valueOf(floorName)
                    } catch (e: Exception) {
                        Floor.ALL
                    }

                    val entries = mutableListOf<ScaleEntry>()
                    if (obj.has("entries")) {
                        val eArr = obj.getJSONArray("entries")
                        for (j in 0 until eArr.length()) {
                            val eObj = eArr.getJSONObject(j)
                            entries.add(
                                ScaleEntry(
                                    scaleNumber = eObj.optInt("scaleNumber", j + 1),
                                    birdCount = eObj.optInt("birdCount", 0),
                                    netWeightKg = eObj.optDouble("netWeightKg", 0.0),
                                    grossWeightKg = eObj.optDouble("grossWeightKg", 0.0),
                                    tareWeightKg = eObj.optDouble("tareWeightKg", 0.0)
                                )
                            )
                        }
                    }

                    ticketList.add(
                        HarvestTicket(
                            id = obj.optString("id", "ticket_$i"),
                            ticketNumber = obj.optString("ticketNumber", "SJ-P14-0$i"),
                            date = obj.optString("date", "Hari ini"),
                            time = obj.optString("time", "03:00 WIB"),
                            dayName = obj.optString("dayName", "Hari ini"),
                            farmerName = obj.optString("farmerName", "Ika Ayu Wandira"),
                            buyerName = obj.optString("buyerName", ""),
                            driverName = obj.optString("driverName", ""),
                            vehiclePlate = obj.optString("vehiclePlate", ""),
                            floor = floor,
                            pricePerKg = obj.optDouble("pricePerKg", 21500.0),
                            entries = entries,
                            notes = obj.optString("notes", ""),
                            status = obj.optString("status", "Selesai")
                        )
                    )
                }
            } else {
                ticketList.addAll(MockDataProvider.initialHarvestTickets)
            }

            // 9. Auth & Farmer (Selalu minta login ulang saat aplikasi dibuka kembali)
            val isLoggedIn = false
            val userId = root.optString("userId", "ProfitBroiler")
            val password = root.optString("password", "BroilerClub123")
            val recoveryEmail = root.optString("recoveryEmail", "pratamaramadhan007@gmail.com")
            val farmerName = root.optString("defaultFarmerName", "Ika Ayu Wandira")

            BroilerUiState(
                currentPeriod = curPeriod,
                previousPeriod = prevPeriod,
                archivedPeriods = archivedList,
                selectedComparisonPeriodId = selCompId,
                feeds = feedsList,
                ovks = ovkList,
                dailyRecordings = recList,
                operationalExpenses = expList,
                harvestTickets = ticketList,
                isLoggedIn = isLoggedIn,
                userId = userId,
                password = password,
                recoveryEmail = recoveryEmail,
                defaultFarmerName = farmerName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun archivedPeriodToJson(arch: ArchivedPeriodRecord): JSONObject {
        return JSONObject().apply {
            put("id", arch.id)
            put("title", arch.title)
            put("startDate", arch.startDate)
            put("closeDate", arch.closeDate)
            put("harvestAgeDays", arch.harvestAgeDays)
            put("initialPopulation", arch.initialPopulation)
            put("harvestedPopulation", arch.harvestedPopulation)
            put("totalMortality", arch.totalMortality)
            put("depletionRatePercent", arch.depletionRatePercent)
            put("livabilityPercent", arch.livabilityPercent)
            put("avgWeightKg", arch.avgWeightKg)
            put("totalTonaseKg", arch.totalTonaseKg)
            put("totalFeedUsedKg", arch.totalFeedUsedKg)
            put("fcr", arch.fcr)
            put("performanceIndex", arch.performanceIndex)
            put("ipCategory", arch.ipCategory)
            put("totalProductionCost", arch.totalProductionCost)
            put("hppPerKg", arch.hppPerKg)
            put("totalRevenue", arch.totalRevenue)
            put("netProfit", arch.netProfit)
        }
    }

    private fun jsonToArchivedPeriod(obj: JSONObject): ArchivedPeriodRecord {
        return ArchivedPeriodRecord(
            id = obj.optString("id", "P00"),
            title = obj.optString("title", "Periode Arsip"),
            startDate = obj.optString("startDate", ""),
            closeDate = obj.optString("closeDate", ""),
            harvestAgeDays = obj.optInt("harvestAgeDays", 35),
            initialPopulation = obj.optInt("initialPopulation", 20000),
            harvestedPopulation = obj.optInt("harvestedPopulation", 19200),
            totalMortality = obj.optInt("totalMortality", 800),
            depletionRatePercent = obj.optDouble("depletionRatePercent", 4.0),
            livabilityPercent = obj.optDouble("livabilityPercent", 96.0),
            avgWeightKg = obj.optDouble("avgWeightKg", 1.85),
            totalTonaseKg = obj.optDouble("totalTonaseKg", 35520.0),
            totalFeedUsedKg = obj.optDouble("totalFeedUsedKg", 56832.0),
            fcr = obj.optDouble("fcr", 1.60),
            performanceIndex = obj.optDouble("performanceIndex", 330.0),
            ipCategory = obj.optString("ipCategory", "Baik"),
            totalProductionCost = obj.optDouble("totalProductionCost", 620000000.0),
            hppPerKg = obj.optDouble("hppPerKg", 17400.0),
            totalRevenue = obj.optDouble("totalRevenue", 690000000.0),
            netProfit = obj.optDouble("netProfit", 70000000.0)
        )
    }
}
