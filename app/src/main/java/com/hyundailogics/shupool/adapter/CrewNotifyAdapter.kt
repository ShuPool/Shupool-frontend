package com.hyundailogics.shupool.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hyundailogics.shupool.CrewNotify
import com.hyundailogics.shupool.databinding.CrewNotifyItemBinding

class CrewNotifyAdapter : RecyclerView.Adapter<CrewNotifyAdapter.ViewHolder>() {
    var items = ArrayList<CrewNotify>()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(CrewNotifyItemBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    inner class ViewHolder(val binding: CrewNotifyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setItem(item: CrewNotify) {
            binding.name.text = item.name
            binding.carPoolName.text = item.carpool
            binding.state.text = item.state
        }
    }
}