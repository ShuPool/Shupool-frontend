package com.hyundailogics.shupool.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.application.GlobalApplication
import com.hyundailogics.shupool.databinding.ActivityFullRouteBinding
import com.hyundailogics.shupool.utils.EventObserver
import com.kakaomobility.knsample.viewmodel.FullRouteViewModel
import com.kakaomobility.knsdk.*
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.common.util.IntPoint
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCoordinateRegion
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.knmapview.idl.KNMapViewEventListener
import com.kakaomobility.knsdk.map.knmapview.idl.KNMarkerEventListener
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.KNMapMarker
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.KNMapRouteTheme
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.KNMapTheme
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.manager.KNMapObjectFactory
import kotlinx.coroutines.ObsoleteCoroutinesApi

class FullRouteActivity : BaseActivity(), KNMapViewEventListener, KNMarkerEventListener {

    private lateinit var binding: ActivityFullRouteBinding
    private lateinit var fullRouteViewModel : FullRouteViewModel

    private val routeCustomObjectList = arrayListOf<KNMapMarker>()

    private var poiMarker: KNMapMarker? = null
    private var knTrip: KNTrip? = null
    private var knRoutes: MutableList<KNRoute>? = null
    private var knDestination: KNPOI? = null
    private var knWayPointList: MutableList<KNPOI>? = null
    private var knAvoidOption: Int = 0
    private var knRouteOption: KNRoutePriority = KNRoutePriority.KNRoutePriority_Recommand

    private var isDisplayType = true
    private var isNight = true
    private var isTraffic = false
    private var showPoi = true
    private var showBuilding = false
    private var isLangType = true

//    @ObsoleteCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBindingComponent()
        initialize()

        if (intent == null) {
            finish()
        }

        knDestination = intent.getParcelableExtra("goal") as? KNPOI
        knAvoidOption = intent.getIntExtra("avoidOption", 0)
        knRouteOption = (intent.getSerializableExtra("routeOption") as KNRoutePriority?) ?: KNRoutePriority.KNRoutePriority_Recommand
        knWayPointList = null

        GlobalApplication.prefs.apply {
            isDisplayType = mapDisplayType()
            isNight = mapUseDarkMode()
            isTraffic = mapTrafficMode()
            isLangType = mapLanguage()
            showPoi = showPoi()
            showBuilding = showBuilding()
        }

        binding.mapview.apply {
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

        reqRoute(knDestination, knWayPointList, knRouteOption)
    }

    override fun initialize() {
        setStatusBarColor(Color.parseColor("#008577"))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun initMapView() {
        binding.mapview.apply {
            usePanningGesture = true
            useZoomGesture = true

            GlobalApplication.knsdk.bindingMapView(this, this.mapTheme) {
                if (it == null) {
                    knMarkerEventListener = this@FullRouteActivity
                    mapViewEventListener = this@FullRouteActivity
                }
            }
        }
    }

    fun setRouteCustomObjects() {
        //  도착
        routeCustomObjectList.add(
            KNMapObjectFactory.createRoutePinMarker(
                this,
                KNMapObjectFactory.KNRouteObjType.ROUTE_END,
                FloatPoint(knTrip!!.goal.pos.x.toFloat(), knTrip!!.goal.pos.y.toFloat()),
                knTrip!!.goal))

        //  출발
        routeCustomObjectList.add(
            KNMapObjectFactory.createRoutePinMarker(
                this,
                KNMapObjectFactory.KNRouteObjType.ROUTE_START,
                FloatPoint(knTrip!!.start.pos.x.toFloat(), knTrip!!.start.pos.y.toFloat()),
                knTrip!!.start))

        //  경유
        if (knWayPointList != null && knWayPointList!!.isNotEmpty()) {
            if (knWayPointList!!.size > 1) {
                knWayPointList!!.forEachIndexed { idx, location ->
                    routeCustomObjectList.add(
                        KNMapObjectFactory.createViaPinMarker(
                            this,
                            "${idx + 1}",
                            FloatPoint(location.pos.x.toFloat(), location.pos.y.toFloat()),
                            location
                        )
                    )
                }
            } else {
                routeCustomObjectList.add(
                    KNMapObjectFactory.createRoutePinMarker(
                        this,
                        KNMapObjectFactory.KNRouteObjType.ROUTE_VIA,
                        FloatPoint(knWayPointList!![0].pos.x.toFloat(), knWayPointList!![0].pos.y.toFloat()),
                        knWayPointList!![0]
                    )
                )
            }
        }

        binding.mapview.addMarkers(routeCustomObjectList)
    }

    private fun initBindingComponent() {
        binding = DataBindingUtil.setContentView<ActivityFullRouteBinding>(this, R.layout.activity_full_route).apply {
            lifecycleOwner = this@FullRouteActivity
        }

        fullRouteViewModel = ViewModelProvider(this).get(FullRouteViewModel::class.java)
        binding.viewmodel = fullRouteViewModel

        fullRouteViewModel.addEvent.observe(this, EventObserver {
            wayPointAdd()
        })

        fullRouteViewModel.removeEvent.observe(this, EventObserver {pos ->
            wayPointRemove(pos)
        })

        fullRouteViewModel.prevEvent.observe(this, EventObserver {
            onBackPressed()
        })

        fullRouteViewModel.selectGuideEvent.observe(this, EventObserver {priRoute ->
            guideSelect(priRoute)
        })

        fullRouteViewModel.guideStartEvent.observe(this, EventObserver {priRoute ->
            guideStart(priRoute)
        })

        initMapView()

        val layoutParams = binding.topLayout.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topMargin = getStatusbarHeight()
        binding.topLayout.layoutParams = layoutParams
    }

    private fun getStatusbarHeight(): Int {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) statusBarHeight = resources.getDimensionPixelSize(resourceId)
        return statusBarHeight
    }

