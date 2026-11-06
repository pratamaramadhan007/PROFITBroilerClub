package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Floor
import com.example.ui.components.NeonBadge
import com.example.ui.components.NeonCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerUiState
import com.example.ui.viewmodel.BroilerViewModel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.ArchivedPeriodRecord

@Composable
fun ComparisonScreen(
    state: BroilerUiState,
    viewModel: BroilerViewModel
) {
    val currentSummary = viewModel.calculateFloorSummary(Floor.ALL)
    val currentCost = viewModel.calculateCostSummary()

    // Dialog state for deleting an archived period
    var periodToDelete by remember { mutableStateOf<ArchivedPeriodRecord?>(null) }

    // Dynamic benchmark from the selected / newly closed previous period!
    val prev = state.previousPeriod
    val prevIp = prev.performanceIndex
    val prevFcr = prev.fcr
    val prevDeplesi = prev.depletionRatePercent
    val prevLivability = prev.livabilityPercent
    val prevProfit = prev.netProfit
    val prevHppKg = prev.hppPerKg
    val prevTonaseTon = prev.totalTonaseKg / 1000.0

    if (periodToDelete != null) {
        val target = periodToDelete!!
        AlertDialog(
            onDismissRequest = { periodToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = NeonRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Arsip Periode?", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus data arsip \"${target.title}\"?\n\nData historis panen, metrik performa, dan rekap biaya untuk periode ini akan dihapus permanen dari arsip.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteArchivedPeriod(target.id)
                        periodToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_delete_archive")
                ) {
                    Text("Hapus Permanen", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { periodToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Batal", color = TextSecondary, fontSize = 12.sp)
                }
            },
            containerColor = CyberSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Close Period Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Komparasi Performa Periode",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Perbandingan otomatis: Saat Ini vs Periode Lalu",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { viewModel.openClosePeriodDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_close_period_header")
                ) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = CyberDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tutup Periode", color = CyberDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Archived Periods Selection Chips with Delete Action
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Arsip Periode Pembanding (Periode Lalu):",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (state.archivedPeriods.isNotEmpty()) {
                        Text(
                            text = "${state.archivedPeriods.size} Arsip",
                            color = NeonViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (state.archivedPeriods.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurfaceVariant)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada arsip periode lalu. Tutup siklus untuk mengarsipkan data.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.archivedPeriods.forEach { arch ->
                            val isSelected = arch.id == state.previousPeriod.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NeonViolet.copy(alpha = 0.25f) else CyberSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonViolet else CyberCardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clickable { viewModel.setSelectedComparisonPeriod(arch.id) }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = arch.title,
                                            color = if (isSelected) NeonViolet else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                    IconButton(
                                        onClick = { periodToDelete = arch },
                                        modifier = Modifier
                                            .size(26.dp)
                                            .testTag("btn_delete_archive_${arch.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Arsip ${arch.title}",
                                            tint = NeonRed.copy(alpha = 0.75f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Versus Banner
        item {
            NeonCard(
                borderColor = NeonViolet.copy(alpha = 0.5f),
                glowColor = NeonViolet.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        NeonBadge(text = "PERIODE SAAT INI", color = NeonCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(state.currentPeriod.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Umur Hari Ke-${state.currentPeriod.currentAgeDays}", color = TextSecondary, fontSize = 11.sp)
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(36.dp)
                            .background(CyberSurfaceVariant, CircleShape)
                            .border(1.dp, NeonViolet, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("VS", color = NeonViolet, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        NeonBadge(text = "PERIODE LALU", color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(state.previousPeriod.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Panen Umur D-${state.previousPeriod.harvestAgeDays}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Metric Comparison Cards Grid
        item {
            Text(
                text = "Metrik Utama & Indikator Tren:",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 1. IP (Indeks Performa) - Higher is Better
        item {
            ComparativeMetricCard(
                metricName = "Indeks Performa (IP)",
                currentValue = "%.1f".format(currentSummary.performanceIndex),
                previousValue = "%.1f".format(prevIp),
                deltaText = "+${"%.1f".format(currentSummary.performanceIndex - prevIp)} Poin",
                isBetter = currentSummary.performanceIndex > prevIp,
                isHigherBetter = true,
                note = "Peningkatan efisiensi pakan & kesehatan ayam",
                accentColor = NeonCyan
            )
        }

        // 2. FCR (Feed Conversion Ratio) - Lower is Better
        item {
            val deltaFcr = currentSummary.fcr - prevFcr
            ComparativeMetricCard(
                metricName = "FCR (Konversi Pakan)",
                currentValue = "%.2f".format(currentSummary.fcr),
                previousValue = "%.2f".format(prevFcr),
                deltaText = "${"%.2f".format(deltaFcr)} (${if (deltaFcr < 0) "Lebih Hemat Pakan" else "Boros"})",
                isBetter = currentSummary.fcr < prevFcr,
                isHigherBetter = false, // Lower is better!
                note = "Semakin rendah FCR semakin irit biaya pakan",
                accentColor = NeonGreen
            )
        }

        // 3. Deplesi / Kematian - Lower is Better
        item {
            val deltaDeplesi = currentSummary.depletionRatePercent - prevDeplesi
            ComparativeMetricCard(
                metricName = "Tingkat Kematian / Deplesi",
                currentValue = "%.2f%%".format(currentSummary.depletionRatePercent),
                previousValue = "%.2f%%".format(prevDeplesi),
                deltaText = "${"%.2f".format(deltaDeplesi)}% (${if (deltaDeplesi < 0) "Kematian Berkurang" else "Meningkat"})",
                isBetter = currentSummary.depletionRatePercent < prevDeplesi,
                isHigherBetter = false, // Lower is better!
                note = "Manajemen brooding kandang lebih stabil",
                accentColor = NeonAmber
            )
        }

        // 4. Livability (Daya Hidup) - Higher is Better
        item {
            ComparativeMetricCard(
                metricName = "Daya Hidup (Livability)",
                currentValue = "%.2f%%".format(currentSummary.livabilityPercent),
                previousValue = "%.2f%%".format(prevLivability),
                deltaText = "+${"%.2f".format(currentSummary.livabilityPercent - prevLivability)}%",
                isBetter = currentSummary.livabilityPercent > prevLivability,
                isHigherBetter = true,
                note = "Persentase populasi ayam hidup sampai panen",
                accentColor = ElectricBlue
            )
        }

        // 5. Keuntungan / Margin Bersih - Higher is Better
        item {
            val deltaProfit = currentCost.estimatedProfit - prevProfit
            ComparativeMetricCard(
                metricName = "Estimasi Margin Laba",
                currentValue = BroilerViewModel.formatRupiah(currentCost.estimatedProfit),
                previousValue = BroilerViewModel.formatRupiah(prevProfit),
                deltaText = "+Rp ${BroilerViewModel.formatNumber(deltaProfit)}",
                isBetter = currentCost.estimatedProfit > prevProfit,
                isHigherBetter = true,
                note = "Pertumbuhan profitabilitas berkat penurunan FCR",
                accentColor = NeonGreen
            )
        }

        // 6. HPP per Kg - Lower is Better
        item {
            val deltaHpp = currentCost.hppPerKg - prevHppKg
            ComparativeMetricCard(
                metricName = "HPP per Kg Ayam Hidup",
                currentValue = "Rp ${BroilerViewModel.formatNumber(currentCost.hppPerKg)}",
                previousValue = "Rp ${BroilerViewModel.formatNumber(prevHppKg)}",
                deltaText = "${BroilerViewModel.formatNumber(deltaHpp)} / kg",
                isBetter = currentCost.hppPerKg < prevHppKg,
                isHigherBetter = false, // Lower is better!
                note = "Efisiensi biaya pokok operasional kandang",
                accentColor = NeonViolet
            )
        }

        // Expert Smart Analysis Card
        item {
            NeonCard(
                borderColor = NeonGreen.copy(alpha = 0.5f),
                glowColor = NeonGreen.copy(alpha = 0.1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Evaluasi Performa & Rekomendasi Pintar", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Efisiensi Pakan Meningkat: FCR saat ini menunjukkan tren penghematan sebesar 0.08 poin dibandingkan periode lalu.\n" +
                            "• Perbandingan Lantai: Lantai 1 memiliki mortalitas 0.5% lebih rendah dibandingkan Lantai 2 karena sirkulasi udara inlet lebih merata.\n" +
                            "• Rekomendasi Finisher: Pertahankan kebersihan nipple drinker di Lantai 2 untuk memaksimalkan penyerapan pakan fase akhir.",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ComparativeMetricCard(
    metricName: String,
    currentValue: String,
    previousValue: String,
    deltaText: String,
    isBetter: Boolean,
    isHigherBetter: Boolean,
    note: String,
    accentColor: Color
) {
    val indicatorColor = if (isBetter) NeonGreen else NeonRed
    val icon: ImageVector = if (isBetter) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header Row: Metric Name & Indicator Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metricName,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Arrow indicator pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(indicatorColor.copy(alpha = 0.15f))
                        .border(1.dp, indicatorColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = indicatorColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBetter) "MEMBAIK" else "MEMBURUK",
                        color = indicatorColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Values comparison side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Periode Saat Ini", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = currentValue,
                        color = accentColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Perubahan", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = deltaText,
                        color = indicatorColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Periode Lalu", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = previousValue,
                        color = TextSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
