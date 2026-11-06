package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.ScaleEntry
import com.example.ui.theme.*

@Composable
fun EditScaleEntryDialog(
    entry: ScaleEntry,
    onDismiss: () -> Unit,
    onSubmit: (Int, Double) -> Unit
) {
    var birdCountInput by remember { mutableStateOf(entry.birdCount.toString()) }
    var weightInput by remember { mutableStateOf(entry.netWeightKg.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Timbangan #${entry.scaleNumber}",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Koreksi jumlah ekor atau bobot timbangan",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = birdCountInput,
                    onValueChange = { birdCountInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Ekor", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_scale_bird_count")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Bobot Timbangan (Kg)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_scale_weight")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = NeonRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                            val birds = birdCountInput.toIntOrNull() ?: 0
                            val weight = weightInput.toDoubleOrNull() ?: 0.0

                            if (birds <= 0) {
                                errorMessage = "Jumlah ekor harus lebih dari 0!"
                                return@Button
                            }
                            if (weight <= 0.0) {
                                errorMessage = "Bobot kg harus lebih dari 0!"
                                return@Button
                            }

                            onSubmit(birds, weight)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_save_edit_scale")
                    ) {
                        Text("Simpan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
