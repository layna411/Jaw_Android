package com.simats.newjaw.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.newjaw.data.network.*
import com.simats.newjaw.ui.components.*
import com.simats.newjaw.ui.theme.*
import com.simats.newjaw.ui.viewmodel.AuthViewModel
import com.simats.newjaw.ui.viewmodel.PatientViewModel
import com.simats.newjaw.ui.viewmodel.SessionViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue

@Composable
fun ReportsScreen(
    onNavigateToSessionReport: (Int) -> Unit,
    patientViewModel: PatientViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    sessionViewModel: SessionViewModel = viewModel()
) {
    val doctor by authViewModel.currentUser.collectAsState()
    val patients by patientViewModel.patients.collectAsState()
    val sessions by sessionViewModel.sessions.collectAsState()

    val weeklyData = sessions.map { session ->
        ChartData(session.session_date.takeLast(2), session.max_disp.toFloat())
    }.takeLast(7)

    val romData = sessions.map { session ->
        ChartData(session.session_date.takeLast(5), session.max_rom.toFloat())
    }.takeLast(5)

    LaunchedEffect(doctor) {
        doctor?.let {
            patientViewModel.fetchPatients(it.id)
        }
    }
    
    LaunchedEffect(patients) {
        if (patients.isNotEmpty()) {
            // For now, fetch sessions for the first patient as a demo
            // Or ideally backend should have a /jaw/history/all endpoint
            sessionViewModel.fetchSessionsForPatient(patients.first().id)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text("Analytics & Reports", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Track recovery progress", fontSize = 14.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Total Sessions",
                        value = sessions.size.toString(),
                        change = if (sessions.isNotEmpty()) "+${sessions.size}" else "0",
                        icon = Icons.Default.Description,
                        iconColorStart = Color(0xFF22D3EE),
                        iconColorEnd = Color(0xFF0891B2)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Avg ROM",
                        value = if (sessions.isNotEmpty()) String.format("%.1f", sessions.map { it.max_rom }.average()) + "mm" else "0mm",
                        change = if (sessions.isNotEmpty()) "+5%" else "0%",
                        icon = Icons.Default.TrendingUp,
                        iconColorStart = Color(0xFF4ADE80),
                        iconColorEnd = Color(0xFF16A34A)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Weekly Progress", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("This Week", fontSize = 14.sp, color = PurplePrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        LineChart(
                            data = weeklyData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Range of Motion Analytics", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (romData.isNotEmpty()) {
                            BarChart(
                                data = romData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No data available", color = TextSecondary)
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent Session Reports", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("View All", fontSize = 14.sp, color = PurplePrimary)
                }
            }

            items(sessions) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Detail View */ },
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Session Analysis", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(report.session_date, color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("• ${String.format("%.1f", report.avg_symmetry)} Symmetry", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                        
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 16.dp)) {
                                Text(String.format("%.1f", report.max_rom), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("ROM mm", fontSize = 12.sp, color = TextSecondary)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF3E8FF))
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
            }
        }
    }
}

