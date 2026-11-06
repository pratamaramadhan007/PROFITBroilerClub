package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Floor
import com.example.ui.components.LiveClockHud
import com.example.ui.components.NeonBadge
import com.example.ui.components.NeonCard
import com.example.ui.components.NeonFcrDeplesiBarChart
import com.example.ui.components.NeonFcrDeplesiDualChart
import com.example.ui.components.NeonWeightGrowthChart
import com.example.ui.components.ProfitLossCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerUiState
import com.example.ui.viewmodel.BroilerViewModel

@Composable
fun DashboardScreen(
    state: BroilerUiState,
    viewModel: BroilerViewModel,
    onNavigateToRecording: () -> Unit,
    onNavigateToPanen: () -> Unit = {}
) {
    val summary = viewModel.calculateFloorSummary(state.selectedFloor)
    val l1Summary = viewModel.calculateFloorSummary(Floor.L1)
    val l2Summary = viewModel.calculateFloorSummary(Floor.L2)
    val costSummary = viewModel.calculateCostSummary()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Live Telemetry HUD: Modern Robotic Broiler Logo + Day, Date, and Time
        item {
            LiveClockHud(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_clock_hud")
            )
        }

        // 1. Floor Selector Chips (Semua, Lantai 1, Lantai 2)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Tampilan Monitoring:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when (state.selectedFloor) {
                            Floor.ALL -> "Semua Lantai (Gabungan)"
                            Floor.L1 -> "Lantai 1 (Bawah)"
                            Floor.L2 -> "Lantai 2 (Atas)"
                        },
                        color = when (state.selectedFloor) {
                            Floor.ALL -> AllFloorsColor
                            Floor.L1 -> Floor1Color
                            Floor.L2 -> Floor2Color
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FloorChip(
                        title = "Semua (Gabungan)",
                        sub = "${BroilerViewModel.formatNumber(state.currentPeriod.totalInitialPopulation.toDouble())} Ekor",
                        isSelected = state.selectedFloor == Floor.ALL,
                        activeColor = AllFloorsColor,
                        modifier = Modifier.weight(1f).testTag("filter_floor_all"),
                        onClick = { viewModel.setSelectedFloor(Floor.ALL) }
                    )
                    FloorChip(
                        title = "Lantai 1",
                        sub = "${BroilerViewModel.formatNumber(state.currentPeriod.initialPopulationL1.toDouble())} Ekor",
                        isSelected = state.selectedFloor == Floor.L1,
                        activeColor = Floor1Color,
                        modifier = Modifier.weight(1f).testTag("filter_floor_l1"),
                        onClick = { viewModel.setSelectedFloor(Floor.L1) }
                    )
                    FloorChip(
                        title = "Lantai 2",
                        sub = "${BroilerViewModel.formatNumber(state.currentPeriod.initialPopulationL2.toDouble())} Ekor",
                        isSelected = state.selectedFloor == Floor.L2,
                        activeColor = Floor2Color,
                        modifier = Modifier.weight(1f).testTag("filter_floor_l2"),
                        onClick = { viewModel.setSelectedFloor(Floor.L2) }
                    )
                }
            }
        }

        // 2. Header Period & Live Monitoring Banner (Separated per Floor Selection)
        item {
            val activeFloorColor = when (state.selectedFloor) {
                Floor.L1 -> Floor1Color
                Floor.L2 -> Floor2Color
                Floor.ALL -> NeonCyan
            }
            val (monitoringHeader, monitoringSub) = when (state.selectedFloor) {
                Floor.L1 -> Pair("MONITORING AKTIF - LANTAI 1 (Bawah)", "Kandang Lantai 1 • Bawah")
                Floor.L2 -> Pair("MONITORING AKTIF - LANTAI 2 (Atas)", "Kandang Lantai 2 • Atas")
                Floor.ALL -> Pair("MONITORING AKTIF - GABUNGAN (Semua Lantai)", "Closed House 2 Lantai • Total Kandang")
            }
            val currentAge = when (state.selectedFloor) {
                Floor.L1 -> l1Summary.ageDays
                Floor.L2 -> l2Summary.ageDays
                Floor.ALL -> summary.ageDays
            }
            val currentAgeLabel = when (state.selectedFloor) {
                Floor.L1 -> "LT 1 (HARI)"
                Floor.L2 -> "LT 2 (HARI)"
                Floor.ALL -> "GABUNGAN"
            }

            NeonCard(
                borderColor = activeFloorColor,
                glowColor = activeFloorColor.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(activeFloorColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = monitoringHeader,
                                color = activeFloorColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.currentPeriod.title,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mulai: ${state.currentPeriod.startDate} • $monitoringSub",
                            color = TextMuted,
                            fontSize = 11.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Harvest Age Badge synchronized with floor recording
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeFloorColor.copy(alpha = 0.15f))
                            .border(1.dp, activeFloorColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UMUR", color = TextSecondary, fontSize = 9.sp)
                            Text(
                                text = "D-$currentAge",
                                color = activeFloorColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = currentAgeLabel,
                                color = activeFloorColor,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detail DOC Masuk (Chick-In) Box - Custom View for Selected Floor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberDarkElevated)
                        .border(1.dp, activeFloorColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = activeFloorColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (state.selectedFloor) {
                                        Floor.L1 -> "DOC MASUK - LANTAI 1 (Bawah)"
                                        Floor.L2 -> "DOC MASUK - LANTAI 2 (Atas)"
                                        Floor.ALL -> "DOC MASUK (CHICK-IN) - TOTAL KANDANG"
                                    },
                                    color = activeFloorColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            FilledTonalButton(
                                onClick = { viewModel.openEditDocDialog(true) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = activeFloorColor.copy(alpha = 0.15f),
                                    contentColor = activeFloorColor
                                ),
                                modifier = Modifier.height(28.dp).testTag("btn_edit_doc_chick_in")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Input / Edit DOC", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (state.selectedFloor) {
                            Floor.L1 -> {
                                // Specific Lantai 1 Active View
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Tgl & Jam Masuk", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${state.currentPeriod.startDate} • ${state.currentPeriod.docEntryTime}",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("DOC Masuk L1", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${BroilerViewModel.formatNumber(state.currentPeriod.initialPopulationL1.toDouble())} Ekor",
                                            color = Floor1Color,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Populasi Aktif L1", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${BroilerViewModel.formatNumber(l1Summary.activePopulation.toDouble())} Ekor",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Umur: D-${l1Summary.ageDays} Hari",
                                            color = Floor1Color,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Deplesi / Mati L1", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${l1Summary.totalMortality + l1Summary.totalCulling} Ekor (${"%.2f".format(l1Summary.depletionRatePercent)}%)",
                                            color = if (l1Summary.depletionRatePercent <= 3.0) NeonGreen else NeonAmber,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Pakan: ${"%.1f".format(l1Summary.totalFeedKg)} kg",
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                            }

                            Floor.L2 -> {
                                // Specific Lantai 2 Active View
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Tgl & Jam Masuk", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${state.currentPeriod.startDate} • ${state.currentPeriod.docEntryTime}",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("DOC Masuk L2", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${BroilerViewModel.formatNumber(state.currentPeriod.initialPopulationL2.toDouble())} Ekor",
                                            color = Floor2Color,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Populasi Aktif L2", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${BroilerViewModel.formatNumber(l2Summary.activePopulation.toDouble())} Ekor",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Umur: D-${l2Summary.ageDays} Hari",
                                            color = Floor2Color,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Deplesi / Mati L2", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${l2Summary.totalMortality + l2Summary.totalCulling} Ekor (${"%.2f".format(l2Summary.depletionRatePercent)}%)",
                                            color = if (l2Summary.depletionRatePercent <= 3.0) NeonGreen else NeonAmber,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Pakan: ${"%.1f".format(l2Summary.totalFeedKg)} kg",
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                            }

                            Floor.ALL -> {
                                // Combined Total Kandang Active View
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Tgl & Waktu Masuk", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${state.currentPeriod.startDate} • ${state.currentPeriod.docEntryTime}",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total DOC Masuk", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${BroilerViewModel.formatNumber(state.currentPeriod.totalInitialPopulation.toDouble())} Ekor",
                                            color = NeonCyan,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(6.dp))

                                // Per Floor breakdown with synchronized age
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Lantai 1 (Bawah):",
                                            color = Floor1Color,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${BroilerViewModel.formatNumber(state.currentPeriod.initialPopulationL1.toDouble())} Ekor • Aktif ${BroilerViewModel.formatNumber(l1Summary.activePopulation.toDouble())} • Umur D-${l1Summary.ageDays}",
                                            color = TextPrimary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Lantai 2 (Atas):",
                                            color = Floor2Color,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${BroilerViewModel.formatNumber(state.currentPeriod.initialPopulationL2.toDouble())} Ekor • Aktif ${BroilerViewModel.formatNumber(l2Summary.activePopulation.toDouble())} • Umur D-${l2Summary.ageDays}",
                                            color = TextPrimary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Siklus panen selesai?",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = { viewModel.openClosePeriodDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_dashboard_close_period")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tutup Periode (Arsipkan)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Primary Performance Hero Cards (Glowing Neon HUD)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // IP Card (Indeks Performa)
                val ipColor = when {
                    summary.performanceIndex >= 400 -> NeonGreen
                    summary.performanceIndex >= 351 -> NeonCyan
                    summary.performanceIndex >= 326 -> ElectricBlue
                    summary.performanceIndex >= 301 -> NeonAmber
                    else -> NeonRed
                }

                NeonCard(
                    modifier = Modifier.weight(1.1f).testTag("card_ip"),
                    borderColor = ipColor,
                    glowColor = ipColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("INDEKS PERFORMA", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        NeonBadge(text = summary.ipCategory, color = ipColor)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "%.1f".format(summary.performanceIndex),
                        color = ipColor,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Target: > 350 (Standar Industri)",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // FCR Card
                val fcrColor = if (summary.fcr <= 1.50) NeonGreen else NeonAmber
                NeonCard(
                    modifier = Modifier.weight(0.9f).testTag("card_fcr"),
                    borderColor = fcrColor,
                    glowColor = fcrColor.copy(alpha = 0.15f)
                ) {
                    Text("FCR (KONVERSI)", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "%.2f".format(summary.fcr),
                        color = fcrColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Standar Cobb: 1.49",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 4. Secondary Metric Grid (Mortalitas, Livability, Bobot, Tonase)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    title = "Deplesi / Mati",
                    value = "%.2f%%".format(summary.depletionRatePercent),
                    subtitle = "${summary.totalMortality + summary.totalCulling} ekor",
                    accentColor = if (summary.depletionRatePercent <= 3.0) NeonGreen else NeonAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Daya Hidup",
                    value = "%.2f%%".format(summary.livabilityPercent),
                    subtitle = "${BroilerViewModel.formatNumber(summary.activePopulation.toDouble())} ekor",
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
                    value = "%.2f kg".format(summary.latestAvgWeightKg),
                    subtitle = "${(summary.latestAvgWeightKg * 1000).toInt()} gr / ekor",
                    accentColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Total Tonase",
                    value = "%.1f Ton".format(summary.totalTonaseKg / 1000.0),
                    subtitle = "Pakan: ${(summary.totalFeedKg / 1000).toInt()} Ton",
                    accentColor = NeonViolet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Weight Growth Line Chart
        item {
            NeonCard(
                borderColor = NeonCyan.copy(alpha = 0.5f),
                glowColor = NeonCyan.copy(alpha = 0.1f)
            ) {
                NeonWeightGrowthChart(
                    recordings = state.dailyRecordings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 6. FCR & Deplesi Interactive Comparison Chart (Aktual vs Standar)
        item {
            NeonCard(
                borderColor = NeonCyan.copy(alpha = 0.5f),
                glowColor = NeonCyan.copy(alpha = 0.1f)
            ) {
                NeonFcrDeplesiDualChart(
                    recordings = state.dailyRecordings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 6.5 Analisis Laba / Rugi Hasil Penjualan Panen vs Biaya Produksi (HPP)
        item {
            ProfitLossCard(
                costSummary = costSummary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 7. Lantai 1 vs Lantai 2 Side-by-Side Summary Spotlight
        item {
            NeonCard(
                borderColor = ElectricBlue.copy(alpha = 0.4f),
                glowColor = ElectricBlue.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perbandingan Lantai 1 vs Lantai 2",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    NeonBadge(text = "Closed House", color = ElectricBlue)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Lantai 1 Column
                    FloorColumnSummary(
                        floorName = "LANTAI 1 (Bawah)",
                        umur = "D-${l1Summary.ageDays} Hari",
                        populasi = "${l1Summary.activePopulation} ekor",
                        mortalitas = "${l1Summary.depletionRatePercent}% (${l1Summary.totalMortality} m)",
                        fcr = "%.2f".format(l1Summary.fcr),
                        ip = "%.1f".format(l1Summary.performanceIndex),
                        accentColor = Floor1Color,
                        modifier = Modifier.weight(1f)
                    )

                    // Lantai 2 Column
                    FloorColumnSummary(
                        floorName = "LANTAI 2 (Atas)",
                        umur = "D-${l2Summary.ageDays} Hari",
                        populasi = "${l2Summary.activePopulation} ekor",
                        mortalitas = "${l2Summary.depletionRatePercent}% (${l2Summary.totalMortality} m)",
                        fcr = "%.2f".format(l2Summary.fcr),
                        ip = "%.1f".format(l2Summary.performanceIndex),
                        accentColor = Floor2Color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 7.5 Harvest & Scale Quick Hub Banner
        item {
            val totalHarvestBirds = state.harvestTickets.sumOf { it.totalBirdCount }
            val totalHarvestKg = state.harvestTickets.sumOf { it.totalWeightKg }
            NeonCard(
                borderColor = NeonGreen.copy(alpha = 0.5f),
                glowColor = NeonGreen.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Scale, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TIMBANGAN & PANEN AYAM", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (totalHarvestBirds > 0) "${BroilerViewModel.formatNumber(totalHarvestBirds.toDouble())} Ekor Terpanen (${"%.1f".format(totalHarvestKg)} Kg)" else "Siap Panen & Timbang Ayam",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Catat driver, no plat, ekor, kg & kirim nota ke WhatsApp pembeli",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onNavigateToPanen,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_dashboard_to_panen")
                    ) {
                        Text("Buka Panen", color = CyberDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CyberDark, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // 8. Quick Actions: Instant Calculator & Recording shortcut
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.openCalculatorDialog(true) },
                    modifier = Modifier.weight(1f).testTag("btn_open_calc"),
                    border = BorderStroke(1.dp, NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kalkulator IP", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.openAddRecordingDialog(true) },
                    modifier = Modifier.weight(1f).testTag("btn_quick_record"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.EditCalendar, contentDescription = null, tint = CyberDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Catat Hari Ini", color = CyberDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FloorChip(
    title: String,
    sub: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = 0.2f) else CyberSurfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                1.dp,
                if (isSelected) activeColor else CyberCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isSelected) activeColor else TextPrimary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = sub,
                color = if (isSelected) activeColor.copy(alpha = 0.8f) else TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = accentColor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun FloorColumnSummary(
    floorName: String,
    umur: String,
    populasi: String,
    mortalitas: String,
    fcr: String,
    ip: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurface.copy(alpha = 0.7f))
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(floorName, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            SummaryRow("Umur Ayam:", umur, valueColor = NeonGreen)
            SummaryRow("Populasi:", populasi)
            SummaryRow("Mortalitas:", mortalitas)
            SummaryRow("FCR:", fcr)
            SummaryRow("IP:", ip, valueColor = accentColor)
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 11.sp)
        Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
