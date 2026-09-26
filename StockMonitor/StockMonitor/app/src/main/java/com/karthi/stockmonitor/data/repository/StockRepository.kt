package com.karthi.stockmonitor.data.repository

import com.karthi.stockmonitor.BuildConfig
import com.karthi.stockmonitor.data.local.PortfolioEntity
import com.karthi.stockmonitor.data.local.StockDao
import com.karthi.stockmonitor.data.local.StockEntity
import com.karthi.stockmonitor.data.remote.StockApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Simple sealed result so the UI can distinguish success/error/loading. */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

@Singleton
class StockRepository @Inject constructor(
    private val dao: StockDao,
    private val api: StockApiService
) {
    private val apiKey = BuildConfig.STOCK_API_KEY

    fun watchlist(): Flow<List<StockEntity>> = dao.getWatchlist()
    fun portfolio(): Flow<List<PortfolioEntity>> = dao.getPortfolio()

    suspend fun searchSymbols(query: String): Result<List<Pair<String, String>>> {
        return try {
            val response = api.searchSymbol(keywords = query, apiKey = apiKey)
            val matches = response.bestMatches?.map { it.`1. symbol` to it.`2. name` } ?: emptyList()
            Result.Success(matches)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Search failed")
        }
    }

    /** Fetches a fresh quote from the network and caches it in Room. */
    suspend fun refreshQuote(symbol: String, companyName: String): Result<StockEntity> {
        return try {
            val response = api.getQuote(symbol = symbol, apiKey = apiKey)
            val quote = response.`Global Quote`
                ?: return Result.Error("No data returned for $symbol")

            val price = quote.`05. price`.toDoubleOrNull() ?: 0.0
            val changePercent = quote.`10. change percent`
                .removeSuffix("%")
                .toDoubleOrNull() ?: 0.0

            val entity = StockEntity(
                symbol = symbol,
                companyName = companyName,
                lastPrice = price,
                changePercent = changePercent,
                lastUpdated = System.currentTimeMillis(),
                alertPrice = dao.getStock(symbol)?.alertPrice
            )
            dao.upsertStock(entity)
            Result.Success(entity)
        } catch (e: Exception) {
            // Network failed — fall back to whatever is cached, so the UI
            // still has something to show (offline support).
            val cached = dao.getStock(symbol)
            if (cached != null) Result.Success(cached)
            else Result.Error(e.message ?: "Failed to fetch $symbol")
        }
    }

    suspend fun getDailyClosePrices(symbol: String): Result<List<Pair<String, Double>>> {
        return try {
            val response = api.getDailySeries(symbol = symbol, apiKey = apiKey)
            val series = response.`Time Series (Daily)` ?: emptyMap()
            val points = series.entries
                .sortedBy { it.key }
                .takeLast(30) // last 30 trading days
                .map { it.key to (it.value.`4. close`.toDoubleOrNull() ?: 0.0) }
            Result.Success(points)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to load chart data")
        }
    }

    suspend fun removeFromWatchlist(symbol: String) = dao.removeFromWatchlist(symbol)

    suspend fun setAlert(symbol: String, price: Double?) = dao.setAlertPrice(symbol, price)

    suspend fun getStocksWithAlerts() = dao.getStocksWithAlerts()

    suspend fun addHolding(holding: PortfolioEntity) = dao.upsertHolding(holding)

    suspend fun removeHolding(symbol: String) = dao.removeHolding(symbol)
}