    private fun reqRoute(aDestination: KNPOI?, aWayPoints: MutableList<KNPOI>?, aRouteOption: KNRoutePriority) {
        showProgress()
        route(aDestination, aWayPoints, knAvoidOption, aRouteOption) { aError, aTrip, aRoutes ->
            if (aError != null) {
                finish()

            } else {
                knTrip = aTrip
                knRoutes = aRoutes
                knAvoidOption = knRoutes?.get(0)!!.avoidOptions

                if (knTrip != null && knRoutes != null) {
                    fullRouteViewModel.setFullRouteData(knTrip!!, knRoutes!!)
                    routeDraw(knRoutes!!)

                } else {
                    finish()
                }

                dismissProgress()
            }
        }
    }

    private fun dpToPx(aContext: Context, dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, aContext.resources.displayMetrics).toInt()

    private fun routeDraw(aRoute: MutableList<KNRoute>, fixScale: Boolean = false) {
        with(binding.mapview) {
            val isNight = mapTheme == KNMapTheme.driveNight()
            routeTheme = if (isNight) KNMapRouteTheme.trafficNight() else KNMapRouteTheme.trafficDay()
            setRoutes(aRoute)

            if (!fixScale) {
                val marginw = dpToPx(context, 50f)
                val marginh = dpToPx(context, 70f)

                moveCamera(
                    KNMapCameraUpdate()
                        .bearingTo(0f)
                        .tiltTo(0f)
                        .fitTo(
                            KNMapCoordinateRegion.initWithRoute(aRoute),
                            RectF(
                                0f + marginw,
                                0f + marginh,
                                binding.mapview.width.toFloat() - marginw,
                                binding.mapview.height.toFloat() - marginh
                            )
                        ),
                    false
                )
            }

            setRouteCustomObjects()

            binding.mapview.visibility = View.VISIBLE
        }
    }

    private fun wayPointAdd() {
        binding.mapview.onPause()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("mode", WayPointSearch)
        startForResult.launch(intent)
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            binding.mapview.onResume()

            when(result.resultCode) {
                Activity.RESULT_OK -> {
                    val intent = result.data as Intent
                    val viaPt = intent.getParcelableExtra("wayPoint") as? KNPOI
                    if (viaPt != null) {

                        if (knTrip?.start?.pos?.x == viaPt.pos.x && knTrip?.start?.pos?.y == viaPt.pos.y) {
                            return@registerForActivityResult
                        }

                        if (knTrip?.goal?.pos?.x == viaPt.pos.x && knTrip?.goal?.pos?.y == viaPt.pos.y) {
                            return@registerForActivityResult
                        }

                        if (knWayPointList == null) {
                            knWayPointList = mutableListOf()
                        }

                        //경유지 중복체크
                        for (via in knWayPointList!!) {
                            if (via.equals(viaPt)) {
                                return@registerForActivityResult
                            }
                        }

                        knWayPointList?.add(viaPt)
                        reqRoute(knDestination, knWayPointList, knRouteOption)
                    }
                }
            }
        }

    private fun wayPointRemove(pos: Int) {
        binding.mapview.removeMarkersAll()
        routeCustomObjectList.clear()
        knWayPointList?.removeAt(pos)
        reqRoute(knDestination, knWayPointList, knRouteOption)
    }

