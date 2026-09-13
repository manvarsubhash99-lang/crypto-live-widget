package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CoinMarket
import com.example.data.PriceAlert
import com.example.ui.theme.*
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    alerts: List<PriceAlert>,
    availableCoins: List<CoinMarket>,
    currency: String,
    onAddAlert: (String, String, String, Double, Boolean) -> Unit,
    onToggleAlert: (String) -> Unit,
    onDeleteAlert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCoinId by remember(availableCoins) {
        mutableStateOf(availableCoins.firstOrNull()?.id ?: "bitcoin")
    }
    var isAbove by remember { mutableStateOf(true) }
    var targetPriceText by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val selectedCoin = availableCoins.find { it.id == selectedCoinId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column {
            Text(
                text = "Cryptocurrency Price Alerts",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = LightText
            )
            Text(
                text = "Get real-time notification alerts when market thresholds trigger",
                fontSize = 12.sp,
                color = MutedText
            )
        }

        // Add Alert Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardDark,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Create Target Alert",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = LightText
                )

                // Coin Selector
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCoin?.let { "${it.name} (${it.symbol.uppercase()})" } ?: "Select Coin",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        availableCoins.forEach { coin ->
                            DropdownMenuItem(
                                text = { Text("${coin.name} (${coin.symbol.uppercase()})") },
                                onClick = {
                                    selectedCoinId = coin.id
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Condition Toggle: Above (>) vs Below (<)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = isAbove,
                        onClick = { isAbove = true },
                        label = { Text("Price Rises Above (>)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                            selectedLabelColor = NeonGreen
                        )
                    )
                    FilterChip(
                        selected = !isAbove,
                        onClick = { isAbove = false },
                        label = { Text("Price Drops Below (<)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonRed.copy(alpha = 0.2f),
                            selectedLabelColor = NeonRed
                        )
                    )
                }

                // Target Price Input
                OutlinedTextField(
                    value = targetPriceText,
                    onValueChange = { targetPriceText = it },
                    label = { Text("Target Price (${FormatUtils.getCurrencySymbol(currency)})") },
                    placeholder = { Text("e.g. 10000000", color = DimText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert_target_price_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText
                    )
                )

                Button(
                    onClick = {
                        val target = targetPriceText.toDoubleOrNull()
                        if (target != null && selectedCoin != null) {
                            onAddAlert(
                                selectedCoin.id,
                                selectedCoin.name,
                                selectedCoin.symbol,
                                target,
                                isAbove
                            )
                            targetPriceText = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("add_alert_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = SurfaceDark)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Alert", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }

        // Active Alerts List
        Text(
            text = "Active Price Monitors (${alerts.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = LightText
        )

        if (alerts.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CardDark,
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No alerts configured. Set a price alert above to receive live notifications.",
                        color = DimText,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(alerts) { alert ->
                    val condSymbol = if (alert.isAbove) ">" else "<"
                    val condColor = if (alert.isAbove) NeonGreen else NeonRed

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CardDark,
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (alert.isActive) NeonCyan else DimText,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "${alert.coinSymbol} (${alert.coinName})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = LightText
                                    )
                                    Text(
                                        text = "Trigger when price is $condSymbol ${FormatUtils.formatPrice(alert.targetPrice, alert.currency)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = condColor
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = alert.isActive,
                                    onCheckedChange = { onToggleAlert(alert.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonGreen,
                                        checkedTrackColor = NeonGreen.copy(alpha = 0.3f)
                                    )
                                )
                                IconButton(onClick = { onDeleteAlert(alert.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete alert",
                                        tint = NeonRed.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
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
