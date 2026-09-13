package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CryptoViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoLiveTheme {
                CryptoLiveAppRoot()
            }
        }
    }
}

enum class NavDestination(val label: String) {
    DASHBOARD("Top 5"),
    WIDGET("Widget"),
    ALERTS("Alerts"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoLiveAppRoot(viewModel: CryptoViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var currentNav by remember { mutableStateOf(NavDestination.DASHBOARD) }
    var searchTargetCoinId by remember { mutableStateOf<String?>(null) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var isCurrencyMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            Surface(
                color = SurfaceDark,
                border = BorderStroke(0.8.dp, GlassBorder)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand Logo + Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₿",
                                    color = Color.Black,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Column {
                                Text(
                                    text = "CryptoLive Widget",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LightText
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(NeonGreen)
                                    )
                                    Text(
                                        text = "CoinGecko Live Feed",
                                        fontSize = 10.sp,
                                        color = MutedText
                                    )
                                }
                            }
                        }

                        // Currency Selector & Refresh
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CardDark,
                                    border = BorderStroke(0.8.dp, GlassBorder),
                                    modifier = Modifier
                                        .clickable { isCurrencyMenuOpen = true }
                                        .testTag("currency_selector_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = state.currency.uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NeonGreen
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = MutedText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isCurrencyMenuOpen,
                                    onDismissRequest = { isCurrencyMenuOpen = false }
                                ) {
                                    listOf("inr" to "INR (₹)", "usd" to "USD ($)", "eur" to "EUR (€)", "gbp" to "GBP (£)").forEach { (code, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name, fontSize = 12.sp) },
                                            onClick = {
                                                viewModel.setCurrency(code)
                                                isCurrencyMenuOpen = false
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.fetchPrices() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(CardDark, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Navigation Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NavDestination.values().forEach { destination ->
                            val isSelected = currentNav == destination && state.selectedCoin == null
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NeonGreen.copy(alpha = 0.18f) else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.clearSelectedCoin()
                                        currentNav = destination
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = destination.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) NeonGreen else MutedText
                                    )
                                    if (destination == NavDestination.ALERTS) {
                                        val activeCount = state.alerts.count { it.isActive }
                                        if (activeCount > 0) {
                                            Surface(
                                                shape = CircleShape,
                                                color = NeonGreen,
                                                modifier = Modifier.size(14.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$activeCount",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Content
            if (state.selectedCoin != null) {
                CoinDetailScreen(
                    coin = state.selectedCoin!!,
                    currency = state.currency,
                    chartData = state.chartData,
                    isChartLoading = state.isChartLoading,
                    selectedRange = state.selectedTimeRange,
                    onRangeChange = { viewModel.setTimeRange(it) },
                    onBack = { viewModel.clearSelectedCoin() }
                )
            } else {
                when (currentNav) {
                    NavDestination.DASHBOARD -> DashboardScreen(
                        state = state,
                        onCoinClick = { viewModel.selectCoin(it) },
                        onManageCoin = { targetId ->
                            searchTargetCoinId = if (targetId.isNotEmpty()) targetId else null
                            showSearchDialog = true
                        },
                        onReorderCoins = { from, to -> viewModel.reorderCoins(from, to) },
                        onRefresh = { viewModel.fetchPrices() }
                    )

                    NavDestination.WIDGET -> WidgetModeScreen(
                        state = state,
                        onCoinClick = { viewModel.selectCoin(it) },
                        onToggleExpanded = { viewModel.toggleWidgetExpanded() },
                        onOpacityChange = { viewModel.setWidgetOpacity(it) },
                        onToggleAlwaysOnTop = { viewModel.toggleAlwaysOnTop() }
                    )

                    NavDestination.ALERTS -> AlertsScreen(
                        alerts = state.alerts,
                        availableCoins = state.coins,
                        currency = state.currency,
                        onAddAlert = { coinId, name, symbol, target, isAbove ->
                            viewModel.addAlert(coinId, name, symbol, target, isAbove)
                        },
                        onToggleAlert = { viewModel.toggleAlert(it) },
                        onDeleteAlert = { viewModel.deleteAlert(it) }
                    )

                    NavDestination.SETTINGS -> SettingsScreen(
                        state = state,
                        onCurrencyChange = { viewModel.setCurrency(it) },
                        onUpdateSettings = { refresh, boot, min, tray, chart, pct, sound ->
                            viewModel.updateSettings(refresh, boot, min, tray, chart, pct, sound)
                        }
                    )
                }
            }

            // Notification Banner for triggered alerts
            AnimatedVisibility(
                visible = state.triggeredAlertMessage != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonGreen,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.triggeredAlertMessage ?: "",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissTriggeredAlert() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }

            // Coin Search / Replacement Dialog
            if (showSearchDialog) {
                CoinSearchDialog(
                    targetCoinId = searchTargetCoinId,
                    searchResults = state.searchResults,
                    isSearching = state.isSearching,
                    onSearch = { viewModel.searchCoins(it) },
                    onSelectCoin = { newCoinId ->
                        if (searchTargetCoinId != null) {
                            viewModel.replaceCoin(searchTargetCoinId!!, newCoinId)
                        } else {
                            val current = state.topCoinIds.firstOrNull() ?: "bitcoin"
                            viewModel.replaceCoin(current, newCoinId)
                        }
                        showSearchDialog = false
                    },
                    onDismiss = { showSearchDialog = false }
                )
            }
        }
    }
}
