package com.hyundailogics.shupool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyundailogics.shupool.Notify
import com.hyundailogics.shupool.adapter.NotifyAdapter
import com.hyundailogics.shupool.databinding.FragmentNotifyBinding

class NotifyFragment : Fragment() {
    lateinit var binding: FragmentNotifyBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotifyBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager

        val adapter = NotifyAdapter()

        adapter.items.add(Notify("유진", "국민대"))
        adapter.items.add(Notify("은선", "출근"))

        binding.recyclerView.adapter = adapter

        return binding.root
    }
}