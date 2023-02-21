package com.hyundailogics.shupool.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hyundailogics.shupool.Notify
import com.hyundailogics.shupool.databinding.NotifyItemBinding

class NotifyAdapter : RecyclerView.Adapter<NotifyAdapter.ViewHolder>() {
    var items = ArrayList<Notify>()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(NotifyItemBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    inner class ViewHolder(val binding: NotifyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setItem(item: Notify) {
            binding.name.text = item.name
            binding.carPoolName.text = item.carpool
        }
    }
}