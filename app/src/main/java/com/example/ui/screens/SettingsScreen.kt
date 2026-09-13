package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CryptoUiState
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    state: CryptoUiState,
    onCurrencyChange: (String) -> Unit,
    onUpdateSettings: (Int, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Preferences & Settings",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = LightText
            )
            Text(
                text = "Configure desktop widget modes, refresh periods, and display preferences",
                fontSize = 12.sp,
                color = MutedText
            )
        }

        // Currency Setting
        SettingsSection(title = "Primary Currency") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("inr" to "INR (₹)", "usd" to "USD ($)", "eur" to "EUR (€)", "gbp" to "GBP (£)").forEach { (code, label) ->
                    val isSelected = state.currency.equals(code, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCurrencyChange(code) },
                        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                            selectedLabelColor = NeonGreen
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Refresh Interval
        SettingsSection(title = "Auto-Refresh Interval") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15 to "15s", 20 to "20s", 30 to "30s", 60 to "60s").forEach { (secs, label) ->
                    val isSelected = state.refreshIntervalSeconds == secs
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onUpdateSettings(
                                secs,
                                state.startOnBoot,
                                state.startMinimized,
                                state.showTrayIcon,
                                state.showMiniChart,
                                state.showPercentage,
                                state.alertSounds
                            )
                        },
                        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Windows Desktop Integration Options
        SettingsSection(title = "Desktop Integration Options") {
            SettingsToggleRow(
                title = "Always on Top",
                description = "Keep widget floating above other applications",
                checked = state.isAlwaysOnTop,
                onCheckedChange = { checked ->
                    onUpdateSettings(
                        state.refreshIntervalSeconds,
                        state.startOnBoot,
                        state.startMinimized,
                        state.showTrayIcon,
                        state.showMiniChart,
                        state.showPercentage,
                        state.alertSounds
                    )
                }
            )
            SettingsToggleRow(
                title = "Launch on Windows Startup",
                description = "Automatically start CryptoLive Widget when PC boots",
                checked = state.startOnBoot,
                onCheckedChange = { checked ->
                    onUpdateSettings(
                        state.refreshIntervalSeconds,
                        checked,
                        state.startMinimized,
                        state.showTrayIcon,
                        state.showMiniChart,
                        state.showPercentage,
                        state.alertSounds
                    )
                }
            )
            SettingsToggleRow(
                title = "Start Minimized to System Tray",
                description = "Launch silently in background tray without opening window",
                checked = state.startMinimized,
                onCheckedChange = { checked ->
                    onUpdateSettings(
                        state.refreshIntervalSeconds,
                        state.startOnBoot,
                        checked,
                        state.showTrayIcon,
                        state.showMiniChart,
                        state.showPercentage,
                        state.alertSounds
                    )
                }
            )
            SettingsToggleRow(
                title = "Show System Tray Icon",
                description = "Keep quick-access menu in Windows taskbar notification tray",
                checked = state.showTrayIcon,
                onCheckedChange = { checked ->
                    onUpdateSettings(
                        state.refreshIntervalSeconds,
                        state.startOnBoot,
                        state.startMinimized,
                        checked,
                        state.showMiniChart,
                        state.showPercentage,
                        state.alertSounds
                    )
                }
            )
        }

        // Widget Card Preferences
        SettingsSection(title = "Widget Aesthetics") {
            SettingsToggleRow(
                title = "Show Mini 24H Sparkline Charts",
                description = "Display vector trend lines on crypto cards",
                checked = state.showMiniChart,
                onCheckedChange = { checked ->
                    onUpdateSettings(
                        state.refreshIntervalSeconds,
                        state.startOnBoot,
                        state.startMinimized,
                        state.showTrayIcon,
                        checked,
                        state.showPercentage,
                        state.alertSounds
                    )
                }
            )
            SettingsToggleRow(
                title = "Show 24H Percentage Change Badges",
                description = "Color-coded gain/loss percentage indicators",
                checked = state.showPercentage,
                onCheckedChange = { checked ->
                    onUpdateSettings(
                        state.refreshIntervalSeconds,
                        state.startOnBoot,
                        state.startMinimized,
                        state.showTrayIcon,
                        state.showMiniChart,
                        checked,
                        state.alertSounds
                    )
                }
            )
            SettingsToggleRow(
                title = "Notification Chime Sounds",
                description = "Play audio chime when a price alert triggers",
                checked = state.alertSounds,
                onCheckedChange = { checked ->
                    onUpdateSettings(
                        state.refreshIntervalSeconds,
                        state.startOnBoot,
                        state.startMinimized,
                        state.showTrayIcon,
                        state.showMiniChart,
                        state.showPercentage,
                        checked
                    )
                }
            )
        }

        // Windows Electron Application Information
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan)
                Column {
                    Text(
                        text = "Windows Desktop App: Ready to Build",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightText
                    )
                    Text(
                        text = "The complete Windows Electron project is located in /crypto-live-widget with electron-builder preconfigured for .exe builds.",
                        fontSize = 11.sp,
                        color = MutedText,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = LightText
            )
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LightText)
            Text(text = description, fontSize = 11.sp, color = MutedText)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonGreen,
                checkedTrackColor = NeonGreen.copy(alpha = 0.3f)
            )
        )
    }
}
