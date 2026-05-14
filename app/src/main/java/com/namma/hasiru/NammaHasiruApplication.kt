package com.namma.hasiru

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.namma.hasiru.workers.ReminderWorker
import java.util.concurrent.TimeUnit

class NammaHasiruApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "plantation_reminder_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderWorker.CHANNEL_ID,
                "Plantation reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
