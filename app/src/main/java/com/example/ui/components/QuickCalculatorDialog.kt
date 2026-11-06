package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun QuickCalculatorDialog(
    onDismiss: () -> Unit
) {
    var initialPopInput by remember { mutableStateOf("10000") }
    var harvestAgeInput by remember { mutableStateOf("28") }
    var avgWeightInput by remember { mutableStateOf("1.78") }
    var totalFeedInput by remember { mutableStateOf("25600") }
    var totalDeathInput by remember { mutableStateOf("290") }

    // Calculated states
    val pop = initialPopInput.toDoubleOrNull() ?: 10000.0
    val age = harvestAgeInput.toDoubleOrNull() ?: 28.0
    val avgWeight = avgWeightInput.toDoubleOrNull() ?: 1.78
    val totalFeed = totalFeedInput.toDoubleOrNull() ?: 25600.0
    val totalDeath = totalDeathInput.toDoubleOrNull() ?: 290.0

    val deplesi = if (pop > 0) (totalDeath / pop) * 100.0 else 0.0
    val livability = (100.0 - deplesi).coerceAtLeast(0.0)
    val activePop = (pop - totalDeath).coerceAtLeast(0.0)
    val tonaseKg = activePop * avgWeight
    val fcr = if (tonaseKg > 0) totalFeed / tonaseKg else 0.0
    val ip = if (fcr > 0 && age > 0) (livability * avgWeight) / (fcr * age) * 100.0 else 0.0

    val ipGrade = when {
        ip >= 400 -> Pair("Sangat Istimewa (Super)", NeonGreen)
        ip >= 351 -> Pair("Sangat Baik (Excellent)", NeonCyan)
        ip >= 326 -> Pair("Baik (Good)", ElectricBlue)
        ip >= 301 -> Pair("Sedang (Average)", NeonAmber)
        else -> Pair("Kurang / Evaluasi", NeonRed)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
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
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Kalkulator",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kalkulator IP & FCR Broiler",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary
                        )
                    }
                }

                Text(
                    text = "Simulasi instan rumus standar peternakan broiler modern",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Output Result Card (Futuristic Neon HUD)
                NeonCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ipGrade.second,
                    glowColor = ipGrade.second.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Indeks Performa (IP)", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "%.1f".format(ip),
                                color = ipGrade.second,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        NeonBadge(
                            text = ipGrade.first,
                            color = ipGrade.second
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FCR", color = TextSecondary, fontSize = 11.sp)
                            Text("%.2f".format(fcr), color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Livability", color = TextSecondary, fontSize = 11.sp)
                            Text("%.1f%%".format(livability), color = NeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Deplesi", color = TextSecondary, fontSize = 11.sp)
                            Text("%.1f%%".format(deplesi), color = NeonAmber, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Tonase", color = TextSecondary, fontSize = 11.sp)
                            Text("%.1f Ton".format(tonaseKg / 1000.0), color = ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Parameter Input:", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                // Inputs
                OutlinedTextField(
                    value = initialPopInput,
                    onValueChange = { initialPopInput = it },
                    label = { Text("Populasi Awal (ekor)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("calc_pop_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = harvestAgeInput,
                        onValueChange = { harvestAgeInput = it },
                        label = { Text("Umur (hari)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("calc_age_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = avgWeightInput,
                        onValueChange = { avgWeightInput = it },
                        label = { Text("Bobot Rata-rata (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("calc_weight_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalFeedInput,
                        onValueChange = { totalFeedInput = it },
                        label = { Text("Total Pakan (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("calc_feed_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = totalDeathInput,
                        onValueChange = { totalDeathInput = it },
                        label = { Text("Mati + Afkir (ekor)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("calc_death_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("close_calc_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup", color = CyberDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
