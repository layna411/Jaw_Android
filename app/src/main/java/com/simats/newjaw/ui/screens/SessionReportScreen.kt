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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.newjaw.ui.theme.*
import com.simats.newjaw.ui.viewmodel.SessionViewModel
import com.simats.newjaw.ui.viewmodel.PatientViewModel
import com.simats.newjaw.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

@Composable
fun SessionReportScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel(),
    patientViewModel: PatientViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val session by sessionViewModel.selectedSession.collectAsState()
    val patients by patientViewModel.patients.collectAsState()
    val doctor by authViewModel.currentUser.collectAsState()
    
    val patient = patients.firstOrNull() 
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(BackgroundStart, BackgroundEnd)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Improved Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Session Report",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                lineHeight = 28.sp
                            )
                            Text(
                                text = "#${reportId.toString().padStart(5, '0')}",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HeaderActionIcon(
                            icon = Icons.Default.Download,
                            color = Color(0xFF22C55E),
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Downloading PDF Report...")
                                }
                            }
                        )
                        HeaderActionIcon(
                            icon = Icons.Default.Share,
                            color = PurplePrimary,
                            onClick = {
                                val shareText = "Jaw Rehab Report: ${patient?.patient_name ?: "Patient"} - ${session?.max_angle ?: 0.0}° Max Angle"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Report"))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Main Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = patient?.patient_name ?: "Patient Name",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${session?.session_date ?: "Date N/A"} • Session Analysis",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            // Score Badge
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val score = (((session?.max_angle ?: 0.0) / 45.0) * 100).coerceAtMost(100.0)
                                    Text(
                                        text = String.format("%.0f", score),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Score",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Recovery Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { (((session?.max_angle ?: 0.0) / 45.0)).toFloat().coerceAtMost(1f) },
                                        modifier = Modifier.size(140.dp),
                                        color = PurplePrimary,
                                        strokeWidth = 10.dp,
                                        trackColor = Color.White.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "${String.format("%.0f", (((session?.max_angle ?: 0.0) / 45.0) * 100).coerceAtMost(100.0))}%",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Recovery Achievement",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Detailed Metrics
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricItem("Angle", "${String.format("%.1f", session?.max_angle ?: 0.0)}°", Color(0xFFA855F7))
                            MetricItem("Disp", "${String.format("%.1f", session?.max_disp ?: 0.0)}mm", Color(0xFF06B6D4))
                            MetricItem("Symmetry", "92%", Color(0xFF22C55E))
                            MetricItem("Recovery", "${String.format("%.0f", (((session?.max_angle ?: 0.0) / 45.0) * 100).coerceAtMost(100.0))}%", Color(0xFFF97316))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AchievementCard(
                        modifier = Modifier.weight(1f),
                        label = "Max Protrusive",
                        value = "${String.format("%.1f", session?.max_angle ?: 0.0)}°",
                        icon = Icons.Default.TrendingUp,
                        colorStart = Color(0xFFA855F7),
                        colorEnd = Color(0xFF7E22CE)
                    )
                    AchievementCard(
                        modifier = Modifier.weight(1f),
                        label = "Max Displacement",
                        value = "${String.format("%.1f", session?.max_disp ?: 0.0)} mm",
                        icon = Icons.Default.Speed,
                        colorStart = Color(0xFF06B6D4),
                        colorEnd = Color(0xFF0E7490)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AchievementCard(
                        modifier = Modifier.weight(1f),
                        label = "Symmetry Score",
                        value = "92%",
                        icon = Icons.Default.Check,
                        colorStart = Color(0xFF22C55E),
                        colorEnd = Color(0xFF15803D)
                    )
                    AchievementCard(
                        modifier = Modifier.weight(1f),
                        label = "Recovery Score",
                        value = "${String.format("%.0f", (((session?.max_angle ?: 0.0) / 45.0) * 100).coerceAtMost(100.0))}/100",
                        icon = Icons.Default.CheckCircle,
                        colorStart = Color(0xFFF97316),
                        colorEnd = Color(0xFFC2410C)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Analysis Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Analysis Summary", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF))))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Session results for ${session?.session_date ?: "today"}. Achievement of ${String.format("%.1f", session?.max_angle ?: 0.0)}° shows positive progress in jaw rehabilitation.",
                                color = TextPrimary,
                                lineHeight = 22.sp,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { 
                            scope.launch {
                                snackbarHostState.showSnackbar("Generating PDF Report...")
                            }
                        },
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
                                .background(Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF16A34A)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download PDF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    Button(
                        onClick = { 
                             val shareText = "Jaw Rehab Report: ${patient?.patient_name ?: "Patient"} - ${session?.max_angle ?: 0.0}°"
                             val intent = Intent(Intent.ACTION_SEND).apply {
                                 type = "text/plain"
                                 putExtra(Intent.EXTRA_TEXT, shareText)
                             }
                             context.startActivity(Intent.createChooser(intent, "Share Via"))
                        },
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
                                .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun HeaderActionIcon(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
fun AchievementCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    colorStart: Color,
    colorEnd: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(colorStart, colorEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(label, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
        }
    }
}
