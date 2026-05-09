package com.simats.newjaw.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.newjaw.ui.components.*
import com.simats.newjaw.ui.theme.*
import com.simats.newjaw.ui.viewmodel.BleViewModel
import com.simats.newjaw.ui.viewmodel.SensorViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint

@Composable
fun LiveMonitorScreen(
    patientId: String,
    onNavigateBack: () -> Unit,
    onNavigateToReport: () -> Unit,
    viewModel: SensorViewModel = viewModel(),
    bleViewModel: BleViewModel = viewModel()
) {
    var isMonitoring by remember { mutableStateOf(false) }
    var sessionTime by remember { mutableIntStateOf(0) }
    val trajectoryPoints = remember { mutableStateListOf<com.simats.newjaw.ui.viewmodel.JawSensorData>() }

    val sensorData by viewModel.sensorData.collectAsState()
    val isBackendLoading by viewModel.isLoading.collectAsState()
    val connectedDevices by bleViewModel.connectedDevices.collectAsState()

    // Real-time metrics from backend
    val displacement = sensorData.jawDisp.toFloat()
    val velocity = sensorData.velocity.toFloat()
    val acceleration = sensorData.acceleration.toFloat()
    val rangeOfMotion = sensorData.rom.toFloat()
    val frequency = sensorData.chewingFrequency.toFloat()
    val symmetry = sensorData.symmetry.toFloat()

    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            trajectoryPoints.clear()
            viewModel.startPolling()
            bleViewModel.startScanning(patientId)
            while (true) {
                delay(1000)
                sessionTime++
            }
        } else {
            viewModel.stopPolling()
            bleViewModel.stopScanning()
        }
    }

    val formatTime = { seconds: Int ->
        val mins = seconds / 60
        val secs = seconds % 60
        "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Live Monitor", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Real-time jaw analytics", fontSize = 12.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatTime(sessionTime), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Session Time", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { isMonitoring = !isMonitoring },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isMonitoring) Brush.linearGradient(listOf(Color(0xFFFB923C), Color(0xFFEA580C)))
                                else Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isMonitoring) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isMonitoring) "Pause" else "Start", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .clickable {
                            isMonitoring = false
                            sessionTime = 0
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = TextPrimary)
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF4ADE80), Color(0xFF16A34A))))
                        .clickable { onNavigateToReport() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (connectedDevices.isNotEmpty()) Color.Green else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sensors: ${connectedDevices.size}/2", fontSize = 12.sp, color = TextPrimary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isMonitoring) Color.Green else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backend", fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                LaunchedEffect(sensorData) {
                    if (isMonitoring) {
                        trajectoryPoints.add(sensorData)
                        if (trajectoryPoints.size > 200) trajectoryPoints.removeAt(0)
                    }
                }

                val graphPoints = trajectoryPoints.toList().map {
                    Point3D(it.roll.toFloat(), it.pitch.toFloat(), it.jawOpening.toFloat())
                }

                val metricsMap = mapOf(
                    "Disp" to "${"%.2f".format(displacement)} mm",
                    "Vel" to "${"%.2f".format(velocity)} mm/s",
                    "Acc" to "${"%.2f".format(acceleration)} mm/s²",
                    "ROM" to "${"%.2f".format(rangeOfMotion)} mm",
                    "Freq" to "${"%.2f".format(frequency)} Hz",
                    "Sym" to "%.2f".format(symmetry)
                )

                ThreeDGraph(
                    points = graphPoints,
                    metrics = metricsMap,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Cards
            val metricsData = listOf(
                MetricData("Displacement", "%.2f".format(displacement), "mm", Icons.Default.ShowChart, Color(0xFFA855F7)),
                MetricData("Velocity", "%.2f".format(velocity), "mm/s", Icons.Default.Speed, Color(0xFF06B6D4)),
                MetricData("Acceleration", "%.2f".format(acceleration), "mm/s²", Icons.Default.TrendingUp, Color(0xFF22C55E)),
                MetricData("Range of Motion", "%.2f".format(rangeOfMotion), "mm", Icons.Default.ShowChart, Color(0xFFF97316)),
                MetricData("Frequency", "%.2f".format(frequency), "Hz", Icons.Default.Speed, Color(0xFFEC4899)),
                MetricData("Symmetry", "%.2f".format(symmetry), "", Icons.Default.TrendingUp, Color(0xFFA855F7))
            )

            // Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (i in metricsData.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCardItem(modifier = Modifier.weight(1f), data = metricsData[i])
                        if (i + 1 < metricsData.size) {
                            MetricCardItem(modifier = Modifier.weight(1f), data = metricsData[i + 1])
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

data class MetricData(val label: String, val value: String, val unit: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

@Composable
fun MetricCardItem(modifier: Modifier = Modifier, data: MetricData) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(data.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(data.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = data.value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = data.unit, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
            }
            Text(text = data.label, fontSize = 12.sp, color = TextSecondary)
        }
    }
}
