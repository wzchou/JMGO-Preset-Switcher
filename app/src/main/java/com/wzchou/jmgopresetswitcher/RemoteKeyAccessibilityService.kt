package com.wzchou.jmgopresetswitcher

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class RemoteKeyAccessibilityService : AccessibilityService() {
    companion object { @Volatile var instance: RemoteKeyAccessibilityService? = null }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount > 0) return false
        val key = event.keyCode
        AppPrefs.setLastKey(this, key)

        if (AppPrefs.learnNextKey(this)) {
            AppPrefs.setTriggerKey(this, key)
            AppPrefs.setLearnNextKey(this, false)
            Toast.makeText(this, "Trigger saved: ${KeyEvent.keyCodeToString(key)}", Toast.LENGTH_SHORT).show()
            return true
        }

        if (key == AppPrefs.triggerKey(this)) {
            JmgoPresetAutomation(this).switchNext()
            return true
        }
        return false
    }
}
