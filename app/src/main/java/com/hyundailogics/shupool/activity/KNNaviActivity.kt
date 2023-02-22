package com.hyundailogics.shupool.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import com.hyundailogics.shupool.KNSampleService
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.application.GlobalApplication
import com.hyundailogics.shupool.carTypeWithIdx
import com.hyundailogics.shupool.fuelTypeWithIdx
import com.kakaomobility.knsdk.*
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.guidance.knguidance.*
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.component.MapViewCameraMode
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kakaomobility.knsdk.ui.view.KNNaviView_GuideStateDelegate
import com.kakaomobility.knsdk.ui.view.KNNaviView_StateDelegate

class KNNaviActivity : BaseActivity(),
    KNNaviView_StateDelegate, KNNaviView_GuideStateDelegate,
    KNGuidance_GuideStateDelegate, KNGuidance_LocationGuideDelegate, KNGuidance_RouteGuideDelegate,
    KNGuidance_SafetyGuideDelegate, KNGuidance_VoiceGuideDelegate, KNGuidance_CitsGuideDelegate {

    private lateinit var naviView : KNNaviView

    //현재 주행상태
    private var guidestate = KNGuideState.KNGuideState_Init

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navi)
        initialize()

        naviView = findViewById(R.id.navi_view)

        naviView.stateDelegate = this@KNNaviActivity
        naviView.guideStateDelegate = this@KNNaviActivity
        naviView.useDarkMode = if (intent.hasExtra("useDarkMode")) {
            intent.getBooleanExtra("useDarkMode", GlobalApplication.prefs.mapUseDarkMode())
        } else {
            GlobalApplication.prefs.mapUseDarkMode()
        }

        naviView.mapViewMode = MapViewCameraMode.Bird
        naviView.sndVolume = GlobalApplication.prefs.getSndVolume()
        naviView.fuelType = fuelTypeWithIdx(GlobalApplication.prefs.getFuelType())
        naviView.carType = carTypeWithIdx(GlobalApplication.prefs.getCarType())

        val avoidOption = intent?.getIntExtra("avoidOption", 0)
        val routeOption = intent?.getSerializableExtra("routeOption") as KNRoutePriority?
        val pri_route = intent?.getBooleanExtra("priRoute", true)
        val key = intent?.getStringExtra("tripKey")

        var trip: KNTrip? = null
        if (!TextUtils.isEmpty(key)) {
            trip = GlobalApplication.instance.getDataHolder(key)
        }

        if (trip != null) {
            GlobalApplication.knsdk.sharedGuidance()?.apply {
                //  guidance delegate 등록
                guideStateDelegate = this@KNNaviActivity
                locationGuideDelegate = this@KNNaviActivity
                routeGuideDelegate = this@KNNaviActivity
                safetyGuideDelegate = this@KNNaviActivity
                voiceGuideDelegate = this@KNNaviActivity
                citsGuideDelegate = this@KNNaviActivity

                naviView.initWithGuidance(
                    this,
                    trip,
                    routeOption!!,
                    avoidOption!!
                )

                naviView.post {
                    run {
                        if (pri_route == false) {
                            changeRoute()
                        }
                    }
                }
            }
        } else {
            GlobalApplication.knsdk.sharedGuidance()?.apply {
                //  guidance delegate 등록
                guideStateDelegate = this@KNNaviActivity
                locationGuideDelegate = this@KNNaviActivity
                routeGuideDelegate = this@KNNaviActivity
                safetyGuideDelegate = this@KNNaviActivity
                voiceGuideDelegate = this@KNNaviActivity
                citsGuideDelegate = this@KNNaviActivity

                naviView.initWithGuidance(this, null, KNRoutePriority.KNRoutePriority_Recommand, KNRouteAvoidOption.KNRouteAvoidOption_None.value)
            }
        }

        startForegroundService(this, "길안내 주행중", "빠르고 즐거운 운전, 카카오내비")
    }

    override fun onResume() {
        super.onResume()

        GlobalApplication.knsdk.handleWillEnterForeground()
    }

    override fun onPause() {
        super.onPause()

        GlobalApplication.knsdk.handleDidEnterBackground()
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForegroundService(this)
        KNSDK.sharedGuidance()?.stop()
    }

    override fun initialize() {
        setStatusBarColor(Color.TRANSPARENT)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

//    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        return
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNNaviView_StateDelegate
    //  ===========================================================================================================================

    override fun naviViewDidUpdateStatusBarColor(aColor: Int) {
        setStatusBarColor(aColor)
    }

    override fun naviViewDidUpdateMapCameraMode(aCameraMode: MapViewCameraMode) {}

    override fun naviViewDidUpdateUseDarkMode(aMode: Boolean) {}

    override fun naviViewDidUpdateSndVolume(aVolume: Float) {
        GlobalApplication.prefs.setSndVolume(aVolume)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNNaviView_GuideStateDelegate
    //  ===========================================================================================================================

    override fun naviViewGuideEnded() {
        naviView.mapComponent.mapView.onPause()
        finish()
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_GuideStateDelegate
    //  ===========================================================================================================================

    override fun guidanceGuideStarted(aGuidance: KNGuidance) {
        naviView.guidanceGuideStarted(aGuidance)
    }

    override fun guidanceCheckingRouteChange(aGuidance: KNGuidance) {
        naviView.guidanceCheckingRouteChange(aGuidance)
    }

    override fun guidanceRouteUnchanged(aGuidance: KNGuidance) {
        naviView.guidanceRouteUnchanged(aGuidance)
    }

    override fun guidanceRouteUnchangedWithError(aGuidnace: KNGuidance, aError: KNError) {
        naviView.guidanceRouteUnchangedWithError(aGuidnace, aError)
    }

    override fun guidanceOutOfRoute(aGuidance: KNGuidance) {
        naviView.guidanceOutOfRoute(aGuidance)
    }

    override fun guidanceRouteChanged(aGuidance: KNGuidance) {
        naviView.guidanceRouteChanged(aGuidance)
    }

    override fun guidanceGuideEnded(aGuidance: KNGuidance) {
        naviView.guidanceGuideEnded(aGuidance)
    }

    override fun guidanceDidUpdateRoutes(aGuidance: KNGuidance, aRoutes: List<KNRoute>, aMultiRouteInfo: KNMultiRouteInfo?) {
        naviView.guidanceDidUpdateRoutes(aGuidance, aRoutes, aMultiRouteInfo)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_LocationGuideDelegate
    //  ===========================================================================================================================

    override fun guidanceDidUpdateLocation(aGuidance: KNGuidance, aLocationGuide: KNGuide_Location) {
        naviView.guidanceDidUpdateLocation(aGuidance, aLocationGuide)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_RouteGuideDelegate
    //  ===========================================================================================================================

    override fun guidanceDidUpdateRouteGuide(aGuidance: KNGuidance, aRouteGuide: KNGuide_Route) {
        naviView.guidanceDidUpdateRouteGuide(aGuidance, aRouteGuide)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_SafetyGuideDelegate
    //  ===========================================================================================================================

    override fun naviViewGuideState(state: KNGuideState) {
        if (guidestate != state) {
            guidestate = state
        }
    }


    override fun guidanceDidUpdateSafetyGuide(aGuidance: KNGuidance, aSafetyGuide: KNGuide_Safety?) {
        naviView.guidanceDidUpdateSafetyGuide(aGuidance, aSafetyGuide)
    }

    override fun guidanceDidUpdateAroundSafeties(aGuidance: KNGuidance, aSafeties: List<KNSafety>?) {
        naviView.guidanceDidUpdateAroundSafeties(aGuidance, aSafeties)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------


    //  KNGuidance_VoiceGuideDelegate
    //  ===========================================================================================================================
    override fun shouldPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice, aNewData: MutableList<ByteArray>): Boolean {
        return naviView.shouldPlayVoiceGuide(aGuidance, aVoiceGuide, aNewData)
    }

    override fun willPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        naviView.willPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun didFinishPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        naviView.didFinishPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_CitsGuideDelegate
    //  ===========================================================================================================================

    override fun didUpdateCitsGuide(aGuidance: KNGuidance, aCitsGuide: KNGuide_Cits) {
        naviView.didUpdateCitsGuide(aGuidance, aCitsGuide)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  Service
    //  ===========================================================================================================================

    private fun startForegroundService(context : Context, title : String, content : String) {
        val bundle = Bundle().apply {
            putString("title", title)
            putString("content", content)
        }

        val intent = Intent(context, KNSampleService::class.java).apply {
            putExtra(KNSampleService.EXTRA_BUNDLE, bundle)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopForegroundService(context : Context) {
        context.stopService(Intent(context, KNSampleService::class.java))
    }

    //  ---------------------------------------------------------------------------------------------------------------------------
}