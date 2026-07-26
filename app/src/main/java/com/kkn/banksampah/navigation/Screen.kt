package com.kkn.banksampah.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Nasabah : Screen("nasabah")
    object JenisSampah : Screen("jenis_sampah")
    object TransaksiMenu : Screen("transaksi_menu")
    object Setor : Screen("setor")
    object Tarik : Screen("tarik")
    object Riwayat : Screen("riwayat")
    object Laporan : Screen("laporan")
    object Pengaturan : Screen("pengaturan")
}
