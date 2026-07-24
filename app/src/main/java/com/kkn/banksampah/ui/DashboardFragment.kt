package com.kkn.banksampah.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kkn.banksampah.MainActivity
import com.kkn.banksampah.R
import com.kkn.banksampah.adapter.JenisSampahAdapter
import com.kkn.banksampah.databinding.FragmentDashboardBinding
import com.kkn.banksampah.databinding.DialogJenisSampahBinding
import com.kkn.banksampah.helper.CurrencyHelper
import com.kkn.banksampah.helper.FirebaseHelper
import com.kkn.banksampah.model.JenisSampah

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDashboardStats()

        binding.btnSetorSampah.setOnClickListener {
            findNavController().navigate(R.id.nav_transaksi)
        }

        binding.btnTarikSaldo.setOnClickListener {
            findNavController().navigate(R.id.nav_transaksi)
        }

        binding.btnTambahNasabah.setOnClickListener {
            findNavController().navigate(R.id.nav_nasabah)
        }

        binding.btnKelolaJenisSampah.setOnClickListener {
            showKelolaJenisSampahDialog()
        }

        binding.btnLogout?.setOnClickListener {
            (activity as? MainActivity)?.logout()
        }

        binding.btnLihatRiwayat?.setOnClickListener {
            findNavController().navigate(R.id.nav_riwayat)
        }
    }

    private fun loadDashboardStats() {
        FirebaseHelper.getDashboardStats(
            onResult = { totalNasabah, totalSampahKg, totalSaldo ->
                if (_binding != null) {
                    binding.tvTotalNasabah.text = totalNasabah.toString()
                    binding.tvTotalSampah.text = CurrencyHelper.formatKg(totalSampahKg)
                    binding.tvTotalSaldo.text = CurrencyHelper.formatRupiah(totalSaldo)
                }
            },
            onFailure = { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showKelolaJenisSampahDialog() {
        val dialogBinding = DialogJenisSampahBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Setup RecyclerView for existing jenis sampah
        val adapter = JenisSampahAdapter { jenisSampah ->
            // Delete confirmation
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Jenis Sampah")
                .setMessage("Hapus ${jenisSampah.namaSampah}?")
                .setPositiveButton("Ya") { _, _ ->
                    FirebaseHelper.hapusJenisSampah(jenisSampah.id ?: "",
                        onSuccess = {
                            Toast.makeText(requireContext(), "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { e ->
                            Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
        dialogBinding.rvJenisSampah.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvJenisSampah.adapter = adapter

        // Load existing data
        FirebaseHelper.getAllJenisSampah(
            onSuccess = { list -> adapter.submitList(list) },
            onFailure = { e ->
                Toast.makeText(requireContext(), "Gagal memuat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )

        dialogBinding.btnSimpan.setOnClickListener {
            val nama = dialogBinding.etNamaSampah.text.toString().trim()
            val harga = dialogBinding.etHargaSampah.text.toString().toDoubleOrNull()

            if (nama.isEmpty()) {
                dialogBinding.etNamaSampah.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            if (harga == null || harga <= 0) {
                dialogBinding.etHargaSampah.error = "Harga tidak valid"
                return@setOnClickListener
            }

            val jenisSampah = JenisSampah(namaSampah = nama, hargaPerKg = harga)
            FirebaseHelper.tambahJenisSampah(jenisSampah,
                onSuccess = {
                    Toast.makeText(requireContext(), "$nama berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    dialogBinding.etNamaSampah.text?.clear()
                    dialogBinding.etHargaSampah.text?.clear()
                },
                onFailure = { e ->
                    Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        dialogBinding.btnBatal.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
