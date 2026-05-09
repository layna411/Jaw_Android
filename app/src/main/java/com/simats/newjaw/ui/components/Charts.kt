package com.simats.newjaw.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.newjaw.ui.theme.PurplePrimary
import com.simats.newjaw.ui.theme.TextSecondary

data class ChartData(val label: String, val value: Float)

@OptIn(ExperimentalTextApi::class)
@Composable
fun LineChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    lineColor: Color = PurplePrimary,
    maxY: Float = 100f,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = TextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 40.dp.toPx()
        val paddingBottom = 30.dp.toPx()
        val chartWidth = width - paddingLeft
        val chartHeight = height - paddingBottom

        // Draw Y Axis Labels and Grid
        val ySteps = 4
        for (i in 0..ySteps) {
            val yValue = (maxY / ySteps) * i
            val yPos = chartHeight - ((yValue / maxY) * chartHeight)
            
            // Draw Label
            drawText(
                textMeasurer = textMeasurer,
                text = yValue.toInt().toString(),
                style = labelStyle,
                topLeft = Offset(10.dp.toPx(), yPos - 10.dp.toPx())
            )

            // Draw Grid Line
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(paddingLeft, yPos),
                end = Offset(width, yPos),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Draw X Axis Labels and Grid
        val xStep = chartWidth / (data.size - 1)
        data.forEachIndexed { index, chartData ->
            val xPos = paddingLeft + (index * xStep)
            
            // Draw Label
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(chartData.label),
                style = labelStyle
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(xPos - textLayoutResult.size.width / 2, chartHeight + 10.dp.toPx())
            )

            // Draw Vertical Grid Line
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(xPos, 0f),
                end = Offset(xPos, chartHeight),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Draw Axes
        drawLine(
            color = Color.LightGray,
            start = Offset(paddingLeft, 0f),
            end = Offset(paddingLeft, chartHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.LightGray,
            start = Offset(paddingLeft, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 2f
        )

        // Draw Line and Dots
        val path = Path()
        data.forEachIndexed { index, chartData ->
            val xPos = paddingLeft + (index * xStep)
            val yPos = chartHeight - ((chartData.value / maxY) * chartHeight)
            
            if (index == 0) {
                path.moveTo(xPos, yPos)
            } else {
                path.lineTo(xPos, yPos)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        data.forEachIndexed { index, chartData ->
            val xPos = paddingLeft + (index * xStep)
            val yPos = chartHeight - ((chartData.value / maxY) * chartHeight)
            
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(xPos, yPos)
            )
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(xPos, yPos)
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun BarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = PurplePrimary,
    maxY: Float = 80f,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = TextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 40.dp.toPx()
        val paddingBottom = 30.dp.toPx()
        val chartWidth = width - paddingLeft
        val chartHeight = height - paddingBottom

        // Draw Y Axis Labels and Grid
        val ySteps = 4
        for (i in 0..ySteps) {
            val yValue = (maxY / ySteps) * i
            val yPos = chartHeight - ((yValue / maxY) * chartHeight)
            
            drawText(
                textMeasurer = textMeasurer,
                text = yValue.toInt().toString(),
                style = labelStyle,
                topLeft = Offset(10.dp.toPx(), yPos - 10.dp.toPx())
            )

            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(paddingLeft, yPos),
                end = Offset(width, yPos),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Draw X Axis and Bars
        val barWidth = (chartWidth / data.size) * 0.6f
        val xSpace = chartWidth / data.size
        
        data.forEachIndexed { index, chartData ->
            val xPos = paddingLeft + (index * xSpace) + (xSpace - barWidth) / 2
            val barHeight = ((chartData.value / maxY) * chartHeight)
            val yPos = chartHeight - barHeight

            // Draw Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(xPos, yPos),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Draw Label
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(chartData.label),
                style = labelStyle
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(paddingLeft + (index * xSpace) + (xSpace - textLayoutResult.size.width) / 2, chartHeight + 10.dp.toPx())
            )
        }

        // Draw Axes
        drawLine(
            color = Color.LightGray,
            start = Offset(paddingLeft, 0f),
            end = Offset(paddingLeft, chartHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.LightGray,
            start = Offset(paddingLeft, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 2f
        )
    }
}
