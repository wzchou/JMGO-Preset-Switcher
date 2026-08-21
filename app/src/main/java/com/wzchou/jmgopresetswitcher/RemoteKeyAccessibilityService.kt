package com.wzchou.jmgopresetswitcher

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class RemoteKeyAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: RemoteKeyAccessibilityService? = null
    }

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

        // 三條橫線按鈕 = KEYCODE_MENU
        if (event.keyCode == KeyEvent.KEYCODE_MENU) {

            // ACTION_DOWN 時執行一次
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                JmgoPresetAutomation(this).switchNext()
            }

            // DOWN / UP 都吃掉，避免原本設定選單出現
            return true
        }

        // 保留原本的按鍵學習功能
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount > 0) {
            return false
        }

        val key = event.keyCode
        AppPrefs.setLastKey(this, key)

        if (AppPrefs.learnNextKey(this)) {
            AppPrefs.setTriggerKey(this, key)
            AppPrefs.setLearnNextKey(this, false)

            Toast.makeText(
                this,
                "Trigger saved: ${KeyEvent.keyCodeToString(key)}",
                Toast.LENGTH_SHORT
            ).show()

            return true
        }

        if (key == AppPrefs.triggerKey(this)) {
            JmgoPresetAutomation(this).switchNext()
            return true
        }

        return false
    }
}
