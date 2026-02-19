package com.ssti.alermapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ssti.alermapp.databinding.ActivityMainBinding
import com.ssti.alermapp.local.AlarmEntity
import com.ssti.alermapp.viewmodel.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmManager: AlarmManager
    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        binding.btnSubmit.setOnClickListener {

            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute

            val title = binding.edtTitle.text.toString()
            val description = binding.edtDescription.text.toString()

            val alarmUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)?.toString() ?: ""

            val triggerTime = getTriggerTime(hour, minute)
            val alarm = AlarmEntity(
                hour = hour,
                minute = minute,
                triggerTimeMillis = triggerTime,
                title = title,
                description = description,
                ringUrl = alarmUri
            )

            // Insert alarm and schedule it
            lifecycleScope.launch {
                val alarmId = viewModel.insertAlarmSuspend(alarm)
                scheduleAlarm(alarmId.toInt(), triggerTime, alarm)
            }
        }
    }

    private fun scheduleAlarm(alarmId: Int, triggerTime: Long, alarm: AlarmEntity) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId, // Use alarm ID as request code for unique PendingIntent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            @Suppress("DEPRECATION")
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    // 🔹 Calculate next trigger time
    private fun getTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time already passed → set for next day
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis
    }

}
