package com.kkn.banksampah.ui.riwayat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkn.banksampah.data.model.Transaksi
import com.kkn.banksampah.ui.components.AppTopBar
import com.kkn.banksampah.ui.components.EmptyState
import com.kkn.banksampah.ui.components.TransactionCard
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
            AppTopBar(
                title = "Riwayat Transaksi"
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("SEMUA", "SETOR", "TARIK")
                items(filters) { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f, fontWeight = if (filter == f) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (transaksiList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(message = "Belum ada riwayat transaksi.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transaksiList, key = { it.id }) { t ->
                        TransactionCard(transaksi = t, onClick = { selectedTransaksi = t })
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
fun TransactionDetail(transaksi: Transaksi) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Detail Transaksi",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(label = "Nasabah", value = transaksi.namaNasabah)
                DetailRow(label = "Petugas", value = transaksi.petugasNama.ifBlank { "Petugas" })
                DetailRow(
                    label = "Waktu",
                    value = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(transaksi.tanggal))
                )
                DetailRow(label = "Jenis Transaksi", value = transaksi.jenisTransaksi)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        if (transaksi.jenisTransaksi == "SETOR" && transaksi.detailSampah.isNotEmpty()) {
            Text(
                text = "Rincian Sampah:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            transaksi.detailSampah.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.namaSampah} (${item.beratKg} kg)", style = MaterialTheme.typography.bodyMedium)
                    Text(CurrencyHelper.formatRupiah(item.subtotal), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Berat", style = MaterialTheme.typography.titleSmall)
                Text("${transaksi.detailSampah.sumOf { it.beratKg }} kg", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Nominal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = CurrencyHelper.formatRupiah(transaksi.totalRupiah),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (transaksi.jenisTransaksi == "SETOR") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
    }
}

