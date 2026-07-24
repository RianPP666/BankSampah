package com.kkn.banksampah.ui.transaksi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
fun TarikScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = viewModel()
) {
    val nasabahList by viewModel.nasabahList.collectAsStateWithLifecycle()
    val tarikState by viewModel.tarikState.collectAsStateWithLifecycle()

    var selectedNasabah by remember { mutableStateOf<Nasabah?>(null) }
    var showNasabahPicker by remember { mutableStateOf(false) }

    var jumlahTarikText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Result dialog states
    var showResultDialog by remember { mutableStateOf(false) }
    var resultType by remember { mutableStateOf(ResultType.SUCCESS) }
    var resultTitle by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    // Validation error
    var validationError by remember { mutableStateOf<String?>(null) }

    val amount = jumlahTarikText.toDoubleOrNull() ?: 0.0
    val quickAmounts = listOf(10_000.0, 25_000.0, 50_000.0, 100_000.0)

    LaunchedEffect(tarikState) {
        when (tarikState) {
            is UiState.Success -> {
                resultType = ResultType.SUCCESS
                resultTitle = "Berhasil"
                resultMessage = "Penarikan sebesar ${CurrencyHelper.formatRupiah(amount)} untuk ${selectedNasabah?.nama ?: ""} berhasil disimpan."
                showResultDialog = true
            }
            is UiState.Error -> {
                resultType = ResultType.ERROR
                resultTitle = "Gagal"
                resultMessage = (tarikState as UiState.Error).message
                showResultDialog = true
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Step 1: Pilih Nasabah ──
            StepHeaderTarik(step = 1, title = "Pilih Nasabah")

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

            // Saldo card if nasabah is selected
            if (selectedNasabah != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saldo Tersedia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(
                            CurrencyHelper.formatRupiah(selectedNasabah!!.saldo),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // ── Step 2: Input Nominal ──
            Spacer(modifier = Modifier.height(4.dp))
            StepHeaderTarik(step = 2, title = "Input Nominal Penarikan")

            OutlinedTextField(
                value = jumlahTarikText,
                onValueChange = { jumlahTarikText = it.filter { c -> c.isDigit() }; validationError = null },
                label = { Text("Jumlah Penarikan (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = validationError != null,
                supportingText = validationError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            // Quick preset buttons
            Text("Pilih Cepat", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAmounts.forEach { preset ->
                    val presetInt = preset.toLong()
                    OutlinedButton(
                        onClick = { jumlahTarikText = presetInt.toString(); validationError = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${presetInt / 1000}rb",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // ── Step 3: Ringkasan ──
            Spacer(modifier = Modifier.height(4.dp))
            StepHeaderTarik(step = 3, title = "Ringkasan Penarikan")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Jumlah Penarikan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        CurrencyHelper.formatRupiah(amount),
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
                        val sisaSaldo = selectedNasabah!!.saldo - amount
                        Text(
                            "Sisa saldo setelah tarik: ${CurrencyHelper.formatRupiah(if (sisaSaldo < 0) 0.0 else sisaSaldo)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Submit Button
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    validationError = null
                    if (selectedNasabah == null) {
                        validationError = "Pilih nasabah terlebih dahulu"
                        return@Button
                    }
                    if (amount <= 0) {
                        validationError = "Jumlah penarikan harus lebih dari 0"
                        return@Button
                    }
                    if (amount > (selectedNasabah?.saldo ?: 0.0)) {
                        validationError = "Saldo tidak mencukupi (saldo: ${CurrencyHelper.formatRupiah(selectedNasabah?.saldo ?: 0.0)})"
                        return@Button
                    }
                    showConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = tarikState !is UiState.Loading
            ) {
                if (tarikState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Konfirmasi Penarikan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
            title = "Konfirmasi Penarikan",
            message = "Tarik saldo sebesar ${CurrencyHelper.formatRupiah(amount)} dari nasabah ${selectedNasabah?.nama}?",
            confirmText = "Ya, Tarik",
            dismissText = "Batal",
            onConfirm = {
                showConfirmDialog = false
                viewModel.tarik(
                    idNasabah = selectedNasabah?.id ?: "",
                    namaNasabah = selectedNasabah?.nama ?: "",
                    jumlah = amount
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

@Composable
private fun StepHeaderTarik(step: Int, title: String) {
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
