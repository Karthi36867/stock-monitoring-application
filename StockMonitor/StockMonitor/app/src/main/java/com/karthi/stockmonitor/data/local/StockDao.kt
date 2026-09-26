package com.karthi.stockmonitor.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {

    // ---------- Watchlist ----------

    @Query("SELECT * FROM watchlist ORDER BY symbol ASC")
    fun getWatchlist(): Flow<List<StockEntity>>

    @Query("SELECT * FROM watchlist WHERE symbol = :symbol LIMIT 1")
    suspend fun getStock(symbol: String): StockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStock(stock: StockEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun removeFromWatchlist(symbol: String)

    @Query("UPDATE watchlist SET alertPrice = :alertPrice WHERE symbol = :symbol")
    suspend fun setAlertPrice(symbol: String, alertPrice: Double?)

    @Query("SELECT * FROM watchlist WHERE alertPrice IS NOT NULL")
    suspend fun getStocksWithAlerts(): List<StockEntity>

    // ---------- Portfolio ----------

    @Query("SELECT * FROM portfolio ORDER BY symbol ASC")
    fun getPortfolio(): Flow<List<PortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHolding(holding: PortfolioEntity)

    @Query("DELETE FROM portfolio WHERE symbol = :symbol")
    suspend fun removeHolding(symbol: String)
}
