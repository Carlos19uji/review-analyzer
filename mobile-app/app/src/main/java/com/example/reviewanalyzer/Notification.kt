package com.example.reviewanalyzer

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object Notification{

    private const val CHANNEL_ID = "review_analysis_channel"
    private const val CHANNEL_NAME = "Review Analysis"
    private const val CHANNEL_DESCRIPTION = "Notifications for completed review analysis"

    fun createNotificationChannel(context: Context) {
        Log.d("Notification", "Attempting to create notification channel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("Notification", "Notification channel created successfully")
        }
    }

    fun requestNotificationPermission(context: Context, requestPermissionLauncher: ActivityResultLauncher<String>) {
        Log.d("Notification", "Checking notification permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Notification", "Permission not granted, requesting permission")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun sendNotification(context: Context, title: String, message: String) {
        Log.d("Notification", "Preparing to send notification")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Notification", "Notification permission not granted. Aborting notification.")
            Toast.makeText(context, "Notification permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val notificationId = System.currentTimeMillis().toInt()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
            Log.d("Notification", "Notification sent successfully")
        } catch (e: Exception) {
            Log.e("Notification", "Error sending notification: ${e.message}", e)
        }
    }
}
