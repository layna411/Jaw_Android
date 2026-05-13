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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
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
    val statusMessage by viewModel.statusMessage.collectAsState()
    val statusType by viewModel.statusType.collectAsState()
    val isBackendLoading by viewModel.isLoading.collectAsState()
    val isSocketConnected by viewModel.isSocketConnected.collectAsState()
    val connectedDevices by bleViewModel.connectedDevices.collectAsState()

    // Real-time metrics from backend
    val protrusiveAngle = sensorData.protrusiveAngle.toFloat()
    val protrusiveDisp = sensorData.protrusiveDisp.toFloat()
    val maxAngle = sensorData.maxProtrusiveAngle.toFloat()
    val maxDisp = sensorData.maxProtrusiveDisp.toFloat()

    val formatTime = { seconds: Int ->
        val mins = seconds / 60
        val secs = seconds % 60
        "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
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
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Clinical Monitor", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("Session ID: #$patientId", fontSize = 12.sp, color = TextSecondary)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        formatTime(sessionTime),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = when(statusType) {
                        "success" -> Color(0xFFECFDF5)
                        "ready" -> Color(0xFFEFF6FF)
                        "motion" -> Color(0xFFFFF7ED)
                        "steady" -> Color(0xFFFAF5FF)
                        "result" -> Color(0xFFF7FEE7)
                        else -> Color.White
                    }
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = when(statusType) {
                            "success" -> Color(0xFF10B981)
                            "motion" -> Color(0xFFF97316)
                            "steady" -> Color(0xFFA855F7)
                            "result" -> Color(0xFF84CC16)
                            else -> PurplePrimary
                        }.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when(statusType) {
                                    "success" -> Icons.Default.CheckCircle
                                    "motion" -> Icons.Default.Warning
                                    "steady" -> Icons.Default.Notifications
                                    "result" -> Icons.Default.Star
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = when(statusType) {
                                    "success" -> Color(0xFF10B981)
                                    "motion" -> Color(0xFFF97316)
                                    "steady" -> Color(0xFFA855F7)
                                    "result" -> Color(0xFF84CC16)
                                    else -> PurplePrimary
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (statusMessage.contains("Calibrating")) "CALIBRATING..." else "SYSTEM STATUS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Visualization
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "%.1f°".format(protrusiveAngle),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                            color = if (protrusiveAngle > 0.5) Color(0xFF6366F1) else Color(0xFF94A3B8)
                        )
                        Text(
                            text = "Current Protrusive Angle",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MAX ANGLE", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("%.1f°".format(maxAngle), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.LightGray))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MAX DISP", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("%.1f mm".format(maxDisp), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isMonitoring) Color.Red else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isMonitoring) "LIVE" else "IDLE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { isMonitoring = !isMonitoring },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isMonitoring) Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
                                else Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4338CA)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (isMonitoring) "End Session" else "Start Session",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = onNavigateToReport,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))))
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics Grid
            val metricsData = listOf(
                MetricData("Live Displacement", "%.2f".format(protrusiveDisp), "mm", Icons.Default.Speed, Color(0xFFF43F5E)),
                MetricData("Session Max", "%.2f".format(maxDisp), "mm", Icons.Default.TrendingUp, Color(0xFF8B5CF6))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                metricsData.forEach { data ->
                    MetricCardItem(modifier = Modifier.weight(1f), data = data)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Connection Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFF3B82F6))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sensors: ${connectedDevices.size}/2 Connected", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSocketConnected) Color(0xFF10B981) else Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSocketConnected) "Cloud Sync Active" else "Offline Mode", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
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
