package com.hyundailogics.shupool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hyundailogics.shupool.activity.CarpoolJoinActivity
import com.hyundailogics.shupool.databinding.FragmentSetPickupBinding

class SetPickupFragment : Fragment() {
    lateinit var binding: FragmentSetPickupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetPickupBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener {
            val activity = activity as CarpoolJoinActivity?
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
            activity?.supportFragmentManager?.popBackStack()
        }

        binding.RequestButton.setOnClickListener {
            val activity = activity as CarpoolJoinActivity?
            activity?.finish()
        }
        return binding.root
    }
}