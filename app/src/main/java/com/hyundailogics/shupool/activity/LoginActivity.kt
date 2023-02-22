package com.hyundailogics.shupool.activity

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.hyundailogics.shupool.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var actionBar: ActionBar?

        actionBar = supportActionBar
        actionBar?.hide()
    }
}