package com.ssti.alermapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_STOP, ACTION_DISMISS -> stopAlarm(context)
            else -> {
                acquireWakeLock(context)
                startAlarm(context)
                showNotification(context)
            }
        }
    }

    private fun acquireWakeLock(context: Context) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply { acquire(WAKE_LOCK_TIMEOUT_MS) }
        } catch (e: Exception) {
            Log.e(TAG, "acquireWakeLock failed", e)
        }
    }

    private fun startAlarm(context: Context) {
        synchronized(Companion) {
            if (mediaPlayer != null) return
            try {
                val uri = Settings.System.DEFAULT_ALARM_ALERT_URI
                    ?: Settings.System.DEFAULT_RINGTONE_URI
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, uri)
                    isLooping = true
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                        stopAlarm(context)
                        true
                    }
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "startAlarm failed", e)
                stopAlarm(context)
            }
        }
    }

    private fun showNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!nm.areNotificationsEnabled()) {
                Log.w(TAG, "Notifications disabled")
                return
            }
        }

        createChannelIfNeeded(context)

        val stopPi = createStopPendingIntent(context)
        val dismissPi = createDismissPendingIntent(context)
        val contentPi = createContentPendingIntent(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm Ringing!")
            .setContentText("Tap STOP or DISMISS to stop the alarm")
            .setContentIntent(contentPi)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(VIBRATE_PATTERN)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "STOP", stopPi)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "DISMISS", dismissPi)

        buildCustomView(context, stopPi, dismissPi)?.let { remoteViews ->
            builder
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setFullScreenIntent(contentPi, false)
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, builder.build())
    }

    private fun buildCustomView(
        context: Context,
        stopPi: PendingIntent,
        dismissPi: PendingIntent
    ): RemoteViews? = try {
        RemoteViews(context.packageName, R.layout.custom_alarm_notification).apply {
            setOnClickPendingIntent(R.id.btnStop, stopPi)
            setOnClickPendingIntent(R.id.btnDismiss, dismissPi)
        }
    } catch (e: Exception) {
        Log.e(TAG, "buildCustomView failed", e)
        null
    }

    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarm Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notifications"
            enableVibration(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
            setSound(null, null)
            enableLights(true)
            lightColor = android.graphics.Color.RED
            setShowBadge(true)
        }
        nm.createNotificationChannel(channel)
    }

    private fun createStopPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, REQUEST_STOP,
            Intent(context, AlarmReceiver::class.java).apply { action = ACTION_STOP },
            PENDING_FLAGS
        )

    private fun createDismissPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, REQUEST_DISMISS,
            Intent(context, AlarmReceiver::class.java).apply { action = ACTION_DISMISS },
            PENDING_FLAGS
        )

    private fun createContentPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, REQUEST_CONTENT,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PENDING_FLAGS
        )

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_STOP = "ACTION_STOP"
        private const val ACTION_DISMISS = "ACTION_DISMISS"
        private const val WAKE_LOCK_TAG = "AlermApp::WakeLock"
        private const val WAKE_LOCK_TIMEOUT_MS = 10 * 60 * 1000L
        private const val REQUEST_STOP = 1
        private const val REQUEST_DISMISS = 2
        private const val REQUEST_CONTENT = 0
        private val PENDING_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        private val VIBRATE_PATTERN = longArrayOf(0, 250, 250, 250)

        @Volatile
        private var mediaPlayer: MediaPlayer? = null

        @Volatile
        private var wakeLock: PowerManager.WakeLock? = null

        fun stopAlarm(context: Context) {
            synchronized(Companion) {
                try {
                    mediaPlayer?.apply {
                        if (isPlaying) stop()
                        release()
                    }
                    mediaPlayer = null
                    wakeLock?.takeIf { it.isHeld }?.release()
                    wakeLock = null
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .cancel(NOTIFICATION_ID)
                } catch (e: Exception) {
                    Log.e(TAG, "stopAlarm failed", e)
                }
            }
        }
    }
}
