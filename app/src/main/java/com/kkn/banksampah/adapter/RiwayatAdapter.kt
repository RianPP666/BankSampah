package com.kkn.banksampah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kkn.banksampah.R
import com.kkn.banksampah.databinding.ItemRiwayatBinding
import com.kkn.banksampah.helper.CurrencyHelper
import com.kkn.banksampah.model.Transaksi
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatAdapter : ListAdapter<Transaksi, RiwayatAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemRiwayatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaksi: Transaksi) {
            binding.tvNama.text = transaksi.namaNasabah

            // Format date
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            binding.tvTanggal.text = sdf.format(transaksi.tanggal.toDate())

            // Transaction type
            val isSetor = transaksi.jenisTransaksi == "SETOR"
            binding.tvJenisTransaksi.text = if (isSetor) "SETOR" else "TARIK"

            val bgColor = if (isSetor) R.color.card_setor else R.color.card_tarik
            binding.tvJenisTransaksi.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, bgColor)
            )

            // Detail
            if (isSetor && transaksi.detailSampah.isNotEmpty()) {
                val details = transaksi.detailSampah.joinToString("\n") {
                    "${it.namaSampah}: ${it.beratKg} Kg x ${CurrencyHelper.formatRupiah(it.hargaPerKg)}"
                }
                binding.tvDetail.text = details
            } else {
                binding.tvDetail.text = "Penarikan saldo"
            }

            // Total
            val prefix = if (isSetor) "+ " else "- "
            binding.tvTotal.text = prefix + CurrencyHelper.formatRupiah(transaksi.totalRupiah)
            val totalColor = if (isSetor) R.color.success else R.color.warning
            binding.tvTotal.setTextColor(
                ContextCompat.getColor(binding.root.context, totalColor)
            )

            // Indicator strip
            val indicatorColor = if (isSetor) R.color.success else R.color.warning
            binding.viewIndicator.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, indicatorColor)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Transaksi>() {
        override fun areItemsTheSame(oldItem: Transaksi, newItem: Transaksi) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaksi, newItem: Transaksi) = oldItem == newItem
    }
}
