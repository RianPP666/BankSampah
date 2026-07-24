package com.kkn.banksampah.ui.nasabah

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kkn.banksampah.ui.components.*
import com.kkn.banksampah.data.model.*
import com.kkn.banksampah.util.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NasabahScreen(
    navController: NavController,
    viewModel: NasabahViewModel = viewModel()
) {
    val nasabahList by viewModel.nasabahList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var nasabahToEdit by remember { mutableStateOf<Nasabah?>(null) }
    var nasabahToDelete by remember { mutableStateOf<Nasabah?>(null) }

    LaunchedEffect(operationState) {
        when (operationState) {
            is UiState.Error -> {
                snackbarHostState.showSnackbar((operationState as UiState.Error).message)
            }
            is UiState.Success -> {
                // handled transparently
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Data Nasabah",
                onBackClick = if (navController.previousBackStackEntry != null) { { navController.popBackStack() } } else null
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Nasabah")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            when (operationState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    if (nasabahList.isEmpty()) {
                        EmptyState(message = "Tidak ada data nasabah.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(nasabahList, key = { it.id }) { nasabah ->
                                NasabahCard(
                                    nasabah = nasabah,
                                    onEdit = { nasabahToEdit = nasabah },
                                    onDelete = { nasabahToDelete = nasabah }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || nasabahToEdit != null) {
        NasabahDialog(
            nasabah = nasabahToEdit,
            onDismiss = { 
                showAddDialog = false
                nasabahToEdit = null
            },
            onSave = { nama, alamat, hp ->
                val cleanNama = nama.trim()
                val cleanAlamat = alamat.trim()
                val cleanHp = hp.trim()
                if (nasabahToEdit != null) {
                    viewModel.updateNasabah(nasabahToEdit!!.copy(nama = cleanNama, alamat = cleanAlamat, noHp = cleanHp))
                } else {
                    viewModel.addNasabah(cleanNama, cleanAlamat, cleanHp)
                }
                showAddDialog = false
                nasabahToEdit = null
            }
        )
    }

    if (nasabahToDelete != null) {
        AlertDialog(
            onDismissRequest = { nasabahToDelete = null },
            title = { Text("Hapus Nasabah") },
            text = { Text("Apakah Anda yakin ingin menghapus ${nasabahToDelete?.nama}?") },
            confirmButton = {
                TextButton(onClick = {
                    nasabahToDelete?.id?.let { viewModel.deleteNasabah(it) }
                    nasabahToDelete = null
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { nasabahToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun NasabahDialog(
    nasabah: Nasabah?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var nama by remember { mutableStateOf(nasabah?.nama ?: "") }
    var alamat by remember { mutableStateOf(nasabah?.alamat ?: "") }
    var noHp by remember { mutableStateOf(nasabah?.noHp ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (nasabah == null) "Tambah Nasabah" else "Edit Nasabah") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = alamat,
                    onValueChange = { alamat = it },
                    label = { Text("Alamat") }
                )
                OutlinedTextField(
                    value = noHp,
                    onValueChange = { noHp = it },
                    label = { Text("No HP") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nama, alamat, noHp) },
                enabled = nama.isNotBlank() && alamat.isNotBlank() && noHp.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
