package com.alarmify.wakeupnerd.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alarmify.wakeupnerd.AlarmReceiver
import com.alarmify.wakeupnerd.data.Alarm
import java.util.*

object AlarmScheduler {

    /**
     * Schedule an alarm using AlarmManager
     */
    fun scheduleAlarm(context: Context, alarm: Alarm) {
        if (!alarm.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_RINGTONE", alarm.ringtone)
            putExtra("ALARM_VIBRATE", alarm.vibrate)
            putExtra("ALARM_VOLUME", alarm.volume)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmTimeMillis = getAlarmTimeInMillis(alarm)

        // Use setAlarmClock for reliable alarm that works even in Doze mode
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            alarmTimeMillis,
            pendingIntent
        )

        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    /**
     * Cancel a scheduled alarm
     */
    fun cancelAlarm(context: Context, alarmId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * Convert alarm time to milliseconds
     */
    private fun getAlarmTimeInMillis(alarm: Alarm): Long {
        val calendar = Calendar.getInstance().apply {
            // Convert 12-hour to 24-hour format
            val hour24 = when {
                !alarm.isPM && alarm.hour == 12 -> 0 // 12 AM = 00:00
                alarm.isPM && alarm.hour != 12 -> alarm.hour + 12 // PM hours except 12
                else -> alarm.hour // AM hours and 12 PM
            }

            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If alarm time is before current time, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return calendar.timeInMillis
    }

    /**
     * Reschedule all enabled alarms (useful after device reboot)
     */
    fun rescheduleAllAlarms(context: Context) {
        val alarms = AlarmStorageManager.getAllAlarms(context)
        alarms.filter { it.isEnabled }.forEach { alarm ->
            scheduleAlarm(context, alarm)
        }
    }
}
