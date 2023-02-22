package com.hyundailogics.shupool.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.fragment.CarpoolCompletedFragment
import com.hyundailogics.shupool.fragment.DriverRouteFragment
import com.hyundailogics.shupool.fragment.SetRouteFragment

class CarpoolCreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carpool_create)

        supportFragmentManager.beginTransaction().replace(R.id.createContainer, DriverRouteFragment()).commit()

//        onFragmentChanged(0)
    }
//    fun onFragmentChanged(index: Int) {
//        when (index) {
//            0 ->supportFragmentManager.beginTransaction().replace(R.id.createContainer, DriverRouteFragment()).commit()
//            1 ->supportFragmentManager.beginTransaction().replace(R.id.createContainer, CarpoolCompletedFragment()).commit()
//        }
//    }
}