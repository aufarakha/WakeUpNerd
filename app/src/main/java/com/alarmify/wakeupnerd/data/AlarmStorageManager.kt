package com.alarmify.wakeupnerd.data

import android.content.Context
import com.alarmify.wakeupnerd.data.Alarm
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object AlarmStorageManager {

    private const val ALARM_DIR = "Alarms"
    private const val ALARMS_FILE = "alarms.json"

    /**
     * Get the directory where alarms are stored
     * Path: /data/data/com.alarmify.wakeupnerd/files/Alarms/
     */
    private fun getAlarmDirectory(context: Context): File {
        val dir = File(context.filesDir, ALARM_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Get the alarms JSON file
     */
    private fun getAlarmsFile(context: Context): File {
        return File(getAlarmDirectory(context), ALARMS_FILE)
    }

    /**
     * Save an alarm (add new or update existing)
     */
    fun saveAlarm(context: Context, alarm: Alarm): Boolean {
        return try {
            val alarms = getAllAlarms(context).toMutableList()

            // Check if alarm with this ID already exists
            val existingIndex = alarms.indexOfFirst { it.id == alarm.id }

            if (existingIndex != -1) {
                // Update existing alarm
                alarms[existingIndex] = alarm
            } else {
                // Add new alarm
                alarms.add(alarm)
            }

            // Save all alarms
            saveAllAlarms(context, alarms)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Delete an alarm by ID
     */
    fun deleteAlarm(context: Context, alarmId: String): Boolean {
        return try {
            val alarms = getAllAlarms(context).toMutableList()
            alarms.removeAll { it.id == alarmId }
            saveAllAlarms(context, alarms)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get all alarms
     */
    fun getAllAlarms(context: Context): List<Alarm> {
        return try {
            val file = getAlarmsFile(context)
            if (!file.exists()) {
                return emptyList()
            }

            val jsonString = file.readText()
            if (jsonString.isEmpty()) {
                return emptyList()
            }

            val jsonArray = JSONArray(jsonString)
            val alarms = mutableListOf<Alarm>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                alarms.add(jsonToAlarm(jsonObject))
            }

            alarms
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get a single alarm by ID
     */
    fun getAlarmById(context: Context, alarmId: String): Alarm? {
        return getAllAlarms(context).find { it.id == alarmId }
    }

    /**
     * Save all alarms to file
     */
    private fun saveAllAlarms(context: Context, alarms: List<Alarm>) {
        val jsonArray = JSONArray()

        alarms.forEach { alarm ->
            jsonArray.put(alarmToJson(alarm))
        }

        val file = getAlarmsFile(context)
        file.writeText(jsonArray.toString(2)) // Pretty print with indent of 2
    }

    /**
     * Convert Alarm object to JSON
     */
    private fun alarmToJson(alarm: Alarm): JSONObject {
        return JSONObject().apply {
            put("id", alarm.id)
            put("hour", alarm.hour)
            put("minute", alarm.minute)
            put("isPM", alarm.isPM)
            put("label", alarm.label)
            put("ringtone", alarm.ringtone)
            put("repeat", alarm.repeat)
            put("vibrate", alarm.vibrate)
            put("volume", alarm.volume)
            put("isEnabled", alarm.isEnabled)
            put("isHighlighted", alarm.isHighlighted)
        }
    }

    /**
     * Convert JSON to Alarm object
     */
    private fun jsonToAlarm(json: JSONObject): Alarm {
        return Alarm(
            id = json.getString("id"),
            hour = json.getInt("hour"),
            minute = json.getInt("minute"),
            isPM = json.getBoolean("isPM"),
            label = json.optString("label", ""),
            ringtone = json.optString("ringtone", "Alarm clock"),
            repeat = json.optString("repeat", "Sekali"),
            vibrate = json.optBoolean("vibrate", true),
            volume = json.optInt("volume", 50),
            isEnabled = json.optBoolean("isEnabled", true),
            isHighlighted = json.optBoolean("isHighlighted", false)
        )
    }

    /**
     * Clear all alarms (for testing purposes)
     */
    fun clearAllAlarms(context: Context): Boolean {
        return try {
            val file = getAlarmsFile(context)
            if (file.exists()) {
                file.delete()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
