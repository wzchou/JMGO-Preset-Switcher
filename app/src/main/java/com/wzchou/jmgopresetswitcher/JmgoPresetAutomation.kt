package com.wzchou.jmgopresetswitcher

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class JmgoPresetAutomation(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())

    // Desired cycle:
    // 2 = 正前方
    // 3 = 右邊
    // 4 = 左邊
    // 1 = full
    private val cycleIds = intArrayOf(2, 3, 4, 1)

    fun switchNext() {
        val service = RemoteKeyAccessibilityService.instance
        if (service == null) {
            Toast.makeText(
                context,
                "Accessibility service not enabled",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val index = AppPrefs.nextPreset(context) % cycleIds.size
        val memoryId = cycleIds[index]

        openDisplayMemory()

        // Wait for JMGO page to appear, then locate target card and apply it.
        handler.postDelayed({
            applyMemoryByVisibleCard(service, memoryId)
        }, 700)
    }

    fun applyMemory(memoryId: Int) {
        val service = RemoteKeyAccessibilityService.instance
        if (service == null) {
            Toast.makeText(
                context,
                "Accessibility service not enabled",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        openDisplayMemory()

        handler.postDelayed({
            applyMemoryByVisibleCard(service, memoryId)
        }, 700)
    }

    private fun openDisplayMemory() {
        val intent = Intent("com.jmgo.ptz.DISPLAY_MEMORY").apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        runCatching {
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(
                context,
                "Cannot open JMGO Display Memory",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun applyMemoryByVisibleCard(
        service: AccessibilityService,
        memoryId: Int
    ) {
        val root = service.rootInActiveWindow ?: run {
            Toast.makeText(
                context,
                "JMGO memory page not detected",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val targetName = when (memoryId) {
            1 -> "full"
            2 -> "正前方"
            3 -> "右邊"
            4 -> "左邊"
            else -> return
        }

        val matched = findNodeByText(root, targetName)

        if (matched == null) {
            Toast.makeText(
                context,
                "Memory not found: $targetName",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val card = findClickableAncestor(matched) ?: matched.parent ?: matched

        val applyButton = findApplyButtonNear(card)

        if (applyButton != null && clickNodeOrParent(applyButton)) {
            Toast.makeText(
                context,
                "Applied: $targetName",
                Toast.LENGTH_SHORT
            ).show()

            // Return to previous screen after JMGO receives the command.
            handler.postDelayed({
                service.performGlobalAction(
                    AccessibilityService.GLOBAL_ACTION_BACK
                )
            }, 700)

            return
        }

        // Fallback: try clicking the card itself.
        if (clickNodeOrParent(card)) {
            handler.postDelayed({
                val newRoot = service.rootInActiveWindow ?: return@postDelayed

                val buttons = mutableListOf<AccessibilityNodeInfo>()
                collectNodes(newRoot, buttons)

                val apply = buttons.firstOrNull {
                    val txt = nodeText(it)
                    txt.contains("立即套用", true) ||
                    txt.contains("套用", true) ||
                    txt.contains("Apply", true)
                }

                if (apply != null) {
                    clickNodeOrParent(apply)

                    handler.postDelayed({
                        service.performGlobalAction(
                            AccessibilityService.GLOBAL_ACTION_BACK
                        )
                    }, 700)
                }
            }, 300)
        } else {
            Toast.makeText(
                context,
                "Could not apply: $targetName",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun findNodeByText(
        root: AccessibilityNodeInfo,
        target: String
    ): AccessibilityNodeInfo? {
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        collectNodes(root, nodes)

        return nodes.firstOrNull {
            nodeText(it).equals(target, ignoreCase = true)
        } ?: nodes.firstOrNull {
            nodeText(it).contains(target, ignoreCase = true)
        }
    }

    private fun findApplyButtonNear(
        node: AccessibilityNodeInfo
    ): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node

        repeat(5) {
            val n = current ?: return null

            val descendants = mutableListOf<AccessibilityNodeInfo>()
            collectNodes(n, descendants)

            descendants.firstOrNull {
                val txt = nodeText(it)
                txt.contains("立即套用", true) ||
                txt.equals("套用", true) ||
                txt.equals("Apply", true)
            }?.let { return it }

            current = n.parent
        }

        return null
    }

    private fun collectNodes(
        node: AccessibilityNodeInfo,
        out: MutableList<AccessibilityNodeInfo>
    ) {
        out += node

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let {
                collectNodes(it, out)
            }
        }
    }

    private fun nodeText(node: AccessibilityNodeInfo): String {
        return (
            node.text?.toString()
                ?: node.contentDescription?.toString()
                ?: ""
        ).trim()
    }

    private fun findClickableAncestor(
        node: AccessibilityNodeInfo
    ): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node

        repeat(6) {
            val n = current ?: return null
            if (n.isClickable) return n
            current = n.parent
        }

        return null
    }

    private fun clickNodeOrParent(
        node: AccessibilityNodeInfo
    ): Boolean {
        var current: AccessibilityNodeInfo? = node

        repeat(6) {
            val n = current ?: return false

            if (
                n.isClickable &&
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            ) {
                return true
            }

            current = n.parent
        }

        return false
    }
}
