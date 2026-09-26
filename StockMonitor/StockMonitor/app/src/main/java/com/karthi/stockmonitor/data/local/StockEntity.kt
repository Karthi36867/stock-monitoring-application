package com.karthi.stockmonitor.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A stock the user is watching. Cached locally so the app still shows
 * the last known price when offline.
 */
@Entity(tableName = "watchlist")
data class StockEntity(
    @PrimaryKey val symbol: String,
    val companyName: String,
    val lastPrice: Double,
    val changePercent: Double,
    val lastUpdated: Long,
    // Optional price alert target; null means no alert set
    val alertPrice: Double? = null
)

/**
 * A user's holding of a stock — separate table from the watchlist so a
 * stock can be watched without being "owned", and vice versa.
 */
@Entity(tableName = "portfolio")
data class PortfolioEntity(
    @PrimaryKey val symbol: String,
    val companyName: String,
    val quantity: Double,
    val buyPrice: Double,
    val buyDate: Long
)
