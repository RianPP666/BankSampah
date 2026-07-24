package com.kkn.banksampah.ui.laporan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kkn.banksampah.data.repository.NasabahStat
import com.kkn.banksampah.ui.components.AppTopBar
import com.kkn.banksampah.ui.components.EmptyState
import com.kkn.banksampah.util.CurrencyHelper
import com.kkn.banksampah.util.PdfHelper
import kotlinx.coroutines.launch
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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                coroutineScope.launch {
                    PdfHelper.generatePdf(
                        context = context,
                        uri = uri,
                        laporan = laporan,
                        bulan = bulanList[selectedBulan],
                        tahun = selectedTahun.toString()
                    )
                }
            }
        }
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Laporan Bulanan",
                onBackClick = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                },
                actions = {
                    IconButton(onClick = {
                        createPdfLauncher.launch("Laporan_${bulanList[selectedBulan]}_${selectedTahun}.pdf")
                    }) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "Ekspor PDF")
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
                text = "📊 Grafik Saldo (Setoran vs Penarikan)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LaporanBarChart(laporan.totalSetor, laporan.totalTarik)

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "📋 Tabel Detail Penyetor Sampah Bulan Ini",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (laporan.daftarPenyetor.isEmpty()) {
                EmptyState(message = "Belum ada data penyetor pada periode ini.")
            } else {
                TabelDaftarPenyetor(laporan.daftarPenyetor)
            }
        }
    }
}

@Composable
fun TabelDaftarPenyetor(daftar: List<NasabahStat>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(28.dp)
                )
                Text(
                    text = "Nama Penyetor",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "Berat",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(0.7f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Total Setor",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Table Rows
            daftar.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = item.nama,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = "${item.totalKg} Kg",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = CurrencyHelper.formatRupiah(item.totalSetor),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1.1f),
                        textAlign = TextAlign.End
                    )
                }
                if (index < daftar.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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

@Composable
fun LaporanBarChart(totalSetor: Double, totalTarik: Double) {
    val maxVal = maxOf(totalSetor, totalTarik).coerceAtLeast(1.0)
    val heightSetor = (totalSetor / maxVal).toFloat()
    val heightTarik = (totalTarik / maxVal).toFloat()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (totalSetor == 0.0 && totalTarik == 0.0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada data untuk digambar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Setoran Bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Text(
                        text = CurrencyHelper.formatRupiah(totalSetor),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight(heightSetor)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(Color(0xFF16A34A))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Setoran", style = MaterialTheme.typography.labelMedium)
                }

                // Penarikan Bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Text(
                        text = CurrencyHelper.formatRupiah(totalTarik),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight(heightTarik)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(Color(0xFFDC2626))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Penarikan", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
