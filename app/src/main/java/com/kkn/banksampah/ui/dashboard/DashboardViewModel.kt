package com.kkn.banksampah.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.model.Transaksi
import com.kkn.banksampah.data.repository.LaporanRepository
import com.kkn.banksampah.data.repository.TransaksiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val transaksiRepository = TransaksiRepository()
    private val laporanRepository = LaporanRepository()

    private val _totalNasabah = MutableStateFlow(0)
    val totalNasabah: StateFlow<Int> = _totalNasabah.asStateFlow()

    private val _totalSampahKg = MutableStateFlow(0.0)
    val totalSampahKg: StateFlow<Double> = _totalSampahKg.asStateFlow()

    private val _totalSaldo = MutableStateFlow(0.0)
    val totalSaldo: StateFlow<Double> = _totalSaldo.asStateFlow()

    private val _stokGudangKg = MutableStateFlow(0.0)
    val stokGudangKg: StateFlow<Double> = _stokGudangKg.asStateFlow()

    private val _totalKas = MutableStateFlow(0.0)
    val totalKas: StateFlow<Double> = _totalKas.asStateFlow()

    private val _recentTransactions = MutableStateFlow<List<Transaksi>>(emptyList())
    val recentTransactions: StateFlow<List<Transaksi>> = _recentTransactions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Only collect once in init to prevent duplicate collectors
        loadDashboardStats()
        loadRecentTransactions()
    }

    private fun loadDashboardStats() {
        viewModelScope.launch {
            laporanRepository.getDashboardStats()
                .catch { e ->
                    Log.e("DashboardVM", "Error loading stats: ${e.message}")
                    _isLoading.value = false
                }
                .collect { stats ->
                    _totalNasabah.value = stats.totalNasabah
                    _totalSampahKg.value = stats.totalSampahKg
                    _totalSaldo.value = stats.totalSaldo
                    _stokGudangKg.value = stats.stokGudangKg
                    _totalKas.value = stats.totalKas
                    _isLoading.value = false
                }
        }
    }

    private fun loadRecentTransactions() {
        viewModelScope.launch {
            transaksiRepository.getRecentTransactions(5)
                .catch { e ->
                    Log.e("DashboardVM", "Error loading transactions: ${e.message}")
                    _isLoading.value = false
                }
                .collect { transactions ->
                    _recentTransactions.value = transactions
                    _isLoading.value = false
                }
        }
    }
}
