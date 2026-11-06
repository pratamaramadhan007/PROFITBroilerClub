package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Floor
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerUiState
import com.example.ui.viewmodel.BroilerViewModel

@Composable
fun ClosePeriodDialog(
    state: BroilerUiState,
    viewModel: BroilerViewModel,
    onDismiss: () -> Unit
) {
    val currentPeriod = state.currentPeriod
    val allSummary = viewModel.calculateFloorSummary(Floor.ALL)
    val costSummary = viewModel.calculateCostSummary()

    val currentNum = currentPeriod.id.filter { it.isDigit() }.toIntOrNull() ?: 14
    val nextNum = currentNum + 1

    val liveTime = rememberLiveDateTime()
    var newPeriodTitleInput by remember { mutableStateOf("Periode $nextNum (Aktif - DOC Baru)") }
    var docDateInput by remember { mutableStateOf(liveTime.dateFormatted) }
    var docTimeInput by remember { mutableStateOf("02:30 WIB") }
    var popL1Input by remember { mutableStateOf(currentPeriod.initialPopulationL1.toString()) }
    var popL2Input by remember { mutableStateOf(currentPeriod.initialPopulationL2.toString()) }
    var docPriceInput by remember { mutableStateOf(currentPeriod.docPricePerBird.toInt().toString()) }
    var liveBirdPriceInput by remember { mutableStateOf("20500") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.5.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tutup Periode Panen",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Text(
                    text = "Finalisasi hasil panen periode saat ini dan otomatis simpan ke 'Periode Lalu' sebagai bahan komparasi.",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current Period Final Performance Summary Card
                NeonCard(
                    borderColor = NeonCyan,
                    glowColor = NeonCyan.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HASIL AKHIR: ${currentPeriod.title}",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        NeonBadge(text = allSummary.ipCategory, color = NeonGreen)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Indeks Performa (IP)", color = TextMuted, fontSize = 10.sp)
                            Text("%.1f".format(allSummary.performanceIndex), color = NeonGreen, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("FCR Akhir", color = TextMuted, fontSize = 10.sp)
                            Text("%.2f".format(allSummary.fcr), color = NeonCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Deplesi", color = TextMuted, fontSize = 10.sp)
                            Text("%.2f%%".format(allSummary.depletionRatePercent), color = NeonAmber, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tonase: ${"%.1f".format(allSummary.totalTonaseKg / 1000.0)} Ton (${allSummary.activePopulation} ekor)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Laba: ${BroilerViewModel.formatRupiah(costSummary.estimatedProfit)}",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Konfigurasi Periode Baru (Mendatang):",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPeriodTitleInput,
                    onValueChange = { newPeriodTitleInput = it },
                    label = { Text("Nama / Judul Periode Baru") },
                    modifier = Modifier.fillMaxWidth().testTag("input_new_period_title"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = docDateInput,
                        onValueChange = { docDateInput = it },
                        label = { Text("Tanggal DOC Masuk") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("input_new_doc_date"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = docTimeInput,
                        onValueChange = { docTimeInput = it },
                        label = { Text("Waktu Masuk") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f).testTag("input_new_doc_time"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = popL1Input,
                        onValueChange = { popL1Input = it },
                        label = { Text("DOC Lantai 1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_new_pop_l1"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = popL2Input,
                        onValueChange = { popL2Input = it },
                        label = { Text("DOC Lantai 2") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_new_pop_l2"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = docPriceInput,
                        onValueChange = { docPriceInput = it },
                        label = { Text("Harga DOC/Ekor (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = liveBirdPriceInput,
                        onValueChange = { liveBirdPriceInput = it },
                        label = { Text("Harga Panen/Kg (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val popL1 = popL1Input.toIntOrNull() ?: 10000
                            val popL2 = popL2Input.toIntOrNull() ?: 10000
                            val docPrice = docPriceInput.toDoubleOrNull() ?: 7500.0
                            val liveBirdPrice = liveBirdPriceInput.toDoubleOrNull() ?: 20500.0

                            viewModel.closeCurrentPeriodAndStartNew(
                                newPeriodTitle = newPeriodTitleInput,
                                newPopL1 = popL1,
                                newPopL2 = popL2,
                                docPrice = docPrice,
                                marketPricePerKg = liveBirdPrice,
                                docDate = docDateInput,
                                docTime = docTimeInput
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.weight(1.5f).testTag("btn_confirm_close_period"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null, tint = CyberDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tutup & Buka Baru", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
