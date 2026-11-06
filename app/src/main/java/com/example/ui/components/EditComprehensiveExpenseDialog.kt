package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExpenseCategory
import com.example.data.model.Floor
import com.example.data.model.OperationalExpense
import com.example.ui.theme.*

@Composable
fun EditComprehensiveExpenseDialog(
    expense: OperationalExpense,
    onDismiss: () -> Unit,
    onSubmit: (OperationalExpense) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var selectedFloor by remember { mutableStateOf(expense.floorScope) }
    var amountInput by remember { mutableStateOf(expense.amount.toInt().toString()) }
    var descInput by remember { mutableStateOf(expense.description) }
    var quantityInput by remember { mutableStateOf(expense.quantity.toString()) }
    var unitInput by remember { mutableStateOf(expense.unit) }
    var vendorInput by remember { mutableStateOf(expense.vendor) }
    var dateInput by remember { mutableStateOf(expense.date) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, NeonAmber.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
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
                            text = "Edit Biaya Operasional",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = expense.category.displayName,
                            color = NeonAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Kategori Selector
                Text("Kategori Operasional:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val categories = ExpenseCategory.values()
                    val rows = categories.toList().chunked(3)
                    rows.forEach { rowCats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowCats.forEach { cat ->
                                val isSelected = cat == selectedCategory
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonAmber.copy(alpha = 0.2f) else CyberDarkElevated)
                                        .border(1.dp, if (isSelected) NeonAmber else CyberCardBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.shortName,
                                        color = if (isSelected) NeonAmber else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scope Lantai
                Text("Peruntukan Lantai:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Floor.values().forEach { fl ->
                        val isSelected = fl == selectedFloor
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberDarkElevated)
                                .border(1.dp, if (isSelected) NeonCyan else CyberCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedFloor = fl }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fl.displayName,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nominal Biaya
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Biaya (Rp)", fontSize = 12.sp) },
                    prefix = { Text("Rp ", color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_expense_amount")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Deskripsi
                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("Deskripsi Pengeluaran", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_expense_desc")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Volume & Satuan
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Volume / Qty", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unitInput,
                        onValueChange = { unitInput = it },
                        label = { Text("Satuan", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Vendor / Toko & Tanggal
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = vendorInput,
                        onValueChange = { vendorInput = it },
                        label = { Text("Vendor / Toko", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("Tanggal", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = NeonRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val amount = amountInput.toDoubleOrNull() ?: 0.0
                            if (amount <= 0) {
                                errorMessage = "Nominal biaya harus lebih dari 0!"
                                return@Button
                            }
                            if (descInput.isBlank()) {
                                errorMessage = "Deskripsi pengeluaran tidak boleh kosong!"
                                return@Button
                            }

                            val qty = quantityInput.toDoubleOrNull() ?: 1.0
                            val updated = expense.copy(
                                category = selectedCategory,
                                floorScope = selectedFloor,
                                amount = amount,
                                description = descInput,
                                quantity = qty,
                                unit = unitInput.ifBlank { "Paket" },
                                vendor = vendorInput,
                                date = dateInput.ifBlank { expense.date }
                            )
                            onSubmit(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_save_edit_expense")
                    ) {
                        Text("Simpan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
