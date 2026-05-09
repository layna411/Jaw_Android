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
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
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
import com.simats.newjaw.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit,
    authViewModel: com.simats.newjaw.ui.viewmodel.AuthViewModel
) {
    val doctor by authViewModel.currentUser.collectAsState()
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
                        Text(text = doctor?.email ?: "", fontSize = 10.sp, color = TextSecondary)
                    }
                    TextButton(onClick = { /* TODO */ }) {
                        Text("Edit", color = PurplePrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Device Section
            SettingsSection("DEVICE") {
                SettingsItem(Icons.Default.Bluetooth, "Bluetooth Connection", badge = "Not Connected")
                SettingsItem(Icons.Default.Wifi, "Connect Device")
                SettingsItem(Icons.Default.Speed, "Sensor Calibration")
            }

            // Preferences Section
            SettingsSection("PREFERENCES") {
                var cloudSync by remember { mutableStateOf(true) }
                var notifications by remember { mutableStateOf(true) }
                var darkMode by remember { mutableStateOf(false) }

                SettingsToggleItem(Icons.Default.Cloud, "Cloud Sync", cloudSync) { cloudSync = it }
                SettingsToggleItem(Icons.Default.Notifications, "Notifications", notifications) { notifications = it }
                SettingsToggleItem(Icons.Default.Cloud, "Dark Mode", darkMode) { darkMode = it }
                SettingsItem(Icons.Default.Timer, "Session Reminders")
            }

            // About Section
            SettingsSection("ABOUT") {
                SettingsItem(Icons.Default.Info, "About App", badge = "v1.0.0")
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
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE9D5FF))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(badge, color = PurplePrimary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PurplePrimary
            )
        )
    }
}
