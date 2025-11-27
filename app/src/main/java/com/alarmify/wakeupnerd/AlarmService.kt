package com.alarmify.wakeupnerd

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    companion object {
        private const val CHANNEL_ID = "alarm_service_channel"
        private const val NOTIFICATION_ID = 999
        private var wakeLock: PowerManager.WakeLock? = null

        fun startService(context: Context, alarmData: Intent) {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtras(alarmData.extras ?: return)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Acquire wake lock to keep CPU running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "WakeUpNerd::AlarmWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes max
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start foreground service
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Get alarm data
        val alarmId = intent?.getStringExtra("ALARM_ID") ?: ""
        val label = intent?.getStringExtra("ALARM_LABEL") ?: ""
        val ringtone = intent?.getStringExtra("ALARM_RINGTONE") ?: "Alarm clock"
        val vibrate = intent?.getBooleanExtra("ALARM_VIBRATE", true) ?: true
        val volume = intent?.getIntExtra("ALARM_VOLUME", 50) ?: 50

        // Launch AlarmPlayActivity
        val activityIntent = Intent(this, AlarmPlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_RINGTONE", ringtone)
            putExtra("ALARM_VIBRATE", vibrate)
            putExtra("ALARM_VOLUME", volume)
        }

        startActivity(activityIntent)

        // Stop service after launching activity
        stopSelf()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps alarm service running"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm")
            .setContentText("Alarm is ringing...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
