package com.hyundailogics.shupool.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.hyundailogics.shupool.R

class SelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        var actionBar: ActionBar?
        val driverButton: AppCompatButton by lazy {
            findViewById(R.id.driverButton)
        }

        val crewButton: AppCompatButton by lazy {
            findViewById(R.id.crewButton)
        }

        val street: ImageView by lazy {
            findViewById(R.id.street)
        }

        street.bringToFront();


        actionBar = supportActionBar
        actionBar?.hide()
    }
}