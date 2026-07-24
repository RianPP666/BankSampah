package com.kkn.banksampah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kkn.banksampah.databinding.ItemNasabahBinding
import com.kkn.banksampah.helper.CurrencyHelper
import com.kkn.banksampah.model.Nasabah

class NasabahAdapter(
    private val onEdit: (Nasabah) -> Unit,
    private val onDelete: (Nasabah) -> Unit
) : ListAdapter<Nasabah, NasabahAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemNasabahBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(nasabah: Nasabah) {
            binding.tvNama.text = nasabah.nama
            binding.tvAlamat.text = nasabah.alamat.ifEmpty { "-" }
            binding.tvNoHp.text = nasabah.noHp.ifEmpty { "-" }
            binding.tvSaldo.text = CurrencyHelper.formatRupiah(nasabah.saldo)
            binding.btnEdit.setOnClickListener { onEdit(nasabah) }
            binding.btnHapus.setOnClickListener { onDelete(nasabah) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNasabahBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Nasabah>() {
        override fun areItemsTheSame(oldItem: Nasabah, newItem: Nasabah) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Nasabah, newItem: Nasabah) = oldItem == newItem
    }
}
