package com.namma.hasiru.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.namma.hasiru.MainActivity
import com.namma.hasiru.R
import com.namma.hasiru.data.NammaHasiruDatabase
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        val dao = NammaHasiruDatabase.getDatabase(applicationContext).plantationDao()
        dao.getPlantationsNeedingReminder(System.currentTimeMillis()).forEach { plantation ->
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("plantationId", plantation.id)
            }
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                plantation.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Time to check your tree")
                .setContentText("${plantation.commonName} needs a survival update")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(applicationContext).notify(plantation.id.toInt(), notification)
            dao.updatePlantation(plantation.copy(nextReminderDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(90)))
        }
        Result.success()
    } catch (_: SecurityException) {
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }

    companion object {
        const val CHANNEL_ID = "plantation_reminders"
    }
}
