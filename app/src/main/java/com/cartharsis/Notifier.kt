package com.cartharsis

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
 * is intentional, the stress is not. Deliveries get a normal ping (one per
 * order, the payoff of the core loop); price drops live on a low-importance
 * channel so they land silently in the shade. When and how often anything fires
 * is decided upstream by [com.cartharsis.data.NotificationPolicy].
 */
object Notifier {

    private const val DELIVERY_CHANNEL_ID = "cartharsis.deliveries"
    private const val WISHLIST_CHANNEL_ID = "cartharsis.wishlist"

    /** Pre-rework channel that buzzed for everything; removed for upgraders. */
    private const val LEGACY_CHANNEL_ID = "cartharsis.events"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.deleteNotificationChannel(LEGACY_CHANNEL_ID)
            manager.createNotificationChannel(
                NotificationChannel(
                    DELIVERY_CHANNEL_ID,
                    "Deliveries of nothing",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "One ping per order, when your nothing arrives. Not real."
                },
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    WISHLIST_CHANNEL_ID,
                    "Wishlist price drifts",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Occasional, silent, entirely imaginary deal sightings."
                },
            )
        }
    }

    /** Intent extra carrying the in-app route a tapped notification should open. */
    const val EXTRA_ROUTE = "cartharsis.route"

    fun notifyDelivered(context: Context, orderId: Int, moneyKept: String) {
        post(
            context,
            channelId = DELIVERY_CHANNEL_ID,
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
            channelId = WISHLIST_CHANNEL_ID,
            id = 2_000 + productId,
            title = "🍃 A wishlist price drifted down",
            text = "$productName eased $discountPercent% to $newPrice. No timer, " +
                "no rush — it costs \$0.00 whenever you wander back.",
            route = "product/$productId",
        )
    }

    @SuppressLint("MissingPermission") // guarded by areNotificationsEnabled + catch
    private fun post(context: Context, channelId: String, id: Int, title: String, text: String, route: String) {
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
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        try {
            manager.notify(id, notification)
        } catch (_: SecurityException) {
            // Permission revoked mid-flight; the nothing will arrive silently.
        }
    }
}
