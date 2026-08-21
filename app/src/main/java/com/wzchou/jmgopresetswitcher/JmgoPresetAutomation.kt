package com.wzchou.jmgopresetswitcher

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class JmgoPresetAutomation(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())

    private val projectorLabels = listOf(
        "Projector settings", "Projector Settings", "投影機設定", "投影仪设置", "プロジェクター設定"
    )
    private val imageLabels = listOf(
        "Image adjustment", "Image Adjustment", "Image calibration", "Projection / Image Calibration",
        "畫面調整", "图像调整", "畫面校正", "画面校正", "画像調整"
    )
    private val memoryLabels = listOf(
        "AI Spatial Image Memory", "Spatial Image Memory", "AI空間畫面記憶", "AI空间画面记忆", "AI空間画像メモリ"
    )

    fun switchNext() {
        val presetIndex = AppPrefs.nextPreset(context)
        Toast.makeText(context, "JMGO preset ${presetIndex + 1}", Toast.LENGTH_SHORT).show()

        // Public JMGO firmware API for saved positions is not documented. v0.1 therefore opens
        // projector settings and lets the AccessibilityService navigate by visible labels.
        val intent = findBestSettingsIntent() ?: Intent(Settings.ACTION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

        val service = RemoteKeyAccessibilityService.instance ?: return
        navigate(service, presetIndex)
    }

    private fun findBestSettingsIntent(): Intent? {
        val pm = context.packageManager
        val candidates = pm.getInstalledApplications(0)
            .asSequence()
            .filter { app ->
                val p = app.packageName.lowercase()
                p.contains("jmgo") || p.contains("projection") || p.contains("projector")
            }
            .mapNotNull { pm.getLaunchIntentForPackage(it.packageName) }
            .toList()
        return candidates.firstOrNull()
    }

    private fun navigate(service: AccessibilityService, presetIndex: Int) {
        val attempts = listOf<(AccessibilityNodeInfo) -> Boolean>(
            { root -> clickAny(root, projectorLabels) },
            { root -> clickAny(root, imageLabels) },
            { root -> clickAny(root, memoryLabels) },
            { root -> clickPresetByIndex(root, presetIndex) }
        )
        attempts.forEachIndexed { index, step ->
            handler.postDelayed({
                val root = service.rootInActiveWindow ?: return@postDelayed
                step(root)
                if (index == attempts.lastIndex) {
                    handler.postDelayed({ service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }, 500)
                }
            }, 450L + index * 650L)
        }
    }

    private fun clickAny(root: AccessibilityNodeInfo, labels: List<String>): Boolean {
        for (label in labels) {
            val nodes = root.findAccessibilityNodeInfosByText(label)
            val hit = nodes.firstOrNull { it.isVisibleToUser }
            if (hit != null && clickNodeOrParent(hit)) return true
        }
        return false
    }

    private fun clickPresetByIndex(root: AccessibilityNodeInfo, presetIndex: Int): Boolean {
        val clickable = mutableListOf<AccessibilityNodeInfo>()
        collectClickable(root, clickable)
        val filtered = clickable.filter { node ->
            val t = (node.text?.toString() ?: node.contentDescription?.toString() ?: "").trim()
            t.isNotEmpty() && memoryLabels.none { t.contains(it, ignoreCase = true) } &&
                !t.contains("add", true) && !t.contains("delete", true) && !t.contains("新增", true) && !t.contains("刪除", true)
        }.distinctBy { it.viewIdResourceName to (it.text?.toString() ?: it.contentDescription?.toString()) }

        val target = filtered.getOrNull(presetIndex) ?: return false
        return clickNodeOrParent(target)
    }

    private fun collectClickable(node: AccessibilityNodeInfo, out: MutableList<AccessibilityNodeInfo>) {
        if (node.isClickable && node.isVisibleToUser) out += node
        for (i in 0 until node.childCount) node.getChild(i)?.let { collectClickable(it, out) }
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        repeat(5) {
            val n = current ?: return false
            if (n.isClickable && n.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            current = n.parent
        }
        return false
    }
}
