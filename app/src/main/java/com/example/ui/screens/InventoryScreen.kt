package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FeedItem
import com.example.data.model.OvkCategory
import com.example.data.model.OvkItem
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EditFeedDialog
import com.example.ui.components.EditOvkDialog
import com.example.ui.components.NeonBadge
import com.example.ui.components.NeonCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerUiState
import com.example.ui.viewmodel.BroilerViewModel

@Composable
fun InventoryScreen(
    state: BroilerUiState,
    viewModel: BroilerViewModel
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Pakan, 1 = OVK
    var feedToEdit by remember { mutableStateOf<FeedItem?>(null) }
    var feedToDelete by remember { mutableStateOf<FeedItem?>(null) }
    var ovkToEdit by remember { mutableStateOf<OvkItem?>(null) }
    var ovkToDelete by remember { mutableStateOf<OvkItem?>(null) }

    Scaffold(
        containerColor = CyberDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        viewModel.openAddFeedDialog(true)
                    } else {
                        viewModel.openAddOvkDialog(true)
                    }
                },
                containerColor = NeonCyan,
                contentColor = CyberDark,
                modifier = Modifier.testTag("fab_add_inventory")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedTab == 0) "Tambah Pakan" else "Tambah OVK",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Inventaris Pakan & OVK",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manajemen stok logistik & pengurangan otomatis",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Inventory Valuation summary
                val totalFeedStockKg = state.feeds.sumOf { it.currentStockKg }
                NeonBadge(
                    text = "${(totalFeedStockKg / 1000).toInt()} Ton Tersisa",
                    color = NeonGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Sync Logic Explanation Banner
            NeonCard(
                borderColor = ElectricBlue.copy(alpha = 0.4f),
                glowColor = ElectricBlue.copy(alpha = 0.1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SyncAlt,
                        contentDescription = "Sinkronisasi",
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Logika Pengurangan Stok Otomatis:",
                            color = ElectricBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Stok Pakan Terkini = Stok Awal - Total Pemakaian di Recording. Jika stok = 0 kg, input otomatis terkunci.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch Tab: Pakan vs OVK
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CyberSurfaceVariant,
                contentColor = NeonCyan,
                divider = {},
                indicator = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                        .testTag("tab_pakan"),
                    text = {
                        Text(
                            "Stok Pakan (${state.feeds.size})",
                            color = if (selectedTab == 0) NeonCyan else TextSecondary,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) NeonGreen.copy(alpha = 0.2f) else Color.Transparent)
                        .testTag("tab_ovk"),
                    text = {
                        Text(
                            "OVK & Medis (${state.ovks.size})",
                            color = if (selectedTab == 1) NeonGreen else TextSecondary,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Content List
            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.feeds, key = { it.id }) { feed ->
                        FeedInventoryCard(
                            feed = feed,
                            onEdit = { feedToEdit = feed },
                            onDelete = { feedToDelete = feed }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.ovks, key = { it.id }) { ovk ->
                        OvkInventoryCard(
                            ovk = ovk,
                            onEdit = { ovkToEdit = ovk },
                            onDelete = { ovkToDelete = ovk }
                        )
                    }
                }
            }
        }
    }

    // Add Feed Dialog
    if (state.isAddFeedDialogOpen) {
        AddFeedDialog(
            onDismiss = { viewModel.openAddFeedDialog(false) },
            onSubmit = { name, code, stock, price, supplier ->
                viewModel.addOrReplenishFeed(name, code, stock, price, supplier)
            }
        )
    }

    // Add OVK Dialog
    if (state.isAddOvkDialogOpen) {
        AddOvkDialog(
            onDismiss = { viewModel.openAddOvkDialog(false) },
            onSubmit = { name, cat, stock, unit, price ->
                viewModel.addOrReplenishOvk(name, cat, stock, unit, price)
            }
        )
    }

    // Edit Feed Dialog
    val activeFeedEdit = feedToEdit
    if (activeFeedEdit != null) {
        EditFeedDialog(
            feed = activeFeedEdit,
            onDismiss = { feedToEdit = null },
            onSubmit = { updated ->
                viewModel.updateFeedItem(updated)
                feedToEdit = null
            }
        )
    }

    // Delete Feed Dialog
    val activeFeedDelete = feedToDelete
    if (activeFeedDelete != null) {
        DeleteConfirmDialog(
            title = "Hapus Data Pakan?",
            message = "Apakah Anda yakin ingin menghapus ${activeFeedDelete.name} (${activeFeedDelete.code}) dari inventaris? Stok tersisa ${activeFeedDelete.currentStockKg} kg.",
            itemName = "${activeFeedDelete.name} • ${activeFeedDelete.code} • Stok ${activeFeedDelete.currentStockKg} Kg",
            onConfirm = {
                viewModel.deleteFeedItem(activeFeedDelete.id)
                feedToDelete = null
            },
            onDismiss = { feedToDelete = null }
        )
    }

    // Edit OVK Dialog
    val activeOvkEdit = ovkToEdit
    if (activeOvkEdit != null) {
        EditOvkDialog(
            ovk = activeOvkEdit,
            onDismiss = { ovkToEdit = null },
            onSubmit = { updated ->
                viewModel.updateOvkItem(updated)
                ovkToEdit = null
            }
        )
    }

    // Delete OVK Dialog
    val activeOvkDelete = ovkToDelete
    if (activeOvkDelete != null) {
        DeleteConfirmDialog(
            title = "Hapus Data OVK?",
            message = "Apakah Anda yakin ingin menghapus ${activeOvkDelete.name} (${activeOvkDelete.category.displayName}) dari inventaris?",
            itemName = "${activeOvkDelete.name} • ${activeOvkDelete.category.displayName} • ${activeOvkDelete.currentStock} ${activeOvkDelete.unit}",
            onConfirm = {
                viewModel.deleteOvkItem(activeOvkDelete.id)
                ovkToDelete = null
            },
            onDismiss = { ovkToDelete = null }
        )
    }
}

