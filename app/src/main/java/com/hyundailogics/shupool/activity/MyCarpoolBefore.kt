package com.hyundailogics.shupool.activity

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.hyundailogics.shupool.R

class MyCarpoolBefore : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        var actionBar: ActionBar?

        actionBar = supportActionBar
        actionBar?.hide()
    }
}