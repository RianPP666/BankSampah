package com.kkn.banksampah.ui.sampah

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<JenisSampah?>(null) }
    var itemToDelete by remember { mutableStateOf<JenisSampah?>(null) }

    LaunchedEffect(operationState) {
        if (operationState is UiState.Error) {
            snackbarHostState.showSnackbar((operationState as UiState.Error).message)
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Jenis Sampah", onBackClick = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jenisSampahList, key = { it.id }) { sampah ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = sampah.kategori,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${CurrencyHelper.formatRupiah(sampah.hargaPerSatuan)} / Kg",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Jenis Sampah") },
            text = { Text("Hapus ${itemToDelete?.nama}?") },
            confirmButton = {
                TextButton(onClick = {
                    itemToDelete?.id?.let { viewModel.deleteJenisSampah(it) }
                    itemToDelete = null
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Batal") }
            }
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
    
    val categories = listOf("Organik", "Plastik", "Kertas", "Logam", "Kaca", "Elektronik")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (jenisSampah == null) "Tambah Jenis Sampah" else "Edit Jenis Sampah") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Sampah") },
                    singleLine = true
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
                        modifier = Modifier.menuAnchor()
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
                    onValueChange = { harga = it },
                    label = { Text("Harga per Kg") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val hargaDouble = harga.toDoubleOrNull() ?: 0.0
                    onSave(nama, kategori, hargaDouble, deskripsi) 
                },
                enabled = nama.isNotBlank() && harga.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
