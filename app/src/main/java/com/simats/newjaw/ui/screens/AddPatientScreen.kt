package com.simats.newjaw.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.newjaw.data.network.*
import com.simats.newjaw.ui.theme.*
import com.simats.newjaw.ui.viewmodel.AuthViewModel
import com.simats.newjaw.ui.viewmodel.PatientViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientScreen(
    onNavigateBack: () -> Unit,
    onSavePatient: () -> Unit,
    patientViewModel: PatientViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val doctor by authViewModel.currentUser.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
//    var gender by remember { mutableStateOf("male") }
    var phone by remember { mutableStateOf("") }
    
//    var medicalCondition by remember { mutableStateOf("") }
//    var exerciseProgram by remember { mutableStateOf("Basic Range of Motion") }
//    var notes by remember { mutableStateOf("") }
    
//    var showExerciseDropdown by remember { mutableStateOf(false) }
//    val exerciseOptions = listOf(
//        "Basic Range of Motion",
//        "Advanced Mobility Training",
//        "Post-Surgical Recovery",
//        "TMJ Rehabilitation Protocol",
//        "Muscle Strengthening"
//    )
    
    val error by patientViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 40.dp)
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Add New Patient",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text("Enter patient information", fontSize = 14.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Personal Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Patient Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("Enter patient's full name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.6f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                                unfocusedBorderColor = BorderColor,
                                focusedBorderColor = PurplePrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age") },
                            placeholder = { Text("Enter age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.6f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                                unfocusedBorderColor = BorderColor,
                                focusedBorderColor = PurplePrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("Enter mobile number") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.6f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                                unfocusedBorderColor = BorderColor,
                                focusedBorderColor = PurplePrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val isLoading by patientViewModel.isLoading.collectAsState()

                // Save Button
                Button(
                    onClick = {
                        if (fullName.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter patient name") }
                            return@Button
                        }
                        if (phone.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter mobile number") }
                            return@Button
                        }

                        val docId = doctor?.id
                        if (docId == null) {
                            scope.launch { snackbarHostState.showSnackbar("Authentication error. Please login again.") }
                            return@Button
                        }

                        patientViewModel.addPatient(
                            PatientCreate(
                                doctor_id = docId,
                                patient_name = fullName,
                                age = age.toIntOrNull(),
                                gender = "Not Specified",
                                phone = phone,
                                medical_condition = "",
                                assigned_exercise = ""
                            )
                        ) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Patient saved successfully")
                                onSavePatient()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        PurplePrimary,
                                        PurpleSecondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Save Patient",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }}
