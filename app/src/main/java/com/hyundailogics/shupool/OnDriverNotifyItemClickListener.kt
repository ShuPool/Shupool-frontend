package com.hyundailogics.shupool

import android.view.View
import com.hyundailogics.shupool.adapter.DriverNotifyAdapter

interface OnDriverNotifyItemClickListener {

    fun onItemClick(holder: DriverNotifyAdapter.ViewHolder?, view: View?, position: Int)

}