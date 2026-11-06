package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.mock.MockDataProvider
import com.example.data.model.*
import com.example.data.storage.BroilerStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BroilerUiState(
    val currentPeriod: PeriodData = MockDataProvider.periodCurrent,
    val previousPeriod: ArchivedPeriodRecord = MockDataProvider.periodPrevious,
    val archivedPeriods: List<ArchivedPeriodRecord> = MockDataProvider.initialArchivedPeriods,
    val selectedComparisonPeriodId: String = MockDataProvider.periodPrevious.id,
    val isClosePeriodDialogOpen: Boolean = false,
    val feeds: List<FeedItem> = MockDataProvider.initialFeeds,
    val ovks: List<OvkItem> = MockDataProvider.initialOvks,
    val dailyRecordings: List<DailyRecording> = emptyList(),
    val operationalExpenses: List<OperationalExpense> = MockDataProvider.initialExpenses,
    val selectedFloor: Floor = Floor.ALL,
    val isAddRecordingDialogOpen: Boolean = false,
    val isAddFeedDialogOpen: Boolean = false,
    val isAddOvkDialogOpen: Boolean = false,
    val isAddExpenseDialogOpen: Boolean = false,
    val isCalculatorDialogOpen: Boolean = false,
    val isResetPasswordDialogOpen: Boolean = false,
    val isChangePasswordDialogOpen: Boolean = false,
    val isEditDocDialogOpen: Boolean = false,
    val feedbackMessage: String? = null,
    // Harvest & Weighing Tickets State
    val harvestTickets: List<HarvestTicket> = MockDataProvider.initialHarvestTickets,
    val isAddHarvestTicketDialogOpen: Boolean = false,
    val activeScaleTicketId: String? = null,
    val isScaleWeighingSheetOpen: Boolean = false,
    val isReceiptDialogOpen: Boolean = false,
    val selectedTicketForReceipt: HarvestTicket? = null,
    val defaultFarmerName: String = "Ika Ayu Wandira",
    // Financial & Profit-Loss State (Driven by Harvest Tickets)
    val simulatedSellingPricePerKg: Double = 21500.0,
    // Authentication State
    val isLoggedIn: Boolean = false,
    val userId: String = "ProfitBroiler",
    val password: String = "BroilerClub123",
    val recoveryEmail: String = "pratamaramadhan007@gmail.com",
    val currentOtp: String? = null,
    val lastOtpMessage: String? = null,
    val loginError: String? = null
)

class BroilerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        BroilerUiState(
            dailyRecordings = MockDataProvider.generateInitialDailyRecordings()
        )
    )
    val uiState: StateFlow<BroilerUiState> = _uiState.asStateFlow()

    private var appContext: Context? = null

    /**
     * Memuat data persisten yang tersimpan di disk dan mengaitkan konteks aplikasi
     */
    fun attachStorage(context: Context) {
        val app = context.applicationContext
        appContext = app
        val saved = BroilerStorageManager.loadSavedState(app)
        if (saved != null) {
            _uiState.update {
                saved.copy(
                    isLoggedIn = false, // Selalu mewajibkan login ulang setiap kali membuka aplikasi
                    loginError = null,
                    isAddRecordingDialogOpen = false,
                    isAddFeedDialogOpen = false,
                    isAddOvkDialogOpen = false,
                    isAddExpenseDialogOpen = false,
                    isCalculatorDialogOpen = false,
                    isResetPasswordDialogOpen = false,
                    isChangePasswordDialogOpen = false,
                    isEditDocDialogOpen = false,
                    isClosePeriodDialogOpen = false,
                    isAddHarvestTicketDialogOpen = false,
                    isScaleWeighingSheetOpen = false,
                    isReceiptDialogOpen = false,
                    feedbackMessage = null
                )
            }
        }
    }

    /**
     * Mengunci sesi aplikasi saat keluar/berpindah sehingga pengguna wajib login kembali
     */
    fun lockSession() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                loginError = null
            )
        }
        persistDataSync()
    }

    fun persistData() {
        val ctx = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                BroilerStorageManager.saveState(ctx, _uiState.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun persistDataSync() {
        val ctx = appContext ?: return
        try {
            BroilerStorageManager.saveState(ctx, _uiState.value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun login(userIdInput: String, passwordInput: String): Boolean {
        val state = _uiState.value
        if (userIdInput.trim().equals(state.userId, ignoreCase = true) && passwordInput == state.password) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    loginError = null,
                    feedbackMessage = "Login berhasil! Selamat datang di PROFIT Broiler Club."
                )
            }
            persistData()
            return true
        } else {
            val error = when {
                !userIdInput.trim().equals(state.userId, ignoreCase = true) -> "User ID salah! Gunakan: ${state.userId}"
                else -> "Password salah! Periksa kembali password Anda."
            }
            _uiState.update { it.copy(loginError = error) }
            return false
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                loginError = null,
                feedbackMessage = "Sesi telah keluar. Silakan login kembali."
            )
        }
        persistData()
    }

    fun openResetPasswordDialog(isOpen: Boolean) {
        _uiState.update {
            it.copy(
                isResetPasswordDialogOpen = isOpen,
                currentOtp = if (!isOpen) null else it.currentOtp,
                lastOtpMessage = if (!isOpen) null else it.lastOtpMessage
            )
        }
    }

    fun openChangePasswordDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isChangePasswordDialogOpen = isOpen) }
    }

    fun requestPasswordResetOtp(): String {
        val otp = (100000..999999).random().toString()
        val email = _uiState.value.recoveryEmail
        val msg = "Kode OTP verifikasi: $otp (dikirim ke $email)"
        _uiState.update {
            it.copy(
                currentOtp = otp,
                lastOtpMessage = msg,
                feedbackMessage = "OTP $otp telah dikirim ke $email!"
            )
        }
        return otp
    }

    fun verifyOtpAndResetPassword(otpInput: String, newPassword: String): Pair<Boolean, String> {
        val state = _uiState.value
        if (state.currentOtp == null) {
            return Pair(false, "Klik 'Kirim Kode OTP' terlebih dahulu!")
        }
        if (otpInput.trim() != state.currentOtp) {
            return Pair(false, "Kode OTP salah! Masukkan 6 digit yang sesuai.")
        }
        if (newPassword.length < 6) {
            return Pair(false, "Password baru minimal 6 karakter!")
        }

        _uiState.update {
            it.copy(
                password = newPassword,
                currentOtp = null,
                lastOtpMessage = null,
                isResetPasswordDialogOpen = false,
                isChangePasswordDialogOpen = false,
                feedbackMessage = "Sukses! Password berhasil diubah menjadi baru."
            )
        }
        persistData()
        return Pair(true, "Password berhasil diperbarui!")
    }

    fun setSelectedFloor(floor: Floor) {
        _uiState.update { it.copy(selectedFloor = floor) }
    }

    fun openAddRecordingDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isAddRecordingDialogOpen = isOpen) }
    }

    fun openAddFeedDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isAddFeedDialogOpen = isOpen) }
    }

    fun openAddOvkDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isAddOvkDialogOpen = isOpen) }
    }

    fun openAddExpenseDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isAddExpenseDialogOpen = isOpen) }
    }

    fun openCalculatorDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isCalculatorDialogOpen = isOpen) }
    }

    fun openClosePeriodDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isClosePeriodDialogOpen = isOpen) }
    }

    fun openEditDocDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isEditDocDialogOpen = isOpen) }
    }

    /**
     * Input & update incoming DOC chick-in info: date, time, and population per floor
     */
    fun updateDocChickIn(
        startDate: String,
        docEntryTime: String,
        initialPopulationL1: Int,
        initialPopulationL2: Int,
        docPricePerBird: Double = 7500.0,
        docStrain: String = "Cobb 500",
        docHatchery: String = ""
    ) {
        val current = _uiState.value.currentPeriod
        val validL1 = initialPopulationL1.coerceAtLeast(0)
        val validL2 = initialPopulationL2.coerceAtLeast(0)
        val maxRecorded = _uiState.value.dailyRecordings.maxOfOrNull { it.dayNumber } ?: 1
        val finalAge = maxRecorded.coerceAtLeast(1)

        val updatedPeriod = current.copy(
            startDate = startDate.ifBlank { current.startDate },
            docEntryTime = docEntryTime.ifBlank { current.docEntryTime },
            currentAgeDays = finalAge,
            initialPopulationL1 = validL1,
            initialPopulationL2 = validL2,
            docPricePerBird = if (docPricePerBird > 0) docPricePerBird else current.docPricePerBird,
            docStrain = docStrain.ifBlank { current.docStrain },
            docHatchery = docHatchery.ifBlank { current.docHatchery }
        )
        val totalPop = updatedPeriod.totalInitialPopulation
        _uiState.update {
            it.copy(
                currentPeriod = updatedPeriod,
                isEditDocDialogOpen = false,
                feedbackMessage = "Data DOC Masuk diperbarui: ${formatNumber(totalPop.toDouble())} Ekor (L1: ${formatNumber(validL1.toDouble())}, L2: ${formatNumber(validL2.toDouble())}, Umur D-$finalAge)!"
            )
        }
        persistData()
    }

    fun setSelectedComparisonPeriod(periodId: String) {
        val selected = _uiState.value.archivedPeriods.find { it.id == periodId }
        if (selected != null) {
            _uiState.update {
                it.copy(
                    previousPeriod = selected,
                    selectedComparisonPeriodId = periodId
                )
            }
            persistData()
        }
    }

    /**
     * Hapus arsip periode yang lalu
     */
    fun deleteArchivedPeriod(periodId: String) {
        val target = _uiState.value.archivedPeriods.find { it.id == periodId }
        val updated = _uiState.value.archivedPeriods.filterNot { it.id == periodId }
        val fallbackPeriod = updated.firstOrNull() ?: MockDataProvider.periodPrevious.copy(
            id = "EMPTY",
            title = "(Belum Ada Arsip)",
            harvestAgeDays = 0,
            initialPopulation = 0,
            harvestedPopulation = 0,
            totalMortality = 0,
            depletionRatePercent = 0.0,
            livabilityPercent = 0.0,
            avgWeightKg = 0.0,
            totalTonaseKg = 0.0,
            totalFeedUsedKg = 0.0,
            fcr = 0.0,
            performanceIndex = 0.0,
            ipCategory = "-",
            totalProductionCost = 0.0,
            hppPerKg = 0.0,
            totalRevenue = 0.0,
            netProfit = 0.0
        )
        val newPrev = if (_uiState.value.previousPeriod.id == periodId) {
            fallbackPeriod
        } else {
            _uiState.value.previousPeriod
        }
        _uiState.update {
            it.copy(
                archivedPeriods = updated,
                previousPeriod = newPrev,
                selectedComparisonPeriodId = newPrev.id,
                feedbackMessage = "Arsip ${target?.title ?: periodId} berhasil dihapus."
            )
        }
        persistData()
    }

    /**
     * Close the current production period, snapshot final harvest metrics into archived history,
     * make it the new 'Periode Lalu', and initialize the new active period cycle.
     */
    fun closeCurrentPeriodAndStartNew(
        newPeriodTitle: String,
        newPopL1: Int,
        newPopL2: Int,
        docPrice: Double,
        marketPricePerKg: Double = 20500.0,
        docDate: String = "Hari ini",
        docTime: String = "02:30 WIB"
    ) {
        val state = _uiState.value
        val currentPeriod = state.currentPeriod
        val allSummary = calculateFloorSummary(Floor.ALL)
        val costSummary = calculateCostSummary()

        val finalRevenue = allSummary.totalTonaseKg * marketPricePerKg
        val finalProfit = finalRevenue - costSummary.totalProductionCost

        // 1. Snapshot the finalized period
        val closedPeriodRecord = ArchivedPeriodRecord(
            id = currentPeriod.id,
            title = "${currentPeriod.title.substringBefore(" (")} (Selesai Panen)",
            startDate = currentPeriod.startDate,
            closeDate = "Hari ini",
            harvestAgeDays = currentPeriod.currentAgeDays,
            initialPopulation = currentPeriod.totalInitialPopulation,
            harvestedPopulation = allSummary.activePopulation,
            totalMortality = allSummary.totalMortality + allSummary.totalCulling,
            depletionRatePercent = allSummary.depletionRatePercent,
            livabilityPercent = allSummary.livabilityPercent,
            avgWeightKg = allSummary.latestAvgWeightKg,
            totalTonaseKg = allSummary.totalTonaseKg,
            totalFeedUsedKg = allSummary.totalFeedKg,
            fcr = allSummary.fcr,
            performanceIndex = allSummary.performanceIndex,
            ipCategory = allSummary.ipCategory,
            totalProductionCost = costSummary.totalProductionCost,
            hppPerKg = costSummary.hppPerKg,
            totalRevenue = finalRevenue,
            netProfit = finalProfit
        )

        // 2. Generate next period number (e.g. P14 -> P15)
        val currentNum = currentPeriod.id.filter { it.isDigit() }.toIntOrNull() ?: 14
        val nextNum = currentNum + 1
        val nextPeriodId = "P$nextNum"
        val nextTitle = newPeriodTitle.ifBlank { "Periode $nextNum (Aktif - DOC Masuk)" }

        val newPeriod = PeriodData(
            id = nextPeriodId,
            title = nextTitle,
            startDate = docDate.ifBlank { "Hari ini" },
            docEntryTime = docTime.ifBlank { "02:30 WIB" },
            currentAgeDays = 1,
            initialPopulationL1 = newPopL1,
            initialPopulationL2 = newPopL2,
            docPricePerBird = docPrice,
            targetFcr = 1.48,
            targetWeightKg = 1.95,
            isCurrent = true
        )

        // 3. New recordings for Day 1 chick-in of the new period
        val initialNewRecordings = listOf(
            DailyRecording(
                id = "rec_L1_D1_new_${System.currentTimeMillis()}",
                date = "Hari ini",
                dayNumber = 1,
                floor = Floor.L1,
                mortalityCount = 4,
                cullingCount = 0,
                feedTypeId = "feed_1",
                feedTypeName = "BR-1 Pre-Starter Crumble",
                feedAmountKg = 180.0,
                sampleWeightGram = 44.0,
                temperatureC = 33.0,
                humidityPercent = 65.0,
                notes = "DOC Chick-in Lantai 1, kondisi lincah & pemanas aktif"
            ),
            DailyRecording(
                id = "rec_L2_D1_new_${System.currentTimeMillis()}",
                date = "Hari ini",
                dayNumber = 1,
                floor = Floor.L2,
                mortalityCount = 5,
                cullingCount = 0,
                feedTypeId = "feed_1",
                feedTypeName = "BR-1 Pre-Starter Crumble",
                feedAmountKg = 180.0,
                sampleWeightGram = 43.5,
                temperatureC = 33.2,
                humidityPercent = 65.0,
                notes = "DOC Chick-in Lantai 2, air minum vitastress"
            )
        )

        val updatedArchiveList = listOf(closedPeriodRecord) + state.archivedPeriods

        _uiState.update {
            it.copy(
                currentPeriod = newPeriod,
                previousPeriod = closedPeriodRecord, // Automatically sets the newly closed period as the 'Periode Lalu'!
                archivedPeriods = updatedArchiveList,
                selectedComparisonPeriodId = closedPeriodRecord.id,
                dailyRecordings = initialNewRecordings,
                isClosePeriodDialogOpen = false,
                feedbackMessage = "Selamat! ${closedPeriodRecord.title} berhasil ditutup. Otomatis masuk ke 'Periode Lalu' untuk perbandingan performa!"
            )
        }
        persistData()
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    /**
     * Submit daily recording with automatic stock deduction and safety check
     */
    fun addDailyRecording(
        floor: Floor,
        dayNumber: Int,
        mortality: Int,
        culling: Int,
        feedTypeId: String,
        feedAmountKg: Double,
        sampleWeightGram: Double,
        temperatureC: Double,
        humidityPercent: Double,
        ovkUsageList: List<OvkUsageEntry> = emptyList(),
        notes: String = ""
    ): Boolean {
        val state = _uiState.value

        // Safety check 1: Find feed
        val feed = state.feeds.find { it.id == feedTypeId }
        if (feed == null) {
            _uiState.update { it.copy(feedbackMessage = "Error: Jenis pakan tidak ditemukan!") }
            return false
        }

        // Safety check 2: Stok habis atau tidak mencukupi
        if (feed.currentStockKg <= 0) {
            _uiState.update { it.copy(feedbackMessage = "Gagal: Stok ${feed.name} sudah HABIS (0 kg)! Tambah stok di Inventaris terlebih dahulu.") }
            return false
        }

        if (feed.currentStockKg < feedAmountKg) {
            _uiState.update { it.copy(feedbackMessage = "Peringatan: Stok ${feed.name} (${feed.currentStockKg} kg) kurang dari pemakaian (${feedAmountKg} kg).") }
        }

        // 1. Decrement feed stock
        val updatedFeeds = state.feeds.map {
            if (it.id == feedTypeId) {
                it.copy(currentStockKg = (it.currentStockKg - feedAmountKg).coerceAtLeast(0.0))
            } else {
                it
            }
        }

        // 2. Decrement OVK stock if any
        val updatedOvks = state.ovks.map { ovkItem ->
            val used = ovkUsageList.find { it.ovkId == ovkItem.id }
            if (used != null) {
                ovkItem.copy(currentStock = (ovkItem.currentStock - used.amount).coerceAtLeast(0.0))
            } else {
                ovkItem
            }
        }

        // 3. Create new recording entry
        val newRecord = DailyRecording(
            id = "rec_${floor.name}_D${dayNumber}_${System.currentTimeMillis()}",
            date = "Hari ini",
            dayNumber = dayNumber,
            floor = floor,
            mortalityCount = mortality,
            cullingCount = culling,
            feedTypeId = feedTypeId,
            feedTypeName = feed.name,
            feedAmountKg = feedAmountKg,
            sampleWeightGram = sampleWeightGram,
            temperatureC = temperatureC,
            humidityPercent = humidityPercent,
            ovkUsages = ovkUsageList,
            notes = notes
        )

        val updatedRecordings = state.dailyRecordings + newRecord
        val newAge = (updatedRecordings.maxOfOrNull { it.dayNumber } ?: 1).coerceAtLeast(1)
        val updatedPeriod = state.currentPeriod.copy(currentAgeDays = newAge)

        _uiState.update {
            it.copy(
                feeds = updatedFeeds,
                ovks = updatedOvks,
                currentPeriod = updatedPeriod,
                dailyRecordings = updatedRecordings,
                isAddRecordingDialogOpen = false,
                feedbackMessage = "Sukses: Recording ${floor.displayName} Hari ke-$dayNumber tersimpan. Stok ${feed.name} berkurang $feedAmountKg kg!"
            )
        }
        persistData()
        return true
    }

    /**
     * Update existing daily recording with stock adjustment
     */
    fun updateDailyRecording(updated: DailyRecording) {
        val state = _uiState.value
        val old = state.dailyRecordings.find { it.id == updated.id }

        // Feed stock delta adjustment
        val feedDelta = if (old != null && old.feedTypeId == updated.feedTypeId) {
            updated.feedAmountKg - old.feedAmountKg
        } else 0.0

        val updatedFeeds = state.feeds.map { feed ->
            if (feed.id == updated.feedTypeId && feedDelta != 0.0) {
                feed.copy(currentStockKg = (feed.currentStockKg - feedDelta).coerceAtLeast(0.0))
            } else feed
        }

        val updatedRecordings = state.dailyRecordings.map {
            if (it.id == updated.id) updated else it
        }

        val maxRecorded = (updatedRecordings.maxOfOrNull { it.dayNumber } ?: 1).coerceAtLeast(1)
        val updatedPeriod = state.currentPeriod.copy(currentAgeDays = maxRecorded)

        _uiState.update {
            it.copy(
                dailyRecordings = updatedRecordings,
                feeds = updatedFeeds,
                currentPeriod = updatedPeriod,
                feedbackMessage = "Recording ${updated.floor.displayName} Hari ke-${updated.dayNumber} berhasil diperbarui!"
            )
        }
        persistData()
    }

    /**
     * Delete daily recording and restore feed inventory
     */
    fun deleteDailyRecording(recordingId: String) {
        val state = _uiState.value
        val target = state.dailyRecordings.find { it.id == recordingId }
        val updatedRecordings = state.dailyRecordings.filterNot { it.id == recordingId }

        // Restore feed stock when recording is deleted
        val updatedFeeds = if (target != null) {
            state.feeds.map { feed ->
                if (feed.id == target.feedTypeId) {
                    feed.copy(currentStockKg = feed.currentStockKg + target.feedAmountKg)
                } else feed
            }
        } else state.feeds

        val maxRecorded = (updatedRecordings.maxOfOrNull { it.dayNumber } ?: 1).coerceAtLeast(1)
        val updatedPeriod = state.currentPeriod.copy(currentAgeDays = maxRecorded)

        _uiState.update {
            it.copy(
                dailyRecordings = updatedRecordings,
                feeds = updatedFeeds,
                currentPeriod = updatedPeriod,
                feedbackMessage = "Data recording Hari ke-${target?.dayNumber ?: ""} berhasil dihapus dan stok pakan dikembalikan."
            )
        }
        persistData()
    }

    /**
     * Add new feed batch into inventory
     */
    fun addOrReplenishFeed(
        name: String,
        code: String,
        stockKg: Double,
        pricePerKg: Double,
        supplier: String
    ) {
        val state = _uiState.value
        val existingIndex = state.feeds.indexOfFirst { it.code.equals(code, ignoreCase = true) || it.name.equals(name, ignoreCase = true) }

        val updatedFeeds = if (existingIndex != -1) {
            state.feeds.mapIndexed { idx, item ->
                if (idx == existingIndex) {
                    item.copy(
                        initialStockKg = item.initialStockKg + stockKg,
                        currentStockKg = item.currentStockKg + stockKg,
                        pricePerKg = pricePerKg,
                        supplier = supplier.ifBlank { item.supplier }
                    )
                } else item
            }
        } else {
            val newFeed = FeedItem(
                id = "feed_${System.currentTimeMillis()}",
                name = name,
                code = code.ifBlank { "BR-${state.feeds.size + 1}" },
                initialStockKg = stockKg,
                currentStockKg = stockKg,
                pricePerKg = pricePerKg,
                supplier = supplier.ifBlank { "Supplier Pakan" },
                colorHex = if (state.feeds.size % 2 == 0) 0xFF00F5FF else 0xFF00FF87
            )
            state.feeds + newFeed
        }

        _uiState.update {
            it.copy(
                feeds = updatedFeeds,
                isAddFeedDialogOpen = false,
                feedbackMessage = "Stok pakan $name berhasil ditambahkan (+${stockKg} kg / ${stockKg / 50.0} sak)!"
            )
        }
        persistData()
    }

    /**
     * Add new OVK into inventory
     */
    fun addOrReplenishOvk(
        name: String,
        category: OvkCategory,
        stock: Double,
        unit: String,
        pricePerUnit: Double
    ) {
        val state = _uiState.value
        val newOvk = OvkItem(
            id = "ovk_${System.currentTimeMillis()}",
            name = name,
            category = category,
            initialStock = stock,
            currentStock = stock,
            unit = unit,
            pricePerUnit = pricePerUnit
        )
        _uiState.update {
            it.copy(
                ovks = it.ovks + newOvk,
                isAddOvkDialogOpen = false,
                feedbackMessage = "OVK $name ($unit) berhasil ditambahkan ke inventaris!"
            )
        }
        persistData()
    }

    fun updateFeedItem(updatedFeed: FeedItem) {
        val updatedFeeds = _uiState.value.feeds.map {
            if (it.id == updatedFeed.id) updatedFeed else it
        }
        _uiState.update {
            it.copy(
                feeds = updatedFeeds,
                feedbackMessage = "Data pakan ${updatedFeed.name} berhasil diperbarui!"
            )
        }
        persistData()
    }

    fun deleteFeedItem(feedId: String) {
        val feed = _uiState.value.feeds.find { it.id == feedId }
        val updatedFeeds = _uiState.value.feeds.filterNot { it.id == feedId }
        _uiState.update {
            it.copy(
                feeds = updatedFeeds,
                feedbackMessage = "Pakan ${feed?.name ?: ""} berhasil dihapus dari inventaris."
            )
        }
        persistData()
    }

    fun updateOvkItem(updatedOvk: OvkItem) {
        val updatedOvks = _uiState.value.ovks.map {
            if (it.id == updatedOvk.id) updatedOvk else it
        }
        _uiState.update {
            it.copy(
                ovks = updatedOvks,
                feedbackMessage = "Data OVK ${updatedOvk.name} berhasil diperbarui!"
            )
        }
        persistData()
    }

    fun deleteOvkItem(ovkId: String) {
        val ovk = _uiState.value.ovks.find { it.id == ovkId }
        val updatedOvks = _uiState.value.ovks.filterNot { it.id == ovkId }
        _uiState.update {
            it.copy(
                ovks = updatedOvks,
                feedbackMessage = "OVK ${ovk?.name ?: ""} berhasil dihapus dari inventaris."
            )
        }
        persistData()
    }

    /**
     * Add operational expense with specific facility attributes
     */
    fun addOperationalExpense(
        category: ExpenseCategory,
        amount: Double,
        date: String,
        description: String,
        floorScope: Floor = Floor.ALL,
        quantity: Double = 1.0,
        unit: String = "Paket",
        vendor: String = ""
    ) {
        val newExp = OperationalExpense(
            id = "exp_${System.currentTimeMillis()}",
            category = category,
            amount = amount,
            date = date.ifBlank { "Hari ini" },
            description = description,
            floorScope = floorScope,
            quantity = quantity,
            unit = unit.ifBlank { "Paket" },
            vendor = vendor
        )
        _uiState.update {
            it.copy(
                operationalExpenses = it.operationalExpenses + newExp,
                isAddExpenseDialogOpen = false,
                feedbackMessage = "Pengeluaran ${category.displayName} senilai Rp ${formatNumber(amount)} tercatat!"
            )
        }
        persistData()
    }

    fun updateOperationalExpense(updatedExpense: OperationalExpense) {
        val updatedExpenses = _uiState.value.operationalExpenses.map {
            if (it.id == updatedExpense.id) updatedExpense else it
        }
        _uiState.update {
            it.copy(
                operationalExpenses = updatedExpenses,
                feedbackMessage = "Pengeluaran ${updatedExpense.description} berhasil diperbarui!"
            )
        }
        persistData()
    }

    fun deleteOperationalExpense(expenseId: String) {
        val expense = _uiState.value.operationalExpenses.find { it.id == expenseId }
        val updatedExpenses = _uiState.value.operationalExpenses.filterNot { it.id == expenseId }
        _uiState.update {
            it.copy(
                operationalExpenses = updatedExpenses,
                feedbackMessage = "Pengeluaran ${expense?.description ?: ""} berhasil dihapus."
            )
        }
        persistData()
    }

    // ==========================================
    // HARVEST & LIVE SCALE WEIGHING MANAGEMENT
    // ==========================================

    fun openAddHarvestTicketDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isAddHarvestTicketDialogOpen = isOpen) }
    }

    fun openScaleWeighingSheet(ticketId: String?, isOpen: Boolean) {
        _uiState.update {
            it.copy(
                activeScaleTicketId = ticketId,
                isScaleWeighingSheetOpen = isOpen
            )
        }
    }

    fun openReceiptDialog(ticket: HarvestTicket?, isOpen: Boolean) {
        _uiState.update {
            it.copy(
                selectedTicketForReceipt = ticket,
                isReceiptDialogOpen = isOpen
            )
        }
    }

    fun createHarvestTicket(
        driverName: String,
        vehiclePlate: String,
        buyerName: String,
        floor: Floor,
        pricePerKg: Double,
        farmerName: String,
        date: String,
        time: String,
        dayName: String,
        notes: String
    ): HarvestTicket {
        val state = _uiState.value
        val nextSeq = state.harvestTickets.size + 1
        val ticketId = "tkt_${System.currentTimeMillis()}"
        val ticketNo = "TKT-${state.currentPeriod.id}-%03d".format(nextSeq)

        val newTicket = HarvestTicket(
            id = ticketId,
            ticketNumber = ticketNo,
            date = date.ifBlank { "Hari ini" },
            time = time.ifBlank { "10:00 WIB" },
            dayName = dayName.ifBlank { "Hari ini" },
            farmerName = farmerName.ifBlank { state.defaultFarmerName },
            buyerName = buyerName.ifBlank { "Pembeli Ayam" },
            driverName = driverName.ifBlank { "Driver Armada" },
            vehiclePlate = vehiclePlate.ifBlank { "B 1234 XXX" }.uppercase(),
            floor = floor,
            pricePerKg = if (pricePerKg > 0) pricePerKg else 21500.0,
            entries = emptyList(),
            notes = notes,
            status = "Dalam Penimbangan"
        )

        _uiState.update {
            it.copy(
                harvestTickets = listOf(newTicket) + it.harvestTickets,
                isAddHarvestTicketDialogOpen = false,
                activeScaleTicketId = ticketId,
                isScaleWeighingSheetOpen = true,
                feedbackMessage = "Tiket Panen $ticketNo ($driverName - ${newTicket.vehiclePlate}) dibuat! Mulai pencatatan timbangan."
            )
        }
        persistData()
        return newTicket
    }

    fun addScaleEntry(
        ticketId: String,
        birdCount: Int,
        weightKg: Double,
        tareWeightKg: Double = 0.0
    ) {
        val state = _uiState.value
        val netKg = if (tareWeightKg > 0) (weightKg - tareWeightKg).coerceAtLeast(0.1) else weightKg.coerceAtLeast(0.1)

        val updatedTickets = state.harvestTickets.map { ticket ->
            if (ticket.id == ticketId) {
                val nextScaleNumber = ticket.entries.size + 1
                val newEntry = ScaleEntry(
                    scaleNumber = nextScaleNumber,
                    birdCount = birdCount,
                    netWeightKg = Math.round(netKg * 10.0) / 10.0,
                    grossWeightKg = Math.round(weightKg * 10.0) / 10.0,
                    tareWeightKg = Math.round(tareWeightKg * 10.0) / 10.0
                )
                ticket.copy(entries = ticket.entries + newEntry)
            } else ticket
        }

        val updatedTicket = updatedTickets.find { it.id == ticketId }
        val lastEntry = updatedTicket?.entries?.lastOrNull()

        _uiState.update {
            it.copy(
                harvestTickets = updatedTickets,
                feedbackMessage = if (lastEntry != null) {
                    "Timbangan #${lastEntry.scaleNumber} tercatat: ${lastEntry.birdCount} ekor = ${lastEntry.netWeightKg} kg (avg ${"%.2f".format(lastEntry.avgWeightKg)} kg)"
                } else null
            )
        }
        persistData()
    }

    fun removeScaleEntry(ticketId: String, scaleNumber: Int) {
        val state = _uiState.value
        val updatedTickets = state.harvestTickets.map { ticket ->
            if (ticket.id == ticketId) {
                val filtered = ticket.entries.filterNot { it.scaleNumber == scaleNumber }
                val renumbered = filtered.mapIndexed { idx, entry ->
                    entry.copy(scaleNumber = idx + 1)
                }
                ticket.copy(entries = renumbered)
            } else ticket
        }
        _uiState.update {
            it.copy(
                harvestTickets = updatedTickets,
                feedbackMessage = "Timbangan #$scaleNumber telah dihapus."
            )
        }
        persistData()
    }

    fun updateScaleEntry(ticketId: String, scaleNumber: Int, birdCount: Int, weightKg: Double) {
        val state = _uiState.value
        val updatedTickets = state.harvestTickets.map { ticket ->
            if (ticket.id == ticketId) {
                val updatedEntries = ticket.entries.map { entry ->
                    if (entry.scaleNumber == scaleNumber) {
                        val validKg = Math.round(weightKg.coerceAtLeast(0.1) * 10.0) / 10.0
                        entry.copy(
                            birdCount = birdCount.coerceAtLeast(1),
                            netWeightKg = validKg,
                            grossWeightKg = validKg
                        )
                    } else entry
                }
                ticket.copy(entries = updatedEntries)
            } else ticket
        }
        _uiState.update {
            it.copy(
                harvestTickets = updatedTickets,
                feedbackMessage = "Timbangan #$scaleNumber diperbarui: $birdCount ekor, $weightKg kg"
            )
        }
        persistData()
    }

    fun finalizeHarvestTicket(ticketId: String) {
        val state = _uiState.value
        var finishedTicket: HarvestTicket? = null

        val updatedTickets = state.harvestTickets.map { ticket ->
            if (ticket.id == ticketId) {
                val finalized = ticket.copy(status = "Selesai")
                finishedTicket = finalized
                finalized
            } else ticket
        }

        _uiState.update {
            it.copy(
                harvestTickets = updatedTickets,
                isScaleWeighingSheetOpen = false,
                activeScaleTicketId = null,
                selectedTicketForReceipt = finishedTicket,
                isReceiptDialogOpen = true,
                feedbackMessage = "Tiket ${finishedTicket?.ticketNumber} selesai! Total ${finishedTicket?.totalBirdCount} ekor (${"%.1f".format(finishedTicket?.totalWeightKg)} kg) tersinkron."
            )
        }
        persistData()
    }

    fun deleteHarvestTicket(ticketId: String) {
        val state = _uiState.value
        val ticket = state.harvestTickets.find { it.id == ticketId }
        val updatedTickets = state.harvestTickets.filterNot { it.id == ticketId }
        _uiState.update {
            it.copy(
                harvestTickets = updatedTickets,
                isScaleWeighingSheetOpen = if (it.activeScaleTicketId == ticketId) false else it.isScaleWeighingSheetOpen,
                activeScaleTicketId = if (it.activeScaleTicketId == ticketId) null else it.activeScaleTicketId,
                feedbackMessage = "Tiket ${ticket?.ticketNumber ?: ticketId} dihapus."
            )
        }
        persistData()
    }

    fun updateHarvestTicket(updatedTicket: HarvestTicket) {
        val updatedTickets = _uiState.value.harvestTickets.map {
            if (it.id == updatedTicket.id) updatedTicket else it
        }
        _uiState.update {
            it.copy(
                harvestTickets = updatedTickets,
                feedbackMessage = "Surat jalan tiket ${updatedTicket.ticketNumber} (${updatedTicket.driverName}) berhasil diperbarui!"
            )
        }
        persistData()
    }

    // ==========================================
    // LIVESTOCK & PERFORMANCE FORMULA CALCULATIONS
    // ==========================================

    fun getFilteredRecordings(floor: Floor): List<DailyRecording> {
        val recordings = _uiState.value.dailyRecordings
        return if (floor == Floor.ALL) recordings else recordings.filter { it.floor == floor }
    }

    fun calculateFloorSummary(floor: Floor): FloorMetricSummary {
        val state = _uiState.value
        val period = state.currentPeriod
        val records = getFilteredRecordings(floor)

        val initialPop = when (floor) {
            Floor.L1 -> period.initialPopulationL1
            Floor.L2 -> period.initialPopulationL2
            Floor.ALL -> period.totalInitialPopulation
        }

        val totalMortality = records.sumOf { it.mortalityCount }
        val totalCulling = records.sumOf { it.cullingCount }
        val totalLoss = totalMortality + totalCulling
        val activePopulation = (initialPop - totalLoss).coerceAtLeast(0)

        // Deplesi % = (Total Kematian / Populasi Awal) * 100
        val depletionRate = if (initialPop > 0) (totalLoss.toDouble() / initialPop) * 100.0 else 0.0

        // Livability % = 100 - Deplesi
        val livability = (100.0 - depletionRate).coerceIn(0.0, 100.0)

        // Total Pakan Habis (kg)
        val totalFeedKg = records.sumOf { it.feedAmountKg }

        // SINKRONISASI TIMBANGAN PANEN AKTUAL
        val relevantTickets = state.harvestTickets.filter {
            floor == Floor.ALL || it.floor == floor || it.floor == Floor.ALL
        }
        val totalHarvestedBirds = relevantTickets.sumOf { it.totalBirdCount }
        val totalHarvestedWeightKg = relevantTickets.sumOf { it.totalWeightKg }

        // Latest Sample Average Weight (kg)
        val latestWeightGram = records.maxByOrNull { it.dayNumber }?.sampleWeightGram ?: 1750.0
        val sampleAvgWeightKg = latestWeightGram / 1000.0

        // Bobot Rata-rata: jika ada timbangan panen nyata, gunakan bobot riil panen
        val latestAvgWeightKg = if (totalHarvestedBirds > 0 && totalHarvestedWeightKg > 0) {
            totalHarvestedWeightKg / totalHarvestedBirds
        } else {
            sampleAvgWeightKg
        }

        // Total Tonase Panen (kg) = Terakumulasi timbangan nyata + sisa ayam aktif di kandang
        val remainingBirds = (activePopulation - totalHarvestedBirds).coerceAtLeast(0)
        val totalTonaseKg = if (totalHarvestedWeightKg > 0) {
            totalHarvestedWeightKg + (remainingBirds * sampleAvgWeightKg)
        } else {
            activePopulation * sampleAvgWeightKg
        }

        // FCR = Total Pakan Habis (kg) / Total Tonase Panen (kg)
        val fcr = if (totalTonaseKg > 0) totalFeedKg / totalTonaseKg else 0.0

        // SINKRONISASI UMUR AYAM SESUAI RECORDING HARIAN
        val maxRecordingDay = records.maxOfOrNull { it.dayNumber }
        val ageDays = (maxRecordingDay ?: 1).coerceAtLeast(1)

        // IP (Indeks Performa) = (Daya Hidup % * Berat Rata-rata kg) / (FCR * Umur Panen) * 100
        val ip = if (fcr > 0 && ageDays > 0) {
            (livability * latestAvgWeightKg) / (fcr * ageDays) * 100.0
        } else 0.0

        val ipCategory = when {
            ip >= 400 -> "Sangat Istimewa"
            ip >= 351 -> "Sangat Baik"
            ip >= 326 -> "Baik"
            ip >= 301 -> "Sedang"
            else -> "Perlu Evaluasi"
        }

        return FloorMetricSummary(
            floor = floor,
            initialPopulation = initialPop,
            activePopulation = activePopulation,
            totalMortality = totalMortality,
            totalCulling = totalCulling,
            depletionRatePercent = Math.round(depletionRate * 100.0) / 100.0,
            livabilityPercent = Math.round(livability * 100.0) / 100.0,
            totalFeedKg = Math.round(totalFeedKg * 10.0) / 10.0,
            latestAvgWeightKg = Math.round(latestAvgWeightKg * 1000.0) / 1000.0,
            totalTonaseKg = Math.round(totalTonaseKg * 10.0) / 10.0,
            fcr = Math.round(fcr * 100.0) / 100.0,
            performanceIndex = Math.round(ip * 10.0) / 10.0,
            ipCategory = ipCategory,
            ageDays = ageDays
        )
    }

    fun setSimulatedSellingPrice(price: Double) {
        val validPrice = if (price > 0) price else 21500.0
        _uiState.update { it.copy(simulatedSellingPricePerKg = validPrice) }
    }

    /**
     * Calculate Total Production Costs (HPP) & Comprehensive Profit/Loss Analysis
     */
    fun calculateCostSummary(): CostBreakdown {
        val state = _uiState.value
        val period = state.currentPeriod
        val allSummary = calculateFloorSummary(Floor.ALL)

        // 1. DOC cost
        val docCost = period.totalInitialPopulation * period.docPricePerBird

        // 2. Feed cost = sum(feed consumption * feed price)
        var feedCost = 0.0
        state.dailyRecordings.forEach { rec ->
            val feed = state.feeds.find { it.id == rec.feedTypeId }
            val price = feed?.pricePerKg ?: 9200.0
            feedCost += rec.feedAmountKg * price
        }

        // 3. OVK cost
        var ovkCost = 0.0
        state.dailyRecordings.forEach { rec ->
            rec.ovkUsages.forEach { usage ->
                ovkCost += usage.cost
            }
        }
        if (ovkCost == 0.0) {
            // Include baseline OVK value if initial usages weren't granularly logged
            ovkCost = 2850000.0
        }

        // 4. Operational costs (listrik, sekam/atal, pemanas, transport, bongkar muat, dll)
        val operationalCost = state.operationalExpenses.sumOf { it.amount }

        val totalCost = docCost + feedCost + ovkCost + operationalCost
        val totalTonaseKg = allSummary.totalTonaseKg
        val activePopulation = allSummary.activePopulation
        val hppPerKg = if (totalTonaseKg > 0) totalCost / totalTonaseKg else 0.0
        val hppPerBird = if (activePopulation > 0) totalCost / activePopulation else 0.0

        // Live bird selling price derived from actual harvest tickets (internal peternak)
        val harvestTickets = state.harvestTickets
        val totalHarvestWeightKg = harvestTickets.sumOf { it.totalWeightKg }
        val totalHarvestRevenue = harvestTickets.sumOf { it.totalAmount }

        val sellingPricePerKg = when {
            state.simulatedSellingPricePerKg != 21500.0 -> state.simulatedSellingPricePerKg
            totalHarvestWeightKg > 0 -> totalHarvestRevenue / totalHarvestWeightKg
            harvestTickets.isNotEmpty() && harvestTickets.first().pricePerKg > 0 -> harvestTickets.first().pricePerKg
            else -> 21500.0
        }

        // Gross Revenue: Realized revenue from weighed tickets + projected value of remaining tonase
        val grossRevenue = if (totalHarvestWeightKg > 0) {
            val remainingTonase = (totalTonaseKg - totalHarvestWeightKg).coerceAtLeast(0.0)
            totalHarvestRevenue + (remainingTonase * sellingPricePerKg)
        } else {
            totalTonaseKg * sellingPricePerKg
        }
        val netProfitLoss = grossRevenue - totalCost
        val isProfit = netProfitLoss >= 0
        val profitMarginPercent = if (grossRevenue > 0) (netProfitLoss / grossRevenue) * 100.0 else 0.0
        val profitPerKg = if (totalTonaseKg > 0) netProfitLoss / totalTonaseKg else 0.0
        val profitPerBird = if (activePopulation > 0) netProfitLoss / activePopulation else 0.0
        val bepPricePerKg = if (totalTonaseKg > 0) totalCost / totalTonaseKg else 0.0

        return CostBreakdown(
            docCost = docCost,
            feedCost = feedCost,
            ovkCost = ovkCost,
            operationalCost = operationalCost,
            totalProductionCost = totalCost,
            hppPerKg = Math.round(hppPerKg).toDouble(),
            hppPerBird = Math.round(hppPerBird).toDouble(),
            estimatedRevenue = grossRevenue,
            estimatedProfit = netProfitLoss,
            sellingPricePerKg = sellingPricePerKg,
            grossRevenue = grossRevenue,
            netProfitLoss = netProfitLoss,
            isProfit = isProfit,
            profitMarginPercent = profitMarginPercent,
            profitPerKg = profitPerKg,
            profitPerBird = profitPerBird,
            bepPricePerKg = bepPricePerKg,
            totalTonaseKg = totalTonaseKg,
            activePopulation = activePopulation
        )
    }

    companion object {
        fun formatRupiah(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            return format.format(amount)
        }

        fun formatNumber(amount: Double): String {
            val format = NumberFormat.getNumberInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 1
            return format.format(amount)
        }

        /**
         * Calculate chicken age (Hari ke-N) based on chick-in date
         */
        fun calculateAgeDaysFromStartDate(startDateStr: String, referenceDate: Date = Date()): Int? {
            if (startDateStr.isBlank() || startDateStr.equals("Hari ini", ignoreCase = true)) {
                return 1
            }
            val cleanStr = startDateStr.trim()
                .replace("Agt", "Agu", ignoreCase = true)
                .replace("Agustus", "Agu", ignoreCase = true)
                .replace("Oktober", "Okt", ignoreCase = true)
                .replace("Desember", "Des", ignoreCase = true)

            val formats = listOf(
                SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
                SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
                SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
                SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH),
                SimpleDateFormat("yyyy-MM-dd", Locale.ROOT),
                SimpleDateFormat("dd-MM-yyyy", Locale.ROOT),
                SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
            )
            for (sdf in formats) {
                try {
                    sdf.isLenient = true
                    val parsed = sdf.parse(cleanStr)
                    if (parsed != null) {
                        val diffMillis = referenceDate.time - parsed.time
                        val days = (diffMillis / (1000L * 60 * 60 * 24)).toInt()
                        // Day of chick-in is Day 1
                        return (days + 1).coerceAtLeast(1)
                    }
                } catch (_: Exception) {
                    // Try next pattern
                }
            }
            return null
        }
    }
}

data class CostBreakdown(
    val docCost: Double,
    val feedCost: Double,
    val ovkCost: Double,
    val operationalCost: Double,
    val totalProductionCost: Double,
    val hppPerKg: Double,
    val hppPerBird: Double,
    val estimatedRevenue: Double,
    val estimatedProfit: Double,
    val sellingPricePerKg: Double = 21500.0,
    val grossRevenue: Double = estimatedRevenue,
    val netProfitLoss: Double = estimatedProfit,
    val isProfit: Boolean = estimatedProfit >= 0,
    val profitMarginPercent: Double = 0.0,
    val profitPerKg: Double = 0.0,
    val profitPerBird: Double = 0.0,
    val bepPricePerKg: Double = hppPerKg,
    val totalTonaseKg: Double = 0.0,
    val activePopulation: Int = 0
)
