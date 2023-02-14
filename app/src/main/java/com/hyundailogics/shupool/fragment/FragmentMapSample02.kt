package com.hyundailogics.shupool.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.hyundailogics.shupool.databinding.FragmentMapSample02Binding
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.KNMapMarker
import kotlin.random.Random

class FragmentMapSample02 : FragmentBaseMap() {
    lateinit var binding: FragmentMapSample02Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapSample02Binding.inflate(inflater)
        initMapView(binding.mapView)
        initBtnView(binding)
        return binding.root
    }

    private val goneTimer = object : CountDownTimer(125L, 25L) {
        var count = 0
        override fun onTick(millisUntilFinished: Long) {
            when (count) {
                0 -> {
                    binding.locationRow04.visibility = View.GONE
                }
                1 -> {
                    binding.locationRow03.visibility = View.GONE
                }
                2 -> {
                    binding.locationRow02.visibility = View.GONE
                }
                3 -> {
                    binding.locationRow01.visibility = View.GONE
                }
            }
            count++
        }

        override fun onFinish() {
            binding.btn00.text ="▼"
            binding.locationContainer.visibility = View.GONE
            count = 0
        }
    }

    private val visibleTimer = object : CountDownTimer(125L, 25L) {
        var count = 0
        override fun onTick(millisUntilFinished: Long) {
            when (count) {
                0 -> {
                    binding.locationContainer.visibility = View.VISIBLE
                }
                1 -> {
                    binding.locationRow01.visibility = View.VISIBLE
                }
                2 -> {
                    binding.locationRow02.visibility = View.VISIBLE
                }
                3 -> {
                    binding.locationRow03.visibility = View.VISIBLE
                }
            }
            count++
        }

        override fun onFinish() {
            binding.btn00.text ="-"
            binding.locationRow04.visibility = View.VISIBLE
            count = 0
        }
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

            this@FragmentMapSample02.mapView = mapView
            mapView.moveCamera(KNMapCameraUpdate.targetTo(FloatPoint(321408f, 532832f)), false)
        }
    }

    private fun initBtnView(binding: FragmentMapSample02Binding) {
        binding.btn00.setOnClickListener {
            if (binding.locationContainer.visibility == View.VISIBLE) {
                goneTimer.start()
            } else {
                visibleTimer.start()
            }
        }

        binding.btn02.setOnClickListener {
            removeMarker()
        }

        binding.btn03.setOnClickListener {
            addMarker()
        }

        binding.btn04.setOnClickListener {
            moveCoordinateMarker()
        }

        binding.btn08.setOnClickListener {
            moveUserLocation()
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