package com.hyundailogics.shupool.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.hyundailogics.shupool.BuildConfig.*
import com.hyundailogics.shupool.R
import com.hyundailogics.shupool.application.GlobalApplication
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val PERMISSION_PHONE_STATE_REQUEST_CODE = 1000
const val PERMISSION_GPS_REQUEST_CODE = 1001

class IntroActivity : BaseActivity() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        if (!isTaskRoot()) {
            var intent = getIntent()
            var intentAction = intent.action
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish()
                return
            }
        }

        initialize()
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }

    override fun initialize() {
        setStatusBarColor(getColor(applicationContext, R.color.bg_yellow))

        checkPermission()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED -> {
                    makeRequestPermissionGPS()
                }
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED -> {
                    makeRequestPermissionPhoneState()
                }
                else -> {
                    sdkInit()
                }
            }
        } else {
            sdkInit()
        }
    }

    private fun sdkInit() {
        scope.launch {
            val language: KNLanguageType = if (GlobalApplication.prefs.mapLanguage()) {
                KNLanguageType.KNLanguageType_KOREAN
            } else {
                KNLanguageType.KNLanguageType_ENGLISH
            }

            GlobalApplication.knsdk.apply {
                initializeWithAppKey(REAL_APP_KEY, VERSION_NAME, GlobalApplication.prefs.getUserId(), language, aCompletion = {
                    if (it != null) {
                        android.util.Log.e("ABASDBASDB", "failed ${it.code}")
                        when (it.code) {
                            KNError_Code_C302 -> {
                                makeRequestPermissionGPS()
                            }
                            else -> {
                                appFinished()
                            }
                        }
                    } else {
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    }
                })
            }
        }
    }

    private fun makeRequestPermissionPhoneState() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Permission to access the microphone is required for this app to Phone State")
                .setTitle("Permission required")

            builder.setPositiveButton("OK") { _, _->
                makeRequestPermission(arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSION_PHONE_STATE_REQUEST_CODE)
            }

            val dialog = builder.create()
            dialog.show()
        } else {
            makeRequestPermission(arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSION_PHONE_STATE_REQUEST_CODE)
        }
    }

    private fun makeRequestPermissionGPS() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Permission to access the microphone is required for this app to GPS")
                    .setTitle("Permission required")

            builder.setPositiveButton("OK") { _, _->
                makeRequestPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_GPS_REQUEST_CODE)
            }

            val dialog = builder.create()
            dialog.show()
        } else {
            makeRequestPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_GPS_REQUEST_CODE)
        }
    }

    private fun makeRequestPermission(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_PHONE_STATE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                } else {
                    checkPermission()
                }
            }
            PERMISSION_GPS_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                } else {
                    checkPermission()
                }
            }
        }
    }
}