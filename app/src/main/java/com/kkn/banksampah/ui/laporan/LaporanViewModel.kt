package com.kkn.banksampah.ui.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkn.banksampah.data.repository.LaporanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

import com.kkn.banksampah.data.repository.LaporanBulanan

class LaporanViewModel : ViewModel() {
    private val laporanRepository = LaporanRepository()

    private val _laporanData = MutableStateFlow(LaporanBulanan())
    val laporanData: StateFlow<LaporanBulanan> = _laporanData

    init {
        val cal = Calendar.getInstance()
        loadLaporan(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    }

    fun loadLaporan(bulan: Int, tahun: Int) {
        viewModelScope.launch {
            try {
                // Assuming LaporanRepository provides a suspend function or flow to get LaporanBulanan
                val data = laporanRepository.getLaporanBulanan(bulan, tahun)
                _laporanData.value = data
            } catch (e: Exception) {
                _laporanData.value = LaporanBulanan(bulan = bulan, tahun = tahun)
            }
        }
    }
}
