package com.simats.newjaw.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.simats.newjaw.ui.theme.*
import kotlinx.coroutines.launch


@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    authViewModel: com.simats.newjaw.ui.viewmodel.AuthViewModel,
    bleViewModel: com.simats.newjaw.ui.viewmodel.BleViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val doctor by authViewModel.currentUser.collectAsState()
    val connectedDevices by bleViewModel.connectedDevices.collectAsState()
    val isScanning by bleViewModel.isScanning.collectAsState()
    val scannedResults by bleViewModel.scannedDevices.collectAsState()

    var showConnectDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(

        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            bleViewModel.startScanning()
            showConnectDialog = true
        }
    }

    if (showConnectDialog) {
        ConnectBleDialog(
            scannedResults = scannedResults,
            isScanning = isScanning,
            onScanAgain = { bleViewModel.startScanning() },
            onDismiss = {
                bleViewModel.stopScanning()
                showConnectDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showCalibrationDialog) {
        CalibrationDialog(
            onDismiss = { showCalibrationDialog = false },
            onCalibrate = {
                scope.launch {
                    snackbarHostState.showSnackbar("Sensor calibration successful!")
                    showCalibrationDialog = false
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
            Column {
                Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Manage your preferences", fontSize = 14.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = doctor?.full_name?.split(" ")?.joinToString("") { it.take(1) } ?: "DR",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = doctor?.full_name ?: "Doctor", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(text = doctor?.specialization ?: "Medical Professional", fontSize = 12.sp, color = TextSecondary)
                        Text(text = doctor?.hospital_name ?: "", fontSize = 12.sp, color = TextSecondary)
                        Text(text = doctor?.email ?: "", fontSize = 10.sp, color = TextSecondary)
                    }
                    TextButton(onClick = { onNavigateToEditProfile() }) {
                        Text("Edit", color = PurplePrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Device Section
            SettingsSection("DEVICE") {
                SettingsItem(
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth\nConnection",
                    badge = when {
                        connectedDevices.isNotEmpty() -> {
                            val names = connectedDevices.mapNotNull { device ->
                                @SuppressLint("MissingPermission")
                                val n = device.name?.uppercase() ?: ""
                                when {
                                    n.contains("UPPER") -> "Upper"
                                    n.contains("LOWER") -> "Lower"
                                    else -> null
                                }
                            }
                            if (names.isNotEmpty()) {
                                names.joinToString("\n") + "\nConnected"
                            } else {
                                "${connectedDevices.size}\nConnected"
                            }
                        }
                        isScanning -> "Scanning..."
                        else -> "Not\nConnected"
                    },
                    onClick = {
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
                )
                SettingsItem(Icons.Default.Wifi, "Connect Device")
                SettingsItem(Icons.Default.Speed, "Sensor Calibration", onClick = { showCalibrationDialog = true })
            }

            // About Section
            SettingsSection("ABOUT") {
                SettingsItem(Icons.Default.Info, "About App", badge = "v1.0.0", onClick = { showAboutDialog = true })

                SettingsItem(Icons.Default.Security, "Privacy Policy")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out Button
            Button(
                onClick = {
                    authViewModel.logout {
                        onNavigateToLogin()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color(0xFFF87171), Color(0xFFDC2626)))),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Smart Jaw Rehab", fontSize = 14.sp, color = TextSecondary)
                Text("Version 1.0.0 • © 2026", fontSize = 12.sp, color = TextSecondary)
            }
            }
        }
    }
}


@Composable
fun ConnectBleDialog(
    scannedResults: List<android.bluetooth.le.ScanResult>,
    isScanning: Boolean,
    onScanAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Connect BLE Sensor",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Scan for nearby devices",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Found ${scannedResults.size} devices",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Device List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    scannedResults.forEach { result ->
                        val device = result.device
                        @SuppressLint("MissingPermission")
                        val name = device.name ?: "Unknown Device"
                        val rssi = result.rssi

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BackgroundStart.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PurplePrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Bluetooth,
                                        contentDescription = null,
                                        tint = PurplePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    val subtitle = when {
                                        name.uppercase().contains("UPPER") -> "Upper Jaw Sensor"
                                        name.uppercase().contains("LOWER") -> "Lower Jaw Sensor"
                                        else -> "Signal: $rssi dBm"
                                    }
                                    Text(
                                        text = subtitle,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                    
                    if (isScanning && scannedResults.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PurplePrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onScanAgain,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isScanning
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Scan Again", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFEAB308),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ensure Bluetooth is enabled on your device",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            letterSpacing = 1.sp
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    label: String,
    badge: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE9D5FF), Color(0xFFF3E8FF)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium, 
                color = TextPrimary,
                lineHeight = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE9D5FF))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badge, 
                        color = PurplePrimary, 
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Smart Jaw Rehab", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Version 1.0.0", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "This application is designed for clinical jaw rehabilitation tracking using smart sensor hardware. It provides real-time monitoring and progress analytics.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CalibrationDialog(onDismiss: () -> Unit, onCalibrate: () -> Unit) {
    var calibrating by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(calibrating) {
        if (calibrating) {
            for (i in 1..100) {
                progress = i / 100f
                kotlinx.coroutines.delay(20)
            }
            onCalibrate()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Sensor Calibration", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Place sensors on a flat surface", fontSize = 14.sp, color = TextSecondary)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (calibrating) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(80.dp),
                        color = PurplePrimary,
                        strokeWidth = 8.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Calibrating... ${ (progress * 100).toInt() }%", fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(80.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { calibrating = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        enabled = !calibrating
                    ) {
                        Text("Start", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

