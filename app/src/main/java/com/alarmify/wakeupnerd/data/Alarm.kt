package com.alarmify.wakeupnerd.data

data class Alarm(
    val id: String = System.currentTimeMillis().toString(), // Unique ID for each alarm
    val hour: Int,
    val minute: Int,
    val isPM: Boolean, // true for PM, false for AM
    val label: String = "",
    val ringtone: String = "Alarm clock",
    val repeat: String = "Sekali",
    val vibrate: Boolean = true,
    var isEnabled: Boolean = true,
    var isHighlighted: Boolean = false
) {
    // Helper property to get formatted time string
    val time: String
        get() = String.format("%02d:%02d", hour, minute)

    // Helper property to get description (repeat + label)
    val description: String
        get() = if (label.isNotEmpty()) {
            "$repeat, $label"
        } else {
            repeat
        }
}