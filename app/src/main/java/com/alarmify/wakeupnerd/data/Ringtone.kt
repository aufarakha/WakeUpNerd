package com.alarmify.wakeupnerd.data

data class Ringtone(
    val name: String,
    val duration: String,
    val isPlaying: Boolean = false
)
