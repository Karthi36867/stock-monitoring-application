package com.karthi.stockmonitor.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.karthi.stockmonitor.data.repository.StockRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val CHANNEL_ID = "price_alerts"

/**
 * Runs periodically (see setup in MainActivity) to check whether any
 * watchlist stock has crossed its user-set alert price, and fires a
 * local notification if so.
 */
@HiltWorker
class PriceAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: StockRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val stocksWithAlerts = repository.getStocksWithAlerts()

        stocksWithAlerts.forEach { stock ->
            val refreshed = repository.refreshQuote(stock.symbol, stock.companyName)
            if (refreshed is com.karthi.stockmonitor.data.repository.Result.Success) {
                val price = refreshed.data.lastPrice
                val target = stock.alertPrice ?: return@forEach
                val crossed = (stock.lastPrice < target && price >= target) ||
                        (stock.lastPrice > target && price <= target)
                if (crossed) {
                    sendNotification(stock.symbol, price, target)
                }
            }
        }
        return Result.success()
    }

    private fun sendNotification(symbol: String, price: Double, target: Double) {
        val context = applicationContext
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Price Alerts", NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$symbol hit your target")
            .setContentText("Now trading at $price (target: $target)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(symbol.hashCode(), notification)
    }
}
