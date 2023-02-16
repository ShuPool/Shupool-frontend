package com.hyundailogics.shupool.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.hyundailogics.shupool.databinding.FragmentFindLocationMarkerBinding
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KATECToWGS84
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_X
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_Y
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.KNMapMarker
import kotlin.math.roundToInt
import kotlin.random.Random

class FragmentFindLocationMarker() : FragmentBaseMap() {
    lateinit var binding: FragmentFindLocationMarkerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFindLocationMarkerBinding.inflate(inflater)
        initMapView(binding.mapView)
        return binding.root
    }


    private val basicLocationMarkerPriority = 10

    private val basicLocationMarkerId = 0

    private val markerMap = hashMapOf<Int, KNMapMarker>()
    private val rand = Random(2000)

    private var withCamera = false

    private fun initMapView(mapView: KNMapView) {
        KNSDK.makeMapViewWithFrame(mapView) { error ->
            if (error != null) {
                Toast.makeText(context, "맵 초기화 작업이 실패하였습니다. \n[${error.code}] : ${error.msg}",
                    Toast.LENGTH_LONG).show()
                return@makeMapViewWithFrame
            }

            this@FragmentFindLocationMarker.mapView = mapView
            val lastPos = KNSDK.sharedGpsManager()?.lastValidGpsData?.pos ?: FloatPoint(
                KN_DEFAULT_POS_X.toFloat(), KN_DEFAULT_POS_Y.toFloat())
            mapView.moveCamera(KNMapCameraUpdate.targetTo(lastPos).zoomTo(2.5f), false)

        }
    }

    //[좌표 표시]
    private fun showWGS84() {
        activity?.apply {
            mapView.getMapToCenter().let {pos ->
                val point = KATECToWGS84(pos.x.roundToInt(),pos.y.roundToInt())
                val alerts = AlertDialog.Builder(this)
                    .setTitle("현재 위치")
                    .setMessage("longitude: ${point.x}\nlatitude: ${point.y}")
                alerts.setCancelable(true)
                alerts.setNegativeButton("close")
                { dialog, _ ->
                    dialog.dismiss()
                }
                alerts.create().show()
            }
        }
    }

    // MARK: - [마커 추가]
    private fun addMarker() {
        KNMapMarker(mapView.coordinate).apply {
            tag = basicLocationMarkerId
            priority = basicLocationMarkerPriority
//            setVisibleRange(.1f, 30f)
            markerMap[tag] = this
            mapView.addMarker(this)
        }
    }

    // MARK: - [마커 제거]
    private fun removeMarker() {
        mapView.removeMarkersAll()
    }

    // MARK: - [마커 이동]
    private fun moveCoordinateMarker() {
        markerMap[basicLocationMarkerId]?.apply {
            val x = rand.nextInt(-50,50)
            val y = rand.nextInt(-50,50)
            animate(FloatPoint(this.coordinate.x + x, this.coordinate.y + y), 1000L)
        }
    }

    // MARK: - [유저 로케이션 이동]
    private fun moveUserLocation() {
        mapView.userLocation?.apply {
            isVisible = true
            val pos = FloatPoint(mapView.coordinate.x + rand.nextInt(-50, 50), mapView.coordinate.y + rand.nextInt(-50, 50))
            val angles = (angle + rand.nextInt(0, 50)) % 360
            if (withCamera) {
                mapView.animateCamera(KNMapCameraUpdate.targetTo(pos).bearingTo(angles),500L, true)
            } else {
                animate(pos, angles)
            }
            withCamera = !withCamera
        }
    }
}