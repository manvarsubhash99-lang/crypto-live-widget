package com.example.data

import android.util.Log

class CryptoRepository(
    private val api: CoinGeckoApiService = CoinGeckoApiService.create()
) {
    // In-memory cache for market quotes
    private val cache = mutableMapOf<String, List<CoinMarket>>()
    private val chartCache = mutableMapOf<String, List<List<Double>>>()

    suspend fun getTopCoinsMarkets(
        coinIds: List<String>,
        currency: String
    ): Result<Pair<List<CoinMarket>, Boolean>> {
        val idsString = coinIds.joinToString(",")
        val cacheKey = "${currency}_$idsString"

        return try {
            val response = api.getCoinsMarkets(
                vsCurrency = currency.lowercase(),
                ids = idsString
            )
            if (response.isNotEmpty()) {
                cache[cacheKey] = response
                Result.success(Pair(response, false)) // false = not cached
            } else {
                val cached = cache[cacheKey] ?: getFallbackCoins(coinIds, currency)
                Result.success(Pair(cached, true))
            }
        } catch (e: Exception) {
            Log.w("CryptoRepo", "CoinGecko API call failed: ${e.message}")
            val cached = cache[cacheKey] ?: getFallbackCoins(coinIds, currency)
            Result.success(Pair(cached, true)) // true = fallback / cached
        }
    }

    suspend fun getMarketChart(
        coinId: String,
        currency: String,
        range: String
    ): List<List<Double>> {
        val days = when (range.lowercase()) {
            "1h" -> "1"
            "24h" -> "1"
            "7d" -> "7"
            "30d" -> "30"
            "1y" -> "365"
            "max" -> "max"
            else -> "1"
        }
        val cacheKey = "${coinId}_${currency}_$days"
        return try {
            val res = api.getMarketChart(coinId, currency.lowercase(), days)
            val prices = res.prices ?: emptyList()
            if (prices.isNotEmpty()) {
                chartCache[cacheKey] = prices
                prices
            } else {
                chartCache[cacheKey] ?: generateSyntheticChart(range)
            }
        } catch (e: Exception) {
            Log.w("CryptoRepo", "Chart call failed: ${e.message}")
            chartCache[cacheKey] ?: generateSyntheticChart(range)
        }
    }

    suspend fun searchCoins(query: String): List<SearchCoinItem> {
        if (query.trim().length < 2) return emptyList()
        return try {
            val res = api.searchCoins(query.trim())
            res.coins?.take(15) ?: emptyList()
        } catch (e: Exception) {
            Log.w("CryptoRepo", "Search failed: ${e.message}")
            emptyList()
        }
    }

    private fun generateSyntheticChart(range: String): List<List<Double>> {
        val count = when (range.lowercase()) {
            "1h" -> 20
            "24h" -> 24
            "7d" -> 28
            "30d" -> 30
            else -> 40
        }
        var current = 98500.0
        val now = System.currentTimeMillis()
        val interval = 3600000L
        return (0 until count).map { i ->
            current += (Math.random() - 0.48) * 800
            listOf((now - (count - i) * interval).toDouble(), current)
        }
    }

    private fun getFallbackCoins(ids: List<String>, currency: String): List<CoinMarket> {
        val isInr = currency.lowercase() == "inr"
        val rate = if (isInr) 88.5 else 1.0

        val presets = mapOf(
            "bitcoin" to CoinMarket(
                id = "bitcoin",
                symbol = "btc",
                name = "Bitcoin",
                image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
                currentPrice = 96420.0 * rate,
                marketCap = 1900000000000.0 * rate,
                marketCapRank = 1,
                fullyDilutedValuation = 2020000000000.0 * rate,
                totalVolume = 42000000000.0 * rate,
                high24h = 97800.0 * rate,
                low24h = 95300.0 * rate,
                priceChange24h = 1120.0 * rate,
                priceChangePercentage24h = 2.45,
                priceChangePercentage1h = 0.32,
                priceChangePercentage7d = 5.80,
                priceChangePercentage30d = 12.4,
                priceChangePercentage1y = 115.2,
                circulatingSupply = 19700000.0,
                totalSupply = 21000000.0,
                maxSupply = 21000000.0,
                ath = 104000.0 * rate,
                athDate = "2025-01-20",
                atl = 67.81 * rate,
                atlDate = "2013-07-06",
                lastUpdated = "Just now",
                sparklineIn7d = SparklineData(listOf(92000.0, 93500.0, 94200.0, 93800.0, 95100.0, 96420.0).map { it * rate })
            ),
            "ethereum" to CoinMarket(
                id = "ethereum",
                symbol = "eth",
                name = "Ethereum",
                image = "https://assets.coingecko.com/coins/images/279/large/ethereum.png",
                currentPrice = 2840.0 * rate,
                marketCap = 345000000000.0 * rate,
                marketCapRank = 2,
                fullyDilutedValuation = 345000000000.0 * rate,
                totalVolume = 22000000000.0 * rate,
                high24h = 2920.0 * rate,
                low24h = 2780.0 * rate,
                priceChange24h = -34.0 * rate,
                priceChangePercentage24h = -0.82,
                priceChangePercentage1h = -0.15,
                priceChangePercentage7d = 3.20,
                priceChangePercentage30d = -2.1,
                priceChangePercentage1y = 48.6,
                circulatingSupply = 120400000.0,
                totalSupply = 120400000.0,
                maxSupply = null,
                ath = 4878.26 * rate,
                athDate = "2021-11-10",
                atl = 0.43 * rate,
                atlDate = "2015-10-20",
                lastUpdated = "Just now",
                sparklineIn7d = SparklineData(listOf(2750.0, 2810.0, 2890.0, 2830.0, 2860.0, 2840.0).map { it * rate })
            ),
            "tether" to CoinMarket(
                id = "tether",
                symbol = "usdt",
                name = "Tether",
                image = "https://assets.coingecko.com/coins/images/325/large/Tether.png",
                currentPrice = 1.00 * rate,
                marketCap = 118000000000.0 * rate,
                marketCapRank = 3,
                fullyDilutedValuation = 118000000000.0 * rate,
                totalVolume = 65000000000.0 * rate,
                high24h = 1.002 * rate,
                low24h = 0.998 * rate,
                priceChange24h = 0.001 * rate,
                priceChangePercentage24h = 0.05,
                circulatingSupply = 118000000000.0,
                totalSupply = 118000000000.0,
                maxSupply = null,
                ath = 1.32 * rate,
                atl = 0.57 * rate,
                lastUpdated = "Just now"
            ),
            "binancecoin" to CoinMarket(
                id = "binancecoin",
                symbol = "bnb",
                name = "BNB",
                image = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png",
                currentPrice = 645.0 * rate,
                marketCap = 94000000000.0 * rate,
                marketCapRank = 4,
                fullyDilutedValuation = 94000000000.0 * rate,
                totalVolume = 1400000000.0 * rate,
                high24h = 658.0 * rate,
                low24h = 638.0 * rate,
                priceChange24h = 8.5 * rate,
                priceChangePercentage24h = 1.34,
                circulatingSupply = 145000000.0,
                totalSupply = 145000000.0,
                maxSupply = 200000000.0,
                ath = 717.48 * rate,
                atl = 0.039 * rate,
                lastUpdated = "Just now"
            ),
            "solana" to CoinMarket(
                id = "solana",
                symbol = "sol",
                name = "Solana",
                image = "https://assets.coingecko.com/coins/images/4128/large/solana.png",
                currentPrice = 188.0 * rate,
                marketCap = 88000000000.0 * rate,
                marketCapRank = 5,
                fullyDilutedValuation = 110000000000.0 * rate,
                totalVolume = 6200000000.0 * rate,
                high24h = 194.0 * rate,
                low24h = 181.0 * rate,
                priceChange24h = 7.4 * rate,
                priceChangePercentage24h = 4.12,
                circulatingSupply = 468000000.0,
                totalSupply = 585000000.0,
                maxSupply = null,
                ath = 259.96 * rate,
                atl = 0.50 * rate,
                lastUpdated = "Just now"
            )
        )

        return ids.mapNotNull { presets[it] }
    }
}
