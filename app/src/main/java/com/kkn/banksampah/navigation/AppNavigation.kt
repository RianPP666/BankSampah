package com.kkn.banksampah.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kkn.banksampah.ui.auth.LoginScreen
import com.kkn.banksampah.ui.dashboard.DashboardScreen
import com.kkn.banksampah.ui.sampah.JenisSampahScreen
import com.kkn.banksampah.ui.laporan.LaporanScreen
import com.kkn.banksampah.ui.nasabah.NasabahScreen
import com.kkn.banksampah.ui.penjualan.PenjualanScreen
import com.kkn.banksampah.ui.pengaturan.PengaturanScreen
import com.kkn.banksampah.ui.riwayat.RiwayatScreen
import com.kkn.banksampah.ui.transaksi.SetorScreen
import com.kkn.banksampah.ui.transaksi.TarikScreen
import com.kkn.banksampah.ui.transaksi.TransaksiMenuScreen

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom nav routes — these are the root-level tabs
    val bottomNavRoutes = listOf(
        Screen.Dashboard.route,
        Screen.TransaksiMenu.route,
        Screen.Nasabah.route,
        Screen.Riwayat.route,
        Screen.Pengaturan.route
    )

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard.route, Icons.Filled.Dashboard, "Beranda"),
        BottomNavItem(Screen.TransaksiMenu.route, Icons.Filled.SwapHoriz, "Transaksi"),
        BottomNavItem(Screen.Nasabah.route, Icons.Filled.People, "Nasabah"),
        BottomNavItem(Screen.Riwayat.route, Icons.Filled.History, "Riwayat"),
        BottomNavItem(Screen.Pengaturan.route, Icons.Filled.Settings, "Pengaturan")
    )

    // Only show bottom bar on root tab routes (hide on Login, Setor, Tarik, Laporan, JenisSampah)
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController = navController)
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }
            composable(Screen.TransaksiMenu.route) {
                TransaksiMenuScreen(navController = navController)
            }
            composable(Screen.Nasabah.route) {
                NasabahScreen(navController = navController)
            }
            composable(Screen.JenisSampah.route) {
                JenisSampahScreen(navController = navController)
            }
            composable(Screen.Setor.route) {
                SetorScreen(navController = navController)
            }
            composable(Screen.Tarik.route) {
                TarikScreen(navController = navController)
            }
            composable(Screen.Riwayat.route) {
                RiwayatScreen(navController = navController)
            }
            composable(Screen.Laporan.route) {
                LaporanScreen(navController = navController)
            }
            composable(Screen.Pengaturan.route) {
                PengaturanScreen(navController = navController)
            }
            composable(Screen.Penjualan.route) {
                PenjualanScreen(navController = navController)
            }
        }
    }
}
