package com.kkn.banksampah.ui.pengaturan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kkn.banksampah.navigation.Screen
import com.kkn.banksampah.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    navController: NavController,
    viewModel: PengaturanViewModel = viewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Pengaturan"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(user?.nama ?: "Petugas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                        Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                            Text(user?.role ?: "Petugas", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsMenuItem(
                icon = Icons.Default.List,
                title = "Kelola Jenis Sampah",
                onClick = { navController.navigate(Screen.JenisSampah.route) }
            )
            SettingsMenuItem(
                icon = Icons.Default.Assessment,
                title = "Laporan Bulanan",
                onClick = { navController.navigate(Screen.Laporan.route) }
            )
            SettingsMenuItem(
                icon = Icons.Default.Info,
                title = "Tentang Aplikasi",
                onClick = { showAboutDialog = true }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Add Reset Database Button
            var showResetDialog by remember { mutableStateOf(false) }
            var isResetting by remember { mutableStateOf(false) }
            var resetMessage by remember { mutableStateOf<String?>(null) }
            
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) {
                Text("Reset / Kosongkan Database", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Versi 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Reset Dialog
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isResetting) showResetDialog = false },
                    title = { Text("⚠️ PERINGATAN KERAS") },
                    text = { Text("Tindakan ini akan MENGHAPUS PERMANEN seluruh data Nasabah, Transaksi, Penjualan, dan Jenis Sampah. \n\nApakah Anda benar-benar yakin ingin memulai dari nol?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                isResetting = true
                                viewModel.resetDatabase { success, message ->
                                    isResetting = false
                                    showResetDialog = false
                                    resetMessage = message
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            enabled = !isResetting
                        ) {
                            if (isResetting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onError)
                            } else {
                                Text("Ya, HAPUS SEMUA")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }, enabled = !isResetting) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Reset Result Message
            if (resetMessage != null) {
                AlertDialog(
                    onDismissRequest = { resetMessage = null },
                    title = { Text("Informasi") },
                    text = { Text(resetMessage!!) },
                    confirmButton = {
                        TextButton(onClick = { resetMessage = null }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Ya, Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Tentang Aplikasi") },
            text = { Text("Bank Sampah Digital Desa\nDikembangkan untuk mempermudah pengelolaan bank sampah.\n\n© 2026 KKN") },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun SettingsMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
        }
    }
}
