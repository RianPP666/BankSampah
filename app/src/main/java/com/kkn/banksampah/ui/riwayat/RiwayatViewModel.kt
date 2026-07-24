package com.kkn.banksampah.ui.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.model.Transaksi
import com.kkn.banksampah.data.repository.TransaksiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RiwayatViewModel : ViewModel() {
    private val transaksiRepository = TransaksiRepository()

    private val _semuaTransaksi = MutableStateFlow<List<Transaksi>>(emptyList())
    
    private val _filter = MutableStateFlow("SEMUA")
    val filter: StateFlow<String> = _filter

    private val _transaksi = MutableStateFlow<List<Transaksi>>(emptyList())
    val transaksi: StateFlow<List<Transaksi>> = _transaksi

    init {
        loadRiwayat()
        viewModelScope.launch {
            _semuaTransaksi.combine(_filter) { list, filterText ->
                if (filterText == "SEMUA") list
                else list.filter { it.jenisTransaksi == filterText }
            }.collect {
                _transaksi.value = it
            }
        }
    }

    fun loadRiwayat() {
        viewModelScope.launch {
            transaksiRepository.getAll()
                .catch { }
                .collect { list ->
                    _semuaTransaksi.value = list.sortedByDescending { it.tanggal }
                }
        }
    }

    fun setFilter(newFilter: String) {
        _filter.value = newFilter
    }
}
