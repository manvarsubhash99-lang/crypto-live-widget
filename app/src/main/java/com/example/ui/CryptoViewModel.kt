package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CryptoUiState(
    val topCoinIds: List<String> = listOf("bitcoin", "ethereum", "tether", "binancecoin", "solana"),
    val coins: List<CoinMarket> = emptyList(),
    val isLoading: Boolean = false,
    val isCachedWarning: Boolean = false,
    val currency: String = "inr",
    val selectedCoin: CoinMarket? = null,
    val chartData: List<List<Double>> = emptyList(),
    val isChartLoading: Boolean = false,
    val selectedTimeRange: String = "24h",
    val searchResults: List<SearchCoinItem> = emptyList(),
    val isSearching: Boolean = false,
    val alerts: List<PriceAlert> = listOf(
        PriceAlert(
            id = "default-1",
            coinId = "bitcoin",
            coinName = "Bitcoin",
            coinSymbol = "BTC",
            targetPrice = 10000000.0,
            currency = "inr",
            isAbove = true,
            isActive = true
        ),
        PriceAlert(
            id = "default-2",
            coinId = "ethereum",
            coinName = "Ethereum",
            coinSymbol = "ETH",
            targetPrice = 300000.0,
            currency = "inr",
            isAbove = false,
            isActive = true
        )
    ),
    val triggeredAlertMessage: String? = null,
    // Widget Mode properties
    val isWidgetModeActive: Boolean = false,
    val isWidgetExpanded: Boolean = false,
    val widgetOpacity: Float = 0.95f,
    val isAlwaysOnTop: Boolean = true,
    // Settings properties
    val refreshIntervalSeconds: Int = 20,
    val startOnBoot: Boolean = true,
    val startMinimized: Boolean = false,
    val showTrayIcon: Boolean = true,
    val showMiniChart: Boolean = true,
    val showPercentage: Boolean = true,
    val alertSounds: Boolean = true
)

