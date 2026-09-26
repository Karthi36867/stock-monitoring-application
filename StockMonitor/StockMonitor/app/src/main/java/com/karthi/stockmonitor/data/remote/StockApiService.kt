package com.karthi.stockmonitor.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Thin wrapper around Alpha Vantage's free stock API.
 * Docs: https://www.alphavantage.co/documentation/
 *
 * Swap this out for Finnhub or another provider if you hit Alpha
 * Vantage's free-tier rate limit (5 calls/min, 100/day).
 */
interface StockApiService {

    @GET("query")
    suspend fun getQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): QuoteResponse

    @GET("query")
    suspend fun searchSymbol(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String
    ): SearchResponse

    @GET("query")
    suspend fun getDailySeries(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): DailySeriesResponse
}

// ---------- DTOs (match Alpha Vantage's JSON field names) ----------

data class QuoteResponse(
    val `Global Quote`: GlobalQuote?
)

data class GlobalQuote(
    val `01. symbol`: String,
    val `05. price`: String,
    val `09. change`: String,
    val `10. change percent`: String
)

data class SearchResponse(
    val bestMatches: List<SymbolMatch>?
)

data class SymbolMatch(
    val `1. symbol`: String,
    val `2. name`: String
)

data class DailySeriesResponse(
    val `Time Series (Daily)`: Map<String, DailyPoint>?
)

data class DailyPoint(
    val `4. close`: String
)
