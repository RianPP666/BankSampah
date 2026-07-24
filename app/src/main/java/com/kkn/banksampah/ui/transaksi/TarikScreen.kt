package com.kkn.banksampah.ui.transaksi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kkn.banksampah.data.model.Nasabah
import com.kkn.banksampah.ui.components.AppTopBar
import com.kkn.banksampah.util.CurrencyHelper
import com.kkn.banksampah.util.UiState
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarikScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val nasabahList by viewModel.nasabahList.collectAsStateWithLifecycle()
    val tarikState by viewModel.tarikState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedNasabah by remember { mutableStateOf<Nasabah?>(null) }
    var nasabahExpanded by remember { mutableStateOf(false) }

    var jumlahTarikText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tarikState) {
        when (tarikState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Penarikan berhasil disimpan")
                viewModel.resetState()
                navController.popBackStack()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((tarikState as UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Tarik Saldo",
                onBackClick = { navController.popBackStack() }
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
                            text = { Text("${nasabah.nama} (Saldo: ${CurrencyHelper.formatRupiah(nasabah.saldo)})") },
                            onClick = {
                                selectedNasabah = nasabah
                                nasabahExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedNasabah != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Saldo Saat Ini", style = MaterialTheme.typography.labelMedium)
                        Text(CurrencyHelper.formatRupiah(selectedNasabah!!.saldo), style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = jumlahTarikText,
                onValueChange = { jumlahTarikText = it },
                label = { Text("Jumlah Penarikan (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val amount = jumlahTarikText.toDoubleOrNull() ?: 0.0
                    if (selectedNasabah == null) {
                        scope.launch { snackbarHostState.showSnackbar("Pilih nasabah terlebih dahulu") }
                        return@Button
                    }
                    if (amount <= 0) {
                        scope.launch { snackbarHostState.showSnackbar("Jumlah penarikan harus lebih dari 0") }
                        return@Button
                    }
                    if (amount > (selectedNasabah?.saldo ?: 0.0)) {
                        scope.launch { snackbarHostState.showSnackbar("Saldo tidak mencukupi") }
                        return@Button
                    }
                    showConfirmDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = tarikState !is UiState.Loading
            ) {
                if (tarikState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Konfirmasi Penarikan")
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Penarikan") },
            text = { Text("Anda yakin ingin menarik saldo sebesar ${CurrencyHelper.formatRupiah(jumlahTarikText.toDoubleOrNull() ?: 0.0)} untuk ${selectedNasabah?.nama}?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    val amount = jumlahTarikText.toDoubleOrNull() ?: 0.0
                    viewModel.tarik(
                        idNasabah = selectedNasabah?.id ?: "",
                        namaNasabah = selectedNasabah?.nama ?: "",
                        jumlah = amount
                    )
                }) {
                    Text("Ya, Tarik")
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
