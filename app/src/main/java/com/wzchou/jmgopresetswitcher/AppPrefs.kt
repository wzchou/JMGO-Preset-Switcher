package com.wzchou.jmgopresetswitcher

import android.content.Context
import android.view.KeyEvent

object AppPrefs {
    private const val FILE = "prefs"
    private const val TRIGGER_KEY = "trigger_key"
    private const val PRESET_COUNT = "preset_count"
    private const val CURRENT_PRESET = "current_preset"
    private const val LEARN_NEXT_KEY = "learn_next_key"
    private const val LAST_KEY = "last_key"

    fun triggerKey(context: Context): Int = context.getSharedPreferences(FILE, 0)
        .getInt(TRIGGER_KEY, KeyEvent.KEYCODE_MENU)

    fun setTriggerKey(context: Context, keyCode: Int) = context.getSharedPreferences(FILE, 0)
        .edit().putInt(TRIGGER_KEY, keyCode).apply()

    fun presetCount(context: Context): Int = context.getSharedPreferences(FILE, 0)
        .getInt(PRESET_COUNT, 3).coerceIn(2, 10)

    fun setPresetCount(context: Context, count: Int) = context.getSharedPreferences(FILE, 0)
        .edit().putInt(PRESET_COUNT, count.coerceIn(2, 10)).apply()

    fun nextPreset(context: Context): Int {
        val p = context.getSharedPreferences(FILE, 0)
        val count = presetCount(context)
        val next = (p.getInt(CURRENT_PRESET, -1) + 1) % count
        p.edit().putInt(CURRENT_PRESET, next).apply()
        return next
    }

    fun learnNextKey(context: Context): Boolean = context.getSharedPreferences(FILE, 0)
        .getBoolean(LEARN_NEXT_KEY, false)

    fun setLearnNextKey(context: Context, enabled: Boolean) = context.getSharedPreferences(FILE, 0)
        .edit().putBoolean(LEARN_NEXT_KEY, enabled).apply()

    fun lastKey(context: Context): Int = context.getSharedPreferences(FILE, 0)
        .getInt(LAST_KEY, KeyEvent.KEYCODE_UNKNOWN)

    fun setLastKey(context: Context, keyCode: Int) = context.getSharedPreferences(FILE, 0)
        .edit().putInt(LAST_KEY, keyCode).apply()
}
