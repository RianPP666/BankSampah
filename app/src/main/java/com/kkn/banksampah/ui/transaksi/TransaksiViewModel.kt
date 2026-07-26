package com.kkn.banksampah.ui.transaksi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.model.DetailSampah
import com.kkn.banksampah.data.model.JenisSampah
import com.kkn.banksampah.data.model.Nasabah
import com.kkn.banksampah.data.model.Transaksi
import com.kkn.banksampah.data.repository.AuthRepository
import com.kkn.banksampah.data.repository.NasabahRepository
import com.kkn.banksampah.data.repository.SampahRepository
import com.kkn.banksampah.data.repository.TransaksiRepository
import com.kkn.banksampah.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

class TransaksiViewModel : ViewModel() {
    private val nasabahRepository = NasabahRepository()
    private val sampahRepository = SampahRepository()
    private val transaksiRepository = TransaksiRepository()
    private val authRepository = AuthRepository()

    private val _nasabahList = MutableStateFlow<List<Nasabah>>(emptyList())
    val nasabahList: StateFlow<List<Nasabah>> = _nasabahList

    private val _jenisSampahList = MutableStateFlow<List<JenisSampah>>(emptyList())
    val jenisSampahList: StateFlow<List<JenisSampah>> = _jenisSampahList

    private val _setorState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val setorState: StateFlow<UiState<Unit>> = _setorState

    private val _tarikState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val tarikState: StateFlow<UiState<Unit>> = _tarikState

    private val laporanRepository = com.kkn.banksampah.data.repository.LaporanRepository()
    private var currentKasBank = 0.0

    init {
        // Collect only once in init to prevent duplicate collectors
        viewModelScope.launch {
            nasabahRepository.getAll()
                .catch { e -> Log.e("TransaksiVM", "Error loading nasabah: ${e.message}") }
                .collect { _nasabahList.value = it }
        }
        viewModelScope.launch {
            sampahRepository.getAll()
                .catch { e -> Log.e("TransaksiVM", "Error loading jenis sampah: ${e.message}") }
                .collect { _jenisSampahList.value = it }
        }
        viewModelScope.launch {
            laporanRepository.getDashboardStats().collect { stats ->
                currentKasBank = stats.totalKas
            }
        }
    }

    fun setor(idNasabah: String, namaNasabah: String, items: List<DetailSampah>, totalRupiah: Double) {
        viewModelScope.launch {
            _setorState.value = UiState.Loading
            try {
                val petugas = authRepository.getCurrentUser()
                val transaksi = Transaksi(
                    id = UUID.randomUUID().toString(),
                    idNasabah = idNasabah,
                    namaNasabah = namaNasabah,
                    petugasId = petugas?.uid ?: "",
                    petugasNama = petugas?.email ?: "Petugas",
                    jenisTransaksi = "SETOR",
                    totalRupiah = totalRupiah,
                    tanggal = System.currentTimeMillis(),
                    detailSampah = items
                )
                transaksiRepository.setor(transaksi)
                _setorState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _setorState.value = UiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun tarik(idNasabah: String, namaNasabah: String, jumlah: Double) {
        if (jumlah > currentKasBank) {
            val kasFormatted = com.kkn.banksampah.util.CurrencyHelper.formatRupiah(currentKasBank)
            _tarikState.value = UiState.Error("Gagal: Uang Kas Bank tidak mencukupi (Tersedia: $kasFormatted). Silakan jual stok gudang terlebih dahulu.")
            return
        }

        viewModelScope.launch {
            _tarikState.value = UiState.Loading
            try {
                val petugas = authRepository.getCurrentUser()
                transaksiRepository.tarik(
                    idNasabah = idNasabah,
                    amount = jumlah,
                    petugasId = petugas?.uid ?: "",
                    petugasNama = petugas?.email ?: "Petugas"
                )
                _tarikState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _tarikState.value = UiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetState() {
        _setorState.value = UiState.Idle
        _tarikState.value = UiState.Idle
    }
}
