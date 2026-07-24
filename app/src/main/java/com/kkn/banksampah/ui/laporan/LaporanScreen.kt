package com.kkn.banksampah.ui.laporan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kkn.banksampah.ui.components.AppTopBar
import com.kkn.banksampah.ui.components.EmptyState
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
            AppTopBar(
                title = "Laporan Bulanan",
                onBackClick = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
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
            // Periode Picker
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pilih Periode Laporan",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ringkasan Performa",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            SummaryCard(
                title = "Total Setoran",
                value = CurrencyHelper.formatRupiah(laporan.totalSetor),
                icon = Icons.Default.ArrowUpward,
                iconColor = Color(0xFF16A34A),
                containerColor = Color(0xFFDCFCE7)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SummaryCard(
                title = "Total Penarikan",
                value = CurrencyHelper.formatRupiah(laporan.totalTarik),
                icon = Icons.Default.ArrowDownward,
                iconColor = Color(0xFFDC2626),
                containerColor = Color(0xFFFEE2E2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Berat",
                    value = "${laporan.totalKg} Kg",
                    icon = Icons.Default.Delete,
                    iconColor = Color(0xFF2563EB),
                    containerColor = Color(0xFFDBEAFE)
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Transaksi",
                    value = "${laporan.jumlahTransaksi}",
                    icon = Icons.Default.ReceiptLong,
                    iconColor = Color(0xFFD97706),
                    containerColor = Color(0xFFFEF3C7)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "🏆 Top 5 Nasabah Teraktif",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (laporan.topNasabah.isEmpty()) {
                EmptyState(message = "Belum ada data nasabah pada periode ini.")
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        laporan.topNasabah.forEachIndexed { index, nasabahStat ->
                            val rankBadge = when (index) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "#${index + 1}"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = rankBadge,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = nasabahStat.nama,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                Text(
                                    text = CurrencyHelper.formatRupiah(nasabahStat.totalSetor),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (index < laporan.topNasabah.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

