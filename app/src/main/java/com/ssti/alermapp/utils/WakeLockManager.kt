package com.ssti.alermapp.utils

import android.content.Context
import android.os.PowerManager

object WakeLockManager {

    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AlarmApp::WakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    fun release() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }
}
