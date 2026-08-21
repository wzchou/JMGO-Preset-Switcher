package com.wzchou.jmgopresetswitcher

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager

class KeyListenerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        )

        val params = window.attributes
        params.width = 1
        params.height = 1
        params.alpha = 0.01f
        window.attributes = params

        window.decorView.setBackgroundColor(Color.TRANSPARENT)
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
