package com.alarmify.wakeupnerd.data

import android.content.Context
import android.content.SharedPreferences
import com.alarmify.wakeupnerd.SettingsActivity

/**
 * Utility class for accessing app settings stored in SharedPreferences
 */
object SettingsManager {

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            SettingsActivity.PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    /**
     * Get the math challenge difficulty level
     * @return "Easy", "Medium", or "Hard"
     */
    fun getDifficulty(context: Context): String {
        return getPreferences(context).getString(
            SettingsActivity.KEY_DIFFICULTY,
            SettingsActivity.DEFAULT_DIFFICULTY
        ) ?: SettingsActivity.DEFAULT_DIFFICULTY
    }

    /**
     * Get the number of math questions to solve
     * @return Number between 1 and 10
     */
    fun getQuestionCount(context: Context): Int {
        return getPreferences(context).getInt(
            SettingsActivity.KEY_QUESTION_COUNT,
            SettingsActivity.DEFAULT_QUESTION_COUNT
        )
    }

    /**
     * Save difficulty setting
     */
    fun setDifficulty(context: Context, difficulty: String) {
        getPreferences(context).edit()
            .putString(SettingsActivity.KEY_DIFFICULTY, difficulty)
            .apply()
    }

    /**
     * Save question count setting
     */
    fun setQuestionCount(context: Context, count: Int) {
        val validCount = count.coerceIn(
            SettingsActivity.MIN_QUESTIONS,
            SettingsActivity.MAX_QUESTIONS
        )
        getPreferences(context).edit()
            .putInt(SettingsActivity.KEY_QUESTION_COUNT, validCount)
            .apply()
    }

    /**
     * Reset all settings to default values
     */
    fun resetToDefaults(context: Context) {
        getPreferences(context).edit()
            .putString(SettingsActivity.KEY_DIFFICULTY, SettingsActivity.DEFAULT_DIFFICULTY)
            .putInt(SettingsActivity.KEY_QUESTION_COUNT, SettingsActivity.DEFAULT_QUESTION_COUNT)
            .apply()
    }

    /**
     * Get all settings as a formatted string (useful for debugging)
     */
    fun getSettingsSummary(context: Context): String {
        return """
            Difficulty: ${getDifficulty(context)}
            Question Count: ${getQuestionCount(context)}
        """.trimIndent()
    }
}
