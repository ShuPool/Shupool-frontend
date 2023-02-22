package com.hyundailogics.shupool.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.fragment.CarpoolCompletedFragment
import com.hyundailogics.shupool.fragment.CrewNotifyFragment

class TempActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp)

        onFragmentChanged(0)
    }
    fun onFragmentChanged(index: Int) {
        when (index) {
            0 ->supportFragmentManager.beginTransaction().replace(R.id.mainContainer, CrewNotifyFragment()).commit()
            1 ->supportFragmentManager.beginTransaction().replace(R.id.mainContainer, CarpoolCompletedFragment()).commit()
        }
    }
}

