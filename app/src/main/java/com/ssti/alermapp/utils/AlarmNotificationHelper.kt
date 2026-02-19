import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ssti.alermapp.AlarmReceiver
import com.ssti.alermapp.MainActivity
import com.ssti.alermapp.R

object AlarmNotificationHelper {

    private const val CHANNEL_ID = "alarm_channel"
    private const val NOTIFICATION_ID = 1001

    private const val CHANNEL_NAME = "Alarm Alerts"

    const val EXTRA_RING_URL = "extra_ring_url"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_DESCRIPTION = "extra_description"
    const val EXTRA_ALARM_ID = "extra_alarm_id"

    fun show(
        context: Context,
        title: String,
        description: String,
        ringUrl: String? = null,
        alarmId: Int = -1
    ) {
        createChannel(context)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ✅ Intent to open MainActivity when notification is clicked
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_RING_URL, ringUrl)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DESCRIPTION, description)
                if (alarmId != -1) putExtra(EXTRA_ALARM_ID, alarmId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ STOP action
        val stopIntent = PendingIntent.getBroadcast(
            context,
            alarmId * 10 + 1,
            Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_STOP
                if (alarmId != -1) putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ DISMISS action
        val dismissIntent = PendingIntent.getBroadcast(
            context,
            alarmId * 10 + 2,
            Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_DISMISS
                putExtra(EXTRA_RING_URL, ringUrl)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DESCRIPTION, description)
                if (alarmId != -1) putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.timepicker_bg)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_launcher_background, "STOP", stopIntent)
            .addAction(R.drawable.ic_launcher_foreground, "DISMISS", dismissIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null) // Disable sound for this channel
        }

        notificationManager.createNotificationChannel(channel)
    }
}
