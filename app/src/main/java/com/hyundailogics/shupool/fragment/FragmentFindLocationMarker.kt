package com.hyundailogics.shupool.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.hyundailogics.shupool.BuildConfig
import com.hyundailogics.shupool.apitest.KakaoAPI
import com.hyundailogics.shupool.apitest.ResultAddress
import com.hyundailogics.shupool.application.GlobalApplication
import com.hyundailogics.shupool.databinding.FragmentFindLocationMarkerBinding
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KATECToWGS84
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_X
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_Y
import com.kakaomobility.knsdk.common.util.DoublePoint
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.common.util.IntPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.knmapview.idl.KNMapViewEventListener
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.KNMapMarker
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt
import kotlin.random.Random


class FragmentFindLocationMarker() : FragmentBaseMap(), KNMapViewEventListener {

    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = BuildConfig.REST_API_KEY
    // REST API 키
    }

    lateinit var binding: FragmentFindLocationMarkerBinding
    @SuppressLint("ClickableViewAccessibility")
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

            binding.mapView.mapViewEventListener = this@FragmentFindLocationMarker
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

    override fun onPanningEnded(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {
        val coord = mapView!!.coordinate
        val wgs84: DoublePoint =
            GlobalApplication.knsdk.convertKATECToWGS84(coord.x.roundToInt(), coord.y.roundToInt())
        val map_x = wgs84.x.toString()
        val map_y = wgs84.y.toString()

        searchAddress(map_x, map_y)
    }

    private fun searchAddress(x: String, y: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)
        val call = api.getSearchAddress(API_KEY, x, y)

        call.enqueue(object: Callback<ResultAddress> {
            override fun onResponse(
                call: Call<ResultAddress>,
                response: Response<ResultAddress>
            ) {
                val documents = response.body()?.documents
                val address = documents?.component1()?.road_address?.address_name

                binding.button.text = address
                Log.d("로그", ""+address)
            }
            override fun onFailure(call: Call<ResultAddress>, t: Throwable) {
                // 통신 실패
                Log.w("MainActivity", "통신 실패: ${t.message}")
            }
        })
    }

    override fun onSingleTapped(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onDoubleTapped(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onLongPressed(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onPanningStarted(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onRouteSelected(route: KNRoute, index: Int) {}

    override fun onPanningChanging(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onZoomingStarted(mapView: KNMapView?, screenPoint: IntPoint, zoom: Float) {}

    override fun onZoomingChanging(mapView: KNMapView?, screenPoint: IntPoint, zoom: Float) {}

    override fun onZoomingEnded(mapView: KNMapView?, screenPoint: IntPoint, zoom: Float) {}

    override fun onBearingStarted(mapView: KNMapView?, screenPoint: IntPoint, bearing: Float) {}

    override fun onBearingChanging(mapView: KNMapView?, screenPoint: IntPoint, bearing: Float) {}

    override fun onBearingEnded(mapView: KNMapView?, screenPoint: IntPoint, bearing: Float) {}

    override fun onTiltStarted(mapView: KNMapView?, screenPoint: IntPoint, tilt: Float) {}

    override fun onTiltChanging(mapView: KNMapView?, screenPoint: IntPoint, tilt: Float) {}

    override fun onTiltEnded(mapView: KNMapView?, screenPoint: IntPoint, tilt: Float) {}

    override fun onCameraAnimationEnded(mapView: KNMapView?, cameraUpdate: KNMapCameraUpdate?) {}

    override fun onCameraAnimationCanceled(mapView: KNMapView?, cameraUpdate: KNMapCameraUpdate?) {}

    override fun onUserLocationAnimationEnded(mapView: KNMapView?) {}

    override fun onPOISelected(poiId: Long, poiName: Array<String?>?, coordinate: FloatPoint) {}

}