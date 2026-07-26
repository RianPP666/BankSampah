package com.kkn.banksampah.ui.transaksi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kkn.banksampah.navigation.Screen
import com.kkn.banksampah.ui.components.AppTopBar

@Composable
fun TransaksiMenuScreen(navController: NavController) {
    Scaffold(
        topBar = {
            AppTopBar(title = "Transaksi")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pilih Jenis Transaksi",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Setor sampah atau tarik saldo nasabah",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            TransaksiMenuCard(
                title = "Setor Sampah",
                description = "Catat setoran sampah dari nasabah dan konversi menjadi saldo",
                icon = Icons.Default.AddCircle,
                containerColor = Color(0xFFDCFCE7),
                contentColor = Color(0xFF16A34A),
                onClick = { navController.navigate(Screen.Setor.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TransaksiMenuCard(
                title = "Tarik Saldo",
                description = "Proses penarikan saldo tunai oleh nasabah",
                icon = Icons.Default.MoneyOff,
                containerColor = Color(0xFFFEE2E2),
                contentColor = Color(0xFFDC2626),
                onClick = { navController.navigate(Screen.Tarik.route) }
            )
        }
    }
}

@Composable
fun TransaksiMenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
