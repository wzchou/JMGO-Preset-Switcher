package com.wzchou.jmgopresetswitcher

import android.content.Context
import android.os.IBinder
import android.os.Parcel
import android.widget.Toast

class JmgoPresetAutomation(private val context: Context) {

    companion object {
        private const val SYSTEM_SERVICE = "jmgomiddle-01"
        private const val SYSTEM_DESCRIPTOR =
            "com.jmgo.middleware.service.IJmGOSystemService"

        private const val DLP_DESCRIPTOR =
            "com.jmgo.middleware.dlp.IJmGODlpManagerService"

        // Found from JMGO's own JmGOPTZ APK.
        private const val TRANSACTION_GET_DLP = 5
        private const val TRANSACTION_DLP_SET = 1

        // Display-memory APPLY command.
        private const val DISPLAY_MEMORY_APPLY = 326
    }

    fun switchNext() {
        // Temporary v0.2 assumption: memory IDs 1..presetCount.
        // Once direct Binder is proven, we'll read the real saved IDs automatically.
        val id = AppPrefs.nextPreset(context) + 1
        applyMemory(id)
    }

    fun applyMemory(memoryId: Int) {
        val result = runCatching {
            val systemBinder = getSystemServiceBinder()
                ?: error("jmgomiddle-01 not found")

            val dlpBinder = getDlpBinder(systemBinder)
                ?: error("DLP Binder not returned")

            callDlpSet(dlpBinder, DISPLAY_MEMORY_APPLY, memoryId)
        }

        result.onSuccess {
            Toast.makeText(
                context,
                "JMGO Memory ID $memoryId sent",
                Toast.LENGTH_SHORT
            ).show()
        }.onFailure {
            Toast.makeText(
                context,
                "Binder failed: ${it.javaClass.simpleName}: ${it.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getSystemServiceBinder(): IBinder? {
        val serviceManager = Class.forName("android.os.ServiceManager")
        val getService = serviceManager.getDeclaredMethod(
            "getService",
            String::class.java
        )
        return getService.invoke(null, SYSTEM_SERVICE) as? IBinder
    }

    private fun getDlpBinder(systemBinder: IBinder): IBinder? {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()

        return try {
            data.writeInterfaceToken(SYSTEM_DESCRIPTOR)

            val ok = systemBinder.transact(
                TRANSACTION_GET_DLP,
                data,
                reply,
                0
            )

            if (!ok) error("System transaction 5 rejected")

            reply.readException()
            reply.readStrongBinder()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    private fun callDlpSet(
        binder: IBinder,
        key: Int,
        value: Int
    ) {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()

        try {
            data.writeInterfaceToken(DLP_DESCRIPTOR)
            data.writeInt(key)
            data.writeInt(value)

            val ok = binder.transact(
                TRANSACTION_DLP_SET,
                data,
                reply,
                0
            )

            if (!ok) error("DLP transaction 1 rejected")

            reply.readException()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }
}
