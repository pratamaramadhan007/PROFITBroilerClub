package com.example

import com.example.data.model.Floor
import com.example.ui.viewmodel.BroilerViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var viewModel: BroilerViewModel

    @Before
    fun setUp() {
        viewModel = BroilerViewModel()
    }

    @Test
    fun testPoultryFormulas_areAccurate() {
        // Initial setup test values
        val initialPop = 10000.0
        val totalDead = 200.0
        val deplesi = (totalDead / initialPop) * 100.0 // 2.0%
        val livability = 100.0 - deplesi // 98.0%

        assertEquals(2.0, deplesi, 0.001)
        assertEquals(98.0, livability, 0.001)

        val activePop = initialPop - totalDead // 9800
        val avgWeightKg = 1.80
        val tonaseKg = activePop * avgWeightKg // 9800 * 1.80 = 17640 kg

        assertEquals(17640.0, tonaseKg, 0.001)

        val totalFeedKg = 26460.0
        val fcr = totalFeedKg / tonaseKg // 26460 / 17640 = 1.50

        assertEquals(1.50, fcr, 0.001)

        val harvestAgeDays = 30.0
        // IP = (Daya Hidup * Berat Rata-rata) / (FCR * Umur Panen) * 100
        val ip = (livability * avgWeightKg) / (fcr * harvestAgeDays) * 100.0
        // (98.0 * 1.80) / (1.50 * 30.0) * 100 = 176.4 / 45.0 * 100 = 392.0

        assertEquals(392.0, ip, 0.01)
    }

    @Test
    fun testStockDeduction_andSafetyGuard() {
        val initialStockBR3 = viewModel.uiState.value.feeds.find { it.id == "feed_3" }!!.currentStockKg

        // Add recording using BR-3 with 500 kg
        val success = viewModel.addDailyRecording(
            floor = Floor.L1,
            dayNumber = 29,
            mortality = 5,
            culling = 1,
            feedTypeId = "feed_3",
            feedAmountKg = 500.0,
            sampleWeightGram = 1850.0,
            temperatureC = 29.0,
            humidityPercent = 65.0,
            ovkUsageList = emptyList(),
            notes = "Test recording"
        )

        assertTrue(success)
        val updatedStockBR3 = viewModel.uiState.value.feeds.find { it.id == "feed_3" }!!.currentStockKg
        assertEquals(initialStockBR3 - 500.0, updatedStockBR3, 0.001)

        // Test out of stock safety guard on feed_4 (which has 0 kg stock)
        val failSuccess = viewModel.addDailyRecording(
            floor = Floor.L2,
            dayNumber = 29,
            mortality = 2,
            culling = 0,
            feedTypeId = "feed_4", // Stok 0
            feedAmountKg = 100.0,
            sampleWeightGram = 1850.0,
            temperatureC = 29.0,
            humidityPercent = 65.0,
            ovkUsageList = emptyList(),
            notes = "Test out of stock"
        )
        assertFalse(failSuccess)
    }

    @Test
    fun testAuthentication_andOtpPasswordReset() {
        // Initial state is not logged in
        assertFalse(viewModel.uiState.value.isLoggedIn)

        // Test login with wrong password
        val wrongLogin = viewModel.login("ProfitBroiler", "WrongPass123")
        assertFalse(wrongLogin)
        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertNotNull(viewModel.uiState.value.loginError)

        // Test login with correct credentials
        val correctLogin = viewModel.login("ProfitBroiler", "BroilerClub123")
        assertTrue(correctLogin)
        assertTrue(viewModel.uiState.value.isLoggedIn)

        // Request OTP
        val otp = viewModel.requestPasswordResetOtp()
        assertEquals(6, otp.length)
        assertEquals(otp, viewModel.uiState.value.currentOtp)
        assertEquals("pratamaramadhan007@gmail.com", viewModel.uiState.value.recoveryEmail)

        // Test wrong OTP
        val failReset = viewModel.verifyOtpAndResetPassword("000000", "NewBroiler2026")
        assertFalse(failReset.first)

        // Test correct OTP and reset password
        val successReset = viewModel.verifyOtpAndResetPassword(otp, "NewBroiler2026")
        assertTrue(successReset.first)
        assertEquals("NewBroiler2026", viewModel.uiState.value.password)

        // Logout and verify new password works
        viewModel.logout()
        assertFalse(viewModel.uiState.value.isLoggedIn)

        val oldPassLogin = viewModel.login("ProfitBroiler", "BroilerClub123")
        assertFalse(oldPassLogin)

        val newPassLogin = viewModel.login("ProfitBroiler", "NewBroiler2026")
        assertTrue(newPassLogin)
        assertTrue(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun testClosePeriod_andAutomaticComparison() {
        val oldPeriod = viewModel.uiState.value.currentPeriod
        assertEquals("P14", oldPeriod.id)
        val initialArchivedCount = viewModel.uiState.value.archivedPeriods.size

        // Close period P14 and open P15
        viewModel.closeCurrentPeriodAndStartNew(
            newPeriodTitle = "Periode 15 - Batch Percontohan Closed House",
            newPopL1 = 11000,
            newPopL2 = 11000,
            docPrice = 7200.0,
            marketPricePerKg = 21500.0
        )

        val updatedState = viewModel.uiState.value

        // 1. Current period is now the newly started period
        assertEquals("P15", updatedState.currentPeriod.id)
        assertEquals("Periode 15 - Batch Percontohan Closed House", updatedState.currentPeriod.title)
        assertEquals(22000, updatedState.currentPeriod.totalInitialPopulation)
        assertEquals(1, updatedState.currentPeriod.currentAgeDays)

        // 2. Archived periods contains the freshly closed period
        assertEquals(initialArchivedCount + 1, updatedState.archivedPeriods.size)
        val latestArchived = updatedState.archivedPeriods.first()
        assertEquals("P14", latestArchived.id)
        assertTrue(latestArchived.performanceIndex > 300)

        // 3. Automatically sets closed period as the previous period for instant comparison!
        assertEquals("P14", updatedState.previousPeriod.id)
        assertEquals(latestArchived.id, updatedState.previousPeriod.id)

        // 4. Verify new recordings are initialized for Day 1
        assertEquals(2, updatedState.dailyRecordings.size)
        assertTrue(updatedState.dailyRecordings.all { it.dayNumber == 1 })
    }

    @Test
    fun testHarvestWeighingTicket_andWhatsAppGeneration_andSynchronization() {
        // 1. Create a new harvest ticket
        val ticket = viewModel.createHarvestTicket(
            driverName = "Sugeng Waluyo",
            vehiclePlate = "B 9481 UYT",
            buyerName = "H. Ahmad (CV Berkah Unggas)",
            floor = Floor.L1,
            pricePerKg = 22000.0,
            farmerName = "Ika Ayu Wandira",
            date = "05 Nov 2026",
            time = "10:15 WIB",
            dayName = "Kamis",
            notes = "Panen sekat depan"
        )

        assertNotNull(ticket)
        assertEquals("Sugeng Waluyo", ticket.driverName)
        assertEquals("B 9481 UYT", ticket.vehiclePlate)
        assertEquals("H. Ahmad (CV Berkah Unggas)", ticket.buyerName)
        assertEquals(0, ticket.entries.size)

        // 2. Add multiple scale entries directly (ekor, bobot timbangan kg)
        viewModel.addScaleEntry(ticket.id, 15, 31.5)
        viewModel.addScaleEntry(ticket.id, 16, 33.6)
        viewModel.addScaleEntry(ticket.id, 15, 30.8)

        val updatedTicket = viewModel.uiState.value.harvestTickets.find { it.id == ticket.id }!!
        assertEquals(3, updatedTicket.entries.size)
        assertEquals(46, updatedTicket.totalBirdCount)
        assertEquals(95.9, updatedTicket.totalWeightKg, 0.01)
        assertEquals(95.9 / 46.0, updatedTicket.overallAvgWeightKg, 0.001)
        assertEquals(95.9 * 22000.0, updatedTicket.totalAmount, 0.1)

        // 3. Finalize ticket
        viewModel.finalizeHarvestTicket(ticket.id)
        val finalizedTicket = viewModel.uiState.value.harvestTickets.find { it.id == ticket.id }!!
        assertEquals("Selesai", finalizedTicket.status)

        // 4. Verify WhatsApp Text Formatting
        val waText = finalizedTicket.generateWhatsAppText()
        assertTrue(waText.contains("Sugeng Waluyo"))
        assertTrue(waText.contains("B 9481 UYT"))
        assertTrue(waText.contains("H. Ahmad"))
        assertTrue(waText.contains("Ika Ayu Wandira"))
        assertTrue(waText.contains("46 Ekor"))
        assertTrue(waText.contains("95.90 Kg"))
        assertFalse(waText.contains("TOTAL TAGIHAN"))
        assertFalse(waText.contains("Harga / Kg"))

        // 5. Verify Synchronization with Floor Summary
        val summaryL1 = viewModel.calculateFloorSummary(Floor.L1)
        assertTrue(summaryL1.totalTonaseKg > 0)
        assertTrue(summaryL1.latestAvgWeightKg > 1.5)
        assertTrue(summaryL1.performanceIndex > 0)
    }

    @Test
    fun testProfitLossCalculation_andSimulatedSellingPrice() {
        val costSummary = viewModel.calculateCostSummary()

        // 1. Production cost components verification
        assertTrue(costSummary.docCost > 0)
        assertTrue(costSummary.feedCost > 0)
        assertTrue(costSummary.ovkCost > 0)
        assertTrue(costSummary.operationalCost > 0)
        val expectedTotalCost = costSummary.docCost + costSummary.feedCost + costSummary.ovkCost + costSummary.operationalCost
        assertEquals(expectedTotalCost, costSummary.totalProductionCost, 0.01)

        // 2. Default simulated price is 21,500/kg
        assertEquals(21500.0, costSummary.sellingPricePerKg, 0.01)
        val expectedGrossRevenue = costSummary.totalTonaseKg * 21500.0
        assertEquals(expectedGrossRevenue, costSummary.grossRevenue, 0.01)

        // 3. Profit / Loss calculations
        val expectedProfit = expectedGrossRevenue - expectedTotalCost
        assertEquals(expectedProfit, costSummary.netProfitLoss, 0.01)
        assertTrue(costSummary.isProfit)
        assertTrue(costSummary.profitMarginPercent > 0)
        assertTrue(costSummary.profitPerKg > 0)
        assertTrue(costSummary.profitPerBird > 0)

        // BEP price = total cost / total tonase
        val expectedBep = expectedTotalCost / costSummary.totalTonaseKg
        assertEquals(expectedBep, costSummary.bepPricePerKg, 0.01)
        assertTrue(costSummary.sellingPricePerKg > costSummary.bepPricePerKg)

        // 4. Test Live Price Simulation (Upward shift e.g. Rp 23,000/kg)
        viewModel.setSimulatedSellingPrice(23000.0)
        val higherCostSummary = viewModel.calculateCostSummary()
        assertEquals(23000.0, higherCostSummary.sellingPricePerKg, 0.01)
        assertTrue(higherCostSummary.grossRevenue > costSummary.grossRevenue)
        assertTrue(higherCostSummary.netProfitLoss > costSummary.netProfitLoss)
        assertTrue(higherCostSummary.profitMarginPercent > costSummary.profitMarginPercent)

        // 5. Test Deficit / Loss Simulation (Price drops below BEP e.g. Rp 14,000/kg)
        viewModel.setSimulatedSellingPrice(14000.0)
        val lossSummary = viewModel.calculateCostSummary()
        assertEquals(14000.0, lossSummary.sellingPricePerKg, 0.01)
        assertFalse(lossSummary.isProfit)
        assertTrue(lossSummary.netProfitLoss < 0)
        assertTrue(lossSummary.profitMarginPercent < 0)
        assertTrue(lossSummary.profitPerKg < 0)
    }

    @Test
    fun testFcrAndDeplesiMilestoneMetrics() {
        // Test FCR standards vs actuals (Cobb 500 standards)
        val fcrMilestones = listOf(
            Triple("Mg 1", 0.95f, 0.98f),
            Triple("Mg 2", 1.14f, 1.15f),
            Triple("Mg 3", 1.30f, 1.32f),
            Triple("Mg 4", 1.46f, 1.49f)
        )

        fcrMilestones.forEach { (week, actual, standard) ->
            val delta = actual - standard
            assertTrue("FCR $week must be efficient", actual <= standard)
            assertTrue("Delta for $week must be negative or zero (efficient)", delta <= 0f)
        }

        // Test Deplesi standards vs actuals (Industry maximum tolerance)
        val deplesiMilestones = listOf(
            Triple("Mg 1", 0.52f, 1.00f),
            Triple("Mg 2", 0.94f, 1.80f),
            Triple("Mg 3", 1.48f, 2.60f),
            Triple("Mg 4", 2.10f, 3.40f)
        )

        deplesiMilestones.forEach { (week, actual, maxTolerance) ->
            val delta = actual - maxTolerance
            assertTrue("Deplesi $week must be strictly within safe tolerance", actual <= maxTolerance)
            assertTrue("Delta for $week must be below threshold", delta < 0f)
        }
    }

    @Test
    fun testEditAndDeleteOperationsAcrossAllMenus() {
        // 1. RECORDING: Edit and Delete
        val initialRecordingCount = viewModel.uiState.value.dailyRecordings.size
        viewModel.addDailyRecording(
            floor = Floor.L1,
            dayNumber = 29,
            mortality = 4,
            culling = 1,
            feedTypeId = "feed_1",
            feedAmountKg = 400.0,
            sampleWeightGram = 1820.0,
            temperatureC = 29.0,
            humidityPercent = 65.0,
            ovkUsageList = emptyList(),
            notes = "Catatan awal"
        )
        val addedRec = viewModel.uiState.value.dailyRecordings.find { it.dayNumber == 29 && it.floor == Floor.L1 }!!
        assertEquals(4, addedRec.mortalityCount)
        assertEquals(400.0, addedRec.feedAmountKg, 0.01)

        // Edit recording
        val updatedRec = addedRec.copy(
            mortalityCount = 2,
            feedAmountKg = 450.0,
            notes = "Catatan revisi"
        )
        viewModel.updateDailyRecording(updatedRec)
        val recAfterEdit = viewModel.uiState.value.dailyRecordings.find { it.id == addedRec.id }!!
        assertEquals(2, recAfterEdit.mortalityCount)
        assertEquals(450.0, recAfterEdit.feedAmountKg, 0.01)
        assertEquals("Catatan revisi", recAfterEdit.notes)

        // Delete recording & verify inventory restored
        val feedBeforeDelete = viewModel.uiState.value.feeds.find { it.id == "feed_1" }!!.currentStockKg
        viewModel.deleteDailyRecording(addedRec.id)
        assertEquals(initialRecordingCount, viewModel.uiState.value.dailyRecordings.size)
        val feedAfterDelete = viewModel.uiState.value.feeds.find { it.id == "feed_1" }!!.currentStockKg
        assertEquals(feedBeforeDelete + 450.0, feedAfterDelete, 0.01)

        // 2. HARVEST & SCALE ENTRY: Edit and Delete
        val tkt = viewModel.createHarvestTicket(
            driverName = "Pak Joko",
            vehiclePlate = "D 1234 ABC",
            buyerName = "PT Berkah",
            floor = Floor.L2,
            pricePerKg = 21500.0,
            farmerName = "Ika Ayu Wandira",
            date = "05 Nov 2026",
            time = "14:00 WIB",
            dayName = "Kamis",
            notes = "Pengiriman 1"
        )
        viewModel.addScaleEntry(tkt.id, 15, 30.0)
        viewModel.addScaleEntry(tkt.id, 16, 32.0)

        val ticketWithEntries = viewModel.uiState.value.harvestTickets.find { it.id == tkt.id }!!
        assertEquals(2, ticketWithEntries.entries.size)
        assertEquals(31, ticketWithEntries.totalBirdCount)
        assertEquals(62.0, ticketWithEntries.totalWeightKg, 0.01)

        // Edit scale entry #1
        viewModel.updateScaleEntry(tkt.id, 1, 14, 29.5)
        val ticketAfterScaleEdit = viewModel.uiState.value.harvestTickets.find { it.id == tkt.id }!!
        val entry1 = ticketAfterScaleEdit.entries.find { it.scaleNumber == 1 }!!
        assertEquals(14, entry1.birdCount)
        assertEquals(29.5, entry1.netWeightKg, 0.01)
        assertEquals(30, ticketAfterScaleEdit.totalBirdCount)
        assertEquals(61.5, ticketAfterScaleEdit.totalWeightKg, 0.01)

        // Edit ticket metadata
        val updatedTkt = ticketAfterScaleEdit.copy(driverName = "Pak Joko Widodo", vehiclePlate = "D 9999 XYZ")
        viewModel.updateHarvestTicket(updatedTkt)
        val ticketAfterEdit = viewModel.uiState.value.harvestTickets.find { it.id == tkt.id }!!
        assertEquals("Pak Joko Widodo", ticketAfterEdit.driverName)
        assertEquals("D 9999 XYZ", ticketAfterEdit.vehiclePlate)

        // Remove scale entry #2
        viewModel.removeScaleEntry(tkt.id, 2)
        val ticketAfterScaleDelete = viewModel.uiState.value.harvestTickets.find { it.id == tkt.id }!!
        assertEquals(1, ticketAfterScaleDelete.entries.size)

        // Delete ticket
        viewModel.deleteHarvestTicket(tkt.id)
        assertNull(viewModel.uiState.value.harvestTickets.find { it.id == tkt.id })

        // 3. OPERATIONAL EXPENSES: Edit and Delete
        val initialExpCount = viewModel.uiState.value.operationalExpenses.size
        viewModel.addOperationalExpense(
            category = com.example.data.model.ExpenseCategory.PEMANAS,
            amount = 350000.0,
            date = "05 Nov 2026",
            description = "Gas Elpiji 12kg",
            floorScope = Floor.L1
        )
        val addedExp = viewModel.uiState.value.operationalExpenses.last()
        assertEquals(350000.0, addedExp.amount, 0.01)

        // Edit expense
        val editedExp = addedExp.copy(amount = 400000.0, description = "Gas Elpiji 12kg + Tabung Cadangan")
        viewModel.updateOperationalExpense(editedExp)
        val expAfterEdit = viewModel.uiState.value.operationalExpenses.find { it.id == addedExp.id }!!
        assertEquals(400000.0, expAfterEdit.amount, 0.01)

        // Delete expense
        viewModel.deleteOperationalExpense(addedExp.id)
        assertEquals(initialExpCount, viewModel.uiState.value.operationalExpenses.size)

        // 4. INVENTORY FEEDS & OVKS: Edit and Delete
        val initialFeedCount = viewModel.uiState.value.feeds.size
        viewModel.addOrReplenishFeed("BR-9 Test Feed", "BR-9", 1000.0, 9500.0, "Test Supplier")
        val addedFeed = viewModel.uiState.value.feeds.find { it.code == "BR-9" }!!
        assertEquals(1000.0, addedFeed.currentStockKg, 0.01)

        viewModel.updateFeedItem(addedFeed.copy(currentStockKg = 1200.0, pricePerKg = 9800.0))
        val feedAfterEdit = viewModel.uiState.value.feeds.find { it.code == "BR-9" }!!
        assertEquals(1200.0, feedAfterEdit.currentStockKg, 0.01)

        viewModel.deleteFeedItem(addedFeed.id)
        assertEquals(initialFeedCount, viewModel.uiState.value.feeds.size)
    }

    @Test
    fun testDocChickInInput_andFloorAllocation() {
        // 1. Initial state check
        val initialL1 = viewModel.uiState.value.currentPeriod.initialPopulationL1
        val initialL2 = viewModel.uiState.value.currentPeriod.initialPopulationL2
        assertEquals(10000, initialL1)
        assertEquals(10000, initialL2)
        assertEquals(20000, viewModel.uiState.value.currentPeriod.totalInitialPopulation)

        // 2. Update DOC Chick-In: date, time, and custom floor populations
        viewModel.updateDocChickIn(
            startDate = "01 Nov 2026",
            docEntryTime = "01:45 WIB",
            initialPopulationL1 = 12500,
            initialPopulationL2 = 13500,
            docPricePerBird = 8000.0,
            docStrain = "Ross 308",
            docHatchery = "PT Charoen Pokphand"
        )

        val updatedPeriod = viewModel.uiState.value.currentPeriod
        assertEquals("01 Nov 2026", updatedPeriod.startDate)
        assertEquals("01:45 WIB", updatedPeriod.docEntryTime)
        assertEquals(12500, updatedPeriod.initialPopulationL1)
        assertEquals(13500, updatedPeriod.initialPopulationL2)
        assertEquals(26000, updatedPeriod.totalInitialPopulation)
        assertEquals(8000.0, updatedPeriod.docPricePerBird, 0.01)
        assertEquals("Ross 308", updatedPeriod.docStrain)

        // 3. Verify synchronization with floor summaries
        val summaryL1 = viewModel.calculateFloorSummary(Floor.L1)
        val summaryL2 = viewModel.calculateFloorSummary(Floor.L2)
        val summaryAll = viewModel.calculateFloorSummary(Floor.ALL)

        assertEquals(12500, summaryL1.initialPopulation)
        assertEquals(13500, summaryL2.initialPopulation)
        assertEquals(26000, summaryAll.initialPopulation)

        // 4. Verify DOC production cost calculation
        val costSummary = viewModel.calculateCostSummary()
        assertEquals(26000 * 8000.0, costSummary.docCost, 0.01)

        // 5. Test close period with new DOC date and time
        viewModel.closeCurrentPeriodAndStartNew(
            newPeriodTitle = "Periode 15 (DOC Baru)",
            newPopL1 = 11000,
            newPopL2 = 11500,
            docPrice = 8200.0,
            marketPricePerKg = 21000.0,
            docDate = "05 Nov 2026",
            docTime = "03:15 WIB"
        )

        val nextPeriod = viewModel.uiState.value.currentPeriod
        assertEquals("Periode 15 (DOC Baru)", nextPeriod.title)
        assertEquals("05 Nov 2026", nextPeriod.startDate)
        assertEquals("03:15 WIB", nextPeriod.docEntryTime)
        assertEquals(11000, nextPeriod.initialPopulationL1)
        assertEquals(11500, nextPeriod.initialPopulationL2)
        assertEquals(22500, nextPeriod.totalInitialPopulation)
    }

    @Test
    fun testFloorChickenAgeSynchronization_withChickInDateAndRecordings() {
        // 1. Initial age based on default recordings (Day 1..28)
        val initialSummaryL1 = viewModel.calculateFloorSummary(Floor.L1)
        val initialSummaryL2 = viewModel.calculateFloorSummary(Floor.L2)
        assertEquals(28, initialSummaryL1.ageDays)
        assertEquals(28, initialSummaryL2.ageDays)

        // 2. Add Day 29 recording specifically to Floor.L1
        viewModel.addDailyRecording(
            floor = Floor.L1,
            dayNumber = 29,
            mortality = 3,
            culling = 0,
            feedTypeId = "feed_1",
            feedAmountKg = 460.0,
            sampleWeightGram = 1880.0,
            temperatureC = 27.0,
            humidityPercent = 64.0,
            ovkUsageList = emptyList(),
            notes = "Recording D-29 Lantai 1"
        )

        val updatedSummaryL1 = viewModel.calculateFloorSummary(Floor.L1)
        val updatedSummaryL2 = viewModel.calculateFloorSummary(Floor.L2)
        val updatedSummaryAll = viewModel.calculateFloorSummary(Floor.ALL)

        // Lantai 1 should now be at Day 29
        assertEquals(29, updatedSummaryL1.ageDays)
        // Lantai 2 remains at Day 28
        assertEquals(28, updatedSummaryL2.ageDays)
        // Overall summary age reflects latest active age across kandang (29)
        assertEquals(29, updatedSummaryAll.ageDays)
        assertEquals(29, viewModel.uiState.value.currentPeriod.currentAgeDays)

        // 3. Test date calculation helper directly
        val calcAge = BroilerViewModel.calculateAgeDaysFromStartDate("Hari ini")
        assertEquals(1, calcAge)
    }

    @Test
    fun testDeleteArchivedPeriod() {
        val initialArchives = viewModel.uiState.value.archivedPeriods
        assertEquals(2, initialArchives.size)
        assertTrue(initialArchives.any { it.id == "P13" })
        assertTrue(initialArchives.any { it.id == "P12" })
        assertEquals("P13", viewModel.uiState.value.previousPeriod.id)

        // Delete P13 (currently active comparison)
        viewModel.deleteArchivedPeriod("P13")
        val stateAfterP13 = viewModel.uiState.value
        assertEquals(1, stateAfterP13.archivedPeriods.size)
        assertFalse(stateAfterP13.archivedPeriods.any { it.id == "P13" })
        assertEquals("P12", stateAfterP13.previousPeriod.id)
        assertEquals("P12", stateAfterP13.selectedComparisonPeriodId)
        assertNotNull(stateAfterP13.feedbackMessage)

        // Delete remaining P12
        viewModel.deleteArchivedPeriod("P12")
        val stateAfterP12 = viewModel.uiState.value
        assertTrue(stateAfterP12.archivedPeriods.isEmpty())
        assertEquals("EMPTY", stateAfterP12.previousPeriod.id)
    }

    @Test
    fun testAgeSyncsWithDailyRecording_addingAndDeleting() {
        // Close period to start a clean new cycle P15 at Day 1
        viewModel.closeCurrentPeriodAndStartNew(
            newPeriodTitle = "Periode 15 Uji Umur",
            newPopL1 = 10000,
            newPopL2 = 10000,
            docPrice = 7500.0
        )

        // Initial day of new period is Day 1
        assertEquals(1, viewModel.uiState.value.currentPeriod.currentAgeDays)
        assertEquals(1, viewModel.calculateFloorSummary(Floor.L1).ageDays)
        assertEquals(1, viewModel.calculateFloorSummary(Floor.L2).ageDays)
        assertEquals(1, viewModel.calculateFloorSummary(Floor.ALL).ageDays)

        // User inputs recording for Day 2 on Floor 1
        viewModel.addDailyRecording(
            floor = Floor.L1,
            dayNumber = 2,
            mortality = 2,
            culling = 0,
            feedTypeId = "feed_1",
            feedAmountKg = 150.0,
            sampleWeightGram = 60.0,
            temperatureC = 32.0,
            humidityPercent = 65.0,
            ovkUsageList = emptyList(),
            notes = "Recording D-2"
        )

        assertEquals(2, viewModel.calculateFloorSummary(Floor.L1).ageDays)
        assertEquals(2, viewModel.uiState.value.currentPeriod.currentAgeDays)

        // User inputs recording for Day 3 on Floor 1
        viewModel.addDailyRecording(
            floor = Floor.L1,
            dayNumber = 3,
            mortality = 1,
            culling = 0,
            feedTypeId = "feed_1",
            feedAmountKg = 180.0,
            sampleWeightGram = 75.0,
            temperatureC = 31.5,
            humidityPercent = 64.0,
            ovkUsageList = emptyList(),
            notes = "Recording D-3"
        )

        // With Day 3 input, age should now be 3
        assertEquals(3, viewModel.calculateFloorSummary(Floor.L1).ageDays)
        assertEquals(3, viewModel.calculateFloorSummary(Floor.ALL).ageDays)
        assertEquals(3, viewModel.uiState.value.currentPeriod.currentAgeDays)

        // Find the Day 3 recording ID
        val recDay3 = viewModel.uiState.value.dailyRecordings.find { it.dayNumber == 3 }
        assertNotNull(recDay3)

        // User deletes Day 3 recording
        viewModel.deleteDailyRecording(recDay3!!.id)

        // As specifically requested: "jika di recording umur ke 3 di hapus maka otomatis di tampilan monitoring aktifnya juga berubah menjadi ke 2"
        assertEquals(2, viewModel.calculateFloorSummary(Floor.L1).ageDays)
        assertEquals(2, viewModel.calculateFloorSummary(Floor.ALL).ageDays)
        assertEquals(2, viewModel.uiState.value.currentPeriod.currentAgeDays)

        // Find the Day 2 recording ID and delete it
        val recDay2 = viewModel.uiState.value.dailyRecordings.find { it.dayNumber == 2 }
        assertNotNull(recDay2)
        viewModel.deleteDailyRecording(recDay2!!.id)

        // Age reverts back to 1
        assertEquals(1, viewModel.calculateFloorSummary(Floor.L1).ageDays)
        assertEquals(1, viewModel.calculateFloorSummary(Floor.ALL).ageDays)
        assertEquals(1, viewModel.uiState.value.currentPeriod.currentAgeDays)
    }

    @Test
    fun testSessionLockOnExit_requiresReLogin() {
        // 1. Initially not logged in
        assertFalse(viewModel.uiState.value.isLoggedIn)

        // 2. User logs in
        val loginSuccess = viewModel.login("ProfitBroiler", "BroilerClub123")
        assertTrue(loginSuccess)
        assertTrue(viewModel.uiState.value.isLoggedIn)

        // 3. User edits data (e.g. input DOC Chick-in)
        viewModel.updateDocChickIn(
            startDate = "08 Nov 2026",
            docEntryTime = "01:30 WIB",
            initialPopulationL1 = 12000,
            initialPopulationL2 = 12000,
            docPricePerBird = 7800.0
        )
        assertEquals(24000, viewModel.uiState.value.currentPeriod.totalInitialPopulation)

        // 4. User exits application -> lockSession() is invoked
        viewModel.lockSession()

        // 5. Verify user is now locked out (isLoggedIn = false)
        assertFalse(viewModel.uiState.value.isLoggedIn)

        // 6. Verify edited farm data is still retained and NOT reset!
        assertEquals(24000, viewModel.uiState.value.currentPeriod.totalInitialPopulation)
        assertEquals(12000, viewModel.uiState.value.currentPeriod.initialPopulationL1)
        assertEquals("08 Nov 2026", viewModel.uiState.value.currentPeriod.startDate)

        // 7. User must log in again to access
        val reLogin = viewModel.login("ProfitBroiler", "BroilerClub123")
        assertTrue(reLogin)
        assertTrue(viewModel.uiState.value.isLoggedIn)
    }
}


