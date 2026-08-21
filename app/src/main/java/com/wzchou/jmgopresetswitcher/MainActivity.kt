package com.wzchou.jmgopresetswitcher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(56, 42, 56, 42)
            setBackgroundColor(Color.rgb(16, 16, 16))
        }
        fun text(value: String, size: Float) = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.WHITE)
            setPadding(0, 10, 0, 10)
        }
        root.addView(text("JMGO Preset Switcher", 30f))
        root.addView(text("Short-press one remote button to cycle JMGO saved image positions.", 18f))

        status = text("", 18f)
        root.addView(status)

        root.addView(Button(this).apply {
            text = "1. Enable Accessibility Service"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "2. Learn Remote Button"
            setOnClickListener {
                AppPrefs.setLearnNextKey(this@MainActivity, true)
                status.text = "Press the remote button you want to use once."
            }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "Preset count: ${AppPrefs.presetCount(this@MainActivity)} (press to change)"
            setOnClickListener {
                val next = if (AppPrefs.presetCount(this@MainActivity) >= 10) 2 else AppPrefs.presetCount(this@MainActivity) + 1
                AppPrefs.setPresetCount(this@MainActivity, next)
                text = "Preset count: $next (press to change)"
                refreshStatus()
            }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "Test next preset (2→3→4→1)"
            setOnClickListener {
                JmgoPresetAutomation(this@MainActivity).switchNext()
            }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "Test 正前方 (ID 2)"
            setOnClickListener {
                JmgoPresetAutomation(this@MainActivity).applyMemory(2)
            }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "Test 右邊 (ID 3)"
            setOnClickListener {
                JmgoPresetAutomation(this@MainActivity).applyMemory(3)
            }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "Test 左邊 (ID 4)"
            setOnClickListener {
                JmgoPresetAutomation(this@MainActivity).applyMemory(4)
            }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(Button(this).apply {
            text = "Test full (ID 1)"
            setOnClickListener {
                JmgoPresetAutomation(this@MainActivity).applyMemory(1)
            }
        }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(text("First test target: the ≡ / Menu button. Prime Video can also be learned if the firmware exposes it as a key event.", 15f))
        setContentView(root)
    }

    private fun refreshStatus() {
        if (!::status.isInitialized) return
        val trigger = AppPrefs.triggerKey(this)
        val last = AppPrefs.lastKey(this)
        status.text = "Trigger: ${KeyEvent.keyCodeToString(trigger)} ($trigger)\nLast seen: ${KeyEvent.keyCodeToString(last)} ($last)\nPresets: ${AppPrefs.presetCount(this)}"
    }
}
