package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.South
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CoinMarket
import com.example.ui.components.SparklineChart
import com.example.ui.theme.*
import com.example.util.FormatUtils
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CoinDetailScreen(
    coin: CoinMarket,
    currency: String,
    chartData: List<List<Double>>,
    isChartLoading: Boolean,
    selectedRange: String,
    onRangeChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val change24 = coin.priceChangePercentage24h ?: 0.0
    val isPos24 = change24 >= 0.0

    val change1h = coin.priceChangePercentage1h ?: 0.0
    val change7d = coin.priceChangePercentage7d ?: 0.0
    val change30d = coin.priceChangePercentage30d ?: 0.0
    val change1y = coin.priceChangePercentage1y ?: 0.0

    val chartPrices = chartData.mapNotNull { if (it.size > 1) it[1] else null }
    val isChartPos = if (chartPrices.size > 1) chartPrices.last() >= chartPrices.first() else isPos24

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button & Top Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardDark,
                    contentColor = LightText
                ),
                border = BorderStroke(1.dp, GlassBorder),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("back_to_dashboard_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Back to Top 5", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            coin.marketCapRank?.let { rank ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Text(
                        text = "Global Rank #$rank",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Hero Card: Coin Identity and Live Price
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = CardDark,
            border = BorderStroke(1.2.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = coin.image,
                            contentDescription = "${coin.name} logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = coin.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LightText
                            )
                            Text(
                                text = coin.symbol.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MutedText
                            )
                        }
                    }

                    // 24H Range high/low preview
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "24H Range", fontSize = 10.sp, color = DimText)
                        Text(
                            text = "${FormatUtils.formatCompact(coin.low24h, currency)} – ${FormatUtils.formatCompact(coin.high24h, currency)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = MutedText
                        )
                    }
                }

                // Price + 24H Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = FormatUtils.formatPrice(coin.currentPrice, currency),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = LightText
                    )
                    PercentageChangeBadge(value = change24)
                }
            }
        }

        // Interactive Price Chart with Time Ranges: 1H, 24H, 7D, 30D, 1Y, MAX
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = CardDark,
            border = BorderStroke(1.2.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historical Price Chart",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )
                    if (isChartLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = NeonGreen,
                            strokeWidth = 2.dp
                        )
                    }
                }

                // Timeframe Selector Chips (1H, 24H, 7D, 30D, 1Y, MAX)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark.copy(alpha = 0.7f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val ranges = listOf("1h", "24h", "7d", "30d", "1y", "max")
                    ranges.forEach { range ->
                        val isSelected = selectedRange.equals(range, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onRangeChange(range) },
                            color = if (isSelected) NeonGreen else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = range.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.Black else MutedText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Canvas Chart
                if (chartPrices.isNotEmpty()) {
                    SparklineChart(
                        prices = chartPrices,
                        isPositive = isChartPos,
                        height = 140.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isChartLoading) "Loading chart data..." else "Price trend visual unavailable",
                            color = DimText,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Performance Over Time Grid (1H, 24H, 7D, 30D, 1Y)
        Text(
            text = "Performance Over Time",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = LightText
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiniMetricBox("1H", change1h, Modifier.weight(1f))
            MiniMetricBox("24H", change24, Modifier.weight(1f))
            MiniMetricBox("7D", change7d, Modifier.weight(1f))
            MiniMetricBox("30D", change30d, Modifier.weight(1f))
            MiniMetricBox("1Y", change1y, Modifier.weight(1f))
        }

        // Detailed Market & Supply Metrics
        Text(
            text = "Market & Valuation",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = LightText
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardDark,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow("Market Capitalization", FormatUtils.formatPrice(coin.marketCap, currency))
                DetailRow("Fully Diluted Valuation (FDV)", FormatUtils.formatPrice(coin.fullyDilutedValuation, currency))
                DetailRow("24H Trading Volume", FormatUtils.formatPrice(coin.totalVolume, currency))
                DetailRow(
                    "Volume / Market Cap",
                    if (coin.marketCap != null && coin.totalVolume != null && coin.marketCap > 0) {
                        String.format(Locale.US, "%.4f", coin.totalVolume / coin.marketCap)
                    } else "—"
                )
            }
        }

        // Supply Statistics & All-Time High / Low
        Text(
            text = "Supply & Historical Records",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = LightText
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardDark,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow("Circulating Supply", formatSupply(coin.circulatingSupply))
                DetailRow("Total Supply", formatSupply(coin.totalSupply))
                DetailRow("Maximum Supply", if (coin.maxSupply != null) formatSupply(coin.maxSupply) else "∞ (Unlimited)")
                DetailRow("All-Time High (ATH)", "${FormatUtils.formatPrice(coin.ath, currency)} (${coin.athDate?.take(10) ?: "—"})", NeonGreen)
                DetailRow("All-Time Low (ATL)", "${FormatUtils.formatPrice(coin.atl, currency)} (${coin.atlDate?.take(10) ?: "—"})", NeonRed)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MiniMetricBox(label: String, value: Double, modifier: Modifier = Modifier) {
    val isPos = value >= 0.0
    val color = if (isPos) NeonGreen else NeonRed
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CardDark,
        border = BorderStroke(0.8.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DimText)
            Text(
                text = FormatUtils.formatPercentage(value),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = LightText) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MutedText)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = valueColor
        )
    }
}

@Composable
private fun PercentageChangeBadge(value: Double) {
    val isPos = value >= 0.0
    val color = if (isPos) NeonGreen else NeonRed
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isPos) GreenSurface else RedSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (isPos) Icons.Default.North else Icons.Default.South,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = FormatUtils.formatPercentage(value),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private fun formatSupply(value: Double?): String {
    if (value == null) return "—"
    return NumberFormat.getNumberInstance(Locale.US).format(value.toLong())
}
