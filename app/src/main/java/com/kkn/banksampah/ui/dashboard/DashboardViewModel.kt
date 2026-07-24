package com.kkn.banksampah.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.model.Transaksi
import com.kkn.banksampah.data.repository.LaporanRepository
import com.kkn.banksampah.data.repository.NasabahRepository
import com.kkn.banksampah.data.repository.TransaksiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

class DashboardViewModel : ViewModel() {
    private val nasabahRepository = NasabahRepository()
    private val transaksiRepository = TransaksiRepository()
    private val laporanRepository = LaporanRepository()

    private val _totalNasabah = MutableStateFlow(0)
    val totalNasabah: StateFlow<Int> = _totalNasabah.asStateFlow()

    private val _totalSampahKg = MutableStateFlow(0.0)
    val totalSampahKg: StateFlow<Double> = _totalSampahKg.asStateFlow()

    private val _totalSaldo = MutableStateFlow(0.0)
    val totalSaldo: StateFlow<Double> = _totalSaldo.asStateFlow()

    private val _recentTransactions = MutableStateFlow<List<Transaksi>>(emptyList())
    val recentTransactions: StateFlow<List<Transaksi>> = _recentTransactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                laporanRepository.getDashboardStats().collect { stats ->
                    _totalNasabah.value = stats.totalNasabah
                    _totalSampahKg.value = stats.totalSampahKg
                    _totalSaldo.value = stats.totalSaldo
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
        
        viewModelScope.launch {
            try {
                transaksiRepository.getRecentTransactions(5).collect { transactions ->
                    _recentTransactions.value = transactions
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}
