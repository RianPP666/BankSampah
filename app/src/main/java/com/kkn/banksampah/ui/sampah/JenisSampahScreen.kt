package com.kkn.banksampah.ui.sampah

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kkn.banksampah.ui.components.*
import com.kkn.banksampah.data.model.*
import com.kkn.banksampah.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JenisSampahScreen(
    navController: NavController,
    viewModel: SampahViewModel = viewModel()
) {
    val jenisSampahList by viewModel.jenisSampahList.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<JenisSampah?>(null) }
    var itemToDelete by remember { mutableStateOf<JenisSampah?>(null) }

    // Result dialog states
    var showResultDialog by remember { mutableStateOf(false) }
    var resultType by remember { mutableStateOf(ResultType.SUCCESS) }
    var resultTitle by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    LaunchedEffect(operationState) {
        if (operationState is UiState.Error) {
            resultType = ResultType.ERROR
            resultTitle = "Gagal"
            resultMessage = (operationState as UiState.Error).message
            showResultDialog = true
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Jenis Sampah", onBackClick = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { padding ->
        if (operationState is UiState.Loading && jenisSampahList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (jenisSampahList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                EmptyState(message = "Belum ada data jenis sampah.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(jenisSampahList, key = { it.id }) { sampah ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sampah.nama,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = sampah.kategori,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${CurrencyHelper.formatRupiah(sampah.hargaPerSatuan)} / Kg",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { itemToEdit = sampah }) {
                                    Text("Edit")
                                }
                                TextButton(
                                    onClick = { itemToDelete = sampah },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Hapus")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || itemToEdit != null) {
        JenisSampahDialog(
            jenisSampah = itemToEdit,
            onDismiss = {
                showAddDialog = false
                itemToEdit = null
            },
            onSave = { nama, kategori, harga, deskripsi ->
                if (itemToEdit != null) {
                    viewModel.updateJenisSampah(itemToEdit!!.copy(
                        nama = nama, kategori = kategori, hargaPerSatuan = harga, deskripsi = deskripsi
                    ))
                } else {
                    viewModel.addJenisSampah(nama, kategori, harga, deskripsi)
                }
                showAddDialog = false
                itemToEdit = null
            }
        )
    }

    // Delete Confirmation via ResultDialog
    if (itemToDelete != null) {
        ResultDialog(
            type = ResultType.CONFIRMATION,
            title = "Hapus Jenis Sampah",
            message = "Apakah Anda yakin ingin menghapus \"${itemToDelete?.nama}\"? Tindakan ini tidak dapat dibatalkan.",
            confirmText = "Ya, Hapus",
            dismissText = "Batal",
            onConfirm = {
                itemToDelete?.id?.let { viewModel.deleteJenisSampah(it) }
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }

    // Result Dialog (Error feedback)
    if (showResultDialog) {
        ResultDialog(
            type = resultType,
            title = resultTitle,
            message = resultMessage,
            onConfirm = { showResultDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JenisSampahDialog(
    jenisSampah: JenisSampah?,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String) -> Unit
) {
    var nama by remember { mutableStateOf(jenisSampah?.nama ?: "") }
    var kategori by remember { mutableStateOf(jenisSampah?.kategori ?: "Organik") }
    var harga by remember { mutableStateOf(jenisSampah?.hargaPerSatuan?.toString() ?: "") }
    var deskripsi by remember { mutableStateOf(jenisSampah?.deskripsi ?: "") }

    var namaError by remember { mutableStateOf<String?>(null) }
    var hargaError by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Organik", "Plastik", "Kertas", "Logam", "Kaca", "Elektronik")
    var expanded by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        var valid = true
        namaError = if (nama.trim().isBlank()) { valid = false; "Nama sampah tidak boleh kosong" } else null
        val hargaDouble = harga.toDoubleOrNull()
        hargaError = when {
            harga.isBlank() -> { valid = false; "Harga tidak boleh kosong" }
            hargaDouble == null || hargaDouble <= 0 -> { valid = false; "Harga harus lebih dari 0" }
            else -> null
        }
        return valid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(if (jenisSampah == null) "Tambah Jenis Sampah" else "Edit Jenis Sampah", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it; namaError = null },
                    label = { Text("Nama Sampah") },
                    singleLine = true,
                    isError = namaError != null,
                    supportingText = namaError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = kategori,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    kategori = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = harga,
                    onValueChange = { harga = it; hargaError = null },
                    label = { Text("Harga per Kg (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = hargaError != null,
                    supportingText = hargaError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi (opsional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        val hargaDouble = harga.toDoubleOrNull() ?: 0.0
                        onSave(nama.trim(), kategori, hargaDouble, deskripsi.trim())
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
