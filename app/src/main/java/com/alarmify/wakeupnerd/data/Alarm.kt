package com.alarmify.wakeupnerd.data

data class Alarm(
    val time: String,
    val description: String,
    var isEnabled: Boolean,
    var isHighlighted: Boolean = false
)