package com.hyundailogics.shupool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyundailogics.shupool.CrewNotify
import com.hyundailogics.shupool.adapter.CrewNotifyAdapter
import com.hyundailogics.shupool.databinding.FragmentCrewNotifyBinding

class CrewNotifyFragment : Fragment() {
    lateinit var binding: FragmentCrewNotifyBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCrewNotifyBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager

        val adapter = CrewNotifyAdapter()

        adapter.items.add(CrewNotify("유진", "국민대", "거부"))
        adapter.items.add(CrewNotify("은선", "출근", "수락"))

        binding.recyclerView.adapter = adapter

        return binding.root
    }
}