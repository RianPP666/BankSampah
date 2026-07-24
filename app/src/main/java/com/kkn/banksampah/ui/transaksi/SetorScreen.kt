package com.kkn.banksampah.ui.transaksi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kkn.banksampah.data.model.DetailSampah
import com.kkn.banksampah.data.model.Nasabah
import com.kkn.banksampah.util.CurrencyHelper
import com.kkn.banksampah.util.UiState
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetorScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val nasabahList by viewModel.nasabahList.collectAsStateWithLifecycle()
    val jenisSampahList by viewModel.jenisSampahList.collectAsStateWithLifecycle()
    val setorState by viewModel.setorState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedNasabah by remember { mutableStateOf<Nasabah?>(null) }
    var nasabahExpanded by remember { mutableStateOf(false) }

    var items by remember { mutableStateOf(listOf<DetailSampahInput>()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val totalAmount = items.sumOf { it.subtotal }

    LaunchedEffect(setorState) {
        when (setorState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Setoran berhasil disimpan")
                viewModel.resetState()
                navController.popBackStack()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((setorState as UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setor Sampah") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = nasabahExpanded,
                onExpandedChange = { nasabahExpanded = !nasabahExpanded }
            ) {
                OutlinedTextField(
                    value = selectedNasabah?.nama ?: "Pilih Nasabah",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nasabah") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nasabahExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = nasabahExpanded,
                    onDismissRequest = { nasabahExpanded = false }
                ) {
                    nasabahList.forEach { nasabah ->
                        DropdownMenuItem(
                            text = { Text(nasabah.nama) },
                            onClick = {
                                selectedNasabah = nasabah
                                nasabahExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Item Sampah", style = MaterialTheme.typography.titleMedium)
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(items) { index, item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            var expanded by remember { mutableStateOf(false) }
                            
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
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
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
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    val newItems = items.toMutableList()
                                    newItems.removeAt(index)
                                    items = newItems
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Subtotal: ${CurrencyHelper.formatRupiah(item.subtotal)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                
                item {
                    Button(
                        onClick = { items = items + DetailSampahInput() },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("+ Tambah Item Sampah")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                PaddingValues(16.dp).let {
                    Column(modifier = Modifier.padding(it)) {
                        Text("Total Setoran", style = MaterialTheme.typography.titleMedium)
                        Text(CurrencyHelper.formatRupiah(totalAmount), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedNasabah == null) {
                        scope.launch { snackbarHostState.showSnackbar("Pilih nasabah terlebih dahulu") }
                        return@Button
                    }
                    if (items.isEmpty() || items.any { it.jenisSampah == null || it.berat <= 0 }) {
                        scope.launch { snackbarHostState.showSnackbar("Mohon lengkapi item sampah") }
                        return@Button
                    }
                    showConfirmDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = setorState !is UiState.Loading
            ) {
                if (setorState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Konfirmasi Setoran")
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Setoran") },
            text = { Text("Anda yakin ingin menyimpan setoran ini untuk ${selectedNasabah?.nama}?") },
            confirmButton = {
                TextButton(onClick = {
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
                }) {
                    Text("Ya, Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

data class DetailSampahInput(
    val jenisSampah: com.kkn.banksampah.data.model.JenisSampah? = null,
    val beratText: String = "",
    val berat: Double = 0.0,
    val subtotal: Double = 0.0
)
