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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FeedItem
import com.example.data.model.OvkCategory
import com.example.data.model.OvkItem
import com.example.ui.theme.*

@Composable
fun EditFeedDialog(
    feed: FeedItem,
    onDismiss: () -> Unit,
    onSubmit: (FeedItem) -> Unit
) {
    var nameInput by remember { mutableStateOf(feed.name) }
    var codeInput by remember { mutableStateOf(feed.code) }
    var stockInput by remember { mutableStateOf(feed.currentStockKg.toInt().toString()) }
    var priceInput by remember { mutableStateOf(feed.pricePerKg.toInt().toString()) }
    var supplierInput by remember { mutableStateOf(feed.supplier) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Data Pakan",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = feed.code,
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nama Pakan", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_feed_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it.uppercase() },
                    label = { Text("Kode Pakan", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = stockInput,
                    onValueChange = { stockInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Stok Saat Ini (Kg)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_feed_stock")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga per Kg (Rp)", fontSize = 12.sp) },
                    prefix = { Text("Rp ", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_feed_price")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = supplierInput,
                    onValueChange = { supplierInput = it },
                    label = { Text("Supplier Pabrik", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = NeonRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                            val stock = stockInput.toDoubleOrNull() ?: feed.currentStockKg
                            val price = priceInput.toDoubleOrNull() ?: feed.pricePerKg

                            if (nameInput.isBlank()) {
                                errorMessage = "Nama pakan tidak boleh kosong!"
                                return@Button
                            }

                            val updated = feed.copy(
                                name = nameInput,
                                code = codeInput.ifBlank { feed.code },
                                currentStockKg = stock.coerceAtLeast(0.0),
                                pricePerKg = price.coerceAtLeast(100.0),
                                supplier = supplierInput.ifBlank { feed.supplier }
                            )
                            onSubmit(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_save_edit_feed")
                    ) {
                        Text("Simpan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditOvkDialog(
    ovk: OvkItem,
    onDismiss: () -> Unit,
    onSubmit: (OvkItem) -> Unit
) {
    var nameInput by remember { mutableStateOf(ovk.name) }
    var selectedCategory by remember { mutableStateOf(ovk.category) }
    var stockInput by remember { mutableStateOf(ovk.currentStock.toString()) }
    var unitInput by remember { mutableStateOf(ovk.unit) }
    var priceInput by remember { mutableStateOf(ovk.pricePerUnit.toInt().toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit Data OVK",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ovk.category.displayName,
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text("Kategori Obat / Vitamin / Kimia:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OvkCategory.values().forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else CyberDarkElevated)
                                .border(1.dp, if (isSelected) NeonGreen else CyberCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.displayName,
                                color = if (isSelected) NeonGreen else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nama Obat / Vaksin / Vitamin", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_ovk_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stockInput,
                        onValueChange = { stockInput = it },
                        label = { Text("Stok Terkini", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_edit_ovk_stock")
                    )

                    OutlinedTextField(
                        value = unitInput,
                        onValueChange = { unitInput = it },
                        label = { Text("Satuan (Botol, Dosis, Kg)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga per Satuan (Rp)", fontSize = 12.sp) },
                    prefix = { Text("Rp ", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = NeonRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                            val stock = stockInput.toDoubleOrNull() ?: ovk.currentStock
                            val price = priceInput.toDoubleOrNull() ?: ovk.pricePerUnit

                            if (nameInput.isBlank()) {
                                errorMessage = "Nama OVK tidak boleh kosong!"
                                return@Button
                            }

                            val updated = ovk.copy(
                                name = nameInput,
                                category = selectedCategory,
                                currentStock = stock.coerceAtLeast(0.0),
                                unit = unitInput.ifBlank { ovk.unit },
                                pricePerUnit = price.coerceAtLeast(0.0)
                            )
                            onSubmit(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_save_edit_ovk")
                    ) {
                        Text("Simpan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
