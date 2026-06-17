package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Login : Screen("login", "Login", null)
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Reports : Screen("reports", "Reports", Icons.Outlined.DocumentScanner)
    object Chat : Screen("chat", "AI Assistant", Icons.Outlined.SmartToy)
    object Scanner : Screen("scanner/{type}", "Scanner", Icons.Outlined.DocumentScanner) {
        fun createRoute(type: String) = "scanner/$type"
    }
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Analysis : Screen("analysis", "Report Analysis", null) {
        fun createRoute(reportId: String) = "analysis/$reportId"
    }
    object ReportDetail : Screen("reportDetail/{reportId}", "Report Detail", null) {
        fun createRoute(reportId: String) = "reportDetail/$reportId"
    }
    object Vitals : Screen("vitals", "Vitals Tracking", null)
    object Settings : Screen("settings", "Settings", null)
    object Reminders : Screen("reminders", "Reminders", null)
    object ReminderHistory : Screen("reminder_history", "History", null)
    object Emergency : Screen("emergency", "Emergency", Icons.Filled.Warning)
    object Maps : Screen("maps", "Map", null)
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Reports,
    Screen.Emergency,
    Screen.Chat,
    Screen.Profile
)

@Composable
fun MainAppNav(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define routes that should NOT show the bottom bar
    val noBottomBarRoutes = listOf(Screen.Login.route, Screen.Analysis.route + "/{reportId}", "signup", Screen.ReportDetail.route, "scanner/report", "scanner/medicine", "emergency_contacts", "maps", "nearby_services")

    Scaffold(
        bottomBar = {
            if (currentRoute !in noBottomBarRoutes && currentRoute != null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    BottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title, style = MaterialTheme.typography.labelMedium) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { LoginScreen(navController, authViewModel) }
            composable("signup") { SignupScreen(navController, authViewModel) }
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Reports.route) { 
                val reportViewModel: ReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = ReportViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.reportRepository,
                        (navController.context.applicationContext as MediSphereApplication).container.authRepository
                    )
                )
                ReportsScreen(navController, reportViewModel) 
            }
            composable(Screen.ReportDetail.route) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId")
                val reportViewModel: ReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = ReportViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.reportRepository,
                        (navController.context.applicationContext as MediSphereApplication).container.authRepository
                    )
                )
                ReportDetailScreen(navController, reportViewModel, reportId)
            }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Scanner.route) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "medicine"
                val scannerViewModel: ScannerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = ScannerViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.scannerRepository,
                        (navController.context.applicationContext as MediSphereApplication).container.reportRepository,
                        (navController.context.applicationContext as MediSphereApplication).container.authRepository
                    )
                )
                ScannerScreen(navController, scannerViewModel, type)
            }
            composable(Screen.Profile.route) { ProfileScreen(navController, authViewModel) }
            composable(Screen.Analysis.route + "/{reportId}") { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
                val aiAnalysisViewModel: AiAnalysisViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = AiAnalysisViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.aiAnalysisRepository
                    )
                )
                val reportViewModel: ReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = ReportViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.reportRepository,
                        (navController.context.applicationContext as MediSphereApplication).container.authRepository
                    )
                )
                AiAnalysisScreen(navController, aiAnalysisViewModel, reportViewModel, reportId)
            }
            composable(Screen.Vitals.route) { 
                val healthViewModel: HealthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = HealthViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.healthRepository
                    )
                )
                HealthDashboardScreen(navController, healthViewModel, authViewModel.currentUser.value?.id) 
            }
            composable("add_metric") {
                val healthViewModel: HealthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = HealthViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.healthRepository
                    )
                )
                AddMetricScreen(navController, healthViewModel)
            }
            composable("metric_history/{type}") { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: ""
                val healthViewModel: HealthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = HealthViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.healthRepository
                    )
                )
                MetricHistoryScreen(navController, healthViewModel, type)
            }
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = SettingsViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.settingsRepository
                    )
                )
                SettingsScreen(navController, settingsViewModel)
            }
            composable(Screen.Reminders.route) {
                val reminderViewModel: ReminderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = ReminderViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.reminderRepository
                    )
                )
                ReminderScreen(navController, reminderViewModel, authViewModel.currentUser.value?.id)
            }
            composable(Screen.ReminderHistory.route) {
                val reminderViewModel: ReminderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = ReminderViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.reminderRepository
                    )
                )
                ReminderHistoryScreen(navController, reminderViewModel, authViewModel.currentUser.value?.id)
            }
            composable(Screen.Emergency.route) {
                val mapsViewModel: MapsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = MapsViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.locationRepository,
                        (navController.context.applicationContext as MediSphereApplication).container.nearbyServicesRepository
                    )
                )
                EmergencyScreen(navController, mapsViewModel)
            }
            composable("emergency_contacts") {
                val emergencyViewModel: EmergencyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = EmergencyViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.emergencyRepository
                    )
                )
                EmergencyContactsScreen(navController, emergencyViewModel, authViewModel.currentUser.value?.id)
            }
            composable(Screen.Maps.route) {
                val mapsViewModel: MapsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = MapsViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.locationRepository,
                        (navController.context.applicationContext as MediSphereApplication).container.nearbyServicesRepository
                    )
                )
                MapsScreen(navController, mapsViewModel)
            }
            composable("nearby_services") {
                val mapsViewModel: MapsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = MapsViewModel.provideFactory(
                        (navController.context.applicationContext as MediSphereApplication).container.locationRepository,
                        (navController.context.applicationContext as MediSphereApplication).container.nearbyServicesRepository
                    )
                )
                NearbyServicesScreen(navController, mapsViewModel)
            }
        }
    }
}
