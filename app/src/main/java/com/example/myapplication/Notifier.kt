package com.example.myapplication

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Posts the app's two kinds of fake-commerce pings: "your nothing was delivered"
 * and wishlist price drops. Real notifications about fake events — the satire
 * is intentional, the permission handling is not.
 */
object Notifier {

    private const val CHANNEL_ID = "cartharsis.events"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Deliveries of nothing & fake deals",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Order updates and wishlist price drops. None of it is real."
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /** Intent extra carrying the in-app route a tapped notification should open. */
    const val EXTRA_ROUTE = "cartharsis.route"

    fun notifyDelivered(context: Context, orderId: Int, moneyKept: String) {
        post(
            context,
            id = 1_000 + orderId,
            title = "🧘 Your nothing has been delivered",
            text = "Order #$orderId arrived containing exactly nothing. " +
                "$moneyKept stays in your account. Breathe.",
            route = "tracking/$orderId",
        )
    }

    fun notifyPriceDrop(context: Context, productId: Int, productName: String, discountPercent: Int, newPrice: String) {
        post(
            context,
            id = 2_000 + productId,
            title = "🔻 Price drop on your wishlist!",
            text = "$productName just fell $discountPercent% to $newPrice. " +
                "It still costs you \$0.00. The deal of literally no lifetime.",
            route = "product/$productId",
        )
    }

    @SuppressLint("MissingPermission") // guarded by areNotificationsEnabled + catch
    private fun post(context: Context, id: Int, title: String, text: String, route: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        val contentIntent = PendingIntent.getActivity(
            context,
            id, // unique per notification so routes don't overwrite each other
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_ROUTE, route),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(id, notification)
        } catch (_: SecurityException) {
            // Permission revoked mid-flight; the nothing will arrive silently.
        }
    }
}
