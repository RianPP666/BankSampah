package com.kkn.banksampah.ui.sampah

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kkn.banksampah.ui.components.AppTopBar
import com.kkn.banksampah.ui.components.EmptyState
import com.kkn.banksampah.util.CurrencyHelper

@Composable
fun HargaSampahScreen(
    navController: NavController,
    viewModel: SampahViewModel = viewModel()
) {
    val jenisSampahList by viewModel.jenisSampahList.collectAsState()
    
    val groupedSampah = jenisSampahList.groupBy { it.kategori }

    Scaffold(
        topBar = { AppTopBar(title = "Daftar Harga Sampah", onBackClick = { navController.popBackStack() }) }
    ) { padding ->
        if (jenisSampahList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                EmptyState(message = "Belum ada daftar harga sampah.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedSampah.forEach { (kategori, items) ->
                    item {
                        Text(
                            text = kategori,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(items) { sampah ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sampah.nama,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                if (sampah.deskripsi.isNotBlank()) {
                                    Text(
                                        text = sampah.deskripsi,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${CurrencyHelper.formatRupiah(sampah.hargaPerSatuan)}/Kg",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Divider()
                    }
                }
            }
        }
    }
}
