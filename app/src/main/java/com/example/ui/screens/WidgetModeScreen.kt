package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CoinMarket
import com.example.ui.CryptoUiState
import com.example.ui.components.CompactWidgetView
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun WidgetModeScreen(
    state: CryptoUiState,
    onCoinClick: (CoinMarket) -> Unit,
    onToggleExpanded: () -> Unit,
    onOpacityChange: (Float) -> Unit,
    onToggleAlwaysOnTop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Controls Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardDark,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Desktop Floating Widget Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = LightText
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onToggleAlwaysOnTop,
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (state.isAlwaysOnTop) NeonGreen.copy(alpha = 0.2f) else SurfaceDark,
                                    RoundedCornerShape(6.dp)
                                )
                                .testTag("always_on_top_button")
                        ) {
                            Icon(
                                imageVector = if (state.isAlwaysOnTop) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin always on top",
                                tint = if (state.isAlwaysOnTop) NeonGreen else DimText,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Button(
                            onClick = onToggleExpanded,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceDark,
                                contentColor = NeonCyan
                            ),
                            border = BorderStroke(0.8.dp, GlassBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (state.isWidgetExpanded) "Compact View" else "Expanded View",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Opacity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Opacity: ${(state.widgetOpacity * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = MutedText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = state.widgetOpacity,
                        onValueChange = onOpacityChange,
                        valueRange = 0.3f..1.0f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonGreen,
                            activeTrackColor = NeonGreen,
                            inactiveTrackColor = SurfaceDark
                        )
                    )
                }
            }
        }

        // Virtual Desktop Canvas (interactive drag to move)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF030712),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Desktop Canvas • Drag widget to position anywhere",
                    fontSize = 11.sp,
                    color = DimText,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // The Draggable Widget
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        }
                        .widthIn(max = 360.dp)
                        .align(Alignment.TopCenter)
                ) {
                    CompactWidgetView(
                        coins = state.coins,
                        currency = state.currency,
                        opacity = state.widgetOpacity,
                        isExpanded = state.isWidgetExpanded,
                        onCoinClick = onCoinClick
                    )
                }
            }
        }
    }
}
