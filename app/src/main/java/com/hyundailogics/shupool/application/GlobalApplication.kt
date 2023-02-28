package com.hyundailogics.shupool.application

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.hyundailogics.shupool.utils.PreferenceUtil
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.KNSDKDelegate
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class GlobalApplication : Application() {
    companion object {
        lateinit var instance: GlobalApplication
            private set

        lateinit var knsdk: KNSDK

        lateinit var handler: Handler

        lateinit var prefs: PreferenceUtil

        lateinit var dataHolder: Map<String, Any>
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        prefs = PreferenceUtil(this)

        dataHolder = ConcurrentHashMap()

        knsdk = KNSDK.apply {
            //  파일 경로 : data/data/com.kakaomobility.knsample/files/KNSample
            install(instance, "$filesDir/shupool")
            delegate = object : KNSDKDelegate {
                override fun knsdkFoundUnfinishedTrip(
                    aTrip: KNTrip,
                    aPriority: KNRoutePriority,
                    aAvoidOptions: Int
                ) {
                }

                override fun knsdkNeedsLocationAuthorization() {
                }
            }
        }

        handler = Handler(Looper.getMainLooper())
    }

    fun putDataHolder(data: KNTrip): String? {
        (dataHolder as ConcurrentHashMap).clear()
        val dataHolderId = UUID.randomUUID().toString()
        (dataHolder as ConcurrentHashMap).put(dataHolderId, data)
        return dataHolderId
    }

    fun getDataHolder(key: String?): KNTrip? {
        val KNTrip = (dataHolder.get(key) as KNTrip)
        (dataHolder as ConcurrentHashMap).clear()
        return KNTrip
    }
}