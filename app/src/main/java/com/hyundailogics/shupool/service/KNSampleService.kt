package com.hyundailogics.shupool

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.hyundailogics.shupool.activity.KNNaviActivity

/**
 * 메모리 유지를 위한 Foreground Service
 * 주행이 시작되면 Notification과 함께 실행 되며 주행이 종료되거나 앱이 종료되면 같이 종료 한다.
 *
 */

class KNSampleService : Service() {
    companion object {
        const val NOTIFICATION_ID = 1000
        const val CHANNEL_ID = "guidance_notification_channel"
        const val EXTRA_BUNDLE = "bundle"
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel (
                CHANNEL_ID,
                "주행  정보",
                NotificationManager.IMPORTANCE_LOW
            )

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onStartCommand(intent : Intent, flags : Int, startId : Int) : Int {
        startForeground(NOTIFICATION_ID, createNotification(intent))
        return Service.START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent : Intent) {
        super.onTaskRemoved(rootIntent)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onDestroy()
    }

    override fun onBind(intent : Intent) : IBinder? {
        return null
    }

    private fun createNotification(intent: Intent?): Notification {
        val title: String?
        val content: String?
        if (intent != null) {
            val bundle = intent.getBundleExtra("bundle")
            title = bundle?.getString("title")
            content = bundle?.getString("content")
        } else {
            title = "길안내 주행 중"
            content = "빠르고 즐거운 운전, 카카오내비"
        }

        val resultIntent = Intent(this, KNNaviActivity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            2,
            resultIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        // TargetSDK 27 대응. Channel ID 적용된 notification 생성.
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.icon_notify)
            .setLargeIcon(icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}