package com.alarmify.wakeupnerd.data

import java.util.*

data class Alarm(
    val id: String = System.currentTimeMillis().toString(), // Unique ID for each alarm
    val hour: Int,
    val minute: Int,
    val isPM: Boolean, // true for PM, false for AM
    val label: String = "",
    val ringtone: String = "Alarm clock",
    val repeat: String = "Sekali",
    val vibrate: Boolean = true,
    val volume: Int = 50, // Alarm volume (0-100)
    var isEnabled: Boolean = true,
    var isHighlighted: Boolean = false
) {
    // Helper property to get formatted time string
    val time: String
        get() = String.format("%02d:%02d", hour, minute)

    // Helper property to get description (repeat + label + time until alarm)
    val description: String
        get() {
            val baseDescription = if (label.isNotEmpty()) {
                "$repeat, $label"
            } else {
                repeat
            }

            // Calculate time until alarm
            val timeUntil = getTimeUntilAlarm()
            return "$baseDescription | Alarm in $timeUntil"
        }

    // Calculate time until alarm
    private fun getTimeUntilAlarm(): String {
        val now = Calendar.getInstance()
        val alarmTime = Calendar.getInstance().apply {
            // Convert 12-hour to 24-hour format
            val hour24 = when {
                !isPM && hour == 12 -> 0 // 12 AM = 00:00
                isPM && hour != 12 -> hour + 12 // PM hours except 12
                else -> hour // AM hours and 12 PM
            }

            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If alarm time is before current time, it's for tomorrow
            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Calculate difference
        val diffMillis = alarmTime.timeInMillis - now.timeInMillis
        val diffMinutes = (diffMillis / (1000 * 60)).toInt()
        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60

        return "${hours}h ${minutes}m"
    }
}