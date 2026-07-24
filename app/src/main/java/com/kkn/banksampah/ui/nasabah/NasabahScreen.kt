package com.kkn.banksampah.ui.nasabah

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.kkn.banksampah.util.CurrencyHelper
import com.kkn.banksampah.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NasabahScreen(
    navController: NavController,
    viewModel: NasabahViewModel = viewModel()
) {
    val nasabahList by viewModel.nasabahList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var nasabahToEdit by remember { mutableStateOf<Nasabah?>(null) }
    var nasabahToDelete by remember { mutableStateOf<Nasabah?>(null) }
    var nasabahToDetail by remember { mutableStateOf<Nasabah?>(null) }

    // Result dialog states
    var showResultDialog by remember { mutableStateOf(false) }
    var resultType by remember { mutableStateOf(ResultType.SUCCESS) }
    var resultTitle by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    LaunchedEffect(operationState) {
        when (operationState) {
            is UiState.Error -> {
                resultType = ResultType.ERROR
                resultTitle = "Gagal"
                resultMessage = (operationState as UiState.Error).message
                showResultDialog = true
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
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Nasabah")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Field
            SearchField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Chips
            val categories = listOf("Semua", "Ada Saldo", "Saldo Kosong")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = filterCategory == category,
                        onClick = { viewModel.setFilterCategory(category) },
                        label = { Text(category, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(nasabahList, key = { it.id }) { nasabah ->
                                NasabahCard(
                                    nasabah = nasabah,
                                    onEdit = { nasabahToEdit = nasabah },
                                    onDelete = { nasabahToDelete = nasabah },
                                    onDetail = { nasabahToDetail = nasabah }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog || nasabahToEdit != null) {
        NasabahDialog(
            nasabah = nasabahToEdit,
            onDismiss = {
                showAddDialog = false
                nasabahToEdit = null
            },
            onSave = { nama, alamat, hp ->
                if (nasabahToEdit != null) {
                    viewModel.updateNasabah(nasabahToEdit!!.copy(nama = nama, alamat = alamat, noHp = hp))
                } else {
                    viewModel.addNasabah(nama, alamat, hp)
                }
                showAddDialog = false
                nasabahToEdit = null
            }
        )
    }

    // Delete Confirmation via ResultDialog
    if (nasabahToDelete != null) {
        ResultDialog(
            type = ResultType.CONFIRMATION,
            title = "Hapus Nasabah",
            message = "Apakah Anda yakin ingin menghapus nasabah \"${nasabahToDelete?.nama}\"? Tindakan ini tidak dapat dibatalkan.",
            confirmText = "Ya, Hapus",
            dismissText = "Batal",
            onConfirm = {
                nasabahToDelete?.id?.let { viewModel.deleteNasabah(it) }
                nasabahToDelete = null
            },
            onDismiss = { nasabahToDelete = null }
        )
    }

    // Detail Dialog
    if (nasabahToDetail != null) {
        NasabahDetailDialog(
            nasabah = nasabahToDetail!!,
            onDismiss = { nasabahToDetail = null }
        )
    }

    // Result Dialog (Error / Success feedback)
    if (showResultDialog) {
        ResultDialog(
            type = resultType,
            title = resultTitle,
            message = resultMessage,
            onConfirm = {
                showResultDialog = false
                viewModel.resetState()
            }
        )
    }
}

// ─── Nasabah Detail Dialog ───────────────────────────────────────────────────

@Composable
fun NasabahDetailDialog(
    nasabah: Nasabah,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Detail Nasabah", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(icon = Icons.Default.Person, label = "Nama", value = nasabah.nama)
                DetailRow(icon = Icons.Default.Phone, label = "No. HP", value = nasabah.noHp.ifBlank { "-" })
                DetailRow(icon = Icons.Default.Home, label = "Alamat", value = nasabah.alamat.ifBlank { "-" })
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saldo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = CurrencyHelper.formatRupiah(nasabah.saldo),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// ─── Nasabah Add/Edit Dialog with Inline Validation ──────────────────────────

@Composable
fun NasabahDialog(
    nasabah: Nasabah?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var nama by remember { mutableStateOf(nasabah?.nama ?: "") }
    var alamat by remember { mutableStateOf(nasabah?.alamat ?: "") }
    var noHp by remember { mutableStateOf(nasabah?.noHp ?: "") }

    var namaError by remember { mutableStateOf<String?>(null) }
    var alamatError by remember { mutableStateOf<String?>(null) }
    var noHpError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var valid = true
        namaError = if (nama.trim().isBlank()) { valid = false; "Nama tidak boleh kosong" } else null
        alamatError = if (alamat.trim().isBlank()) { valid = false; "Alamat tidak boleh kosong" } else null
        noHpError = when {
            noHp.trim().isBlank() -> { valid = false; "No. HP tidak boleh kosong" }
            !noHp.trim().matches(Regex("^[0-9]{10,14}$")) -> { valid = false; "No. HP harus 10-14 digit angka" }
            else -> null
        }
        return valid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(if (nasabah == null) "Tambah Nasabah" else "Edit Nasabah", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it; namaError = null },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    isError = namaError != null,
                    supportingText = namaError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = alamat,
                    onValueChange = { alamat = it; alamatError = null },
                    label = { Text("Alamat") },
                    isError = alamatError != null,
                    supportingText = alamatError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = noHp,
                    onValueChange = { noHp = it.filter { c -> c.isDigit() }; noHpError = null },
                    label = { Text("No. HP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = noHpError != null,
                    supportingText = noHpError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        onSave(nama.trim(), alamat.trim(), noHp.trim())
                    }
                }
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
