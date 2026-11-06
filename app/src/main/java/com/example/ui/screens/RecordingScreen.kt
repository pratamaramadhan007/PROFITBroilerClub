package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EditRecordingDialog
import com.example.ui.components.NeonBadge
import com.example.ui.components.NeonCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerUiState
import com.example.ui.viewmodel.BroilerViewModel

@Composable
fun RecordingScreen(
    state: BroilerUiState,
    viewModel: BroilerViewModel
) {
    var recordingFloorTab by remember { mutableStateOf(Floor.L1) }
    var recordingToEdit by remember { mutableStateOf<DailyRecording?>(null) }
    var recordingToDelete by remember { mutableStateOf<DailyRecording?>(null) }

    val recordings = remember(state.dailyRecordings, recordingFloorTab) {
        state.dailyRecordings
            .filter { it.floor == recordingFloorTab }
            .sortedByDescending { it.dayNumber }
    }

    Scaffold(
        containerColor = CyberDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddRecordingDialog(true) },
                containerColor = NeonGreen,
                contentColor = CyberDark,
                modifier = Modifier.testTag("fab_add_recording")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Input Recording", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recording Harian Kandang",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manajemen data produksi closed house 2 lantai",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                NeonBadge(
                    text = "${recordings.size} Data Entri",
                    color = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Bar: Lantai 1 vs Lantai 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FloorTabItem(
                    title = "LANTAI 1 (Bawah)",
                    isSelected = recordingFloorTab == Floor.L1,
                    activeColor = Floor1Color,
                    modifier = Modifier.weight(1f).testTag("tab_floor_1"),
                    onClick = { recordingFloorTab = Floor.L1 }
                )
                FloorTabItem(
                    title = "LANTAI 2 (Atas)",
                    isSelected = recordingFloorTab == Floor.L2,
                    activeColor = Floor2Color,
                    modifier = Modifier.weight(1f).testTag("tab_floor_2"),
                    onClick = { recordingFloorTab = Floor.L2 }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Banner for Selected Floor
            val floorSummary = viewModel.calculateFloorSummary(recordingFloorTab)
            NeonCard(
                borderColor = if (recordingFloorTab == Floor.L1) Floor1Color.copy(alpha = 0.5f) else Floor2Color.copy(alpha = 0.5f),
                glowColor = if (recordingFloorTab == Floor.L1) Floor1Color.copy(alpha = 0.1f) else Floor2Color.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ringkasan ${recordingFloorTab.displayName}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Populasi Aktif: ${floorSummary.activePopulation} ekor",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Deplesi", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "${floorSummary.depletionRatePercent}%",
                                color = if (floorSummary.depletionRatePercent <= 3.0) NeonGreen else NeonAmber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pakan Habis", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "${(floorSummary.totalFeedKg / 1000).toInt()} Ton",
                                color = NeonCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of Daily Records
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recordings, key = { it.id }) { item ->
                    DailyRecordItemCard(
                        item = item,
                        onEdit = { recordingToEdit = item },
                        onDelete = { recordingToDelete = item }
                    )
                }
            }
        }
    }

    // Add Recording Form Dialog
    if (state.isAddRecordingDialogOpen) {
        AddRecordingDialog(
            state = state,
            initialFloor = recordingFloorTab,
            onDismiss = { viewModel.openAddRecordingDialog(false) },
            onSubmit = { floor, day, mort, cull, feedId, feedKg, weight, temp, hum, ovk, notes ->
                viewModel.addDailyRecording(
                    floor = floor,
                    dayNumber = day,
                    mortality = mort,
                    culling = cull,
                    feedTypeId = feedId,
                    feedAmountKg = feedKg,
                    sampleWeightGram = weight,
                    temperatureC = temp,
                    humidityPercent = hum,
                    ovkUsageList = ovk,
                    notes = notes
                )
            }
        )
    }

    // Edit Recording Dialog
    val currentEditTarget = recordingToEdit
    if (currentEditTarget != null) {
        EditRecordingDialog(
            recording = currentEditTarget,
            state = state,
            onDismiss = { recordingToEdit = null },
            onSubmit = { updated ->
                viewModel.updateDailyRecording(updated)
                recordingToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    val currentDeleteTarget = recordingToDelete
    if (currentDeleteTarget != null) {
        DeleteConfirmDialog(
            title = "Hapus Data Recording?",
            message = "Apakah Anda yakin ingin menghapus data recording ${currentDeleteTarget.floor.displayName} Hari ke-${currentDeleteTarget.dayNumber}? Stok pakan (${currentDeleteTarget.feedAmountKg} kg) akan otomatis dikembalikan.",
            itemName = "${currentDeleteTarget.floor.displayName} • Hari ke-${currentDeleteTarget.dayNumber} • Pakan ${currentDeleteTarget.feedAmountKg} Kg",
            onConfirm = {
                viewModel.deleteDailyRecording(currentDeleteTarget.id)
                recordingToDelete = null
            },
            onDismiss = { recordingToDelete = null }
        )
    }
}

@Composable
fun FloorTabItem(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) activeColor else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun DailyRecordItemCard(
    item: DailyRecording,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val floorColor = if (item.floor == Floor.L1) Floor1Color else Floor2Color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header Row: Day & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(floorColor.copy(alpha = 0.15f))
                            .border(1.dp, floorColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Hari ke-${item.dayNumber}",
                            color = floorColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.floor.displayName,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = item.date,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Key Metrics in 3 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Mati / Afkir", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = "${item.mortalityCount} + ${item.cullingCount} ekor",
                        color = if (item.mortalityCount > 15) NeonRed else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Text("Pakan Terpakai", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = "${item.feedAmountKg} kg (${(item.feedAmountKg / 50.0).let { "%.1f".format(it) }} sak)",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Bobot Rata-rata", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = "${item.sampleWeightGram} gr",
                        color = NeonGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Feed Name & Climate Subrow
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pakan: ${item.feedTypeName}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "${item.temperatureC}°C • ${item.humidityPercent}% RH",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catatan: ${item.notes}",
                    color = ElectricBlue,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons: Edit & Hapus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_edit_recording_${item.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NeonCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_delete_recording_${item.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = NeonRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordingDialog(
    state: BroilerUiState,
    initialFloor: Floor,
    onDismiss: () -> Unit,
    onSubmit: (Floor, Int, Int, Int, String, Double, Double, Double, Double, List<OvkUsageEntry>, String) -> Boolean
) {
    var selectedFloor by remember { mutableStateOf(if (initialFloor == Floor.ALL) Floor.L1 else initialFloor) }
    val existingRecords = state.dailyRecordings.filter { it.floor == selectedFloor }
    val nextDayNumber = if (existingRecords.isNotEmpty()) {
        existingRecords.maxOf { it.dayNumber } + 1
    } else {
        (BroilerViewModel.calculateAgeDaysFromStartDate(state.currentPeriod.startDate) ?: state.currentPeriod.currentAgeDays).coerceAtLeast(1)
    }
    var dayInput by remember(selectedFloor) { mutableStateOf(nextDayNumber.toString()) }
    var mortalityInput by remember { mutableStateOf("") }
    var cullingInput by remember { mutableStateOf("0") }
    var sampleWeightInput by remember { mutableStateOf("") }
    var feedAmountInput by remember { mutableStateOf("") }
    var temperatureInput by remember { mutableStateOf("29.5") }
    var humidityInput by remember { mutableStateOf("65") }
    var notesInput by remember { mutableStateOf("") }

    // Selected feed (default to first feed with stock > 0)
    var selectedFeedId by remember {
        mutableStateOf(state.feeds.firstOrNull { it.currentStockKg > 0 }?.id ?: state.feeds.firstOrNull()?.id ?: "")
    }

    val selectedFeed = state.feeds.find { it.id == selectedFeedId }
    val isFeedOutOfStock = selectedFeed?.isOutOfStock == true

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Input Recording Harian",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Text(
                    text = "Pilih lantai dan catat mortalitas, konsumsi pakan, serta bobot",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Floor Selector Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedFloor = Floor.L1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedFloor == Floor.L1) Floor1Color else CyberSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).testTag("dialog_select_l1"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Lantai 1",
                            color = if (selectedFloor == Floor.L1) CyberDark else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { selectedFloor = Floor.L2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedFloor == Floor.L2) Floor2Color else CyberSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).testTag("dialog_select_l2"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Lantai 2",
                            color = if (selectedFloor == Floor.L2) CyberDark else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day & Age
                OutlinedTextField(
                    value = dayInput,
                    onValueChange = { dayInput = it },
                    label = { Text("Umur Hari Ke-") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_day"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mortality & Culling Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = mortalityInput,
                        onValueChange = { mortalityInput = it },
                        label = { Text("Mati (ekor)") },
                        placeholder = { Text("Contoh: 8") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_mortality"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cullingInput,
                        onValueChange = { cullingInput = it },
                        label = { Text("Afkir (ekor)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_culling"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sample Weight
                OutlinedTextField(
                    value = sampleWeightInput,
                    onValueChange = { sampleWeightInput = it },
                    label = { Text("Bobot Sampel (Gram / Ekor)") },
                    placeholder = { Text("Contoh: 1820") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_sample_weight"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // FEED SELECTION & SAFETY GUARD (CRITICAL REQUIREMENT)
                Text(
                    text = "Pilih Jenis Pakan (Inventaris):",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurfaceVariant)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.feeds.forEach { feed ->
                        val isSelected = (feed.id == selectedFeedId)
                        val outOfStock = feed.isOutOfStock

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        outOfStock -> Color.Red.copy(alpha = 0.08f)
                                        isSelected -> NeonCyan.copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isSelected -> NeonCyan
                                        outOfStock -> NeonRed.copy(alpha = 0.4f)
                                        else -> Color.Transparent
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !outOfStock) {
                                    selectedFeedId = feed.id
                                    errorMessage = null
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = feed.name,
                                    color = if (outOfStock) TextMuted else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = if (outOfStock) "Sisa: 0 kg (STOK HABIS!)" else "Sisa: ${feed.currentStockKg} kg (${"%.1f".format(feed.currentStockSak)} sak)",
                                    color = if (outOfStock) NeonRed else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            if (outOfStock) {
                                NeonBadge(text = "TERKUNCI (HABIS)", color = NeonRed)
                            } else if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Terpilih", tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (isFeedOutOfStock) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Peringatan: Jenis pakan yang dipilih habis. Tambah stok di menu Inventaris!",
                        color = NeonRed,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Feed Amount Input
                OutlinedTextField(
                    value = feedAmountInput,
                    onValueChange = { feedAmountInput = it },
                    label = { Text("Jumlah Pakan Terpakai (Kg)") },
                    placeholder = { Text("Contoh: 1450") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_feed_amount"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Climate (Temp & Humidity)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = temperatureInput,
                        onValueChange = { temperatureInput = it },
                        label = { Text("Suhu (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = humidityInput,
                        onValueChange = { humidityInput = it },
                        label = { Text("Kelembapan (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Catatan / OVK / Sekam (Opsional)") },
                    placeholder = { Text("Contoh: Pemberian vitamin Vita Stress") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = NeonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val day = dayInput.toIntOrNull() ?: 1
                            val mort = mortalityInput.toIntOrNull() ?: 0
                            val cull = cullingInput.toIntOrNull() ?: 0
                            val weight = sampleWeightInput.toDoubleOrNull() ?: 0.0
                            val feedKg = feedAmountInput.toDoubleOrNull() ?: 0.0
                            val temp = temperatureInput.toDoubleOrNull() ?: 29.5
                            val hum = humidityInput.toDoubleOrNull() ?: 65.0

                            if (isFeedOutOfStock) {
                                errorMessage = "Gagal: Pakan yang dipilih sedang habis!"
                                return@Button
                            }
                            if (feedKg <= 0) {
                                errorMessage = "Masukkan jumlah pakan terpakai (> 0 kg)!"
                                return@Button
                            }
                            if (weight <= 0) {
                                errorMessage = "Masukkan bobot sampel (> 0 gram)!"
                                return@Button
                            }

                            val success = onSubmit(
                                selectedFloor,
                                day,
                                mort,
                                cull,
                                selectedFeedId,
                                feedKg,
                                weight,
                                temp,
                                hum,
                                emptyList(),
                                notesInput
                            )
                            if (!success) {
                                errorMessage = "Gagal menyimpan recording. Cek ketersediaan stok!"
                            }
                        },
                        enabled = !isFeedOutOfStock,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.weight(1f).testTag("btn_submit_recording"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
