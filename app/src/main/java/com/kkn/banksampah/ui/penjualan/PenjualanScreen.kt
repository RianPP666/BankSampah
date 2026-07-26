package com.kkn.banksampah.ui.penjualan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kkn.banksampah.data.model.DetailSampahJual
import com.kkn.banksampah.ui.components.AppTopBar
import com.kkn.banksampah.ui.components.ResultDialog
import com.kkn.banksampah.ui.components.ResultType
import com.kkn.banksampah.util.CurrencyHelper
import com.kkn.banksampah.util.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenjualanScreen(
    navController: NavController,
    viewModel: PenjualanViewModel = viewModel()
) {
    val penjualanList by viewModel.penjualanList.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val totalPendapatan by viewModel.totalPendapatan.collectAsStateWithLifecycle()
    val totalBeratDijual by viewModel.totalBeratDijual.collectAsStateWithLifecycle()
    val stokGudang by viewModel.stokGudang.collectAsStateWithLifecycle()

    var showAddForm by remember { mutableStateOf(false) }
    var namaPengepul by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var totalBeratKg by remember { mutableStateOf("") }
    var totalHargaRp by remember { mutableStateOf("") }

    var showResultDialog by remember { mutableStateOf(false) }
    var resultType by remember { mutableStateOf(ResultType.SUCCESS) }
    var resultTitle by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")) }

    LaunchedEffect(saveState) {
        when (saveState) {
            is UiState.Success -> {
                resultType = ResultType.SUCCESS
                resultTitle = "Berhasil"
                resultMessage = "Penjualan ke pengepul berhasil dicatat."
                showResultDialog = true
                // Reset form
                showAddForm = false
                namaPengepul = ""
                catatan = ""
                totalBeratKg = ""
                totalHargaRp = ""
            }
            is UiState.Error -> {
                resultType = ResultType.ERROR
                resultTitle = "Gagal"
                resultMessage = (saveState as UiState.Error).message
                showResultDialog = true
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Jual ke Pengepul",
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if (!showAddForm) {
                ExtendedFloatingActionButton(
                    onClick = { showAddForm = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Catat Penjualan Baru", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp) // extra padding for FAB
        ) {
            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Pendapatan", style = MaterialTheme.typography.labelMedium)
                            Text(
                                CurrencyHelper.formatRupiah(totalPendapatan),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDBEAFE)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Dijual", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${totalBeratDijual} Kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }

            // Add Form
            if (showAddForm) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Catat Penjualan Baru",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Sisa Stok Gudang Saat Ini", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        Text("$stokGudang Kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = namaPengepul,
                                onValueChange = { namaPengepul = it },
                                label = { Text("Nama Pengepul / Pembeli") },
                                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = totalBeratKg,
                                    onValueChange = { totalBeratKg = it },
                                    label = { Text("Total (Kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = totalHargaRp,
                                    onValueChange = { totalHargaRp = it.filter { c -> c.isDigit() } },
                                    label = { Text("Total Harga") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    visualTransformation = com.kkn.banksampah.util.CurrencyVisualTransformation(),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = catatan,
                                onValueChange = { catatan = it },
                                label = { Text("Catatan (Opsional)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showAddForm = false },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Batal")
                                }
                                Button(
                                    onClick = {
                                        val berat = totalBeratKg.toDoubleOrNull() ?: 0.0
                                        val harga = totalHargaRp.toDoubleOrNull() ?: 0.0
                                        val detailItems = listOf(
                                            DetailSampahJual(
                                                namaJenisSampah = "Sampah Campuran",
                                                beratKg = berat,
                                                hargaJualPerKg = if (berat > 0) harga / berat else 0.0,
                                                subtotal = harga
                                            )
                                        )
                                        viewModel.simpanPenjualan(namaPengepul, detailItems, catatan)
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = saveState !is UiState.Loading
                                ) {
                                    if (saveState is UiState.Loading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Text("Simpan", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Riwayat Penjualan
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Riwayat Penjualan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (penjualanList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada riwayat penjualan ke pengepul.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(penjualanList, key = { it.id }) { penjualan ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(penjualan.namaPengepul, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        dateFormat.format(Date(penjualan.tanggal)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        CurrencyHelper.formatRupiah(penjualan.totalHargaJual),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF16A34A)
                                    )
                                    Text(
                                        "${penjualan.totalBeratKg} Kg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (penjualan.catatan.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Catatan: ${penjualan.catatan}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResultDialog) {
        ResultDialog(
            type = resultType,
            title = resultTitle,
            message = resultMessage,
            onConfirm = {
                showResultDialog = false
                viewModel.resetState()
            }
        )
    }
}
