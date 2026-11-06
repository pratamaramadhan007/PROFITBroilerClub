package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HarvestTicket
import com.example.data.model.ScaleEntry
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerViewModel

@Composable
fun LiveScaleDialog(
    ticket: HarvestTicket,
    viewModel: BroilerViewModel,
    onDismiss: () -> Unit
) {
    var birdCountInput by remember { mutableStateOf("15") }
    var weightInput by remember { mutableStateOf("") }
    var entryToEdit by remember { mutableStateOf<ScaleEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<ScaleEntry?>(null) }

    val nextScaleNumber = ticket.entries.size + 1

    val birdCount = birdCountInput.toIntOrNull() ?: 15
    val weightKg = weightInput.toDoubleOrNull() ?: 0.0
    val avgPerBird = if (birdCount > 0 && weightKg > 0) weightKg / birdCount else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .border(1.5.dp, NeonGreen.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Header: Ticket Metadata Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(NeonGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PENIMBANGAN AKTIF: ${ticket.ticketNumber}",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Text(
                            text = "${ticket.driverName} • Plat ${ticket.vehiclePlate}",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pembeli: ${ticket.buyerName} | ${ticket.floor.displayName}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Box for Next Scale Entry
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
                            text = "CATAT TIMBANGAN #$nextScaleNumber",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (weightKg > 0 && birdCount > 0) {
                            Text(
                                text = "${"%.1f".format(weightKg)} kg (avg ${"%.2f".format(avgPerBird)} kg/ekor)",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Jumlah Ekor
                        OutlinedTextField(
                            value = birdCountInput,
                            onValueChange = { birdCountInput = it },
                            label = { Text("Jumlah Ekor") },
                            placeholder = { Text("15") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_scale_birds"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Berat Timbangan (kg)
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Berat Timbangan (kg)") },
                            placeholder = { Text("31.5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.3f).testTag("input_scale_weight"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (weightKg > 0 && birdCount > 0) {
                                viewModel.addScaleEntry(
                                    ticketId = ticket.id,
                                    birdCount = birdCount,
                                    weightKg = weightKg
                                )
                                weightInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        enabled = weightKg > 0 && birdCount > 0,
                        modifier = Modifier.fillMaxWidth().testTag("btn_add_scale_entry")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CyberDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (weightKg > 0) "Simpan Timbangan #$nextScaleNumber (${"%.1f".format(weightKg)} kg)" else "Simpan Timbangan #$nextScaleNumber",
                            color = CyberDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Weighing Ledger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Catatan Timbangan (${ticket.entries.size} Kali Timbang)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${ticket.floor.displayName} • ${ticket.vehiclePlate}",
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberDark.copy(alpha = 0.6f))
                        .border(1.dp, CyberCardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    if (ticket.entries.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Scale, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada timbangan tercatat.", color = TextMuted, fontSize = 12.sp)
                            Text("Masukkan jumlah ekor dan berat kotor diatas lalu klik 'Simpan Timbangan'.", color = TextSecondary, fontSize = 10.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(ticket.entries.reversed()) { entry ->
                                ScaleEntryItemRow(
                                    entry = entry,
                                    onEdit = {
                                        entryToEdit = entry
                                    },
                                    onDelete = {
                                        entryToDelete = entry
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sticky Live Running Total Bar
                NeonCard(
                    borderColor = NeonGreen,
                    glowColor = NeonGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL TERPANEN", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${ticket.totalBirdCount}",
                                    color = NeonGreen,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(" ekor", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL TONASE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "%.1f".format(ticket.totalWeightKg),
                                    color = NeonCyan,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(" kg", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("RATA-RATA", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "%.2f".format(ticket.overallAvgWeightKg),
                                    color = ElectricBlue,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(" kg", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tonase: ${"%.3f".format(ticket.totalTonaseTon)} Ton",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Driver: ${ticket.driverName} (${ticket.vehiclePlate})",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan & Tutup", color = TextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.finalizeHarvestTicket(ticket.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.8f).testTag("btn_finalize_harvest")
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CyberDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Selesai & Nota WhatsApp", color = CyberDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Edit Scale Entry Dialog
    val activeScaleEdit = entryToEdit
    if (activeScaleEdit != null) {
        EditScaleEntryDialog(
            entry = activeScaleEdit,
            onDismiss = { entryToEdit = null },
            onSubmit = { newCount, newWeight ->
                viewModel.updateScaleEntry(ticket.id, activeScaleEdit.scaleNumber, newCount, newWeight)
                entryToEdit = null
            }
        )
    }

    // Delete Scale Entry Confirmation Dialog
    val activeScaleDelete = entryToDelete
    if (activeScaleDelete != null) {
        DeleteConfirmDialog(
            title = "Hapus Timbangan #${activeScaleDelete.scaleNumber}?",
            message = "Apakah Anda yakin ingin menghapus data timbangan #${activeScaleDelete.scaleNumber} (${activeScaleDelete.birdCount} ekor - ${activeScaleDelete.netWeightKg} kg)? Total bobot dan rata-rata akan otomatis dikalkulasi ulang.",
            itemName = "Timbangan #${activeScaleDelete.scaleNumber} • ${activeScaleDelete.birdCount} Ekor • ${activeScaleDelete.netWeightKg} Kg",
            onConfirm = {
                viewModel.removeScaleEntry(ticket.id, activeScaleDelete.scaleNumber)
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }
}

@Composable
fun ScaleEntryItemRow(
    entry: ScaleEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, CyberCardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonCyan.copy(alpha = 0.2f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "#%02d".format(entry.scaleNumber),
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "${entry.birdCount} Ekor  •  ${entry.netWeightKg} Kg",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Timbangan #${entry.scaleNumber}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f kg".format(entry.avgWeightKg),
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("rata²/ekor", color = TextMuted, fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onEdit, modifier = Modifier.size(26.dp).testTag("btn_edit_scale_${entry.scaleNumber}")) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NeonCyan, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(2.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(26.dp).testTag("btn_delete_scale_${entry.scaleNumber}")) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = NeonRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
