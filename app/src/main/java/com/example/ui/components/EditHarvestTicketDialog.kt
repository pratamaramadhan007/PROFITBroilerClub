package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Floor
import com.example.data.model.HarvestTicket
import com.example.ui.theme.*

@Composable
fun EditHarvestTicketDialog(
    ticket: HarvestTicket,
    onDismiss: () -> Unit,
    onSubmit: (HarvestTicket) -> Unit
) {
    var driverNameInput by remember { mutableStateOf(ticket.driverName) }
    var vehiclePlateInput by remember { mutableStateOf(ticket.vehiclePlate) }
    var buyerNameInput by remember { mutableStateOf(ticket.buyerName) }
    var farmerNameInput by remember { mutableStateOf(ticket.farmerName) }
    var selectedFloor by remember { mutableStateOf(ticket.floor) }
    var dateInput by remember { mutableStateOf(ticket.date) }
    var timeInput by remember { mutableStateOf(ticket.time) }
    var pricePerKgInput by remember { mutableStateOf(ticket.pricePerKg.toInt().toString()) }
    var notesInput by remember { mutableStateOf(ticket.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
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
                            text = "Edit Tiket Panen",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ticket.ticketNumber,
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Floor Selection
                Text("Lantai Kandang:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedFloor = Floor.L1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedFloor == Floor.L1) Floor1Color else CyberSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lantai 1", color = if (selectedFloor == Floor.L1) CyberDark else TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedFloor = Floor.L2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedFloor == Floor.L2) Floor2Color else CyberSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lantai 2", color = if (selectedFloor == Floor.L2) CyberDark else TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Driver & Plat
                OutlinedTextField(
                    value = driverNameInput,
                    onValueChange = { driverNameInput = it },
                    label = { Text("Nama Driver Armada", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_driver_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = vehiclePlateInput,
                    onValueChange = { vehiclePlateInput = it.uppercase() },
                    label = { Text("No. Plat Kendaraan", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_vehicle_plate")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Buyer & Farmer
                OutlinedTextField(
                    value = buyerNameInput,
                    onValueChange = { buyerNameInput = it },
                    label = { Text("Nama Pembeli / Bakul", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_buyer_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = farmerNameInput,
                    onValueChange = { farmerNameInput = it },
                    label = { Text("Nama Peternak", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("Tanggal Panen", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = { Text("Waktu", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pricePerKgInput,
                    onValueChange = { pricePerKgInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Harga Jual Panen / Kg (Rp)", fontSize = 12.sp) },
                    supportingText = {
                        Text(
                            "*(Internal peternak — tidak dicantumkan di nota penjualan pembeli)*",
                            color = AmberWarning,
                            fontSize = 10.sp
                        )
                    },
                    prefix = { Text("Rp ", color = NeonGreen, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_harvest_price")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Catatan Panen", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                            val priceVal = pricePerKgInput.toDoubleOrNull() ?: ticket.pricePerKg
                            val updated = ticket.copy(
                                driverName = driverNameInput.ifBlank { ticket.driverName },
                                vehiclePlate = vehiclePlateInput.ifBlank { ticket.vehiclePlate }.uppercase(),
                                buyerName = buyerNameInput.ifBlank { ticket.buyerName },
                                farmerName = farmerNameInput.ifBlank { ticket.farmerName },
                                floor = selectedFloor,
                                pricePerKg = if (priceVal > 0) priceVal else ticket.pricePerKg,
                                date = dateInput.ifBlank { ticket.date },
                                time = timeInput.ifBlank { ticket.time },
                                notes = notesInput
                            )
                            onSubmit(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_save_edit_ticket")
                    ) {
                        Text("Simpan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
