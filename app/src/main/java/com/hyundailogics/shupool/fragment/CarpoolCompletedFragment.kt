package com.hyundailogics.shupool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.activity.CarpoolCreateActivity
import com.hyundailogics.shupool.databinding.FragmentCarpoolCompletedBinding

class CarpoolCompletedFragment : Fragment() {
    lateinit var binding: FragmentCarpoolCompletedBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCarpoolCompletedBinding.inflate(inflater, container, false)

        binding.cancelBtn.setOnClickListener {
            val activity = activity as CarpoolCreateActivity?
            activity?.finish()
        }

        return binding.root
    }
}