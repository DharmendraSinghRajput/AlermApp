package com.ssti.alermapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ssti.alermapp.hilt.AlarmReceiverEntryPoint
import com.ssti.alermapp.local.PracticalApp
import com.ssti.alermapp.repository.AlarmRepository
import com.ssti.alermapp.utils.AlarmPlayer
import com.ssti.alermapp.utils.WakeLockManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext as PracticalApp,
            AlarmReceiverEntryPoint::class.java
        )
        val repository: AlarmRepository = entryPoint.alarmRepository()

        when (intent?.action) {
            ACTION_STOP -> handleStop(context)
            ACTION_DISMISS -> handleDismiss(context, intent, repository)
            else -> handleAlarmTrigger(context, intent, repository)
        }
    }

    // Stop alarm
    private fun handleStop(context: Context) {
        AlarmPlayer.stop()
        AlarmNotificationHelper.cancel(context)
        WakeLockManager.release()
    }

    // Dismiss alarm and open MainActivity
    private fun handleDismiss(
        context: Context,
        intent: Intent,
        repository: AlarmRepository
    ) {
        handleStop(context)

        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        if (alarmId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                val alarm = repository.getById(alarmId)
                if (alarm != null) {
                    val activityIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("ring_url", alarm.ringUrl)
                        putExtra("title", alarm.title)
                        putExtra("description", alarm.description)
                    }
                    context.startActivity(activityIntent)
                }
            }
        } else {
            startMainWithFallback(context, intent)
        }
    }

    // Trigger alarm
    private fun handleAlarmTrigger(
        context: Context,
        intent: Intent?,
        repository: AlarmRepository
    ) {
        WakeLockManager.acquire(context)

        val alarmId = intent?.getIntExtra(EXTRA_ALARM_ID, -1) ?: -1
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = if (alarmId != -1) repository.getById(alarmId) else null
            withContext(Dispatchers.Main) {
                if (alarm != null) {
                    AlarmPlayer.start(context, alarm.ringUrl)
                    AlarmNotificationHelper.show(
                        context,
                        alarm.title,
                        alarm.description,
                        alarm.ringUrl,
                        alarm.id
                    )
                } else {
                    startAlarmFallback(context, intent)
                }
            }
        }
    }

    // Fallback: start MainActivity
    private fun startMainWithFallback(context: Context, intent: Intent) {
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ring_url", intent.getStringExtra(AlarmNotificationHelper.EXTRA_RING_URL))
            putExtra("title", intent.getStringExtra(AlarmNotificationHelper.EXTRA_TITLE))
            putExtra("description", intent.getStringExtra(AlarmNotificationHelper.EXTRA_DESCRIPTION))
        }
        context.startActivity(activityIntent)
    }

    // Fallback: trigger alarm with defaults
    private fun startAlarmFallback(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra(AlarmNotificationHelper.EXTRA_TITLE) ?: "Alarm"
        val description = intent?.getStringExtra(AlarmNotificationHelper.EXTRA_DESCRIPTION) ?: "Alarm triggered"
        val ringUrl = intent?.getStringExtra(AlarmNotificationHelper.EXTRA_RING_URL)

        AlarmPlayer.start(context, ringUrl)
        AlarmNotificationHelper.show(context, title, description, ringUrl)
    }

    companion object {
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_DISMISS = "ACTION_DISMISS"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
}
