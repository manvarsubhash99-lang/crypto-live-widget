package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SparklineData(
    @Json(name = "price") val price: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class CoinMarket(
    @Json(name = "id") val id: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: String = "",
    @Json(name = "current_price") val currentPrice: Double = 0.0,
    @Json(name = "market_cap") val marketCap: Double? = null,
    @Json(name = "market_cap_rank") val marketCapRank: Int? = null,
    @Json(name = "fully_diluted_valuation") val fullyDilutedValuation: Double? = null,
    @Json(name = "total_volume") val totalVolume: Double? = null,
    @Json(name = "high_24h") val high24h: Double? = null,
    @Json(name = "low_24h") val low24h: Double? = null,
    @Json(name = "price_change_24h") val priceChange24h: Double? = null,
    @Json(name = "price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    @Json(name = "price_change_percentage_1h_in_currency") val priceChangePercentage1h: Double? = null,
    @Json(name = "price_change_percentage_7d_in_currency") val priceChangePercentage7d: Double? = null,
    @Json(name = "price_change_percentage_30d_in_currency") val priceChangePercentage30d: Double? = null,
    @Json(name = "price_change_percentage_1y_in_currency") val priceChangePercentage1y: Double? = null,
    @Json(name = "circulating_supply") val circulatingSupply: Double? = null,
    @Json(name = "total_supply") val totalSupply: Double? = null,
    @Json(name = "max_supply") val maxSupply: Double? = null,
    @Json(name = "ath") val ath: Double? = null,
    @Json(name = "ath_date") val athDate: String? = null,
    @Json(name = "atl") val atl: Double? = null,
    @Json(name = "atl_date") val atlDate: String? = null,
    @Json(name = "last_updated") val lastUpdated: String? = null,
    @Json(name = "sparkline_in_7d") val sparklineIn7d: SparklineData? = null
)

@JsonClass(generateAdapter = true)
data class SearchCoinItem(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "market_cap_rank") val marketCapRank: Int? = null,
    @Json(name = "thumb") val thumb: String? = null,
    @Json(name = "large") val large: String? = null
)

@JsonClass(generateAdapter = true)
data class SearchResponse(
    @Json(name = "coins") val coins: List<SearchCoinItem>? = null
)

@JsonClass(generateAdapter = true)
data class MarketChartResponse(
    @Json(name = "prices") val prices: List<List<Double>>? = null
)

data class PriceAlert(
    val id: String,
    val coinId: String,
    val coinName: String,
    val coinSymbol: String,
    val targetPrice: Double,
    val currency: String = "inr",
    val isAbove: Boolean = true, // true: > target, false: < target
    val isActive: Boolean = true
)
