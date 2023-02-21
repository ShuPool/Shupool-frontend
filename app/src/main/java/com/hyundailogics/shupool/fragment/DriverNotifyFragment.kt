package com.hyundailogics.shupool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyundailogics.shupool.DriverNotify
import com.hyundailogics.shupool.adapter.DriverNotifyAdapter
import com.hyundailogics.shupool.databinding.FragmentDriverNotifyBinding

class DriverNotifyFragment : Fragment() {
    lateinit var binding: FragmentDriverNotifyBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverNotifyBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager

        val adapter = DriverNotifyAdapter()

        adapter.items.add(DriverNotify("유진", "국민대"))
        adapter.items.add(DriverNotify("은선", "출근"))

        binding.recyclerView.adapter = adapter

        return binding.root
    }
}