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
          context.startService(Intent(context, TransferService::class.java))
      }
}
