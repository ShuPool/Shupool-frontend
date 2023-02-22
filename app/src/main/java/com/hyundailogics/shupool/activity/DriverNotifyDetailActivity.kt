package com.hyundailogics.shupool.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hyundailogics.shupool.databinding.ActivityDriverNotifyDetailBinding

class DriverNotifyDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDriverNotifyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = intent.getStringExtra("크루명")
        binding.crewName.text = data
    }
}