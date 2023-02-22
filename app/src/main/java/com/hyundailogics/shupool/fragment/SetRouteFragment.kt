package com.hyundailogics.shupool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hyundailogics.shupool.activity.CarpoolCreateActivity
import com.hyundailogics.shupool.databinding.FragmentSetRouteBinding

class SetRouteFragment : Fragment() {
    lateinit var binding: FragmentSetRouteBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetRouteBinding.inflate(inflater, container, false)

        binding.completeButton.setOnClickListener {
            val activity = activity as CarpoolCreateActivity?
            activity?.onFragmentChanged(1)
        }
        return binding.root
    }
}