class CryptoViewModel(
    private val repository: CryptoRepository = CryptoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CryptoUiState())
    val uiState: StateFlow<CryptoUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    init {
        fetchPrices()
        startPolling()
    }

    fun fetchPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val ids = _uiState.value.topCoinIds
            val curr = _uiState.value.currency

            val result = repository.getTopCoinsMarkets(ids, curr)
            result.onSuccess { (markets, isCached) ->
                _uiState.update { current ->
                    current.copy(
                        coins = markets,
                        isLoading = false,
                        isCachedWarning = isCached
                    )
                }
                checkAlerts(markets)
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, isCachedWarning = true) }
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                delay(_uiState.value.refreshIntervalSeconds * 1000L)
                fetchPrices()
            }
        }
    }

    fun setCurrency(currency: String) {
        _uiState.update { it.copy(currency = currency) }
        fetchPrices()
        _uiState.value.selectedCoin?.let {
            loadCoinDetails(it)
        }
    }

    fun selectCoin(coin: CoinMarket) {
        _uiState.update { it.copy(selectedCoin = coin) }
        loadChart(coin.id, _uiState.value.selectedTimeRange)
    }

    fun clearSelectedCoin() {
        _uiState.update { it.copy(selectedCoin = null, chartData = emptyList()) }
    }

    fun setTimeRange(range: String) {
        _uiState.update { it.copy(selectedTimeRange = range) }
        _uiState.value.selectedCoin?.let { coin ->
            loadChart(coin.id, range)
        }
    }

    private fun loadChart(coinId: String, range: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChartLoading = true) }
            val prices = repository.getMarketChart(coinId, _uiState.value.currency, range)
            _uiState.update { it.copy(chartData = prices, isChartLoading = false) }
        }
    }

    private fun loadCoinDetails(coin: CoinMarket) {
        loadChart(coin.id, _uiState.value.selectedTimeRange)
    }

    fun searchCoins(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val results = repository.searchCoins(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun replaceCoin(oldCoinId: String, newCoinId: String) {
        val currentIds = _uiState.value.topCoinIds.toMutableList()
        val index = currentIds.indexOf(oldCoinId)
        if (index != -1) {
            currentIds[index] = newCoinId
        } else {
            if (!currentIds.contains(newCoinId)) {
                currentIds.add(newCoinId)
                if (currentIds.size > 5) currentIds.removeAt(0)
            }
        }
        _uiState.update { it.copy(topCoinIds = currentIds) }
        fetchPrices()
    }

    fun reorderCoins(fromIndex: Int, toIndex: Int) {
        val currentIds = _uiState.value.topCoinIds.toMutableList()
        if (fromIndex in currentIds.indices && toIndex in currentIds.indices) {
            val item = currentIds.removeAt(fromIndex)
            currentIds.add(toIndex, item)
            _uiState.update { it.copy(topCoinIds = currentIds) }
            fetchPrices()
        }
    }

    // Alerts
    fun addAlert(coinId: String, coinName: String, coinSymbol: String, targetPrice: Double, isAbove: Boolean) {
        val newAlert = PriceAlert(
            id = UUID.randomUUID().toString(),
            coinId = coinId,
            coinName = coinName,
            coinSymbol = coinSymbol.uppercase(),
            targetPrice = targetPrice,
            currency = _uiState.value.currency,
            isAbove = isAbove,
            isActive = true
        )
        _uiState.update { it.copy(alerts = it.alerts + newAlert) }
    }

    fun toggleAlert(alertId: String) {
        _uiState.update { state ->
            val updated = state.alerts.map { a ->
                if (a.id == alertId) a.copy(isActive = !a.isActive) else a
            }
            state.copy(alerts = updated)
        }
    }

    fun deleteAlert(alertId: String) {
        _uiState.update { state ->
            state.copy(alerts = state.alerts.filter { it.id != alertId })
        }
    }

    fun dismissTriggeredAlert() {
        _uiState.update { it.copy(triggeredAlertMessage = null) }
    }

    private fun checkAlerts(coins: List<CoinMarket>) {
        val activeAlerts = _uiState.value.alerts.filter { it.isActive }
        val triggered = mutableListOf<String>()

        for (alert in activeAlerts) {
            val coin = coins.find { it.id.equals(alert.coinId, ignoreCase = true) } ?: continue
            val currentPrice = coin.currentPrice

            val hit = if (alert.isAbove) {
                currentPrice >= alert.targetPrice
            } else {
                currentPrice <= alert.targetPrice
            }

            if (hit) {
                val symbol = if (alert.isAbove) ">" else "<"
                triggered.add("🔔 ${alert.coinSymbol} Price Alert: Current price ${coin.symbol.uppercase()} reached $symbol ${alert.targetPrice}")
            }
        }

        if (triggered.isNotEmpty()) {
            _uiState.update { it.copy(triggeredAlertMessage = triggered.first()) }
        }
    }

    // Widget Mode Controls
    fun setWidgetMode(active: Boolean) {
        _uiState.update { it.copy(isWidgetModeActive = active) }
    }

    fun toggleWidgetExpanded() {
        _uiState.update { it.copy(isWidgetExpanded = !it.isWidgetExpanded) }
    }

    fun setWidgetOpacity(opacity: Float) {
        _uiState.update { it.copy(widgetOpacity = opacity.coerceIn(0.3f, 1.0f)) }
    }

    fun toggleAlwaysOnTop() {
        _uiState.update { it.copy(isAlwaysOnTop = !it.isAlwaysOnTop) }
    }

    fun updateSettings(
        refreshSeconds: Int,
        startOnBoot: Boolean,
        startMinimized: Boolean,
        showTrayIcon: Boolean,
        showMiniChart: Boolean,
        showPercentage: Boolean,
        alertSounds: Boolean
    ) {
        _uiState.update {
            it.copy(
                refreshIntervalSeconds = refreshSeconds,
                startOnBoot = startOnBoot,
                startMinimized = startMinimized,
                showTrayIcon = showTrayIcon,
                showMiniChart = showMiniChart,
                showPercentage = showPercentage,
                alertSounds = alertSounds
            )
        }
        startPolling()
    }
}
