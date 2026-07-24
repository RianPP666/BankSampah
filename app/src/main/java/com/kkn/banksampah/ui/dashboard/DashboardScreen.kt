package com.kkn.banksampah.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kkn.banksampah.ui.components.*
import com.kkn.banksampah.data.model.*
import com.kkn.banksampah.util.*
import com.kkn.banksampah.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val totalNasabah by viewModel.totalNasabah.collectAsState()
    val totalSampahKg by viewModel.totalSampahKg.collectAsState()
    val totalSaldo by viewModel.totalSaldo.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Sampah Digital") },
                actions = {
                    IconButton(onClick = { /* Handle logout */ }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Setor.route) },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Setor Sampah")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Selamat Datang, Admin!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Nasabah",
                            value = totalNasabah.toString(),
                            icon = Icons.Default.AccountCircle,
                            iconColor = Color(0xFF3B82F6)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Sampah",
                            value = "${totalSampahKg} Kg",
                            icon = Icons.Default.Delete,
                            iconColor = Color(0xFF10B981)
                        )
                    }
                }
                
                item {
                    StatCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Total Saldo",
                        value = CurrencyHelper.formatRupiah(totalSaldo),
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = Color(0xFFF59E0B)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { navController.navigate(Screen.Setor.route) }) {
                            Text("Setor Sampah")
                        }
                        Button(onClick = { navController.navigate(Screen.Tarik.route) }) {
                            Text("Tarik Saldo")
                        }
                        Button(onClick = { navController.navigate(Screen.Nasabah.route) }) {
                            Text("Tambah Nasabah")
                        }
                    }
                }

                item {
                    Text(
                        text = "Transaksi Terbaru",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                if (recentTransactions.isEmpty()) {
                    item {
                        EmptyState(message = "Belum ada transaksi.")
                    }
                } else {
                    items(recentTransactions) { transaction ->
                        TransactionCard(transaksi = transaction)
                    }
                }
            }
        }
    }
}
