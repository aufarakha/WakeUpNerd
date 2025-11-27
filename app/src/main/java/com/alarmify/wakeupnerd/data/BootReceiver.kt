package com.alarmify.wakeupnerd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alarmify.wakeupnerd.data.AlarmScheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all alarms after device reboot
            AlarmScheduler.rescheduleAllAlarms(context)
        }
    }
}
