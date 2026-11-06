package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerViewModel
import com.example.ui.viewmodel.CostBreakdown

@Composable
fun ProfitLossCard(
    costSummary: CostBreakdown,
    onSimulatePrice: ((Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isProfit = costSummary.isProfit
    val heroColor = if (isProfit) NeonGreen else NeonRed
    val glowColor = heroColor.copy(alpha = 0.12f)

    NeonCard(
        borderColor = heroColor.copy(alpha = 0.7f),
        glowColor = glowColor,
        modifier = modifier.testTag("profit_loss_analysis_card")
    ) {
        // 1. Header with Title & Profit/Loss Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(heroColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, heroColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = heroColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ANALISIS LABA / RUGI",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Hasil Penjualan vs Total Biaya Produksi",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Margin Badge
            val marginLabel = if (isProfit) {
                "PROFIT (+%.1f%%)".format(costSummary.profitMarginPercent)
            } else {
                "RUGI (%.1f%%)".format(costSummary.profitMarginPercent)
            }
            NeonBadge(
                text = marginLabel,
                color = heroColor
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Main Hero Net Profit/Loss Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberDarkElevated)
                .border(1.dp, heroColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isProfit) "KEUNTUNGAN BERSIH" else "KERUGIAN / DEFISIT",
                        color = heroColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Margin: ${"%.1f".format(costSummary.profitMarginPercent)}%",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val signPrefix = if (costSummary.netProfitLoss >= 0) "+" else ""
                Text(
                    text = "$signPrefix${BroilerViewModel.formatRupiah(costSummary.netProfitLoss)}",
                    color = heroColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Omset Penjualan: ${BroilerViewModel.formatRupiah(costSummary.grossRevenue)} — HPP: ${BroilerViewModel.formatRupiah(costSummary.totalProductionCost)}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Visual Revenue vs Cost Proportion Bar
        val costRatio = if (costSummary.grossRevenue > 0) {
            (costSummary.totalProductionCost / costSummary.grossRevenue).toFloat().coerceIn(0.05f, 1f)
        } else 1f
        val profitRatio = (1f - costRatio).coerceAtLeast(0f)

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Biaya Produksi (${(costRatio * 100).toInt()}%)",
                    color = NeonAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (isProfit) {
                    Text(
                        text = "Laba Bersih (${(profitRatio * 100).toInt()}%)",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dual Progress Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberDarkElevated)
            ) {
                // Biaya segment
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(costRatio.coerceAtLeast(0.01f))
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonAmber, NeonAmber.copy(alpha = 0.7f))
                            )
                        )
                )
                // Profit segment
                if (isProfit && profitRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(profitRatio.coerceAtLeast(0.01f))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(NeonGreen.copy(alpha = 0.7f), NeonGreen)
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. 4-Key Metrics Grid (Laba/Kg, Laba/Ekor, BEP Harga, Tonase Panen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(
                title = "Laba / Kg",
                value = "${if (costSummary.profitPerKg >= 0) "+" else ""}Rp ${BroilerViewModel.formatNumber(costSummary.profitPerKg)}",
                accentColor = if (costSummary.profitPerKg >= 0) NeonGreen else NeonRed,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "Laba / Ekor",
                value = "${if (costSummary.profitPerBird >= 0) "+" else ""}Rp ${BroilerViewModel.formatNumber(costSummary.profitPerBird)}",
                accentColor = if (costSummary.profitPerBird >= 0) NeonGreen else NeonRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(
                title = "BEP Harga Impas",
                value = "Rp ${BroilerViewModel.formatNumber(costSummary.bepPricePerKg)} / kg",
                accentColor = ElectricBlue,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "Tonase Dijual",
                value = "${BroilerViewModel.formatNumber(costSummary.totalTonaseKg)} Kg",
                accentColor = NeonCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        // 5. Real Harvest Selling Price & Revenue Source (Internal Peternak)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberDarkElevated)
                .border(1.dp, AmberWarning.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
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
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HARGA JUAL PANEN (INTERNAL)",
                            color = AmberWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "Rp ${BroilerViewModel.formatNumber(costSummary.sellingPricePerKg)} / Kg",
                        color = NeonGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Harga penjualan diinput langsung pada tiket penimbangan saat panen dan tersinkronisasi otomatis dengan laba/rugi. Harga jual & omset ini strictly privat untuk peternak dan TIDAK dicantumkan pada nota penjualan pembeli.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CyberDarkElevated, RoundedCornerShape(10.dp))
            .border(1.dp, CyberCardBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
