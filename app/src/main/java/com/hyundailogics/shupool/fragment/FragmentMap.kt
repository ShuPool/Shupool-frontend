package com.hyundailogics.shupool.fragment

import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.hyundailogics.shupool.databinding.FragmentMapBinding
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.R
import com.kakaomobility.knsdk.common.gps.*
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView

class FragmentMap : FragmentBaseMap(), KNGPSReceiver {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentMapBinding.inflate(inflater)
        initMapView(binding.mapView)
        return binding.root
    }

    private fun initMapView(mapView: KNMapView) {
        KNSDK.bindingMapView(mapView, mapView.mapTheme) { error ->
            if (error != null) {
                Toast.makeText(context, "맵 초기화 작업이 실패하였습니다. \n[${error.code}] : ${error.msg}",Toast.LENGTH_LONG).show()
                return@bindingMapView
            }
            //현재 클라이언트 좌표
            val lastPos = KNSDK.sharedGpsManager()?.lastValidGpsData?.pos ?: FloatPoint(KN_DEFAULT_POS_X.toFloat(), KN_DEFAULT_POS_Y.toFloat())
            //카카오 판교 본사 좌표
            val center = WGS84ToKATEC(127.11019081347423,37.3941851228957)

            //사용자 설정 아이콘을 위한 부분
            val drawable: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.icon_here, null)
            val bitmapDrawable = drawable as BitmapDrawable
            val icon_here = bitmapDrawable.bitmap

            mapView.moveCamera(KNMapCameraUpdate.targetTo(lastPos).zoomTo(2.5f), false)
            mapView.userLocation?.apply {
                icon = icon_here
                isVisible = true
                isVisibleGuideLine = true
                coordinate = lastPos
            }
        }
    }

    override fun didReceiveGpsData(aGpsData: KNGPSData) {
        mapView.apply {
            val orientation = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 1 else 0

            val scale = when (aGpsData.speed) {
                in -1..19 -> KN_MAP_SCALE_TABLE[orientation][1]
                in 20..39 -> KN_MAP_SCALE_TABLE[orientation][2]
                in 40..59 -> KN_MAP_SCALE_TABLE[orientation][3]
                in 60..79 -> KN_MAP_SCALE_TABLE[orientation][4]
                in 80..99 -> KN_MAP_SCALE_TABLE[orientation][5]
                else -> KN_MAP_SCALE_TABLE[orientation][6]
            }
        }
    }
}