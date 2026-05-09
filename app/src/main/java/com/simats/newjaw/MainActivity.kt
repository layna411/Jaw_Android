package com.simats.newjaw

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.newjaw.ui.screens.LoginScreen
import com.simats.newjaw.ui.screens.SplashScreen
import com.simats.newjaw.ui.theme.NewjawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.entries.all { it.value }
                if (allGranted) {
                    // All permissions granted
                }
            }


            NewjawTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.simats.newjaw.ui.theme.BackgroundStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        com.simats.newjaw.ui.theme.BackgroundStart,
                                        com.simats.newjaw.ui.theme.BackgroundEnd
                                    )
                                )
                            )
                    ) {
                    val navController = rememberNavController()
                    val authViewModel: com.simats.newjaw.ui.viewmodel.AuthViewModel = viewModel()
                    val patientViewModel: com.simats.newjaw.ui.viewmodel.PatientViewModel = viewModel()
                    val bleViewModel: com.simats.newjaw.ui.viewmodel.BleViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(
                                authViewModel = authViewModel,
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                authViewModel = authViewModel,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                            com.simats.newjaw.ui.screens.RegisterScreen(
                                authViewModel = authViewModel,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("register") { inclusive = true }
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("dashboard") {
                            Scaffold(
                                bottomBar = {
                                    com.simats.newjaw.ui.components.BottomNav(
                                        activeRoute = "dashboard",
                                        onNavigate = { route ->
                                            val targetRoute = if (route == "live-monitor") "live-monitor/1" else route
                                            navController.navigate(targetRoute) {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                },
                                containerColor = Color.Transparent,
                                contentWindowInsets = WindowInsets(0.dp)
                            ) { innerPadding ->
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    com.simats.newjaw.ui.screens.DashboardScreen(
                                        authViewModel = authViewModel,
                                        patientViewModel = patientViewModel,
                                        bleViewModel = bleViewModel,
                                        onNavigateToLiveMonitor = { patientId: String -> navController.navigate("live-monitor/$patientId") },
                                        onNavigateToAddPatient = { navController.navigate("add-patient") },
                                        onNavigateToPatients = { navController.navigate("patients") },
                                        onNavigateToReports = { navController.navigate("reports") }
                                    )
                                }
                            }
                        }
                        composable("patients") {
                            Scaffold(
                                bottomBar = {
                                    com.simats.newjaw.ui.components.BottomNav(
                                        activeRoute = "patients",
                                        onNavigate = { route ->
                                            val targetRoute = if (route == "live-monitor") "live-monitor/1" else route
                                            navController.navigate(targetRoute) {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                },
                                containerColor = Color.Transparent,
                                contentWindowInsets = WindowInsets(0.dp)
                            ) { innerPadding ->
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    com.simats.newjaw.ui.screens.PatientListScreen(
                                        authViewModel = authViewModel,
                                        patientViewModel = patientViewModel,
                                        onNavigateToAddPatient = { navController.navigate("add-patient") },
                                        onNavigateToLiveMonitor = { patientId: String -> navController.navigate("live-monitor/$patientId") }
                                    )
                                }
                            }
                        }
                        composable("add-patient") {
                            com.simats.newjaw.ui.screens.AddPatientScreen(
                                authViewModel = authViewModel,
                                patientViewModel = patientViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onSavePatient = { navController.navigate("patients") { popUpTo("dashboard") } }
                            )
                        }
                        composable(
                            route = "live-monitor/{patientId}",
                            arguments = listOf(androidx.navigation.navArgument("patientId") { type = androidx.navigation.NavType.StringType })
                        ) { backStackEntry ->
                            val patientId = backStackEntry.arguments?.getString("patientId") ?: "1"
                            Scaffold(
                                bottomBar = {
                                    com.simats.newjaw.ui.components.BottomNav(
                                        activeRoute = "live-monitor",
                                        onNavigate = { route ->
                                            val targetRoute = if (route == "live-monitor") "live-monitor/1" else route
                                            navController.navigate(targetRoute) {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                },
                                containerColor = Color.Transparent,
                                contentWindowInsets = WindowInsets(0.dp)
                            ) { innerPadding ->
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    com.simats.newjaw.ui.screens.LiveMonitorScreen(
                                        patientId = patientId,
                                        bleViewModel = bleViewModel,
                                        onNavigateBack = { navController.popBackStack() },
                                        onNavigateToReport = { navController.navigate("reports") }
                                    )
                                }
                            }
                        }
                        composable("reports") {
                            Scaffold(
                                bottomBar = {
                                    com.simats.newjaw.ui.components.BottomNav(
                                        activeRoute = "reports",
                                        onNavigate = { route ->
                                            val targetRoute = if (route == "live-monitor") "live-monitor/1" else route
                                            navController.navigate(targetRoute) {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                },
                                containerColor = Color.Transparent,
                                contentWindowInsets = WindowInsets(0.dp)
                            ) { innerPadding ->
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    com.simats.newjaw.ui.screens.ReportsScreen(
                                        authViewModel = authViewModel,
                                        patientViewModel = patientViewModel,
                                        onNavigateToSessionReport = { id ->
                                            navController.navigate("session-report/$id")
                                        }
                                    )
                                }
                            }
                        }
                        composable("session-report/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 1
                            com.simats.newjaw.ui.screens.SessionReportScreen(
                                reportId = id,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            Scaffold(
                                bottomBar = {
                                    com.simats.newjaw.ui.components.BottomNav(
                                        activeRoute = "settings",
                                        onNavigate = { route ->
                                            val targetRoute = if (route == "live-monitor") "live-monitor/1" else route
                                            navController.navigate(targetRoute) {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                },
                                containerColor = Color.Transparent,
                                contentWindowInsets = WindowInsets(0.dp)
                            ) { innerPadding ->
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    com.simats.newjaw.ui.screens.SettingsScreen(
                                        authViewModel = authViewModel,
                                        bleViewModel = bleViewModel,
                                        onNavigateToEditProfile = {
                                            navController.navigate("edit-profile")
                                        },
                                        onNavigateToLogin = {
                                            navController.navigate("login") {
                                                popUpTo(0) // clear backstack
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        composable("jaw-model") {
                            com.simats.newjaw.ui.screens.JawModel3DScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("edit-profile") {
                            com.simats.newjaw.ui.screens.EditProfileScreen(
                                authViewModel = authViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
}
