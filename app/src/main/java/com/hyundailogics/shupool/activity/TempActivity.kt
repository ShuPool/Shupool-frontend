package com.hyundailogics.shupool.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.fragment.CarpoolCompletedFragment
import com.hyundailogics.shupool.fragment.DriverNotifyFragment

class TempActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_notify_detail)

//        onFragmentChanged(0)
    }
    fun onFragmentChanged(index: Int) {
        when (index) {
            0 ->supportFragmentManager.beginTransaction().replace(R.id.mainContainer, DriverNotifyFragment()).commit()
            1 ->supportFragmentManager.beginTransaction().replace(R.id.mainContainer, CarpoolCompletedFragment()).commit()
        }
    }
}

