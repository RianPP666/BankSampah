package com.kkn.banksampah.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kkn.banksampah.adapter.RiwayatAdapter
import com.kkn.banksampah.databinding.FragmentRiwayatBinding
import com.kkn.banksampah.helper.FirebaseHelper
import com.kkn.banksampah.model.Transaksi

class RiwayatFragment : Fragment() {

    private var _binding: FragmentRiwayatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RiwayatAdapter
    private var allTransaksi = listOf<Transaksi>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadRiwayat()

        binding.etCariRiwayat.addTextChangedListener { text ->
            filterRiwayat(text.toString())
        }
    }

    private fun setupRecyclerView() {
        adapter = RiwayatAdapter()
        binding.rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRiwayat.adapter = adapter
    }

    private fun loadRiwayat() {
        FirebaseHelper.getAllTransaksi(
            onSuccess = { list ->
                if (_binding != null) {
                    allTransaksi = list
                    adapter.submitList(list)
                    binding.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvRiwayat.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            },
            onFailure = { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Gagal memuat riwayat: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun filterRiwayat(query: String) {
        val filtered = if (query.isBlank()) allTransaksi
        else allTransaksi.filter { it.namaNasabah.contains(query, ignoreCase = true) }
        adapter.submitList(filtered)
        binding.tvKosong.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvRiwayat.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
