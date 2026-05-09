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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.newjaw.data.network.*
import com.simats.newjaw.ui.theme.*
import com.simats.newjaw.ui.viewmodel.AuthViewModel
import com.simats.newjaw.ui.viewmodel.PatientViewModel
import com.simats.newjaw.ui.viewmodel.BleViewModel

@Composable
fun DashboardScreen(
    onNavigateToLiveMonitor: (String) -> Unit,
    onNavigateToAddPatient: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToReports: () -> Unit,
    patientViewModel: PatientViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    bleViewModel: BleViewModel = viewModel()
) {
    val doctor by authViewModel.currentUser.collectAsState()
    val connectedDevices by bleViewModel.connectedDevices.collectAsState()
    val isDeviceConnected = connectedDevices.isNotEmpty()
    val isScanning by bleViewModel.isScanning.collectAsState()
    
    val patients by patientViewModel.patients.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            bleViewModel.startScanning()
        }
    }

    LaunchedEffect(doctor) {
        doctor?.let {
            patientViewModel.fetchPatients(it.id)
            
            val permissions = mutableListOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
                permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 120.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Dr. ${doctor?.full_name?.split(" ")?.firstOrNull() ?: "Smith"}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Here's your patient dashboard",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("DS", color = Color.White, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Device Connection Status
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (!isDeviceConnected && !isScanning) {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.BLUETOOTH_SCAN,
                                    android.Manifest.permission.BLUETOOTH_CONNECT,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        } else if (isScanning) {
                            bleViewModel.stopScanning()
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = if (isScanning && !isDeviceConnected) 1.2f else 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        when {
                                            isDeviceConnected -> listOf(Color(0xFF22D3EE), Color(0xFF06B6D4))
                                            isScanning -> listOf(PurplePrimary, PurpleSecondary)
                                            else -> listOf(Color.Gray, Color.DarkGray)
                                        }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isScanning && !isDeviceConnected) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when {
                                    isDeviceConnected -> "${connectedDevices.size} Sensors Active"
                                    isScanning -> "Searching for Jaw Sensors..."
                                    else -> "Connect Jaw Sensors"
                                },
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = when {
                                    isDeviceConnected -> connectedDevices.joinToString { it.name?.replace("JAW_", "") ?: "Sensor" }
                                    isScanning -> "Ensure sensors are turned on"
                                    else -> "Tap to scan for hardware"
                                },
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                        if (isScanning && !isDeviceConnected) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = PurplePrimary
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isDeviceConnected) Color(0xFF22C55E) else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDeviceConnected) "Ready" else "Offline",
                                color = if (isDeviceConnected) Color(0xFF16A34A) else Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Patients",
                    value = patients.size.toString(),
                    change = "+${patients.size}",
                    icon = Icons.Default.Group,
                    iconColorStart = Color(0xFFA855F7),
                    iconColorEnd = Color(0xFF9333EA)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Active Sessions",
                    value = "8",
                    change = "+2",
                    icon = Icons.Default.MonitorHeart,
                    iconColorStart = Color(0xFF22D3EE),
                    iconColorEnd = Color(0xFF0891B2)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Avg Recovery",
                    value = "78%",
                    change = "+5%",
                    icon = Icons.Default.TrendingUp,
                    iconColorStart = Color(0xFF4ADE80),
                    iconColorEnd = Color(0xFF16A34A)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Reports Today",
                    value = "12",
                    change = "+4",
                    icon = Icons.Default.Description,
                    iconColorStart = Color(0xFFFB923C),
                    iconColorEnd = Color(0xFFEA580C)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions
            Text(text = "Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    label = "Live Monitor",
                    icon = Icons.Default.PlayArrow,
                    colorStart = Color(0xFFA855F7),
                    colorEnd = Color(0xFF7E22CE),
                    onClick = { onNavigateToLiveMonitor("1") }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    label = "Add Patient",
                    icon = Icons.Default.PersonAdd,
                    colorStart = Color(0xFF06B6D4),
                    colorEnd = Color(0xFF0E7490),
                    onClick = onNavigateToAddPatient
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    label = "All Patients",
                    icon = Icons.Default.Group,
                    colorStart = Color(0xFF22C55E),
                    colorEnd = Color(0xFF15803D),
                    onClick = onNavigateToPatients
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    label = "Analytics",
                    icon = Icons.Default.BarChart,
                    colorStart = Color(0xFFF97316),
                    colorEnd = Color(0xFFC2410C),
                    onClick = onNavigateToReports
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Patients
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Recent Patients", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                TextButton(onClick = onNavigateToPatients) {
                    Text("View All", color = PurplePrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            patients.take(3).forEach { patient ->
                RecentPatientCard(patient.patient_name, patient.medical_condition ?: "N/A", 50, "N/A")
                Spacer(modifier = Modifier.height(12.dp))
            }

        }
    }
}


@Composable
fun RecentPatientCard(name: String, condition: String, progress: Int, lastSession: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
//        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.split(" ").joinToString("") { it.take(1) },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = name, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text(text = condition, fontSize = 12.sp, color = TextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .width(80.dp)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = PurplePrimary,
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "$progress%", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = lastSession, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
