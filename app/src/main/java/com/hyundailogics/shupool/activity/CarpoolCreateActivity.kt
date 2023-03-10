package com.hyundailogics.shupool.activity

import android.os.Bundle
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.databinding.ActivityCarpoolCreateBinding
import com.hyundailogics.shupool.fragment.CarpoolCompletedFragment
import com.hyundailogics.shupool.fragment.DriverRouteFragment
import com.hyundailogics.shupool.fragment.SetRouteFragment

class CarpoolCreateActivity : BaseActivity() {
    private lateinit var binding: ActivityCarpoolCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarpoolCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addFragment(binding.createContainer.id, DriverRouteFragment(), "test")
//        supportFragmentManager.beginTransaction().replace(R.id.createContainer, DriverRouteFragment()).commit()

//        onFragmentChanged(0)
    }
    fun onFragmentChanged(index: Int) {
        when (index) {
            0 ->supportFragmentManager.beginTransaction().replace(R.id.createContainer, SetRouteFragment()).addToBackStack(null).commit()
            1 ->supportFragmentManager.beginTransaction().replace(R.id.createContainer, CarpoolCompletedFragment()).addToBackStack(null).commit()
        }
    }
}