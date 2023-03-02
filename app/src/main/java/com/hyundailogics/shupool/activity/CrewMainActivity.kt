package com.hyundailogics.shupool.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hyundailogics.shupool.databinding.FragmentGrouplistBinding

class CrewMainActivity : AppCompatActivity() {
    private lateinit var binding: FragmentGrouplistBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentGrouplistBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}