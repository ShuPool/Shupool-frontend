package com.hyundailogics.shupool.fragment

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hyundailogics.shupool.application.GlobalApplication
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.KNMapTheme
import kotlinx.coroutines.ObsoleteCoroutinesApi

open class FragmentBaseMap: Fragment() {
    open lateinit var mapView: KNMapView

    @Suppress("PropertyName")
    protected val KN_MAP_SCALE_TABLE = arrayOf(floatArrayOf(0.6f, 0.6f, 1.0f, 1.51f, 2.1f, 3.01f, 3.5f), floatArrayOf(0.75f, 1.0f, 1.51f, 2.1f, 3.01f, 3.01f, 3.5f))

//    @ObsoleteCoroutinesApi
    override fun onResume() {
        super.onResume()

        applyMapSettings()
    }

    @ObsoleteCoroutinesApi
    private fun applyMapSettings() {
        var isDisplayType: Boolean
        var isNight: Boolean
        var isTraffic: Boolean
        var showPoi: Boolean
        var showBuilding: Boolean
        var isLangType: Boolean

        GlobalApplication.prefs.apply {
            isDisplayType = mapDisplayType()
            isNight = mapUseDarkMode()
            isTraffic = mapTrafficMode()
            isLangType = mapLanguage()
            showPoi = showPoi()
            showBuilding = showBuilding()
        }

        if (this::mapView.isInitialized) {
            mapView.apply {
                mapTheme = if (isNight) {
                    if (isDisplayType) KNMapTheme.driveNight()
                    else KNMapTheme.searchNight()
                } else {
                    if (isDisplayType) KNMapTheme.driveDay()
                    else KNMapTheme.searchDay()
                }

                KNSDK.setLanguageType(if (isLangType) KNLanguageType.KNLanguageType_KOREAN else KNLanguageType.KNLanguageType_ENGLISH)
                isVisibleTraffic = isTraffic
                isVisiblePOI = showPoi
                isVisibleBuilding = showBuilding
            }
        }
    }

    fun makeToastText(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}