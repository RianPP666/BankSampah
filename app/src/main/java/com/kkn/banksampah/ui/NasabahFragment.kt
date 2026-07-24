package com.kkn.banksampah.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kkn.banksampah.adapter.NasabahAdapter
import com.kkn.banksampah.databinding.FragmentNasabahBinding
import com.kkn.banksampah.databinding.DialogTambahNasabahBinding
import com.kkn.banksampah.helper.FirebaseHelper
import com.kkn.banksampah.model.Nasabah

class NasabahFragment : Fragment() {

    private var _binding: FragmentNasabahBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NasabahAdapter
    private var allNasabah = listOf<Nasabah>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNasabahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadNasabah()

        binding.btnTambahNasabah.setOnClickListener {
            showTambahNasabahDialog()
        }

        binding.etCariNasabah.addTextChangedListener { text ->
            filterNasabah(text.toString())
        }
    }

    private fun setupRecyclerView() {
        adapter = NasabahAdapter(
            onEdit = { nasabah -> showEditNasabahDialog(nasabah) },
            onDelete = { nasabah -> showDeleteConfirmation(nasabah) }
        )
        binding.rvNasabah.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNasabah.adapter = adapter
    }

    private fun loadNasabah() {
        FirebaseHelper.getAllNasabah(
            onSuccess = { list ->
                if (_binding != null) {
                    allNasabah = list
                    adapter.submitList(list)
                    binding.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvNasabah.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            },
            onFailure = { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun filterNasabah(query: String) {
        val filtered = if (query.isBlank()) allNasabah
        else allNasabah.filter { it.nama.contains(query, ignoreCase = true) }
        adapter.submitList(filtered)
        binding.tvKosong.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvNasabah.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showTambahNasabahDialog(nasabah: Nasabah? = null) {
        val dialogBinding = DialogTambahNasabahBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        if (nasabah != null) {
            dialogBinding.etNama.setText(nasabah.nama)
            dialogBinding.etAlamat.setText(nasabah.alamat)
            dialogBinding.etNoHp.setText(nasabah.noHp)
        }

        dialogBinding.btnBatal.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSimpan.setOnClickListener {
            val nama = dialogBinding.etNama.text.toString().trim()
            val alamat = dialogBinding.etAlamat.text.toString().trim()
            val noHp = dialogBinding.etNoHp.text.toString().trim()

            if (nama.isEmpty()) {
                dialogBinding.etNama.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }

            val data = nasabah?.copy(nama = nama, alamat = alamat, noHp = noHp)
                ?: Nasabah(nama = nama, alamat = alamat, noHp = noHp)

            if (nasabah != null) {
                FirebaseHelper.updateNasabah(data,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Data nasabah berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    },
                    onFailure = { e ->
                        Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                FirebaseHelper.tambahNasabah(data,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Nasabah berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    },
                    onFailure = { e ->
                        Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        dialog.show()
    }

    private fun showEditNasabahDialog(nasabah: Nasabah) {
        showTambahNasabahDialog(nasabah)
    }

    private fun showDeleteConfirmation(nasabah: Nasabah) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Nasabah")
            .setMessage("Apakah Anda yakin ingin menghapus ${nasabah.nama}?")
            .setPositiveButton("Ya") { _, _ ->
                FirebaseHelper.hapusNasabah(nasabah.id ?: "",
                    onSuccess = {
                        Toast.makeText(requireContext(), "Nasabah berhasil dihapus", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
