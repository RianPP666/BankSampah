package com.kkn.banksampah.ui.transaksi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kkn.banksampah.data.model.DetailSampah
import com.kkn.banksampah.data.model.Nasabah
import com.kkn.banksampah.ui.components.AppTopBar
import com.kkn.banksampah.ui.components.NasabahSearchDialog
import com.kkn.banksampah.ui.components.ResultDialog
import com.kkn.banksampah.ui.components.ResultType
import com.kkn.banksampah.util.CurrencyHelper
import com.kkn.banksampah.util.UiState
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetorScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val nasabahList by viewModel.nasabahList.collectAsStateWithLifecycle()
    val jenisSampahList by viewModel.jenisSampahList.collectAsStateWithLifecycle()
    val setorState by viewModel.setorState.collectAsStateWithLifecycle()

    var selectedNasabah by remember { mutableStateOf<Nasabah?>(null) }
    var showNasabahPicker by remember { mutableStateOf(false) }

    var items by remember { mutableStateOf(listOf<DetailSampahInput>()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Result dialog states
    var showResultDialog by remember { mutableStateOf(false) }
    var resultType by remember { mutableStateOf(ResultType.SUCCESS) }
    var resultTitle by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    // Validation error
    var validationError by remember { mutableStateOf<String?>(null) }

    val totalAmount = items.sumOf { it.subtotal }

    LaunchedEffect(setorState) {
        when (setorState) {
            is UiState.Success -> {
                resultType = ResultType.SUCCESS
                resultTitle = "Berhasil"
                resultMessage = "Setoran sebesar ${CurrencyHelper.formatRupiah(totalAmount)} untuk ${selectedNasabah?.nama ?: ""} berhasil disimpan."
                showResultDialog = true
            }
            is UiState.Error -> {
                resultType = ResultType.ERROR
                resultTitle = "Gagal"
                resultMessage = (setorState as UiState.Error).message
                showResultDialog = true
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Setor Sampah",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Step 1: Pilih Nasabah ──
            item {
                StepHeader(step = 1, title = "Pilih Nasabah")
            }
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (selectedNasabah != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    selectedNasabah!!.nama,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Saldo: ${CurrencyHelper.formatRupiah(selectedNasabah!!.saldo)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { showNasabahPicker = true }) {
                                Text("Ganti")
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showNasabahPicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cari & Pilih Nasabah")
                        }
                    }
                }
            }

            // ── Step 2: Input Item Sampah ──
            item {
                Spacer(modifier = Modifier.height(4.dp))
                StepHeader(step = 2, title = "Input Item Sampah")
            }

            itemsIndexed(items) { index, item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        var expanded by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Item #${index + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    val newItems = items.toMutableList()
                                    newItems.removeAt(index)
                                    items = newItems
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = item.jenisSampah?.nama ?: "Pilih Jenis Sampah",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jenis Sampah") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                jenisSampahList.forEach { js ->
                                    DropdownMenuItem(
                                        text = { Text("${js.nama} (${CurrencyHelper.formatRupiah(js.hargaPerSatuan)}/kg)") },
                                        onClick = {
                                            val newItems = items.toMutableList()
                                            newItems[index] = item.copy(
                                                jenisSampah = js,
                                                subtotal = item.berat * js.hargaPerSatuan
                                            )
                                            items = newItems
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = item.beratText,
                                onValueChange = { text ->
                                    val berat = text.toDoubleOrNull() ?: 0.0
                                    val newItems = items.toMutableList()
                                    val js = item.jenisSampah
                                    newItems[index] = item.copy(
                                        beratText = text,
                                        berat = berat,
                                        subtotal = if (js != null) berat * js.hargaPerSatuan else 0.0
                                    )
                                    items = newItems
                                },
                                label = { Text("Berat (Kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    text = CurrencyHelper.formatRupiah(item.subtotal),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { items = items + DetailSampahInput() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Tambah Item Sampah")
                }
            }

            // ── Step 3: Ringkasan ──
            item {
                Spacer(modifier = Modifier.height(4.dp))
                StepHeader(step = 3, title = "Ringkasan Transaksi")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Setoran", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            CurrencyHelper.formatRupiah(totalAmount),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (selectedNasabah != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Nasabah: ${selectedNasabah!!.nama}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Validation error message
            if (validationError != null) {
                item {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        validationError = null
                        if (selectedNasabah == null) {
                            validationError = "Pilih nasabah terlebih dahulu"
                            return@Button
                        }
                        if (items.isEmpty()) {
                            validationError = "Tambahkan minimal satu item sampah"
                            return@Button
                        }
                        if (items.any { it.jenisSampah == null }) {
                            validationError = "Pilih jenis sampah untuk setiap item"
                            return@Button
                        }
                        if (items.any { it.berat <= 0 }) {
                            validationError = "Berat harus lebih dari 0 untuk setiap item"
                            return@Button
                        }
                        showConfirmDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = setorState !is UiState.Loading
                ) {
                    if (setorState is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Konfirmasi Setoran", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Nasabah Search Dialog
    if (showNasabahPicker) {
        NasabahSearchDialog(
            nasabahList = nasabahList,
            onSelect = { nasabah ->
                selectedNasabah = nasabah
                showNasabahPicker = false
            },
            onDismiss = { showNasabahPicker = false }
        )
    }

    // Confirm Dialog
    if (showConfirmDialog) {
        ResultDialog(
            type = ResultType.CONFIRMATION,
            title = "Konfirmasi Setoran",
            message = "Simpan setoran ${CurrencyHelper.formatRupiah(totalAmount)} untuk nasabah ${selectedNasabah?.nama}?\n\n${items.size} jenis sampah akan dicatat.",
            confirmText = "Ya, Simpan",
            dismissText = "Batal",
            onConfirm = {
                showConfirmDialog = false
                val mappedItems = items.map {
                    DetailSampah(
                        idJenisSampah = it.jenisSampah?.id ?: "",
                        namaSampah = it.jenisSampah?.nama ?: "",
                        beratKg = it.berat,
                        hargaPerKg = it.jenisSampah?.hargaPerSatuan ?: 0.0,
                        subtotal = it.subtotal
                    )
                }
                viewModel.setor(
                    idNasabah = selectedNasabah?.id ?: "",
                    namaNasabah = selectedNasabah?.nama ?: "",
                    items = mappedItems,
                    totalRupiah = totalAmount
                )
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    // Result Dialog (Success / Error)
    if (showResultDialog) {
        ResultDialog(
            type = resultType,
            title = resultTitle,
            message = resultMessage,
            onConfirm = {
                showResultDialog = false
                if (resultType == ResultType.SUCCESS) {
                    viewModel.resetState()
                    navController.popBackStack()
                }
            }
        )
    }
}

// ─── Step Header ────────────────────────────────────────────────────────────

@Composable
private fun StepHeader(step: Int, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    "$step",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

data class DetailSampahInput(
    val jenisSampah: com.kkn.banksampah.data.model.JenisSampah? = null,
    val beratText: String = "",
    val berat: Double = 0.0,
    val subtotal: Double = 0.0
)
