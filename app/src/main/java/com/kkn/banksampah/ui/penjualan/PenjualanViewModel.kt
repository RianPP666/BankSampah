package com.kkn.banksampah.ui.penjualan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.model.DetailSampahJual
import com.kkn.banksampah.data.model.Penjualan
import com.kkn.banksampah.data.repository.AuthRepository
import com.kkn.banksampah.data.repository.PenjualanRepository
import com.kkn.banksampah.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PenjualanViewModel : ViewModel() {
    private val repository = PenjualanRepository()
    private val authRepository = AuthRepository()

    private val laporanRepository = com.kkn.banksampah.data.repository.LaporanRepository()
    
    private val _penjualanList = MutableStateFlow<List<Penjualan>>(emptyList())
    val penjualanList: StateFlow<List<Penjualan>> = _penjualanList.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    private val _totalPendapatan = MutableStateFlow(0.0)
    val totalPendapatan: StateFlow<Double> = _totalPendapatan.asStateFlow()

    private val _totalBeratDijual = MutableStateFlow(0.0)
    val totalBeratDijual: StateFlow<Double> = _totalBeratDijual.asStateFlow()
    
    private val _stokGudang = MutableStateFlow(0.0)
    val stokGudang: StateFlow<Double> = _stokGudang.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll()
                .catch { e -> Log.e("PenjualanVM", "Error loading: ${e.message}") }
                .collect { list ->
                    _penjualanList.value = list
                    _totalPendapatan.value = list.sumOf { it.totalHargaJual }
                    _totalBeratDijual.value = list.sumOf { it.totalBeratKg }
                }
        }
        viewModelScope.launch {
            laporanRepository.getDashboardStats().collect { stats ->
                _stokGudang.value = stats.stokGudangKg
            }
        }
    }

    fun simpanPenjualan(
        namaPengepul: String,
        items: List<DetailSampahJual>,
        catatan: String
    ) {
        if (namaPengepul.isBlank()) {
            _saveState.value = UiState.Error("Nama pengepul tidak boleh kosong")
            return
        }
        if (items.isEmpty()) {
            _saveState.value = UiState.Error("Tambahkan minimal 1 jenis sampah")
            return
        }
        val beratJual = items.sumOf { it.beratKg }
        if (beratJual <= 0) {
            _saveState.value = UiState.Error("Berat harus lebih dari 0")
            return
        }
        val currentStokGudang = _stokGudang.value
        if (beratJual > currentStokGudang) {
            _saveState.value = UiState.Error("Gagal: Stok di aplikasi hanya $currentStokGudang Kg. Input melebihi stok.")
            return
        }

        viewModelScope.launch {
            _saveState.value = UiState.Loading
            try {
                val petugas = authRepository.getCurrentUser()
                val penjualan = Penjualan(
                    namaPengepul = namaPengepul.trim(),
                    tanggal = System.currentTimeMillis(),
                    detailSampah = items,
                    totalBeratKg = items.sumOf { it.beratKg },
                    totalHargaJual = items.sumOf { it.subtotal },
                    petugasId = petugas?.uid ?: "",
                    petugasNama = petugas?.nama ?: "Petugas",
                    catatan = catatan.trim()
                )
                val result = repository.add(penjualan)
                if (result.isSuccess) {
                    _saveState.value = UiState.Success(Unit)
                } else {
                    _saveState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Gagal menyimpan")
                }
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetState() {
        _saveState.value = UiState.Idle
    }
}
