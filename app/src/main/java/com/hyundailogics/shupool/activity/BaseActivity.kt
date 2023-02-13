package com.hyundailogics.shupool.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat.*
import androidx.fragment.app.Fragment
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.application.GlobalApplication
import com.hyundailogics.shupool.carTypeWithIdx
import com.hyundailogics.shupool.fuelTypeWithIdx
import com.kakaomobility.knsdk.KNRouteAvoidOption
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_X
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_Y
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.common.util.IntPoint
import com.kakaomobility.knsdk.trip.knrouteconfiguration.KNRouteConfiguration
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.constants.*
import com.kakaomobility.knsdk.ui.utils.getAddressWithReverseGeocodeResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val job = Job()
private val scope = CoroutineScope(Dispatchers.Main + job)

const val DestinationSearch = 0
const val WayPointSearch = 1

open class BaseActivity : AppCompatActivity() {
    private var loadingDlg : ProgressDialog? = null

    open fun initialize() {
        setStatusBarColor(getColor(applicationContext, R.color.colorPrimary))

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    protected fun setStatusBarColor(aId: Int, aIsFullScreen: Boolean = true) {
        if (aIsFullScreen) {
            @Suppress("DEPRECATION")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                window.insetsController?.let{
//                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//                    it.hide(WindowInsets.Type.systemBars())
//                }
//            } else {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            }
        }

        window.statusBarColor = aId

        supportActionBar?.hide()
    }

    protected fun appFinished() {

    }

    protected fun getColor(aContext: Context, id: Int) : Int {
        return aContext.getColor(id)
    }

    open fun addFragment(containerViewId: Int, fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().apply {
            add(containerViewId, fragment, tag)
            commit()
        }
    }

    open fun replaceFragment(containerViewId: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(containerViewId, fragment)
            commit()
        }
    }

    open fun removeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            remove(fragment)
            commit()
        }
    }

    open fun showProgress() {
        if (loadingDlg == null) {
            loadingDlg = ProgressDialog(this)
        }

        loadingDlg?.show()
    }

    open fun dismissProgress() {
        loadingDlg?.dismiss()
    }

    protected fun route(aDestination: KNPOI?, aWayPoints: MutableList<KNPOI>?, aAvoidOption: Int, aRouteOption: KNRoutePriority,
              aCompletion: ((KNError?, KNTrip?, MutableList<KNRoute>?) -> Unit)?) {
        scope.launch {
            //  주행
            if (aDestination != null) {
                val pos = KNSDK.sharedGpsManager()?.recentGpsData?.pos ?: FloatPoint(
                    KN_DEFAULT_POS_X.toFloat(), KN_DEFAULT_POS_Y.toFloat())
//                val pos = FloatPoint(321400f, 532807f)

                KNSDK.reverseGeocodeWithPos(IntPoint(pos)) { aReverseGeocodeError, _, aDoName, aSiGunGuName, aDongName ->
                    val address = if (aReverseGeocodeError != null) {
                        "현위치"
                    } else {
                        getAddressWithReverseGeocodeResult(aDoName, aSiGunGuName, aDongName) ?: "현위치"
                    }

                    val start = KNPOI(address, pos.x.toInt(), pos.y.toInt(), address)
                    val goal = KNPOI(aDestination.name, aDestination.pos.x, aDestination.pos.y, aDestination.address)

                    GlobalApplication.knsdk.makeTripWithStart(
                        start,
                        goal,
                        if (aWayPoints != null && aWayPoints.size > 0) aWayPoints else null
                    ) { aError, aTrip ->
                        if (aError != null) {

                            aCompletion?.invoke(aError, null, null)

                        } else {
                            val routeConfig = KNRouteConfiguration(
                                carTypeWithIdx(GlobalApplication.prefs.getCarType()),
                                fuelTypeWithIdx(GlobalApplication.prefs.getFuelType()),
                                aUseHipass = GlobalApplication.prefs.useHipass(),
                                aCarWidth = GlobalApplication.prefs.getCarWidth(),
                                aCarHeight = GlobalApplication.prefs.getCarHeight(),
                                aCarLength = GlobalApplication.prefs.getCarLength(),
                                aCarWeight = GlobalApplication.prefs.getCarWeight()
                            )
                            aTrip?.setRouteConfig(routeConfig)
                            aTrip?.routeWithPriority(
                                aRouteOption,
                                aAvoidOption
                            ) { aError2, aRoutes ->
                                if (aError2 != null) {
                                    routeErrPopup(
                                        aTrip,
                                        aError2,
                                        aAvoidOption,
                                        aRouteOption,
                                        aCompletion
                                    )
                                } else {
                                    aCompletion?.invoke(null, aTrip, aRoutes)
                                }
                            }
                        }
                    }
                }
            } else {    //  안전운행
                aCompletion?.invoke(null, null, null)
            }
        }
    }

    private fun reRoute(aTrip: KNTrip, aAvoidOption: Int, aRouteOption: KNRoutePriority, aCompletion: ((KNError?, KNTrip?, MutableList<KNRoute>?) -> Unit)?) {
        aTrip.routeWithPriority(
            aRouteOption,
            aAvoidOption
        ) { aError2, aRoutes ->
            if (aError2 != null) {
                routeErrPopup(
                    aTrip,
                    aError2,
                    aAvoidOption,
                    aRouteOption,
                    aCompletion
                )
            } else {
                aCompletion?.invoke(null, aTrip, aRoutes)
            }
        }
    }

    private fun routeErrPopup(aTrip: KNTrip, aError: KNError, aAvoidOption: Int, aRouteOption: KNRoutePriority,
                      aCompletion: ((KNError?, KNTrip?, MutableList<KNRoute>?) -> Unit)?) {
        val tagMsg = when (aError.code) {
            KNRouteError_Code_20410 -> KNRouteError_Msg_20410
            KNRouteError_Code_20411 -> KNRouteError_Msg_20411
            KNRouteError_Code_20412 -> KNRouteError_Msg_20412
            KNRouteError_Code_20413 -> KNRouteError_Msg_20413
            KNRouteError_Code_20414 -> KNRouteError_Msg_20414
            KNRouteError_Code_20415 -> KNRouteError_Msg_20415
            KNRouteError_Code_20010 -> KNRouteError_Msg_20010
            KNRouteError_Code_20011 -> KNRouteError_Msg_20011
            KNRouteError_Code_20012 -> KNRouteError_Msg_20012
            KNRouteError_Code_20013 -> KNRouteError_Msg_20013
            else -> KNRouteError_Msg_Default
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(aError.code)
            setMessage(tagMsg)

            when (aError.code) {
                KNRouteError_Code_20413 -> {
                    setNegativeButton("확인") { _, _ ->
                        aCompletion?.invoke(aError, null, null)
                    }
                    setPositiveButton("자동차 전용도로 해제") { _, _ ->
                        reRoute(aTrip, aAvoidOption xor KNRouteAvoidOption.KNRouteAvoidOption_MotorWay.value, aRouteOption, aCompletion)
                    }
                }
                KNRouteError_Code_20414 -> {
                    setNegativeButton("확인") { _, _ ->
                        aCompletion?.invoke(aError, null, null)
                    }
                    setPositiveButton("무료 도로 해제") { _, _ ->
                        reRoute(aTrip, aAvoidOption xor KNRouteAvoidOption.KNRouteAvoidOption_Fare.value, aRouteOption, aCompletion)
                    }
                }
                KNRouteError_Code_20415 -> {
                    setNegativeButton("확인") { _, _ ->
                        aCompletion?.invoke(aError, null, null)
                    }
                    setPositiveButton("페리 포함") { _, _ ->
                        reRoute(aTrip,aAvoidOption xor KNRouteAvoidOption.KNRouteAvoidOption_Farries.value, aRouteOption, aCompletion)
                    }
                }
                KNRouteError_Code_20011,
                KNRouteError_Code_20012,
                KNRouteError_Code_20013 -> {
                    setNegativeButton("확인") { _, _ ->
                        aCompletion?.invoke(aError, null, null)
                    }
                    setPositiveButton("유고 무시") { _, _ ->
                        reRoute(aTrip,aAvoidOption or KNRouteAvoidOption.KNRouteAvoidOption_RoadEvent.value, aRouteOption, aCompletion)
                    }
                }
                else -> {
                    setPositiveButton("확인") { _, _ ->
                        aCompletion?.invoke(aError, null, null)
                    }
                }
            }
            setCancelable(false)
        }
        val dialog = builder.create()
        dialog.show()
    }

    class ProgressDialog(context: Context) : Dialog(context) {
        init {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.view_loading)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}