package com.kkn.banksampah.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.kkn.banksampah.R
import com.kkn.banksampah.databinding.FragmentTransaksiBinding
import com.kkn.banksampah.databinding.ItemSampahInputBinding
import com.kkn.banksampah.helper.CurrencyHelper
import com.kkn.banksampah.helper.FirebaseHelper
import com.kkn.banksampah.model.*

class TransaksiFragment : Fragment() {

    private var _binding: FragmentTransaksiBinding? = null
    private val binding get() = _binding!!

    private var listNasabah = listOf<Nasabah>()
    private var listJenisSampah = listOf<JenisSampah>()
    private var selectedNasabahSetor: Nasabah? = null
    private var selectedNasabahTarik: Nasabah? = null

    // Track dynamically added sampah input rows
    private val sampahInputBindings = mutableListOf<ItemSampahInputBinding>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData()
        setupRadioGroup()
        setupButtons()
        // Add initial sampah input row
        addSampahInputRow()
    }

    private fun loadData() {
        FirebaseHelper.getAllNasabah(
            onSuccess = { list ->
                listNasabah = list
                setupNasabahDropdowns()
            },
            onFailure = { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Gagal memuat nasabah: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        FirebaseHelper.getAllJenisSampah(
            onSuccess = { list ->
                listJenisSampah = list
                // Update existing sampah input dropdowns
                sampahInputBindings.forEach { setupSampahDropdown(it) }
            },
            onFailure = { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Gagal memuat jenis sampah: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupNasabahDropdowns() {
        if (_binding == null) return
        val names = listNasabah.map { it.nama }
        
        // Setor dropdown
        val adapterSetor = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        binding.spinnerNasabah.setAdapter(adapterSetor)
        binding.spinnerNasabah.setOnItemClickListener { _, _, position, _ ->
            selectedNasabahSetor = listNasabah[position]
        }

        // Tarik dropdown
        val adapterTarik = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        binding.spinnerNasabahTarik.setAdapter(adapterTarik)
        binding.spinnerNasabahTarik.setOnItemClickListener { _, _, position, _ ->
            selectedNasabahTarik = listNasabah[position]
            binding.tvSaldoSaatIni.text = "Saldo saat ini: ${CurrencyHelper.formatRupiah(listNasabah[position].saldo)}"
            binding.tvSaldoSaatIni.visibility = View.VISIBLE
        }
    }

    private fun setupRadioGroup() {
        binding.rgJenisTransaksi.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbSetor -> {
                    binding.layoutSetor.visibility = View.VISIBLE
                    binding.layoutTarik.visibility = View.GONE
                }
                R.id.rbTarik -> {
                    binding.layoutSetor.visibility = View.GONE
                    binding.layoutTarik.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnTambahItem.setOnClickListener {
            addSampahInputRow()
        }

        binding.btnSimpanTransaksi.setOnClickListener {
            simpanTransaksiSetor()
        }

        binding.btnTarikSaldoSubmit.setOnClickListener {
            simpanTransaksiTarik()
        }
    }

    private fun addSampahInputRow() {
        val itemBinding = ItemSampahInputBinding.inflate(layoutInflater)
        setupSampahDropdown(itemBinding)

        // Weight text changed listener - auto calculate subtotal
        itemBinding.etBerat.addTextChangedListener {
            calculateSubtotal(itemBinding)
            calculateTotal()
        }

        // Remove button
        itemBinding.btnHapusItem.setOnClickListener {
            if (sampahInputBindings.size > 1) {
                binding.containerItemSampah.removeView(itemBinding.root)
                sampahInputBindings.remove(itemBinding)
                calculateTotal()
            } else {
                Toast.makeText(requireContext(), "Minimal harus ada 1 item sampah", Toast.LENGTH_SHORT).show()
            }
        }

        sampahInputBindings.add(itemBinding)
        binding.containerItemSampah.addView(itemBinding.root)
    }

    private fun setupSampahDropdown(itemBinding: ItemSampahInputBinding) {
        val names = listJenisSampah.map { it.namaSampah }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        itemBinding.spinnerJenisSampah.setAdapter(adapter)
        itemBinding.spinnerJenisSampah.setOnItemClickListener { _, _, position, _ ->
            val selected = listJenisSampah[position]
            itemBinding.tvHarga.text = CurrencyHelper.formatRupiah(selected.hargaPerKg) + "/Kg"
            itemBinding.spinnerJenisSampah.tag = selected
            calculateSubtotal(itemBinding)
            calculateTotal()
        }
    }

    private fun calculateSubtotal(itemBinding: ItemSampahInputBinding) {
        val selectedSampah = itemBinding.spinnerJenisSampah.tag as? JenisSampah
        val berat = itemBinding.etBerat.text.toString().toDoubleOrNull() ?: 0.0
        if (selectedSampah != null) {
            val subtotal = berat * selectedSampah.hargaPerKg
            itemBinding.tvSubtotal.text = CurrencyHelper.formatRupiah(subtotal)
        }
    }

    private fun calculateTotal() {
        var total = 0.0
        sampahInputBindings.forEach { itemBinding ->
            val selectedSampah = itemBinding.spinnerJenisSampah.tag as? JenisSampah
            val berat = itemBinding.etBerat.text.toString().toDoubleOrNull() ?: 0.0
            if (selectedSampah != null) {
                total += berat * selectedSampah.hargaPerKg
            }
        }
        binding.tvTotal.text = CurrencyHelper.formatRupiah(total)
    }

    private fun simpanTransaksiSetor() {
        if (selectedNasabahSetor == null) {
            Toast.makeText(requireContext(), "Silakan pilih nasabah terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val detailList = mutableListOf<DetailSampah>()
        var totalRupiah = 0.0

        for (itemBinding in sampahInputBindings) {
            val selectedSampah = itemBinding.spinnerJenisSampah.tag as? JenisSampah
            val berat = itemBinding.etBerat.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedSampah == null || berat <= 0) {
                Toast.makeText(requireContext(), "Mohon lengkapi semua item sampah", Toast.LENGTH_SHORT).show()
                return
            }

            val subtotal = berat * selectedSampah.hargaPerKg
            detailList.add(
                DetailSampah(
                    namaSampah = selectedSampah.namaSampah,
                    beratKg = berat,
                    hargaPerKg = selectedSampah.hargaPerKg,
                    subtotal = subtotal
                )
            )
            totalRupiah += subtotal
        }

        val nasabah = selectedNasabahSetor!!

        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Setor Sampah")
            .setMessage("Nasabah: ${nasabah.nama}\nTotal: ${CurrencyHelper.formatRupiah(totalRupiah)}\n\nSimpan transaksi ini?")
            .setPositiveButton("Ya, Simpan") { _, _ ->
                val transaksi = Transaksi(
                    idNasabah = nasabah.id ?: "",
                    namaNasabah = nasabah.nama,
                    tanggal = Timestamp.now(),
                    jenisTransaksi = "SETOR",
                    detailSampah = detailList,
                    totalRupiah = totalRupiah
                )

                FirebaseHelper.simpanTransaksi(transaksi,
                    onSuccess = {
                        FirebaseHelper.updateSaldoNasabah(nasabah.id ?: "", totalRupiah, true,
                            onSuccess = {
                                if (isAdded) {
                                    Toast.makeText(requireContext(), "Transaksi berhasil disimpan!", Toast.LENGTH_LONG).show()
                                    resetFormSetor()
                                }
                            },
                            onFailure = { e ->
                                if (isAdded) {
                                    Toast.makeText(requireContext(), "Transaksi tersimpan tapi gagal update saldo: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    },
                    onFailure = { e ->
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Gagal menyimpan transaksi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun simpanTransaksiTarik() {
        if (selectedNasabahTarik == null) {
            Toast.makeText(requireContext(), "Silakan pilih nasabah terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlah = binding.etJumlahTarik.text.toString().toDoubleOrNull()
        if (jumlah == null || jumlah <= 0) {
            Toast.makeText(requireContext(), "Masukkan jumlah penarikan yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val nasabah = selectedNasabahTarik!!
        if (jumlah > nasabah.saldo) {
            Toast.makeText(requireContext(), "Saldo tidak mencukupi! Saldo saat ini: ${CurrencyHelper.formatRupiah(nasabah.saldo)}", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Penarikan Saldo")
            .setMessage("Nasabah: ${nasabah.nama}\nJumlah tarik: ${CurrencyHelper.formatRupiah(jumlah)}\n\nLanjutkan?")
            .setPositiveButton("Ya, Tarik") { _, _ ->
                val transaksi = Transaksi(
                    idNasabah = nasabah.id ?: "",
                    namaNasabah = nasabah.nama,
                    tanggal = Timestamp.now(),
                    jenisTransaksi = "TARIK",
                    detailSampah = emptyList(),
                    totalRupiah = jumlah
                )

                FirebaseHelper.simpanTransaksi(transaksi,
                    onSuccess = {
                        FirebaseHelper.updateSaldoNasabah(nasabah.id ?: "", jumlah, false,
                            onSuccess = {
                                if (isAdded) {
                                    Toast.makeText(requireContext(), "Penarikan saldo berhasil!", Toast.LENGTH_LONG).show()
                                    resetFormTarik()
                                }
                            },
                            onFailure = { e ->
                                if (isAdded) {
                                    Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    },
                    onFailure = { e ->
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Gagal menyimpan transaksi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun resetFormSetor() {
        selectedNasabahSetor = null
        binding.spinnerNasabah.setText("")
        binding.containerItemSampah.removeAllViews()
        sampahInputBindings.clear()
        addSampahInputRow()
        binding.tvTotal.text = CurrencyHelper.formatRupiah(0.0)
    }

    private fun resetFormTarik() {
        selectedNasabahTarik = null
        binding.spinnerNasabahTarik.setText("")
        binding.etJumlahTarik.text?.clear()
        binding.tvSaldoSaatIni.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
