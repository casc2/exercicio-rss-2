package br.ufpe.cin.if710.rss

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log

class StaticReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DEBUG", "Recebendo StaticReceiver...")
        if (!isForeground) {
            // Create an explicit intent for an Activity in your app
            val mIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, mIntent, 0)

            // Create notification
            val mBuilder = NotificationCompat.Builder(context)
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .setContentTitle("RSS Feed")
                    .setContentText("Seu feed tem novidade! Clique aqui para vê-lo.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(1, mBuilder.build())
            }
        }
    }
}