package com.alarmify.wakeupnerd.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    // Permission request codes
    const val REQUEST_CODE_NOTIFICATIONS = 1001
    const val REQUEST_CODE_SYSTEM_ALERT_WINDOW = 1002
    const val REQUEST_CODE_SCHEDULE_EXACT_ALARM = 1003
    const val REQUEST_CODE_POST_NOTIFICATIONS = 1004

    /**
     * Check if all required permissions are granted
     */
    fun checkAllPermissions(context: Context): Boolean {
        return hasNotificationPermission(context) &&
                hasSystemAlertWindowPermission(context) &&
                hasScheduleExactAlarmPermission(context) &&
                hasPostNotificationPermission(context)
    }

    /**
     * Check notification permission
     */
    fun hasNotificationPermission(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }

    /**
     * Check system alert window permission (display over other apps)
     */
    fun hasSystemAlertWindowPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Check schedule exact alarm permission
     */
    fun hasScheduleExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Check POST_NOTIFICATIONS permission (Android 13+)
     */
    fun hasPostNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Request all missing permissions
     */
    fun requestMissingPermissions(activity: Activity) {
        val missingPermissions = mutableListOf<String>()

        // Check POST_NOTIFICATIONS (Android 13+)
        if (!hasPostNotificationPermission(activity)) {
            missingPermissions.add("Notification Permission")
        }

        // Check System Alert Window
        if (!hasSystemAlertWindowPermission(activity)) {
            missingPermissions.add("Display Over Other Apps")
        }

        // Check Schedule Exact Alarm
        if (!hasScheduleExactAlarmPermission(activity)) {
            missingPermissions.add("Schedule Exact Alarms")
        }

        // Check general notifications
        if (!hasNotificationPermission(activity)) {
            missingPermissions.add("Notifications")
        }

        if (missingPermissions.isNotEmpty()) {
            showPermissionDialog(activity, missingPermissions)
        }
    }

    /**
     * Show dialog explaining why permissions are needed
     */
    private fun showPermissionDialog(activity: Activity, missingPermissions: List<String>) {
        val message = buildString {
            append("WakeUpNerd needs the following permissions to work properly:\n\n")
            missingPermissions.forEach { permission ->
                append("• $permission\n")
            }
            append("\nWithout these permissions, alarms may not trigger when the screen is off or the app is in the background.")
        }

        AlertDialog.Builder(activity)
            .setTitle("Permissions Required")
            .setMessage(message)
            .setPositiveButton("Grant Permissions") { dialog, _ ->
                dialog.dismiss()
                requestPermissionsSequentially(activity)
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Request permissions one by one
     */
    private fun requestPermissionsSequentially(activity: Activity) {
        when {
            // 1. POST_NOTIFICATIONS (Android 13+)
            !hasPostNotificationPermission(activity) -> {
                requestPostNotificationPermission(activity)
            }
            // 2. System Alert Window
            !hasSystemAlertWindowPermission(activity) -> {
                requestSystemAlertWindowPermission(activity)
            }
            // 3. Schedule Exact Alarm
            !hasScheduleExactAlarmPermission(activity) -> {
                requestScheduleExactAlarmPermission(activity)
            }
            // 4. General Notifications
            !hasNotificationPermission(activity) -> {
                requestNotificationPermission(activity)
            }
        }
    }

    /**
     * Request POST_NOTIFICATIONS permission
     */
    fun requestPostNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        }
    }

    /**
     * Request System Alert Window permission
     */
    fun requestSystemAlertWindowPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivityForResult(intent, REQUEST_CODE_SYSTEM_ALERT_WINDOW)
        }
    }

    /**
     * Request Schedule Exact Alarm permission
     */
    fun requestScheduleExactAlarmPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            activity.startActivityForResult(intent, REQUEST_CODE_SCHEDULE_EXACT_ALARM)
        }
    }

    /**
     * Request general notification permission
     */
    fun requestNotificationPermission(activity: Activity) {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        } else {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivityForResult(intent, REQUEST_CODE_NOTIFICATIONS)
    }

    /**
     * Show detailed permission explanation dialog
     */
    fun showDetailedPermissionDialog(activity: Activity) {
        val message = """
            WakeUpNerd requires the following permissions:
            
            📢 Notification Permission
            • Allows the app to show alarm notifications
            • Required for alarms to trigger
            
            🔔 Display Over Other Apps
            • Shows alarm screen over lockscreen
            • Ensures you see the alarm even when phone is locked
            
            ⏰ Schedule Exact Alarms
            • Allows precise alarm timing
            • Ensures alarms trigger at the exact time
            
            🏠 Show on Lockscreen
            • Displays alarm over lockscreen
            • Wake you up even when phone is locked
            
            🔄 Run in Background
            • Keeps alarm service running
            • Ensures alarms work when app is closed
        """.trimIndent()

        AlertDialog.Builder(activity)
            .setTitle("Why These Permissions?")
            .setMessage(message)
            .setPositiveButton("Grant All") { dialog, _ ->
                dialog.dismiss()
                requestMissingPermissions(activity)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
