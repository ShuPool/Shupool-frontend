package com.hyundailogics.shupool.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hyundailogics.shupool.DriverNotify
import com.hyundailogics.shupool.databinding.DriverNotifyItemBinding

class DriverNotifyAdapter : RecyclerView.Adapter<DriverNotifyAdapter.ViewHolder>() {
    var items = ArrayList<DriverNotify>()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DriverNotifyItemBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    inner class ViewHolder(val binding: DriverNotifyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setItem(item: DriverNotify) {
            binding.name.text = item.name
            binding.carPoolName.text = item.carpool
        }
    }
}