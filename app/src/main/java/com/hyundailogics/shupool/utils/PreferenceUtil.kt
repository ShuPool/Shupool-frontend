package com.hyundailogics.shupool.utils

import android.app.Application
import androidx.preference.PreferenceManager
import com.hyundailogics.shupool.R
import kotlin.random.Random

class PreferenceUtil(application: Application) {
    private val context = application.baseContext
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    init {
        getUserId()
    }

    //  userId
    fun getUserId() = getString(context.resources.getString(R.string.KNSample_Settings_user_id), "user_${Random.nextInt(Int.MAX_VALUE)}")
    fun setUserId(value: String) = putString(context.resources.getString(R.string.KNSample_Settings_user_id), value)

    fun getCarType() = getString(context.resources.getString(R.string.KNSample_Settings_Car_Type), "0")
    fun useHipass() = getBoolean(context.resources.getString(R.string.KNSample_Settings_Use_Hipass), true)
    fun getCarWidth() = getString(context.resources.getString(R.string.KNSample_Settings_Car_Width), "-1").toInt()
    fun getCarHeight() = getString(context.resources.getString(R.string.KNSample_Settings_Car_Height), "-1").toInt()
    fun getCarLength() = getString(context.resources.getString(R.string.KNSample_Settings_Car_Length), "-1").toInt()
    fun getCarWeight() = getString(context.resources.getString(R.string.KNSample_Settings_Car_Weight), "-1").toInt()
    fun getFuelType() = getString(context.resources.getString(R.string.KNSample_Settings_Fuel_Type), "0")

    fun mapDisplayType() = getBoolean(context.resources.getString(R.string.KNSample_Settings_Map_Display_Type), true)
    fun mapUseDarkMode() = getBoolean(context.resources.getString(R.string.KNSample_Settings_Map_Use_DarkMode), false)
    fun mapTrafficMode() = getBoolean(context.resources.getString(R.string.KNSample_Settings_Map_Traffic_Mode), false)
    fun mapLanguage() = getBoolean(context.resources.getString(R.string.KNSample_Settings_Map_Language), true)
    fun showPoi() = getBoolean(context.resources.getString(R.string.KNSample_Settings_Show_Poi), true)
    fun showBuilding() = getBoolean(context.resources.getString(R.string.KNSample_Settings_Show_Building), true)

    fun getSndVolume() = getFloat(context.resources.getString(R.string.KNSample_Settings_Sound_Volume), 1f)
    fun setSndVolume(value: Float) = putFloat(context.resources.getString(R.string.KNSample_Settings_Sound_Volume), value)

    private fun getString(aKey: String, defaultValue: String): String {
        var result = defaultValue

        if (prefs.contains(aKey)) {
            result = prefs.getString(aKey, defaultValue)!!
        } else {
            putString(aKey, defaultValue)
        }

        return result
    }

    private fun putString(aKey: String, aValue: String) {
        prefs.edit().apply {
            putString(aKey, aValue)
            apply()
        }
    }

    private fun getBoolean(aKey: String, defaultValue: Boolean): Boolean {
        var result = defaultValue

        if(prefs.contains(aKey)) {
            result = prefs.getBoolean(aKey, defaultValue)
        } else {
            putBoolean(aKey, defaultValue)
        }

        return result
    }

    private fun putBoolean(aKey: String, aValue: Boolean) {
        prefs.edit().apply {
            putBoolean(aKey, aValue)
            apply()
        }
    }

    private fun getInt(aKey: String, defaultValue: Int): Int {
        var result = defaultValue

        if (prefs.contains(aKey)) {
            result = prefs.getInt(aKey, defaultValue)
        } else {
            putInt(aKey, defaultValue)
        }

        return result
    }

    private fun putInt(aKey: String, aValue: Int) {
        prefs.edit().apply {
            putInt(aKey, aValue)
            apply()
        }
    }

    private fun getFloat(aKey: String, defaultValue: Float): Float {
        var result = defaultValue

        if (prefs.contains(aKey)) {
            result = prefs.getFloat(aKey, defaultValue)
        } else {
            putFloat(aKey, defaultValue)
        }

        return result
    }

    private fun putFloat(aKey: String, aValue: Float) {
        prefs.edit().apply {
            putFloat(aKey, aValue)
            apply()
        }
    }
}