    private fun guideSelect(priRoute: Boolean, fixScale: Boolean = false) {
        runOnUiThread {
            knRoutes?.let {
                var route = mutableListOf<KNRoute>()

                if (priRoute) {
                    route.addAll(it)
                    binding.bottomLayout.setBackgroundColor(0xff008577.toInt())
                    binding.bottomLayout2.setBackgroundColor(0xffffffff.toInt())
                } else {
                    route.addAll(it.asReversed())
                    binding.bottomLayout.setBackgroundColor(0xffffffff.toInt())
                    binding.bottomLayout2.setBackgroundColor(0xff008577.toInt())
                }

                routeDraw(route, fixScale)
            }
        }
    }

    private fun guideStart(priRoute: Boolean) {
        val intent = Intent(this, KNNaviActivity::class.java)
        intent.putExtra("tripKey", GlobalApplication.instance.putDataHolder(knTrip!!))
        intent.putExtra("priRoute", priRoute)
        intent.putExtra("avoidOption", knAvoidOption)
        intent.putExtra("routeOption", knRouteOption)
        startActivity(intent)
        setResult(RESULT_OK)
        finish()
    }

//    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        binding.mapview.onPause()
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onSingleTapped(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onDoubleTapped(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onLongPressed(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onPanningStarted(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onPanningChanging(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

    override fun onPanningEnded(mapView: KNMapView?, screenPoint: IntPoint, coordinate: FloatPoint) {}

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

    override fun onPOISelected(poiId: Long, poiName: Array<String?>?, coordinate: FloatPoint) {
        if (poiId == -1L) return
        if (poiMarker == null) {
            poiMarker = KNMapMarker(coordinate).apply {
                setCallOutBubble(this, poiId, poiName)
                binding.mapview.addMarker(this)
            }
        } else {
            poiMarker?.apply {
                binding.mapview.removeMarker(this)
            }
            poiMarker = null

            poiMarker = KNMapMarker(coordinate).apply {
                setCallOutBubble(this, poiId, poiName)
                binding.mapview.addMarker(this)
            }
        }
    }

    override fun onRouteSelected(route: KNRoute, index: Int) {
        guideSelect(knRoutes!!.get(0) == route, true)
    }

    override fun onCalloutBubbleSelected(mapView: KNMapView?, marker: KNMapMarker) {
        if (marker.id == poiMarker?.id) {
            poiMarker?.isVisibleCalloutBubble = false
        }
    }

    override fun onMarkerSelected(mapView: KNMapView?, marker: KNMapMarker) {
        poiMarker?.isVisible = false
        poiMarker?.apply {
            mapView?.removeMarker(this)
        }
        poiMarker = null
    }

    override fun onMarkerAnimateEnded(mapView: KNMapView?, marker: KNMapMarker) {
    }

    private fun setCallOutBubble(poiMarker: KNMapMarker, poiId: Long, poiName: Array<String?>?) {
        fun makePOITitleString(poiName: Array<String?>?): String {
            var title = ""
            val list = poiName?.filterNotNull()
            list?.forEachIndexed { index, it ->
                title+=it
                if (index < list.size - 1) {
                    title += " "
                }
            }
            return if (title.trim().isEmpty()) {"이름없음"} else {title}
        }

        val builder = KNMapMarker.KNMapCalloutBubbleUpdate.Builder()
        poiMarker.isVisibleCalloutBubble = true
        poiMarker.isVisible = true

        requestPoiInfo(
            poiId,
            onResult = {
                poiMarker.callOutBubbleUpdate = builder.setTitle(it)
                    .setSubTitle("$poiId")
                    .setTitleFontColor(Color.RED)
                    .setSubTitleFontColor(Color.MAGENTA)
                    .build()
            }
        )
    }

    private fun requestPoiInfo(poiId: Long, onResult: ((str: String) -> Unit)) {
        KNSDK.reqPoiDetail(arrayListOf(poiId.toString())) { aError, aKNPoiDetailResult ->
            if (aError == null && aKNPoiDetailResult != null && !aKNPoiDetailResult.detailList.isNullOrEmpty()) {
                for (item in aKNPoiDetailResult.detailList!!) {
                    if (item?.info != null) {
                        if (poiId.toString() == item.info!!.poiId) {
                            onResult(item.info!!.name)
                            break
                        }
                    }
                }
            } else {
                onResult("이름없음")
            }
        }
    }
}