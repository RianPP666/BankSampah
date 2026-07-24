package com.kkn.banksampah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kkn.banksampah.databinding.ItemJenisSampahBinding
import com.kkn.banksampah.helper.CurrencyHelper
import com.kkn.banksampah.model.JenisSampah

class JenisSampahAdapter(
    private val onDelete: (JenisSampah) -> Unit
) : ListAdapter<JenisSampah, JenisSampahAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemJenisSampahBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jenisSampah: JenisSampah) {
            binding.tvNamaSampah.text = jenisSampah.namaSampah
            binding.tvHarga.text = "${CurrencyHelper.formatRupiah(jenisSampah.hargaPerKg)}/Kg"
            binding.btnHapus.setOnClickListener { onDelete(jenisSampah) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJenisSampahBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<JenisSampah>() {
        override fun areItemsTheSame(oldItem: JenisSampah, newItem: JenisSampah) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: JenisSampah, newItem: JenisSampah) = oldItem == newItem
    }
}
