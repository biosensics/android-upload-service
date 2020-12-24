package net.gotev.uploadservice.s3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService
import java.util.UUID

class S3Setup(context: Context) {
    private val context = context

    /**
     * You should call this once. It can be either on activity create or the first time you want to do the upload.
     */
    fun startTransferService() {
        val tsIntent = Intent(context, TransferService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = UUID.randomUUID().toString()
            val pendingIntent = PendingIntent.getActivity(context, 0, tsIntent, 0)

            // Notification Manager to listen to a channel
            val channel = NotificationChannel(id, "Transfer Service Channel", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

            // Valid notification object required
            val notification = Notification.Builder(context, id)
                .setContentTitle("Transfer Service Notification")
                .setContentText("Transfer Service is running")
                .setContentIntent(pendingIntent)
                .build()

            tsIntent.putExtra(TransferService.INTENT_KEY_NOTIFICATION, notification)
            tsIntent.putExtra(TransferService.INTENT_KEY_NOTIFICATION_ID, 15)
            tsIntent.putExtra(TransferService.INTENT_KEY_REMOVE_NOTIFICATION, true)

            // Foreground service required starting from Android Oreo
            context.startForegroundService(tsIntent)
        } else {
            context.startService(tsIntent)
        }
    }
}
