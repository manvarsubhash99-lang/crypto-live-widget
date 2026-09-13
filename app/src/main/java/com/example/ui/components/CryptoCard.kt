package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun CryptoCard(
    coin: CoinMarket,
    currency: String,
    onClick: () -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val change24 = coin.priceChangePercentage24h ?: 0.0
    val isPositive = change24 >= 0.0
    val changeColor = if (isPositive) NeonGreen else NeonRed
    val changeBg = if (isPositive) GreenSurface else RedSurface

    // Secondary price: USD if primary is INR, else INR
    val isPrimaryInr = currency.lowercase() == "inr"
    val secondaryCurrency = if (isPrimaryInr) "usd" else "inr"
    val secondaryPrice = if (isPrimaryInr) {
        coin.currentPrice / 87.5
    } else {
        coin.currentPrice * 87.5
    }

    val sparklinePoints = coin.sparklineIn7d?.price?.takeLast(24) ?: emptyList()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("crypto_card_${coin.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row: Logo, Name, Symbol, Rank, More Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = coin.image,
                        contentDescription = "${coin.name} logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = coin.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    coin.marketCapRank?.let { rank ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceDark,
                            border = BorderStroke(0.8.dp, GlassBorder)
                        ) {
                            Text(
                                text = "#$rank",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onManageClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("manage_coin_${coin.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Manage coin",
                            tint = MutedText
                        )
                    }
                }
            }

            // Dual Price Display
            Column {
                Text(
                    text = FormatUtils.formatPrice(coin.currentPrice, currency),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = LightText
                )
                Text(
                    text = FormatUtils.formatPrice(secondaryPrice, secondaryCurrency),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = MutedText
                )
            }

            // 24H Percentage Change Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = changeBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.North else Icons.Default.South,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "24H: ${FormatUtils.formatPercentage(change24)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                }
            }

            // Market Details Grid: High, Low, Volume, Market Cap
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceDark.copy(alpha = 0.6f),
                border = BorderStroke(0.8.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatLabelValue(
                            label = "High:",
                            value = FormatUtils.formatCompact(coin.high24h, currency)
                        )
                        StatLabelValue(
                            label = "Low:",
                            value = FormatUtils.formatCompact(coin.low24h, currency)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatLabelValue(
                            label = "Volume:",
                            value = FormatUtils.formatCompact(coin.totalVolume, currency)
                        )
                        StatLabelValue(
                            label = "Market Cap:",
                            value = FormatUtils.formatCompact(coin.marketCap, currency)
                        )
                    }
                }
            }

            // 24H Mini Chart / Sparkline
            if (sparklinePoints.isNotEmpty()) {
                SparklineChart(
                    prices = sparklinePoints,
                    isPositive = isPositive,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Footer: Last updated
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updated: ${coin.lastUpdated?.take(10) ?: "Live"}",
                    fontSize = 10.sp,
                    color = DimText
                )
                Text(
                    text = "View Analytics ➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
        }
    }
}

@Composable
private fun StatLabelValue(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 11.sp, color = DimText)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = MutedText
        )
    }
}
