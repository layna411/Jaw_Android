package com.simats.newjaw.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.newjaw.data.network.*
import com.simats.newjaw.ui.theme.*
import com.simats.newjaw.ui.viewmodel.AuthViewModel
import com.simats.newjaw.ui.viewmodel.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    onNavigateToAddPatient: () -> Unit,
    onNavigateToLiveMonitor: (String) -> Unit,
    patientViewModel: PatientViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val doctor by authViewModel.currentUser.collectAsState()
    val patients by patientViewModel.patients.collectAsState()
    val isLoading by patientViewModel.isLoading.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    val filterChips = listOf("All Patients", "Active", "Improving", "Needs Attention")
    
    val error by patientViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(doctor) {
        doctor?.let {
            patientViewModel.fetchPatients(it.id)
        }
    }

    val filteredPatients: List<Patient> = patients.filter { patient ->
        patient.patient_name.contains(searchQuery, ignoreCase = true)
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp) // Extra padding for better header dimension
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Patients", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("${patients.size} total patients", color = TextSecondary, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary)))
                        .clickable { onNavigateToAddPatient() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search patients...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    containerColor = Color.White.copy(alpha = 0.75f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.6f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = PurplePrimary
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(filterChips.indices.toList()) { index ->
                    val chip = filterChips[index]
                    val isSelected = index == 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (isSelected) Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))
                                else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.6f)))
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else PurplePrimary.copy(alpha = 0.3f),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = chip,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f))
                            .border(1.dp, PurplePrimary.copy(alpha = 0.3f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Patient List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PurplePrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPatients) { patient ->
                        PatientCard(patient = patient, onClick = { onNavigateToLiveMonitor(patient.unique_id ?: "1") })
                    }


                }
            }
        }
    }
}
}

@Composable
fun PatientCard(patient: com.simats.newjaw.data.network.Patient, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = if (patient.patient_name.isNotBlank()) {
                        patient.patient_name.split(" ").filter { it.isNotBlank() }.joinToString("") { it.take(1).uppercase() }
                    } else "P"
                    Text(text = initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = patient.patient_name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = PurplePrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = patient.unique_id ?: "N/A",
                                color = PurplePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )

                        }
                    }
                    Text(text = "Mobile: ${patient.phone ?: "N/A"} • Age: ${patient.age ?: "N/A"} yrs", color = TextSecondary, fontSize = 14.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7))
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Section
            Column(modifier = Modifier.fillMaxWidth()) {
                val latestAngle = patient.latest_angle ?: 0.0
                val progressPercent = ((latestAngle / 45.0) * 100).coerceAtMost(100.0)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Recovery Progress", color = TextSecondary, fontSize = 12.sp)
                    Text("Last: ${patient.created_at.split("T").first()}", color = TextSecondary, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { (progressPercent / 100f).toFloat() },
                        modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                        color = PurplePrimary,
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("${String.format("%.0f", progressPercent)}%", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }


            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(PurplePrimary, PurpleSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Start Session", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