@Composable
fun FeedInventoryCard(
    feed: FeedItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = feed.percentageRemaining
    val statusColor = when {
        feed.isOutOfStock -> NeonRed
        feed.isLowStock -> NeonAmber
        else -> NeonGreen
    }

    val statusText = when {
        feed.isOutOfStock -> "HABIS (0 kg)"
        feed.isLowStock -> "Stok Menipis"
        else -> "Stok Aman"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .border(
                1.dp,
                if (feed.isOutOfStock) NeonRed.copy(alpha = 0.5f) else CyberCardBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = feed.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        NeonBadge(text = feed.code, color = Color(feed.colorHex))
                    }
                    Text(
                        text = "Supplier: ${feed.supplier} • ${feed.packagingType}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                NeonBadge(text = statusText, color = statusColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Sisa Stok Terkini:", color = TextSecondary, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${BroilerViewModel.formatNumber(feed.currentStockKg)} kg",
                            color = statusColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${"%.1f".format(feed.currentStockSak)} Sak)",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Harga Satuan:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${BroilerViewModel.formatRupiah(feed.pricePerKg)} / kg",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Rp ${BroilerViewModel.formatNumber(feed.pricePerKg * 50)} / sak",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = CyberSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Awal: ${feed.initialStockKg.toInt()} kg (${(feed.initialStockKg / 50).toInt()} sak)",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "Terpakai: ${(feed.initialStockKg - feed.currentStockKg).toInt()} kg",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_edit_feed_${feed.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NeonCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_delete_feed_${feed.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = NeonRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OvkInventoryCard(
    ovk: OvkItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = if (ovk.isOutOfStock) NeonRed else NeonGreen
    val categoryColor = when (ovk.category) {
        OvkCategory.VAKSIN -> NeonCyan
        OvkCategory.ANTIBIOTIK -> NeonAmber
        OvkCategory.VITAMIN -> NeonGreen
        OvkCategory.KIMIA_DISINFEKTAN -> NeonViolet
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = ovk.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    NeonBadge(text = ovk.category.label, color = categoryColor)
                }

                NeonBadge(
                    text = if (ovk.isOutOfStock) "Habis" else "Tersedia",
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Sisa Stok:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${ovk.currentStock} ${ovk.unit}",
                        color = statusColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Harga per Satuan:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = BroilerViewModel.formatRupiah(ovk.pricePerUnit),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_edit_ovk_${ovk.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NeonGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_delete_ovk_${ovk.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = NeonRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddFeedDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Double, Double, String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var stockInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("9200") }
    var supplierInput by remember { mutableStateOf("PT Charoen Pokphand") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = CyberSurface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Tambah Stok Pakan Baru", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nama Pakan") },
                    placeholder = { Text("Contoh: BR-1 Pre-Starter") },
                    modifier = Modifier.fillMaxWidth().testTag("input_feed_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("Kode") },
                        placeholder = { Text("BR-1") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stockInput,
                        onValueChange = { stockInput = it },
                        label = { Text("Stok Masuk (Kg)") },
                        placeholder = { Text("2000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_feed_stock"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Harga per Kg (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = supplierInput,
                    onValueChange = { supplierInput = it },
                    label = { Text("Supplier / Merek") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Batal", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            val stock = stockInput.toDoubleOrNull() ?: 0.0
                            val price = priceInput.toDoubleOrNull() ?: 0.0
                            if (nameInput.isNotBlank() && stock > 0) {
                                onSubmit(nameInput, codeInput, stock, price, supplierInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.weight(1f).testTag("btn_save_feed")
                    ) {
                        Text("Simpan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddOvkDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, OvkCategory, Double, String, Double) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(OvkCategory.VITAMIN) }
    var stockInput by remember { mutableStateOf("10") }
    var unitInput by remember { mutableStateOf("Sachet (100g)") }
    var priceInput by remember { mutableStateOf("35000") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = CyberSurface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Tambah OVK (Obat/Vaksin/Kimia)", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nama Produk OVK") },
                    placeholder = { Text("Contoh: Vita Stress") },
                    modifier = Modifier.fillMaxWidth().testTag("input_ovk_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Kategori:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OvkCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockInput,
                        onValueChange = { stockInput = it },
                        label = { Text("Jumlah Stok") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitInput,
                        onValueChange = { unitInput = it },
                        label = { Text("Satuan (Botol/Sachet/Vial)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Harga Satuan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Batal", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            val stock = stockInput.toDoubleOrNull() ?: 0.0
                            val price = priceInput.toDoubleOrNull() ?: 0.0
                            if (nameInput.isNotBlank() && stock > 0) {
                                onSubmit(nameInput, selectedCategory, stock, unitInput, price)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.weight(1f).testTag("btn_save_ovk")
                    ) {
                        Text("Simpan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
