package com.karthi.stockmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthi.stockmonitor.data.local.PortfolioEntity
import com.karthi.stockmonitor.data.local.StockEntity
import com.karthi.stockmonitor.data.repository.Result
import com.karthi.stockmonitor.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Pair<String, String>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    val watchlist: StateFlow<List<StockEntity>> =
        repository.watchlist().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val portfolio: StateFlow<List<PortfolioEntity>> =
        repository.portfolio().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _chartPoints = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val chartPoints: StateFlow<List<Pair<String, Double>>> = _chartPoints.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
        if (query.length < 2) {
            _searchState.value = _searchState.value.copy(results = emptyList())
            return
        }
        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(isLoading = true, error = null)
            when (val result = repository.searchSymbols(query)) {
                is Result.Success -> _searchState.value =
                    _searchState.value.copy(results = result.data, isLoading = false)
                is Result.Error -> _searchState.value =
                    _searchState.value.copy(error = result.message, isLoading = false)
            }
        }
    }

    fun addToWatchlist(symbol: String, name: String) {
        viewModelScope.launch { repository.refreshQuote(symbol, name) }
    }

    fun refreshAll() {
        viewModelScope.launch {
            watchlist.value.forEach { repository.refreshQuote(it.symbol, it.companyName) }
        }
    }

    fun removeFromWatchlist(symbol: String) {
        viewModelScope.launch { repository.removeFromWatchlist(symbol) }
    }

    fun setAlert(symbol: String, price: Double?) {
        viewModelScope.launch { repository.setAlert(symbol, price) }
    }

    fun loadChart(symbol: String) {
        viewModelScope.launch {
            when (val result = repository.getDailyClosePrices(symbol)) {
                is Result.Success -> _chartPoints.value = result.data
                is Result.Error -> _chartPoints.value = emptyList()
            }
        }
    }

    fun addHolding(symbol: String, name: String, qty: Double, buyPrice: Double) {
        viewModelScope.launch {
            repository.addHolding(
                PortfolioEntity(
                    symbol = symbol,
                    companyName = name,
                    quantity = qty,
                    buyPrice = buyPrice,
                    buyDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun removeHolding(symbol: String) {
        viewModelScope.launch { repository.removeHolding(symbol) }
    }

    /** Gain/loss for a holding against the current cached watchlist price, if known. */
    fun gainLossFor(holding: PortfolioEntity): Double {
        val currentPrice = watchlist.value.find { it.symbol == holding.symbol }?.lastPrice
            ?: holding.buyPrice
        return (currentPrice - holding.buyPrice) * holding.quantity
    }
}
