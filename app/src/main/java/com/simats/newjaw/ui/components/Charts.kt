package com.simats.newjaw.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
import com.simats.newjaw.ui.theme.*
import kotlin.math.*

data class ChartData(val label: String, val value: Float)
data class Point3D(val x: Float, val y: Float, val z: Float)

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

@OptIn(ExperimentalTextApi::class)
@Composable
fun ThreeDGraph(
    points: List<Point3D>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFE15241), // Reddish like in the photo
    metrics: Map<String, String> = emptyMap()
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = TextSecondary, fontSize = 9.sp)
    val metricStyle = TextStyle(color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)

    Column(modifier = modifier) {
        // Draw Metrics Header
        if (metrics.isNotEmpty()) {
            val chunkedMetrics = metrics.toList().chunked(3)
            chunkedMetrics.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    row.forEach { (key, value) ->
                        Text(
                            text = "$key: $value",
                            style = metricStyle,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 1.5f // Adjusted for better view
            val scale = size.minDimension / 150f

            // Rotation angles (fixed for the "standard" 3D view in the photo)
            val angleX = -20.0 * PI / 180.0
            val angleY = 45.0 * PI / 180.0

            fun project(p: Point3D): Offset {
                // Rotation around Y
                val x1 = p.x * cos(angleY) + p.y * sin(angleY)
                val y1 = p.z // Z is vertical in the plot
                val z1 = -p.x * sin(angleY) + p.y * cos(angleY)

                // Rotation around X
                val x2 = x1
                val y2 = y1 * cos(angleX) - z1 * sin(angleX)
                
                return Offset(
                    centerX + (x2.toFloat() * scale),
                    centerY - (y2.toFloat() * scale) // Flip Y for screen coordinates
                )
            }

            // Draw Box/Grid
            val boxSize = 50f
            val corners = listOf(
                Point3D(-boxSize, -boxSize, 0f),
                Point3D(boxSize, -boxSize, 0f),
                Point3D(boxSize, boxSize, 0f),
                Point3D(-boxSize, boxSize, 0f),
                Point3D(-boxSize, -boxSize, boxSize),
                Point3D(boxSize, -boxSize, boxSize),
                Point3D(boxSize, boxSize, boxSize),
                Point3D(-boxSize, boxSize, boxSize)
            ).map { project(it) }

            // Draw grid base
            val gridColor = Color.LightGray.copy(alpha = 0.5f)
            val gridStroke = 1.dp.toPx()
            
            // Bottom square
            drawLine(gridColor, corners[0], corners[1], gridStroke)
            drawLine(gridColor, corners[1], corners[2], gridStroke)
            drawLine(gridColor, corners[2], corners[3], gridStroke)
            drawLine(gridColor, corners[3], corners[0], gridStroke)
            
            // Vertical lines
            drawLine(gridColor, corners[0], corners[4], gridStroke)
            drawLine(gridColor, corners[1], corners[5], gridStroke)
            drawLine(gridColor, corners[2], corners[6], gridStroke)
            drawLine(gridColor, corners[3], corners[7], gridStroke)

            // Top partial lines (matplotlib usually shows a wireframe cage)
            drawLine(gridColor, corners[4], corners[5], gridStroke)
            drawLine(gridColor, corners[5], corners[6], gridStroke)
            drawLine(gridColor, corners[6], corners[7], gridStroke)
            drawLine(gridColor, corners[7], corners[4], gridStroke)

            // Draw Labels
            drawText(textMeasurer, "Roll", project(Point3D(0f, boxSize + 10f, 0f)), labelStyle)
            drawText(textMeasurer, "Pitch", project(Point3D(boxSize + 10f, 0f, 0f)), labelStyle)
            drawText(textMeasurer, "Jaw Opening", project(Point3D(-boxSize - 40f, -boxSize, boxSize / 2)), labelStyle)

            // Draw Axis Ticks (simple)
            val ticks = listOf(-40, -20, 0, 20, 40)
            ticks.forEach { t ->
                drawText(textMeasurer, t.toString(), project(Point3D(t.toFloat(), boxSize + 5f, 0f)), labelStyle)
                drawText(textMeasurer, t.toString(), project(Point3D(boxSize + 5f, t.toFloat(), 0f)), labelStyle)
            }
            val zTicks = listOf(0, 10, 20, 30, 40, 50)
            zTicks.forEach { t ->
                drawText(textMeasurer, t.toString(), project(Point3D(-boxSize - 10f, -boxSize, t.toFloat())), labelStyle)
            }

            // Draw 3D Path
            if (points.size > 1) {
                val path = Path()
                points.forEachIndexed { index, p ->
                    val projected = project(p)
                    if (index == 0) path.moveTo(projected.x, projected.y)
                    else path.lineTo(projected.x, projected.y)
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}
