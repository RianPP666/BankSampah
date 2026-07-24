package com.kkn.banksampah.ui.riwayat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkn.banksampah.data.model.Transaksi
import com.kkn.banksampah.util.CurrencyHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(
    navController: NavController,
    viewModel: RiwayatViewModel = viewModel()
) {
    val transaksiList by viewModel.transaksi.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    var selectedTransaksi by remember { mutableStateOf<Transaksi?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("SEMUA", "SETOR", "TARIK")
                items(filters) { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f) }
                    )
                }
            }

            if (transaksiList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transaksiList) { t ->
                        TransactionCard(t) { selectedTransaksi = t }
                    }
                }
            }
        }
    }

    if (selectedTransaksi != null) {
        ModalBottomSheet(onDismissRequest = { selectedTransaksi = null }) {
            TransactionDetail(selectedTransaksi!!)
        }
    }
}

@Composable
fun TransactionCard(transaksi: Transaksi, onClick: () -> Unit) {
    val isSetor = transaksi.jenisTransaksi == "SETOR"
    val color = if (isSetor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(transaksi.namaNasabah, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(transaksi.tanggal)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(transaksi.jenisTransaksi, style = MaterialTheme.typography.labelSmall, color = color)
            }
            Text(
                text = (if (isSetor) "+" else "-") + CurrencyHelper.formatRupiah(transaksi.totalRupiah),
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Composable
fun TransactionDetail(transaksi: Transaksi) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Detail Transaksi", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nasabah: ${transaksi.namaNasabah}", style = MaterialTheme.typography.bodyMedium)
        Text("Petugas: ${transaksi.petugasNama}", style = MaterialTheme.typography.bodyMedium)
        Text("Tanggal: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(transaksi.tanggal))}", style = MaterialTheme.typography.bodyMedium)
        Text("Jenis: ${transaksi.jenisTransaksi}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (transaksi.jenisTransaksi == "SETOR" && transaksi.detailSampah.isNotEmpty()) {
            Text("Item Sampah:", style = MaterialTheme.typography.titleMedium)
            transaksi.detailSampah.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.namaSampah} (${item.beratKg} kg)")
                    Text(CurrencyHelper.formatRupiah(item.subtotal))
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Berat", style = MaterialTheme.typography.titleSmall)
                Text("${transaksi.detailSampah.sumOf { it.beratKg }} kg", style = MaterialTheme.typography.titleSmall)
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = MaterialTheme.typography.titleMedium)
            Text(CurrencyHelper.formatRupiah(transaksi.totalRupiah), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
