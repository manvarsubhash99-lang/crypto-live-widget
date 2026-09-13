package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CoinMarket
import com.example.ui.CryptoUiState
import com.example.ui.components.CryptoCard
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    state: CryptoUiState,
    onCoinClick: (CoinMarket) -> Unit,
    onManageCoin: (String) -> Unit,
    onReorderCoins: (Int, Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var manageSheetCoinId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Warning Banner if API is limited or using cache
        if (state.isCachedWarning) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NeonAmber.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "⚠️", fontSize = 16.sp)
                    Text(
                        text = "Unable to reach CoinGecko live feed — showing cached market data.",
                        fontSize = 12.sp,
                        color = LightText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section Title & Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Top 5 Cryptocurrencies",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = LightText
                )
                Text(
                    text = "Live price widget & detailed analytics",
                    fontSize = 12.sp,
                    color = MutedText
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { onManageCoin("") },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardDark,
                        contentColor = NeonCyan
                    ),
                    border = BorderStroke(0.8.dp, GlassBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("manage_top5_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Edit (${state.coins.size}/5)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(34.dp)
                        .background(CardDark, RoundedCornerShape(8.dp))
                        .testTag("refresh_dashboard_button")
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = NeonGreen,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh prices",
                            tint = NeonGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Coins List
        if (state.coins.isEmpty() && state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(state.coins) { index, coin ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CryptoCard(
                            coin = coin,
                            currency = state.currency,
                            onClick = { onCoinClick(coin) },
                            onManageClick = { manageSheetCoinId = coin.id }
                        )

                        // Quick Reorder Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (index > 0) {
                                TextButton(
                                    onClick = { onReorderCoins(index, index - 1) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("▲ Move Up", fontSize = 10.sp, color = MutedText)
                                }
                            }
                            if (index < state.coins.size - 1) {
                                TextButton(
                                    onClick = { onReorderCoins(index, index + 1) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("▼ Move Down", fontSize = 10.sp, color = MutedText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal action sheet to replace coin
    manageSheetCoinId?.let { coinId ->
        AlertDialog(
            onDismissRequest = { manageSheetCoinId = null },
            title = {
                Text(
                    text = "Manage ${coinId.uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = LightText
                )
            },
            text = {
                Text(
                    text = "Would you like to search and replace ${coinId.uppercase()} in your Top 5 desktop widget?",
                    fontSize = 13.sp,
                    color = MutedText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = coinId
                        manageSheetCoinId = null
                        onManageCoin(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Search & Replace", color = SurfaceDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { manageSheetCoinId = null }) {
                    Text("Cancel", color = MutedText)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
