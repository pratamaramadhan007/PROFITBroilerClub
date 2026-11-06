package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DailyRecording
import com.example.data.model.Floor
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerUiState

@Composable
fun EditRecordingDialog(
    recording: DailyRecording,
    state: BroilerUiState,
    onDismiss: () -> Unit,
    onSubmit: (DailyRecording) -> Unit
) {
    var selectedFloor by remember { mutableStateOf(recording.floor) }
    var dayInput by remember { mutableStateOf(recording.dayNumber.toString()) }
    var mortalityInput by remember { mutableStateOf(recording.mortalityCount.toString()) }
    var cullingInput by remember { mutableStateOf(recording.cullingCount.toString()) }
    var sampleWeightInput by remember { mutableStateOf(recording.sampleWeightGram.toString()) }
    var feedAmountInput by remember { mutableStateOf(recording.feedAmountKg.toString()) }
    var temperatureInput by remember { mutableStateOf(recording.temperatureC.toString()) }
    var humidityInput by remember { mutableStateOf(recording.humidityPercent.toString()) }
    var notesInput by remember { mutableStateOf(recording.notes) }

    var selectedFeedId by remember { mutableStateOf(recording.feedTypeId) }
    val selectedFeed = state.feeds.find { it.id == selectedFeedId } ?: state.feeds.firstOrNull()

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
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
                    Column {
                        Text(
                            text = "Edit Recording Harian",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${recording.floor.displayName} • Hari ke-${recording.dayNumber}",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Floor Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedFloor = Floor.L1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedFloor == Floor.L1) Floor1Color else CyberSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).testTag("dialog_edit_select_l1"),
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
                        modifier = Modifier.weight(1f).testTag("dialog_edit_select_l2"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Lantai 2",
                            color = if (selectedFloor == Floor.L2) CyberDark else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row: Umur & Berat Rata-rata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dayInput,
                        onValueChange = { dayInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Hari ke-", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dialog_edit_input_day")
                    )

                    OutlinedTextField(
                        value = sampleWeightInput,
                        onValueChange = { sampleWeightInput = it },
                        label = { Text("Bobot (Gram)", fontSize = 12.sp) },
                        placeholder = { Text("cth: 1750") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f).testTag("dialog_edit_input_weight")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row: Kematian & Afkir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = mortalityInput,
                        onValueChange = { mortalityInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Mati (Ekor)", fontSize = 12.sp) },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dialog_edit_input_mortality")
                    )

                    OutlinedTextField(
                        value = cullingInput,
                        onValueChange = { cullingInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Afkir (Ekor)", fontSize = 12.sp) },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dialog_edit_input_culling")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Feed Type Selector
                Text("Pilih Jenis Pakan:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.feeds.forEach { feed ->
                        val isSelected = feed.id == selectedFeedId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberDarkElevated)
                                .border(1.dp, if (isSelected) NeonCyan else CyberCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedFeedId = feed.id }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = feed.name,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Feed Amount (Kg)
                OutlinedTextField(
                    value = feedAmountInput,
                    onValueChange = { feedAmountInput = it },
                    label = { Text("Jumlah Konsumsi Pakan (Kg)", fontSize = 12.sp) },
                    placeholder = { Text("cth: 550") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_edit_input_feed_amount")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Temperature & Humidity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = temperatureInput,
                        onValueChange = { temperatureInput = it },
                        label = { Text("Suhu (°C)", fontSize = 12.sp) },
                        placeholder = { Text("29.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = humidityInput,
                        onValueChange = { humidityInput = it },
                        label = { Text("Kelembaban (%)", fontSize = 12.sp) },
                        placeholder = { Text("65") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Catatan Harian (Opsional)", fontSize = 12.sp) },
                    placeholder = { Text("Kondisi sekat, kipas atau sekam...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = NeonRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val day = dayInput.toIntOrNull() ?: recording.dayNumber
                            val mort = mortalityInput.toIntOrNull() ?: 0
                            val cull = cullingInput.toIntOrNull() ?: 0
                            val weight = sampleWeightInput.toDoubleOrNull() ?: recording.sampleWeightGram
                            val feedKg = feedAmountInput.toDoubleOrNull() ?: recording.feedAmountKg
                            val temp = temperatureInput.toDoubleOrNull() ?: recording.temperatureC
                            val hum = humidityInput.toDoubleOrNull() ?: recording.humidityPercent

                            if (feedKg <= 0) {
                                errorMessage = "Pakan terpakai harus lebih besar dari 0 kg!"
                                return@Button
                            }

                            val updated = recording.copy(
                                floor = selectedFloor,
                                dayNumber = day,
                                mortalityCount = mort,
                                cullingCount = cull,
                                sampleWeightGram = weight,
                                feedTypeId = selectedFeedId,
                                feedTypeName = selectedFeed?.name ?: recording.feedTypeName,
                                feedAmountKg = feedKg,
                                temperatureC = temp,
                                humidityPercent = hum,
                                notes = notesInput
                            )
                            onSubmit(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_edit_btn_save")
                    ) {
                        Text("Simpan Perubahan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
