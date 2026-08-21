package com.wzchou.jmgopresetswitcher

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View

class KeyListenerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setDimAmount(0f)
        window.decorView.setBackgroundColor(Color.TRANSPARENT)

        setContentView(
            View(this).apply {
                setBackgroundColor(Color.TRANSPARENT)
            }
        )
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (
            event.action == KeyEvent.ACTION_DOWN &&
            event.repeatCount == 0 &&
            event.keyCode == KeyEvent.KEYCODE_MENU
        ) {
            JmgoPresetAutomation(this).switchNext()
        }

        return super.dispatchKeyEvent(event)
    }
}
