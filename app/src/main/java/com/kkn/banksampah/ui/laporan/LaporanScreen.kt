package com.kkn.banksampah.ui.laporan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kkn.banksampah.util.CurrencyHelper
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(
    navController: NavController,
    viewModel: LaporanViewModel = viewModel()
) {
    val laporan by viewModel.laporanData.collectAsStateWithLifecycle()
    
    var expandedBulan by remember { mutableStateOf(false) }
    var expandedTahun by remember { mutableStateOf(false) }

    val bulanList = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val tahunList = (2020..2030).toList()

    var selectedBulan by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedTahun by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedBulan,
                    onExpandedChange = { expandedBulan = !expandedBulan },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = bulanList[selectedBulan],
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bulan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBulan) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBulan,
                        onDismissRequest = { expandedBulan = false }
                    ) {
                        bulanList.forEachIndexed { index, nama ->
                            DropdownMenuItem(
                                text = { Text(nama) },
                                onClick = {
                                    selectedBulan = index
                                    expandedBulan = false
                                    viewModel.loadLaporan(selectedBulan, selectedTahun)
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedTahun,
                    onExpandedChange = { expandedTahun = !expandedTahun },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedTahun.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tahun") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTahun) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTahun,
                        onDismissRequest = { expandedTahun = false }
                    ) {
                        tahunList.forEach { tahun ->
                            DropdownMenuItem(
                                text = { Text(tahun.toString()) },
                                onClick = {
                                    selectedTahun = tahun
                                    expandedTahun = false
                                    viewModel.loadLaporan(selectedBulan, selectedTahun)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SummaryCard("Total Setoran", CurrencyHelper.formatRupiah(laporan.totalSetor), MaterialTheme.colorScheme.primaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryCard("Total Penarikan", CurrencyHelper.formatRupiah(laporan.totalTarik), MaterialTheme.colorScheme.errorContainer)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryCard("Total Berat Sampah", "${laporan.totalKg} Kg", MaterialTheme.colorScheme.secondaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryCard("Jumlah Transaksi", "${laporan.jumlahTransaksi} Transaksi", MaterialTheme.colorScheme.tertiaryContainer)

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Top 5 Nasabah (Setoran)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (laporan.topNasabah.isEmpty()) {
                Text("Tidak ada data.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        laporan.topNasabah.forEachIndexed { index, nasabahStat ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${index + 1}. ${nasabahStat.nama}")
                                Text(CurrencyHelper.formatRupiah(nasabahStat.totalSetor))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, containerColor: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
