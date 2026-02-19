package com.ssti.alermapp.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.provider.Settings
import android.util.Log

object AlarmPlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun start(context: Context, ringUrl: String?) {

        if (mediaPlayer != null) return

        try {

            val uri: Uri = if (!ringUrl.isNullOrEmpty()) {
                Uri.parse(ringUrl)   // ✅ Play custom URL
            } else {
                Settings.System.DEFAULT_ALARM_ALERT_URI
                    ?: Settings.System.DEFAULT_RINGTONE_URI
            }

            mediaPlayer = MediaPlayer().apply {

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                setDataSource(context, uri)
                isLooping = true
                prepare()
                start()
            }

        } catch (e: Exception) {
            Log.e("AlarmPlayer", "Error starting alarm", e)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AlarmPlayer", "Error stopping alarm", e)
        }
        mediaPlayer = null
    }
}
