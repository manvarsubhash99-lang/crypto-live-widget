package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CoinMarket
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun CompactWidgetView(
    coins: List<CoinMarket>,
    currency: String,
    opacity: Float,
    isExpanded: Boolean,
    onCoinClick: (CoinMarket) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(opacity),
        shape = RoundedCornerShape(18.dp),
        color = CardDark.copy(alpha = 0.95f),
        border = BorderStroke(1.2.dp, NeonGreen.copy(alpha = 0.35f)),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Widget Titlebar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Text(
                        text = "CryptoLive Widget",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )
                }
                Text(
                    text = if (isExpanded) "Expanded Mode" else "Compact Mode",
                    fontSize = 10.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Coin Rows
            coins.take(5).forEach { coin ->
                val change24 = coin.priceChangePercentage24h ?: 0.0
                val isPos = change24 >= 0.0
                val color = if (isPos) NeonGreen else NeonRed
                val prefix = if (isPos) "+" else ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark.copy(alpha = 0.6f))
                        .clickable { onCoinClick(coin) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = coin.image,
                            contentDescription = coin.name,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = coin.symbol.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = LightText
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = FormatUtils.formatCompact(coin.currentPrice, currency),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = LightText
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = String.format("%s%.1f%%", prefix, change24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Widget Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Market Quotes",
                    fontSize = 10.sp,
                    color = DimText
                )
                Text(
                    text = "Updated 10:42 AM",
                    fontSize = 10.sp,
                    color = MutedText
                )
            }
        }
    }
}
