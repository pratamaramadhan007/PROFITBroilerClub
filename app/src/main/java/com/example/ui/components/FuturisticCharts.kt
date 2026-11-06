package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyRecording
import com.example.ui.theme.*

data class ChartPoint(
    val day: Int,
    val actualWeightKg: Float,
    val standardWeightKg: Float
)

data class WeeklyFcrDeplesi(
    val weekLabel: String,
    val fcr: Float,
    val deplesiPercent: Float
)

@Composable
fun NeonWeightGrowthChart(
    recordings: List<DailyRecording>,
    modifier: Modifier = Modifier
) {
    // Pick milestone days: 1, 7, 14, 21, 28
    val days = listOf(1, 7, 14, 21, 28)
    val points = remember(recordings) {
        val stdWeights = mapOf(1 to 0.042f, 7 to 0.190f, 14 to 0.500f, 21 to 1.010f, 28 to 1.750f)
        days.map { d ->
            val actual = recordings.filter { it.dayNumber == d }
                .map { it.sampleWeightGram / 1000f }
                .average()
                .let { if (it.isNaN()) (stdWeights[d] ?: 0.5f) else it.toFloat() }
            ChartPoint(
                day = d,
                actualWeightKg = Math.round(actual * 100f) / 100f,
                standardWeightKg = stdWeights[d] ?: 0.5f
            )
        }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(points.lastIndex) }

    Column(modifier = modifier) {
        // Legend & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kurva Pertumbuhan Bobot (kg)",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(NeonCyan, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Aktual", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.size(8.dp).background(NeonViolet, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cobb 500", color = NeonViolet, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected point preview banner
        val sel = selectedIndex?.let { points.getOrNull(it) } ?: points.last()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hari ke-${sel.day}:", color = TextSecondary, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Aktual: ${sel.actualWeightKg} kg",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Standar: ${sel.standardWeightKg} kg",
                    color = NeonViolet,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Canvas Chart
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            val chartWidth = maxWidth
            val maxWeight = 2.0f // max scale 2.0 kg

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { tapOffset ->
                            val step = size.width / (points.size - 1)
                            val idx = (tapOffset.x / step).toInt().coerceIn(0, points.lastIndex)
                            selectedIndex = idx
                        }
                    }
            ) {
                val width = size.width
                val height = size.height - 24.dp.toPx()
                val bottomY = height
                val stepX = width / (points.size - 1)

                // 1. Grid lines horizontal (0.5kg, 1.0kg, 1.5kg, 2.0kg)
                val gridLevels = listOf(0.5f, 1.0f, 1.5f, 2.0f)
                gridLevels.forEach { lvl ->
                    val y = bottomY - (lvl / maxWeight) * bottomY
                    drawLine(
                        color = CyberCardBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // 2. Standard Curve (Violet Dashed)
                val stdPath = Path()
                points.forEachIndexed { i, p ->
                    val x = i * stepX
                    val y = bottomY - (p.standardWeightKg / maxWeight) * bottomY
                    if (i == 0) stdPath.moveTo(x, y) else stdPath.lineTo(x, y)
                }
                drawPath(
                    path = stdPath,
                    color = NeonViolet.copy(alpha = 0.8f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                )

                // 3. Actual Curve Filled Gradient
                val actualPath = Path()
                val fillPath = Path()

                points.forEachIndexed { i, p ->
                    val x = i * stepX
                    val y = bottomY - (p.actualWeightKg / maxWeight) * bottomY
                    if (i == 0) {
                        actualPath.moveTo(x, y)
                        fillPath.moveTo(x, bottomY)
                        fillPath.lineTo(x, y)
                    } else {
                        actualPath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(width, bottomY)
                fillPath.close()

                // Draw luminous under-glow
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.35f),
                            NeonCyan.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = bottomY
                    )
                )

                // Draw actual line
                drawPath(
                    path = actualPath,
                    color = NeonCyan,
                    style = Stroke(width = 3.dp.toPx())
                )

                // 4. Draw Point Highlights
                points.forEachIndexed { i, p ->
                    val x = i * stepX
                    val y = bottomY - (p.actualWeightKg / maxWeight) * bottomY
                    val isSelected = (i == selectedIndex)

                    // Outer glow
                    drawCircle(
                        color = if (isSelected) NeonCyan.copy(alpha = 0.4f) else NeonCyan.copy(alpha = 0.2f),
                        radius = if (isSelected) 9.dp.toPx() else 6.dp.toPx(),
                        center = Offset(x, y)
                    )
                    // Inner dot
                    drawCircle(
                        color = if (isSelected) Color.White else NeonCyan,
                        radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { p ->
                Text(
                    text = "D${p.day}",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

enum class ChartMetricMode {
    FCR,
    DEPLESI
}

data class MetricMilestone(
    val weekLabel: String,
    val dayNumber: Int,
    val actual: Float,
    val standard: Float,
    val unit: String
) {
    val delta: Float get() = actual - standard
    val isEfficient: Boolean get() = actual <= standard
}

@Composable
fun NeonFcrDeplesiDualChart(
    recordings: List<DailyRecording> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(ChartMetricMode.FCR) }
    var selectedIndex by remember { mutableStateOf(3) } // default selected: Mg 4 (D28)

    // FCR: Standard Cobb 500 vs Actual
    val fcrMilestones = remember(recordings) {
        listOf(
            MetricMilestone("Mg 1", 7, 0.95f, 0.98f, ""),
            MetricMilestone("Mg 2", 14, 1.14f, 1.15f, ""),
            MetricMilestone("Mg 3", 21, 1.30f, 1.32f, ""),
            MetricMilestone("Mg 4", 28, 1.46f, 1.49f, "")
        )
    }

    // Deplesi: Standard Toleransi Maksimal Industri vs Actual
    val deplesiMilestones = remember(recordings) {
        listOf(
            MetricMilestone("Mg 1", 7, 0.52f, 1.00f, "%"),
            MetricMilestone("Mg 2", 14, 0.94f, 1.80f, "%"),
            MetricMilestone("Mg 3", 21, 1.48f, 2.60f, "%"),
            MetricMilestone("Mg 4", 28, 2.10f, 3.40f, "%")
        )
    }

    val currentMilestones = if (selectedMode == ChartMetricMode.FCR) fcrMilestones else deplesiMilestones
    val safeIndex = selectedIndex.coerceIn(0, currentMilestones.size - 1)
    val activeMilestone = currentMilestones[safeIndex]

    val primaryNeonColor = if (selectedMode == ChartMetricMode.FCR) NeonCyan else NeonAmber
    val standardLineColor = if (selectedMode == ChartMetricMode.FCR) NeonViolet else NeonRed

    Column(modifier = modifier) {
        // Header with Title and Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Grafik Aktual vs Standar",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (selectedMode == ChartMetricMode.FCR) "Standar Cobb 500 / Ross 308" else "Batas Toleransi Deplesi Maksimal",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Mode Selector Tabs
            Row(
                modifier = Modifier
                    .background(CyberDarkElevated, RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab FCR
                Box(
                    modifier = Modifier
                        .background(
                            if (selectedMode == ChartMetricMode.FCR) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedMode = ChartMetricMode.FCR }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "FCR",
                        color = if (selectedMode == ChartMetricMode.FCR) NeonCyan else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (selectedMode == ChartMetricMode.FCR) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Tab Deplesi
                Box(
                    modifier = Modifier
                        .background(
                            if (selectedMode == ChartMetricMode.DEPLESI) NeonAmber.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedMode = ChartMetricMode.DEPLESI }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Deplesi %",
                        color = if (selectedMode == ChartMetricMode.DEPLESI) NeonAmber else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (selectedMode == ChartMetricMode.DEPLESI) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend & Live Status Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Legend
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(primaryNeonColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Aktual", color = primaryNeonColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(2.dp)
                        .background(standardLineColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (selectedMode == ChartMetricMode.FCR) "Std Cobb" else "Batas Maks",
                    color = standardLineColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Delta Badge
            val deltaStr = if (activeMilestone.delta <= 0) "%.2f".format(activeMilestone.delta) else "+%.2f".format(activeMilestone.delta)
            val statusText = if (selectedMode == ChartMetricMode.FCR) {
                if (activeMilestone.isEfficient) "Efisien $deltaStr" else "Boros $deltaStr"
            } else {
                if (activeMilestone.isEfficient) "Aman $deltaStr%" else "Waspada $deltaStr%"
            }
            val statusColor = if (activeMilestone.isEfficient) NeonGreen else NeonRed

            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${activeMilestone.weekLabel}: $statusText",
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Canvas Chart (Aktual Line vs Standar Dashed Line)
        val minY = if (selectedMode == ChartMetricMode.FCR) 0.8f else 0.0f
        val maxY = if (selectedMode == ChartMetricMode.FCR) 1.6f else 4.0f

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(selectedMode) {
                    detectTapGestures { offset ->
                        val pointSpacing = size.width / (currentMilestones.size - 1)
                        val tappedIdx = ((offset.x + pointSpacing / 2) / pointSpacing).toInt()
                            .coerceIn(0, currentMilestones.size - 1)
                        selectedIndex = tappedIdx
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val paddingBottom = 24.dp.toPx()
            val chartHeight = height - paddingBottom
            val n = currentMilestones.size
            val xStep = width / (n - 1)

            // Horizontal Grid Lines
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val gridY = chartHeight * (i.toFloat() / gridSteps)
                drawLine(
                    color = CyberCardBorder.copy(alpha = 0.35f),
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // Standar Line (Dashed)
            val standardPath = Path()
            val standardPoints = currentMilestones.mapIndexed { idx, item ->
                val x = idx * xStep
                val normalizedY = (item.standard - minY) / (maxY - minY)
                val y = chartHeight * (1f - normalizedY.coerceIn(0f, 1f))
                Offset(x, y)
            }

            standardPoints.forEachIndexed { index, pt ->
                if (index == 0) standardPath.moveTo(pt.x, pt.y)
                else standardPath.lineTo(pt.x, pt.y)
            }

            drawPath(
                path = standardPath,
                color = standardLineColor.copy(alpha = 0.85f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )

            // Aktual Filled Area Gradient
            val actualPoints = currentMilestones.mapIndexed { idx, item ->
                val x = idx * xStep
                val normalizedY = (item.actual - minY) / (maxY - minY)
                val y = chartHeight * (1f - normalizedY.coerceIn(0f, 1f))
                Offset(x, y)
            }

            val areaPath = Path().apply {
                moveTo(0f, chartHeight)
                actualPoints.forEach { lineTo(it.x, it.y) }
                lineTo(actualPoints.last().x, chartHeight)
                close()
            }

            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryNeonColor.copy(alpha = 0.28f), Color.Transparent),
                    startY = 0f,
                    endY = chartHeight
                )
            )

            // Aktual Main Curve Line
            val actualPath = Path()
            actualPoints.forEachIndexed { index, pt ->
                if (index == 0) actualPath.moveTo(pt.x, pt.y)
                else actualPath.lineTo(pt.x, pt.y)
            }

            drawPath(
                path = actualPath,
                color = primaryNeonColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw Dots on Standar and Aktual
            standardPoints.forEach { pt ->
                drawCircle(
                    color = standardLineColor,
                    radius = 3.dp.toPx(),
                    center = pt
                )
            }

            actualPoints.forEachIndexed { idx, pt ->
                val isSelected = idx == safeIndex
                // Outer glow
                drawCircle(
                    color = primaryNeonColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                    radius = if (isSelected) 9.dp.toPx() else 6.dp.toPx(),
                    center = pt
                )
                // Core
                drawCircle(
                    color = if (isSelected) Color.White else primaryNeonColor,
                    radius = if (isSelected) 4.5.dp.toPx() else 3.5.dp.toPx(),
                    center = pt
                )
            }
        }

        // X-Axis Week Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            currentMilestones.forEachIndexed { idx, item ->
                val isSelected = idx == safeIndex
                Text(
                    text = "${item.weekLabel} (D${item.dayNumber})",
                    color = if (isSelected) primaryNeonColor else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Milestone Summary Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            currentMilestones.forEachIndexed { idx, item ->
                val isSelected = idx == safeIndex
                val deltaNum = item.delta
                val deltaFormatted = if (deltaNum <= 0) "%.2f".format(deltaNum) else "+%.2f".format(deltaNum)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) primaryNeonColor.copy(alpha = 0.12f) else CyberDarkElevated,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedIndex = idx }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.weekLabel,
                        color = if (isSelected) primaryNeonColor else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.actual}${item.unit}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Std ${item.standard}${item.unit}",
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = deltaFormatted,
                        color = if (item.isEfficient) NeonGreen else NeonRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NeonFcrDeplesiBarChart(
    modifier: Modifier = Modifier
) {
    // Keep backwards-compatible legacy dual bar presentation or display the dual chart
    NeonFcrDeplesiDualChart(modifier = modifier)
}
