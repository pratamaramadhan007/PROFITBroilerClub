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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Floor
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerUiState
import com.example.ui.viewmodel.BroilerViewModel

@Composable
fun AddHarvestTicketDialog(
    state: BroilerUiState,
    viewModel: BroilerViewModel,
    onDismiss: () -> Unit
) {
    val liveTime = rememberLiveDateTime()

    var driverName by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf(state.defaultFarmerName) }
    var selectedFloor by remember { mutableStateOf(Floor.L1) }
    var notes by remember { mutableStateOf("") }
    var pricePerKgInput by remember { mutableStateOf("21500") }
    var dateInput by remember { mutableStateOf(liveTime.dateFormatted) }
    var timeInput by remember { mutableStateOf(liveTime.timeFormatted) }
    var dayNameInput by remember { mutableStateOf(liveTime.dayName) }

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
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tiket & Timbangan Baru",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Text(
                    text = "Catat data armada, driver, pembeli & timbangan ayam saat panen berlangsung.",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Farmer & Date Info Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Peternak: $farmerName", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Waktu: $dayNameInput, $dateInput • $timeInput", color = TextMuted, fontSize = 10.sp)
                    }
                    NeonBadge(text = "LIVE PANEN", color = NeonGreen)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Driver & Plate Fields
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = driverName,
                        onValueChange = { driverName = it },
                        label = { Text("Nama Driver") },
                        placeholder = { Text("Sugeng Waluyo") },
                        modifier = Modifier.weight(1.2f).testTag("input_driver_name"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = vehiclePlate,
                        onValueChange = { vehiclePlate = it.uppercase() },
                        label = { Text("No Plat Truk") },
                        placeholder = { Text("B 9481 UYT") },
                        modifier = Modifier.weight(1f).testTag("input_vehicle_plate"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Buyer & Price Fields
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Nama Pembeli / Bakul / PT") },
                    placeholder = { Text("CV Berkah Unggas / H. Ahmad") },
                    modifier = Modifier.fillMaxWidth().testTag("input_buyer_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Floor Selector (Asal Kandang)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Asal Kandang / Lantai", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Floor.L1, Floor.L2, Floor.ALL).forEach { fl ->
                            val isSelected = selectedFloor == fl
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) NeonCyan.copy(alpha = 0.25f) else CyberSurfaceVariant,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonCyan else CyberCardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedFloor = fl }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fl.displayName,
                                    color = if (isSelected) NeonCyan else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pricePerKgInput,
                    onValueChange = { pricePerKgInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Harga Jual Panen / Kg (Rp)") },
                    supportingText = {
                        Text(
                            "*(Internal peternak — tidak dicantumkan di nota penjualan pembeli)*",
                            color = AmberWarning,
                            fontSize = 10.sp
                        )
                    },
                    prefix = { Text("Rp ", color = NeonGreen, fontWeight = FontWeight.Bold) },
                    placeholder = { Text("21500") },
                    modifier = Modifier.fillMaxWidth().testTag("input_harvest_price_per_kg"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Panen (Opsional)") },
                    placeholder = { Text("Sekat depan, ayam sehat & lincah") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val priceVal = pricePerKgInput.toDoubleOrNull() ?: 21500.0
                            viewModel.createHarvestTicket(
                                driverName = driverName,
                                vehiclePlate = vehiclePlate,
                                buyerName = buyerName,
                                floor = selectedFloor,
                                pricePerKg = if (priceVal > 0) priceVal else 21500.0,
                                farmerName = farmerName,
                                date = dateInput,
                                time = timeInput,
                                dayName = dayNameInput,
                                notes = notes
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.weight(1.6f).testTag("btn_save_harvest_ticket"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = CyberDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buat & Mulai Timbang", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
