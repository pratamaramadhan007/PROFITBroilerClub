package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PeriodData
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerViewModel

@Composable
fun EditDocChickInDialog(
    currentPeriod: PeriodData,
    onDismiss: () -> Unit,
    onSubmit: (
        startDate: String,
        docEntryTime: String,
        popL1: Int,
        popL2: Int,
        docPrice: Double,
        strain: String,
        hatchery: String
    ) -> Unit
) {
    var dateInput by remember { mutableStateOf(currentPeriod.startDate) }
    var timeInput by remember { mutableStateOf(currentPeriod.docEntryTime) }
    var popL1Input by remember { mutableStateOf(currentPeriod.initialPopulationL1.toString()) }
    var popL2Input by remember { mutableStateOf(currentPeriod.initialPopulationL2.toString()) }
    var docPriceInput by remember { mutableStateOf(currentPeriod.docPricePerBird.toInt().toString()) }
    var strainInput by remember { mutableStateOf(currentPeriod.docStrain) }
    var hatcheryInput by remember { mutableStateOf(currentPeriod.docHatchery) }

    val popL1Num = popL1Input.toIntOrNull() ?: 0
    val popL2Num = popL2Input.toIntOrNull() ?: 0
    val totalPop = popL1Num + popL2Num
    val docPriceNum = docPriceInput.toDoubleOrNull() ?: 0.0
    val totalDocCost = totalPop * docPriceNum

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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NeonGreen.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, NeonGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Input Data DOC Masuk",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Chick-In & Alokasi Lantai",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Text(
                    text = "Input tanggal & waktu kedatangan bibit DOC, serta tentukan jumlah ekor per lantai.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Tanggal dan Waktu Masuk DOC
                Text(
                    text = "1. Tanggal & Waktu DOC Tiba (Chick-In)",
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("Tanggal Masuk") },
                        placeholder = { Text("07 Okt 2026") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("input_doc_date"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = { Text("Waktu / Jam") },
                        placeholder = { Text("02:30 WIB") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("input_doc_time"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Jumlah DOC Setiap Lantai
                Text(
                    text = "2. Jumlah Populasi DOC Tiap Lantai",
                    color = NeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = popL1Input,
                        onValueChange = { input ->
                            popL1Input = input.filter { it.isDigit() }
                        },
                        label = { Text("DOC Lantai 1 (Ekor)") },
                        placeholder = { Text("10000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_doc_pop_l1"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Floor1Color,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = popL2Input,
                        onValueChange = { input ->
                            popL2Input = input.filter { it.isDigit() }
                        },
                        label = { Text("DOC Lantai 2 (Ekor)") },
                        placeholder = { Text("10000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_doc_pop_l2"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Floor2Color,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Preset Chips for Easy Population Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "10k + 10k" to (10000 to 10000),
                        "12.5k + 12.5k" to (12500 to 12500),
                        "15k + 15k" to (15000 to 15000),
                        "Bagi Rata" to null
                    ).forEach { (label, preset) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberDarkElevated)
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (preset != null) {
                                        popL1Input = preset.first.toString()
                                        popL2Input = preset.second.toString()
                                    } else {
                                        val half = totalPop / 2
                                        popL1Input = half.toString()
                                        popL2Input = (totalPop - half).toString()
                                    }
                                }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Total Populasi & Cost Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberDarkElevated)
                        .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL DOC MASUK:",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${BroilerViewModel.formatNumber(totalPop.toDouble())} Ekor",
                                color = NeonGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        if (totalDocCost > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Biaya DOC (HPP):",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = BroilerViewModel.formatRupiah(totalDocCost),
                                    color = NeonAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Harga DOC & Identitas Bibit
                Text(
                    text = "3. Harga & Identitas Bibit DOC",
                    color = NeonAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = docPriceInput,
                    onValueChange = { input ->
                        docPriceInput = input.filter { it.isDigit() }
                    },
                    label = { Text("Harga DOC per Ekor (Rp)") },
                    prefix = { Text("Rp ", color = NeonGreen, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_doc_price"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonAmber,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = strainInput,
                        onValueChange = { strainInput = it },
                        label = { Text("Strain Bibit") },
                        placeholder = { Text("Cobb 500 / Ross") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCardBorder,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = hatcheryInput,
                        onValueChange = { hatcheryInput = it },
                        label = { Text("Hatchery / Asal") },
                        placeholder = { Text("Japfa / CP") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCardBorder,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
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
                            onSubmit(
                                dateInput.ifBlank { currentPeriod.startDate },
                                timeInput.ifBlank { currentPeriod.docEntryTime },
                                popL1Num,
                                popL2Num,
                                docPriceNum,
                                strainInput.ifBlank { currentPeriod.docStrain },
                                hatcheryInput.ifBlank { currentPeriod.docHatchery }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_doc_chick_in")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = CyberDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Data DOC", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
