package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.HarvestTicket
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerViewModel
import com.example.util.HarvestPdfGenerator
import java.net.URLEncoder

@Composable
fun HarvestReceiptDialog(
    ticket: HarvestTicket,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val whatsAppText = ticket.generateWhatsAppText()

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
            ctx.startActivity(Intent.createChooser(shareIntent, "Kirim Nota Timbangan"))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.94f)
                .border(1.5.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Surat Jalan & Dokumen PDF Panen",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tabel resmi timbangan panen ayam broiler",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Document Layout (Attractive Table & Official PDF Preview)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column {
                        // Official Document Header Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant.copy(alpha = 0.8f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "PROFIT BROILER CLUB",
                                    color = NeonGreen,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "SURAT JALAN & BERITA ACARA TIMBANGAN PANEN",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Closed House Modern 2 Lantai • Terverifikasi Otomatis",
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Two-Column Metadata Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurface.copy(alpha = 0.8f))
                                .border(1.dp, CyberCardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // Left Column
                                Column(modifier = Modifier.weight(1.1f)) {
                                    ReceiptMetaRow("No. Tiket", ticket.ticketNumber, isHighlight = true)
                                    ReceiptMetaRow("Hari/Tgl", "${ticket.dayName}, ${ticket.date}")
                                    ReceiptMetaRow("Waktu", ticket.time)
                                    ReceiptMetaRow("Asal", ticket.floor.displayName)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Right Column
                                Column(modifier = Modifier.weight(1.2f)) {
                                    ReceiptMetaRow("Peternak", ticket.farmerName.substringBefore(" /"))
                                    ReceiptMetaRow("Pembeli", ticket.buyerName)
                                    ReceiptMetaRow("Driver", ticket.driverName)
                                    ReceiptMetaRow("No. Plat", ticket.vehiclePlate, isHighlight = true)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Section Title: Tabel Rincian Timbangan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TABEL RINCIAN PENIMBANGAN",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${ticket.entries.size} Kali Timbang",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Visual Table Grid
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B))
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NO",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.5f)
                                )
                                Text(
                                    text = "SESI TIMBANGAN",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.4f)
                                )
                                Text(
                                    text = "EKOR",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = "BERAT (KG)",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.1f)
                                )
                                Text(
                                    text = "RATA² (KG)",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.1f)
                                )
                            }

                            HorizontalDivider(color = CyberCardBorder)

                            // Table Data Rows
                            ticket.entries.forEachIndexed { index, entry ->
                                val isEven = index % 2 == 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isEven) Color(0xFF0F172A) else Color(0xFF161F36))
                                        .padding(vertical = 6.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${entry.scaleNumber}",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(0.5f)
                                    )
                                    Text(
                                        text = "Timbang #${entry.scaleNumber}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1.4f)
                                    )
                                    Text(
                                        text = "${entry.birdCount}",
                                        color = TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.9f)
                                    )
                                    Text(
                                        text = "%.1f kg".format(entry.netWeightKg),
                                        color = TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1.1f)
                                    )
                                    Text(
                                        text = "%.2f kg".format(entry.avgWeightKg),
                                        color = ElectricBlue,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1.1f)
                                    )
                                }
                                if (index < ticket.entries.size - 1) {
                                    HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.3f))
                                }
                            }

                            HorizontalDivider(color = NeonGreen.copy(alpha = 0.6f), thickness = 1.5.dp)

                            // Grand Total Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NeonGreen.copy(alpha = 0.12f))
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1.9f).padding(start = 6.dp)
                                )
                                Text(
                                    text = "${ticket.totalBirdCount}",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = "%.1f kg".format(ticket.totalWeightKg),
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.1f)
                                )
                                Text(
                                    text = "%.2f kg".format(ticket.overallAvgWeightKg),
                                    color = ElectricBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // High-Level Summary Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, CyberCardBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tonase Panen: ${"%.3f".format(ticket.totalTonaseTon)} Ton",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rata²: ${(ticket.overallAvgWeightKg * 1000).toInt()} gr / ekor",
                                color = ElectricBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (ticket.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Catatan: ${ticket.notes}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3-Party Signatures Mockup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SignatureColumn("Pihak Peternak", ticket.farmerName.substringBefore(" /"))
                            SignatureColumn("Driver Truk", ticket.driverName)
                            SignatureColumn("Pihak Pembeli", ticket.buyerName.substringBefore(" ("))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Dokumen resmi berformat tabel siap dicetak atau dibagikan sebagai file PDF.",
                            color = TextMuted,
                            fontSize = 9.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Generate PDF, Share PDF, WA Text
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Primary Action: Generate & Share Official PDF Table
                    Button(
                        onClick = {
                            val pdfFile = HarvestPdfGenerator.generatePdf(context, ticket)
                            HarvestPdfGenerator.sharePdf(context, pdfFile, ticket)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_share_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = CyberDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bagikan Dokumen PDF (Tabel Resmi)",
                            color = CyberDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Print / Open PDF directly
                        OutlinedButton(
                            onClick = {
                                val pdfFile = HarvestPdfGenerator.generatePdf(context, ticket)
                                HarvestPdfGenerator.openPdf(context, pdfFile)
                            },
                            modifier = Modifier.weight(1f).testTag("btn_print_pdf"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka / Cetak PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Send WhatsApp Text
                        Button(
                            onClick = {
                                sendViaWhatsApp(whatsAppText, context)
                            },
                            modifier = Modifier.weight(1f).testTag("btn_send_whatsapp_text"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim Teks WA", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignatureColumn(
    role: String,
    name: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(role, color = TextMuted, fontSize = 9.sp)
        Spacer(modifier = Modifier.height(26.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(1.dp)
                .background(CyberCardBorder)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(name, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ReceiptMetaRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label :",
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = if (isHighlight) NeonCyan else TextPrimary,
            fontSize = 10.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
    }
}
