package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExpenseCategory
import com.example.data.model.Floor
import com.example.data.model.OperationalExpense
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EditComprehensiveExpenseDialog
import com.example.ui.components.NeonBadge
import com.example.ui.components.NeonCard
import com.example.ui.components.ProfitLossCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerUiState
import com.example.ui.viewmodel.BroilerViewModel

@Composable
fun ExpensesScreen(
    state: BroilerUiState,
    viewModel: BroilerViewModel
) {
    val costSummary = viewModel.calculateCostSummary()
    val allSummary = viewModel.calculateFloorSummary(Floor.ALL)
    var selectedCategoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }
    var expenseToEdit by remember { mutableStateOf<OperationalExpense?>(null) }
    var expenseToDelete by remember { mutableStateOf<OperationalExpense?>(null) }

    val filteredExpenses = remember(state.operationalExpenses, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            state.operationalExpenses.reversed()
        } else {
            state.operationalExpenses.filter { it.category == selectedCategoryFilter }.reversed()
        }
    }

    val totalOps = costSummary.operationalCost
    val opsPerBird = if (allSummary.activePopulation > 0) totalOps / allSummary.activePopulation else 0.0
    val opsPerKg = if (allSummary.totalTonaseKg > 0) totalOps / allSummary.totalTonaseKg else 0.0

    Scaffold(
        containerColor = CyberDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddExpenseDialog(true) },
                containerColor = NeonAmber,
                contentColor = CyberDark,
                modifier = Modifier.testTag("fab_add_expense")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Catat Biaya Operasional", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Operasional Kandang & HPP",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kelistrikan, alas kandang/atal, pemanas, transportasi & bongkar muat",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    NeonBadge(
                        text = "PROFIT Club",
                        color = NeonGreen
                    )
                }
            }

            // Top KPI Banner: Total Operasional & Rasio per Ekor/Kg
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OperationalSummaryBox(
                        label = "Total Operasional",
                        value = BroilerViewModel.formatRupiah(totalOps),
                        sub = "Akumulasi Periode 14",
                        color = NeonAmber,
                        modifier = Modifier.weight(1.2f)
                    )
                    OperationalSummaryBox(
                        label = "Biaya / Ekor",
                        value = "Rp ${BroilerViewModel.formatNumber(opsPerBird)}",
                        sub = "Populasi Aktif",
                        color = NeonCyan,
                        modifier = Modifier.weight(0.9f)
                    )
                    OperationalSummaryBox(
                        label = "Biaya / Kg",
                        value = "Rp ${BroilerViewModel.formatNumber(opsPerKg)}",
                        sub = "Tonase Panen",
                        color = ElectricBlue,
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }

            // Operational Category Filter Pills
            item {
                Column {
                    Text(
                        text = "Filter Kategori Operasional:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryFilterChip(
                            label = "Semua (${state.operationalExpenses.size})",
                            isSelected = selectedCategoryFilter == null,
                            color = NeonAmber,
                            onClick = { selectedCategoryFilter = null }
                        )

                        ExpenseCategory.values().forEach { cat ->
                            val count = state.operationalExpenses.count { it.category == cat }
                            if (count > 0 || cat == ExpenseCategory.KELISTRIKAN || cat == ExpenseCategory.ATAL_ALAS_KANDANG || cat == ExpenseCategory.PEMANAS) {
                                CategoryFilterChip(
                                    label = "${cat.shortName} ($count)",
                                    isSelected = selectedCategoryFilter == cat,
                                    color = Color(cat.colorHex),
                                    onClick = { selectedCategoryFilter = cat }
                                )
                            }
                        }
                    }
                }
            }

            // Master HPP Hero Card (Formula Implementation)
            item {
                NeonCard(
                    borderColor = NeonAmber,
                    glowColor = NeonAmber.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HARGA POKOK PRODUKSI (HPP) MASTER",
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        NeonBadge(text = "Standar Kemitraan", color = NeonCyan)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("HPP per Kg Ayam Hidup", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "Rp ${BroilerViewModel.formatNumber(costSummary.hppPerKg)}",
                                color = NeonGreen,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("HPP per Ekor", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "Rp ${BroilerViewModel.formatNumber(costSummary.hppPerBird)}",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Breakdown Rows
                    HppCostItem("1. Biaya Pakan (Recording Harian)", costSummary.feedCost, NeonCyan)
                    HppCostItem("2. Biaya DOC (20.000 bibit)", costSummary.docCost, ElectricBlue)
                    HppCostItem("3. Biaya Operasional Kandang Lengkap", costSummary.operationalCost, NeonAmber)
                    HppCostItem("4. Biaya OVK & Sanitasi", costSummary.ovkCost, NeonViolet)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL BIAYA PRODUKSI", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = BroilerViewModel.formatRupiah(costSummary.totalProductionCost),
                            color = NeonAmber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimasi Margin Laba Bersih", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = BroilerViewModel.formatRupiah(costSummary.estimatedProfit),
                            color = if (costSummary.estimatedProfit > 0) NeonGreen else NeonRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Analisis Laba / Rugi Hasil Penjualan Panen vs HPP
            item {
                ProfitLossCard(
                    costSummary = costSummary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Facility Highlights: Kelistrikan, Atal/Alas, Pemanas, Transportasi, Bongkar Muat
            item {
                Text(
                    text = "Rincian Biaya Fasilitas & Sarana Pokok:",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val groupedExpenses = state.operationalExpenses
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FacilityHighlightRow(
                        title = "Kelistrikan & Daya",
                        subtitle = "PLN Pascabayar & Genset Solar Exhaust Fan",
                        amount = groupedExpenses[ExpenseCategory.KELISTRIKAN] ?: 0.0,
                        color = Color(ExpenseCategory.KELISTRIKAN.colorHex)
                    )
                    FacilityHighlightRow(
                        title = "Atal / Alas Kandang (Litter)",
                        subtitle = "Sekam Padi Tebal 3 Truk + Kapur Tohor",
                        amount = groupedExpenses[ExpenseCategory.ATAL_ALAS_KANDANG] ?: 0.0,
                        color = Color(ExpenseCategory.ATAL_ALAS_KANDANG.colorHex)
                    )
                    FacilityHighlightRow(
                        title = "Pemanas & Brooding",
                        subtitle = "Gas LPG 50kg & Selang Brooder Heater",
                        amount = groupedExpenses[ExpenseCategory.PEMANAS] ?: 0.0,
                        color = Color(ExpenseCategory.PEMANAS.colorHex)
                    )
                    FacilityHighlightRow(
                        title = "Jasa Bongkar Muat",
                        subtitle = "Bongkar Pakan 800 Sak, Box DOC & Tebar Sekam",
                        amount = groupedExpenses[ExpenseCategory.BONGKAR_MUAT] ?: 0.0,
                        color = Color(ExpenseCategory.BONGKAR_MUAT.colorHex)
                    )
                    FacilityHighlightRow(
                        title = "Transportasi & Logistik",
                        subtitle = "Sewa Armada Truk DOC, BBM & Tol Operasional",
                        amount = groupedExpenses[ExpenseCategory.TRANSPORTASI] ?: 0.0,
                        color = Color(ExpenseCategory.TRANSPORTASI.colorHex)
                    )
                }
            }

            // Ledger of Operational Expenses
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Transaksi Operasional (${filteredExpenses.size})",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedCategoryFilter != null) {
                        TextButton(onClick = { selectedCategoryFilter = null }) {
                            Text("Reset Filter", color = NeonCyan, fontSize = 11.sp)
                        }
                    }
                }
            }

            items(filteredExpenses, key = { it.id }) { expense ->
                DetailedExpenseItemCard(
                    expense = expense,
                    onEdit = { expenseToEdit = expense },
                    onDelete = { expenseToDelete = expense }
                )
            }
        }
    }

    // Add Expense Dialog
    if (state.isAddExpenseDialogOpen) {
        AddComprehensiveExpenseDialog(
            onDismiss = { viewModel.openAddExpenseDialog(false) },
            onSubmit = { category, amount, date, desc, floor, qty, unit, vendor ->
                viewModel.addOperationalExpense(
                    category = category,
                    amount = amount,
                    date = date,
                    description = desc,
                    floorScope = floor,
                    quantity = qty,
                    unit = unit,
                    vendor = vendor
                )
            }
        )
    }

    // Edit Expense Dialog
    val activeExpenseEdit = expenseToEdit
    if (activeExpenseEdit != null) {
        EditComprehensiveExpenseDialog(
            expense = activeExpenseEdit,
            onDismiss = { expenseToEdit = null },
            onSubmit = { updated ->
                viewModel.updateOperationalExpense(updated)
                expenseToEdit = null
            }
        )
    }

    // Delete Expense Confirmation Dialog
    val activeExpenseDelete = expenseToDelete
    if (activeExpenseDelete != null) {
        DeleteConfirmDialog(
            title = "Hapus Biaya Operasional?",
            message = "Apakah Anda yakin ingin menghapus catatan biaya operasional ${activeExpenseDelete.category.displayName} senilai ${BroilerViewModel.formatRupiah(activeExpenseDelete.amount)}? Total HPP akan otomatis disesuaikan.",
            itemName = "${activeExpenseDelete.category.displayName} • ${activeExpenseDelete.description} • ${BroilerViewModel.formatRupiah(activeExpenseDelete.amount)}",
            onConfirm = {
                viewModel.deleteOperationalExpense(activeExpenseDelete.id)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }
}

@Composable
fun HppCostItem(
    title: String,
    amount: Double,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(color, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, color = TextSecondary, fontSize = 12.sp)
        }
        Text(
            text = BroilerViewModel.formatRupiah(amount),
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun OperationalSummaryBox(
    label: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(sub, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) color else CyberCardBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) color else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun FacilityHighlightRow(
    title: String,
    subtitle: String,
    amount: Double,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurface)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = TextMuted, fontSize = 10.sp)
                }
            }

            Text(
                text = BroilerViewModel.formatRupiah(amount),
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun DetailedExpenseItemCard(
    expense: OperationalExpense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = Color(expense.category.colorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurface)
            .border(1.dp, CyberCardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeonBadge(text = expense.category.shortName, color = categoryColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    if (expense.floorScope != Floor.ALL) {
                        NeonBadge(text = expense.floorScope.displayName, color = NeonCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                Text(
                    text = expense.date,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = expense.description,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vol: ${expense.quantity} ${expense.unit}${if (expense.vendor.isNotBlank()) " • ${expense.vendor}" else ""}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Text(
                    text = BroilerViewModel.formatRupiah(expense.amount),
                    color = categoryColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_edit_expense_${expense.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NeonAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", color = NeonAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_delete_expense_${expense.id}")
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
fun AddComprehensiveExpenseDialog(
    onDismiss: () -> Unit,
    onSubmit: (ExpenseCategory, Double, String, String, Floor, Double, String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.KELISTRIKAN) }
    var selectedFloor by remember { mutableStateOf(Floor.ALL) }
    var amountInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("1") }
    var unitInput by remember { mutableStateOf("Paket") }
    var vendorInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("Hari ini") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Catat Biaya Operasional Kandang",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kelistrikan, atal/sekam, pemanas, transportasi & bongkar muat",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Kategori Operasional:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                // Category Grid Pills
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OperFilterButton(
                            title = "Kelistrikan",
                            isSelected = selectedCategory == ExpenseCategory.KELISTRIKAN,
                            color = Color(ExpenseCategory.KELISTRIKAN.colorHex),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = ExpenseCategory.KELISTRIKAN
                                unitInput = "Bulan"
                            }
                        )
                        OperFilterButton(
                            title = "Atal / Alas",
                            isSelected = selectedCategory == ExpenseCategory.ATAL_ALAS_KANDANG,
                            color = Color(ExpenseCategory.ATAL_ALAS_KANDANG.colorHex),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = ExpenseCategory.ATAL_ALAS_KANDANG
                                unitInput = "Truk"
                            }
                        )
                        OperFilterButton(
                            title = "Pemanas",
                            isSelected = selectedCategory == ExpenseCategory.PEMANAS,
                            color = Color(ExpenseCategory.PEMANAS.colorHex),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = ExpenseCategory.PEMANAS
                                unitInput = "Tabung 50kg"
                            }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OperFilterButton(
                            title = "Bongkar Muat",
                            isSelected = selectedCategory == ExpenseCategory.BONGKAR_MUAT,
                            color = Color(ExpenseCategory.BONGKAR_MUAT.colorHex),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = ExpenseCategory.BONGKAR_MUAT
                                unitInput = "Sak"
                            }
                        )
                        OperFilterButton(
                            title = "Transportasi",
                            isSelected = selectedCategory == ExpenseCategory.TRANSPORTASI,
                            color = Color(ExpenseCategory.TRANSPORTASI.colorHex),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = ExpenseCategory.TRANSPORTASI
                                unitInput = "Trip"
                            }
                        )
                        OperFilterButton(
                            title = "Operator",
                            isSelected = selectedCategory == ExpenseCategory.TENAGA_KERJA,
                            color = Color(ExpenseCategory.TENAGA_KERJA.colorHex),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = ExpenseCategory.TENAGA_KERJA
                                unitInput = "Orang"
                            }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OperFilterButton(
                            title = "Sanitasi",
                            isSelected = selectedCategory == ExpenseCategory.BIOSECURITY_SANITASI,
                            color = Color(ExpenseCategory.BIOSECURITY_SANITASI.colorHex),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = ExpenseCategory.BIOSECURITY_SANITASI
                                unitInput = "Jerigen"
                            }
                        )
                        OperFilterButton(
                            title = "Sparepart",
                            isSelected = selectedCategory == ExpenseCategory.MAINTENANCE_SPAREPART,
                            color = Color(ExpenseCategory.MAINTENANCE_SPAREPART.colorHex),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = ExpenseCategory.MAINTENANCE_SPAREPART
                                unitInput = "Pcs"
                            }
                        )
                        OperFilterButton(
                            title = "Lainnya",
                            isSelected = selectedCategory == ExpenseCategory.LAINNYA,
                            color = Color(ExpenseCategory.LAINNYA.colorHex),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedCategory = ExpenseCategory.LAINNYA
                                unitInput = "Paket"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Floor Scope
                Text("Alokasi Lantai:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Floor.values().forEach { fl ->
                        FilterChip(
                            selected = selectedFloor == fl,
                            onClick = { selectedFloor = fl },
                            label = { Text(fl.shortName, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("Keterangan Pengeluaran") },
                    placeholder = { Text("Contoh: Pembelian sekam padi tebar 2 lantai") },
                    modifier = Modifier.fillMaxWidth().testTag("input_oper_desc"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Total Biaya (Rp)") },
                    placeholder = { Text("Contoh: 1500000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_oper_amount"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Volume / Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitInput,
                        onValueChange = { unitInput = it },
                        label = { Text("Satuan (Truk/Sak/dll)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = vendorInput,
                    onValueChange = { vendorInput = it },
                    label = { Text("Vendor / Toko / Supplier (Opsional)") },
                    placeholder = { Text("Contoh: UD Sekam Berkah / Agen Gas") },
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
                            val amount = amountInput.toDoubleOrNull() ?: 0.0
                            val qty = quantityInput.toDoubleOrNull() ?: 1.0
                            if (amount > 0 && descInput.isNotBlank()) {
                                onSubmit(
                                    selectedCategory,
                                    amount,
                                    dateInput,
                                    descInput,
                                    selectedFloor,
                                    qty,
                                    unitInput,
                                    vendorInput
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                        modifier = Modifier.weight(1f).testTag("btn_save_oper_expense")
                    ) {
                        Text("Simpan", color = CyberDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OperFilterButton(
    title: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else CyberSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) color else CyberCardBorder.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) color else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
