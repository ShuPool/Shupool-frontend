package com.hyundailogics.shupool.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.databinding.ActivitySelectBinding

class SelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.driverButton.setOnClickListener {
            val intent = Intent(this, CarpoolCreateActivity::class.java)
            startActivity(intent)
        }

        binding.crewButton.setOnClickListener {
            val intent = Intent(this, CarpoolJoinActivity::class.java)
            startActivity(intent)
        }

//        setContentView(R.layout.activity_select)
//
//        var actionBar: ActionBar?
//        val driverButton: AppCompatButton by lazy {
//            findViewById(R.id.driverButton)
//        }
//
//        val crewButton: AppCompatButton by lazy {
//            findViewById(R.id.crewButton)
//        }
//
//        val street: ImageView by lazy {
//            findViewById(R.id.street)
//        }
//
//        street.bringToFront()
//
//
//        actionBar = supportActionBar
//        actionBar?.hide()
    }
}