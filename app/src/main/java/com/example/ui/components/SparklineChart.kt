package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed

@Composable
fun SparklineChart(
    prices: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp
) {
    val strokeColor = if (isPositive) NeonGreen else NeonRed
    val fillGradient = Brush.verticalGradient(
        colors = listOf(
            strokeColor.copy(alpha = 0.25f),
            Color.Transparent
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (prices.size < 2) return@Canvas

        val width = size.width
        val canvasHeight = size.height

        val min = prices.minOrNull() ?: 0.0
        val max = prices.maxOrNull() ?: 1.0
        val range = if (max - min > 0.0) max - min else 1.0

        val stepX = width / (prices.size - 1)

        val linePath = Path()
        val fillPath = Path()

        prices.forEachIndexed { i, price ->
            val x = i * stepX
            val y = (canvasHeight - 4f) - ((price - min) / range).toFloat() * (canvasHeight - 8f)

            if (i == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, canvasHeight)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, canvasHeight)
        fillPath.close()

        drawPath(path = fillPath, brush = fillGradient)
        drawPath(
            path = linePath,
            color = strokeColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
