package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.SearchCoinItem
import com.example.ui.theme.*

@Composable
fun CoinSearchDialog(
    targetCoinId: String?,
    searchResults: List<SearchCoinItem>,
    isSearching: Boolean,
    onSearch: (String) -> Unit,
    onSelectCoin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = BorderStroke(1.2.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (targetCoinId != null) "Replace Coin" else "Add / Select Coin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = LightText
                        )
                        if (targetCoinId != null) {
                          Text(
                              text = "Replacing: ${targetCoinId.uppercase()}",
                              fontSize = 12.sp,
                              color = NeonCyan
                          )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MutedText
                        )
                    }
                }

                // Search Input Field
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        onSearch(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("coin_search_input"),
                    placeholder = { Text("Search crypto (e.g. ADA, XRP, Doge)...", fontSize = 13.sp, color = DimText) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText
                    ),
                    singleLine = true
                )

                // Results list
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isSearching -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = NeonGreen
                            )
                        }
                        searchResults.isEmpty() && query.length >= 2 -> {
                            Text(
                                text = "No cryptocurrencies found for \"$query\"",
                                modifier = Modifier.align(Alignment.Center),
                                color = DimText,
                                fontSize = 13.sp
                            )
                        }
                        query.length < 2 -> {
                            Text(
                                text = "Type at least 2 characters to search over 10,000+ coins on CoinGecko",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 20.dp),
                                color = DimText,
                                fontSize = 12.sp
                            )
                        }
                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(searchResults) { coin ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                onSelectCoin(coin.id)
                                                onDismiss()
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = CardDark,
                                        border = BorderStroke(0.8.dp, GlassBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                AsyncImage(
                                                    model = coin.large ?: coin.thumb,
                                                    contentDescription = coin.name,
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Column {
                                                    Text(
                                                        text = coin.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = LightText
                                                    )
                                                    Text(
                                                        text = coin.symbol.uppercase(),
                                                        fontSize = 11.sp,
                                                        color = MutedText
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    onSelectCoin(coin.id)
                                                    onDismiss()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = NeonGreen.copy(alpha = 0.2f),
                                                    contentColor = NeonGreen
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(
                                                    text = if (targetCoinId != null) "Select" else "Add",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
