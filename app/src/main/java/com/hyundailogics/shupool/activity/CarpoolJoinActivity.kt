package com.hyundailogics.shupool.activity

import android.os.Bundle
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.databinding.ActivityCarpoolJoinBinding
import com.hyundailogics.shupool.fragment.*

class CarpoolJoinActivity : BaseActivity() {
    private lateinit var binding: ActivityCarpoolJoinBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarpoolJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addFragment(binding.joinContainer.id, CrewPickupFragment(), "test")
    }
    fun onFragmentChanged(index: Int) {
        when (index) {
            0 ->supportFragmentManager.beginTransaction().replace(R.id.joinContainer, SetPickupFragment()).addToBackStack(null).commit()
        }
    }
}