package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Floor
import com.example.data.model.HarvestTicket
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EditHarvestTicketDialog
import com.example.ui.components.NeonBadge
import com.example.ui.components.NeonCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerUiState
import com.example.ui.viewmodel.BroilerViewModel
import java.net.URLEncoder

@Composable
fun HarvestScreen(
    state: BroilerUiState,
    viewModel: BroilerViewModel
) {
    val context = LocalContext.current
    var selectedFilterFloor by remember { mutableStateOf<Floor?>(null) }
    var ticketToEdit by remember { mutableStateOf<HarvestTicket?>(null) }
    var ticketToDelete by remember { mutableStateOf<HarvestTicket?>(null) }

    val filteredTickets = remember(state.harvestTickets, selectedFilterFloor) {
        if (selectedFilterFloor == null || selectedFilterFloor == Floor.ALL) {
            state.harvestTickets
        } else {
            state.harvestTickets.filter { it.floor == selectedFilterFloor }
        }
    }

    // Cumulative Harvest Statistics
    val totalBirds = state.harvestTickets.sumOf { it.totalBirdCount }
    val totalKg = state.harvestTickets.sumOf { it.totalWeightKg }
    val avgWeight = if (totalBirds > 0) totalKg / totalBirds else 0.0
    val totalRevenue = state.harvestTickets.sumOf { it.totalAmount }

    fun sendViaWhatsApp(text: String, ctx: Context) {
        try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = "https://api.whatsapp.com/send?text=$encoded"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            ctx.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            ctx.startActivity(Intent.createChooser(shareIntent, "Kirim Hasil Timbangan Panen"))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Title & Create Ticket Action
        item {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(NeonGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SISTEM PANEN & TIMBANGAN",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Timbangan Ayam Digital",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Catat driver, plat, pembeli, bobot tiap timbangan & kirim nota WA",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.openAddHarvestTicketDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_open_new_ticket_dialog")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CyberDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tiket Baru", color = CyberDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. Cumulative Harvest Performance Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    title = "Total Terpanen",
                    value = "${BroilerViewModel.formatNumber(totalBirds.toDouble())} Ekor",
                    subtitle = "${state.harvestTickets.size} Surat Jalan/Tiket",
                    accentColor = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Total Tonase",
                    value = "%.1f Kg".format(totalKg),
                    subtitle = "%.3f Ton".format(totalKg / 1000.0),
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    title = "Bobot Rata-rata",
                    value = "%.2f Kg".format(avgWeight),
                    subtitle = "${(avgWeight * 1000).toInt()} gr / ekor",
                    accentColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                val totalWeighings = state.harvestTickets.sumOf { it.entries.size }
                MetricMiniCard(
                    title = "Ritase & Sesi Panen",
                    value = "${state.harvestTickets.size} Ritase Truk",
                    subtitle = "$totalWeighings kali timbang fisik",
                    accentColor = NeonAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Floor Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                FilterChipItem(
                    label = "Semua (${state.harvestTickets.size})",
                    isSelected = selectedFilterFloor == null || selectedFilterFloor == Floor.ALL,
                    onClick = { selectedFilterFloor = Floor.ALL }
                )
                FilterChipItem(
                    label = "Lantai 1",
                    isSelected = selectedFilterFloor == Floor.L1,
                    onClick = { selectedFilterFloor = Floor.L1 }
                )
                FilterChipItem(
                    label = "Lantai 2",
                    isSelected = selectedFilterFloor == Floor.L2,
                    onClick = { selectedFilterFloor = Floor.L2 }
                )
            }
        }

        // 4. Ticket Cards List
        if (filteredTickets.isEmpty()) {
            item {
                NeonCard(borderColor = CyberCardBorder) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Belum ada tiket timbangan panen.", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Klik tombol '+ Tiket Baru' diatas untuk memulai pencatatan panen ayam.", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(filteredTickets) { ticket ->
                HarvestTicketCard(
                    ticket = ticket,
                    onOpenScaleSheet = {
                        viewModel.openScaleWeighingSheet(ticket.id, true)
                    },
                    onOpenReceipt = {
                        viewModel.openReceiptDialog(ticket, true)
                    },
                    onSendWhatsApp = {
                        sendViaWhatsApp(ticket.generateWhatsAppText(), context)
                    },
                    onEdit = {
                        ticketToEdit = ticket
                    },
                    onDelete = {
                        ticketToDelete = ticket
                    }
                )
            }
        }
    }

    // Edit Harvest Ticket Dialog
    val activeTicketEdit = ticketToEdit
    if (activeTicketEdit != null) {
        EditHarvestTicketDialog(
            ticket = activeTicketEdit,
            onDismiss = { ticketToEdit = null },
            onSubmit = { updated ->
                viewModel.updateHarvestTicket(updated)
                ticketToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    val activeTicketDelete = ticketToDelete
    if (activeTicketDelete != null) {
        DeleteConfirmDialog(
            title = "Hapus Tiket Panen?",
            message = "Apakah Anda yakin ingin menghapus surat jalan panen nomor ${activeTicketDelete.ticketNumber} (${activeTicketDelete.driverName} - ${activeTicketDelete.vehiclePlate})? Seluruh ${activeTicketDelete.entries.size} catatan timbangan keranjang juga akan terhapus.",
            itemName = "${activeTicketDelete.ticketNumber} • ${activeTicketDelete.driverName} • ${activeTicketDelete.totalBirdCount} Ekor",
            onConfirm = {
                viewModel.deleteHarvestTicket(activeTicketDelete.id)
                ticketToDelete = null
            },
            onDismiss = { ticketToDelete = null }
        )
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(1.dp, if (isSelected) NeonCyan else CyberCardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) NeonCyan else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun HarvestTicketCard(
    ticket: HarvestTicket,
    onOpenScaleSheet: () -> Unit,
    onOpenReceipt: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    NeonCard(
        borderColor = if (ticket.status == "Selesai") NeonCyan.copy(alpha = 0.6f) else NeonGreen,
        glowColor = if (ticket.status == "Selesai") NeonCyan.copy(alpha = 0.1f) else NeonGreen.copy(alpha = 0.15f)
    ) {
        // Card Header: Ticket No, Floor & Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ticket.ticketNumber,
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                NeonBadge(
                    text = ticket.floor.displayName,
                    color = if (ticket.floor == Floor.L1) Floor1Color else Floor2Color
                )
            }

            NeonBadge(
                text = ticket.status,
                color = if (ticket.status == "Selesai") NeonGreen else NeonAmber
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Driver & Vehicle Plate Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CyberSurfaceVariant.copy(alpha = 0.6f))
                .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Driver: ${ticket.driverName}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No Plat: ${ticket.vehiclePlate}",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${ticket.dayName}, ${ticket.date}",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = ticket.time,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Buyer & Farmer Info
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Pembeli: ${ticket.buyerName}", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text("Peternak: ${ticket.farmerName.substringBefore(" /")}", color = TextMuted, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(8.dp))

        // 3 Key Metrics: Ekor, Kg, Avg Bobot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("JUMLAH EKOR", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${ticket.totalBirdCount} Ekor",
                    color = NeonGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOTAL TONASE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${"%.1f".format(ticket.totalWeightKg)} Kg",
                    color = NeonCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("BOBOT RATA²", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "%.2f Kg".format(ticket.overallAvgWeightKg),
                    color = ElectricBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${ticket.entries.size} Kali Timbang Keranjang Selesai",
                color = TextMuted,
                fontSize = 10.sp
            )
            Text(
                text = "Tonase: ${"%.3f".format(ticket.totalTonaseTon)} Ton",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Internal Peternak Price & Total Bill Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberDarkElevated, RoundedCornerShape(8.dp))
                .border(1.dp, CyberCardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Harga: Rp ${BroilerViewModel.formatNumber(ticket.pricePerKg)}/Kg", color = AmberWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Omset: ${BroilerViewModel.formatRupiah(ticket.totalAmount)}",
                color = NeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "*(Internal peternak — tidak dicantumkan di nota penjualan pembeli)*",
            color = TextMuted,
            fontSize = 9.sp,
            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Actions: WhatsApp, Lihat Nota, + Timbang, Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // WhatsApp Direct Action
            Button(
                onClick = onSendWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1.3f).testTag("btn_ticket_whatsapp_${ticket.id}")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Kirim WA", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Lihat Tabel & Cetak PDF
            OutlinedButton(
                onClick = onOpenReceipt,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                modifier = Modifier.weight(1.3f).testTag("btn_ticket_receipt_${ticket.id}")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tabel & PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // + Timbang Button
            Button(
                onClick = onOpenScaleSheet,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1.1f).testTag("btn_ticket_scale_${ticket.id}")
            ) {
                Icon(Icons.Default.Scale, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+Timbang", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Edit
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(34.dp).testTag("btn_ticket_edit_${ticket.id}")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Tiket", tint = NeonCyan, modifier = Modifier.size(18.dp))
            }

            // Delete
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(34.dp).testTag("btn_ticket_delete_${ticket.id}")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = NeonRed.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}
