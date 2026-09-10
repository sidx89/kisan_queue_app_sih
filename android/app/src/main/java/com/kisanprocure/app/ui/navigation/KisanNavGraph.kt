package com.kisanprocure.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kisanprocure.app.ui.screens.auth.LoginScreen
import com.kisanprocure.app.ui.screens.home.*
import com.kisanprocure.app.ui.screens.operator.OperatorDashboardScreen
import com.kisanprocure.app.ui.viewmodel.AuthViewModel
import com.kisanprocure.app.ui.viewmodel.BookingViewModel
import com.kisanprocure.app.ui.viewmodel.QueueViewModel

@Composable
fun KisanNavGraph(
    authViewModel: AuthViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel(),
    queueViewModel: QueueViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { role ->
                    if (role == "OPERATOR") {
                        navController.navigate("operator/1") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                bookingViewModel = bookingViewModel,
                onNavigateToBookSlot = { navController.navigate("book_slot") },
                onNavigateToLiveQueue = { centreId -> navController.navigate("live_queue/$centreId") },
                onNavigateToQrPass = { token -> navController.navigate("qr_pass/$token") },
                onNavigateToProfile = { navController.navigate("profile") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("book_slot") {
            BookSlotScreen(
                bookingViewModel = bookingViewModel,
                onBack = { navController.popBackStack() },
                onBookingSuccess = { token ->
                    navController.navigate("qr_pass/$token")
                }
            )
        }

        composable(
            route = "live_queue/{centreId}",
            arguments = listOf(navArgument("centreId") { type = NavType.IntType })
        ) { backStackEntry ->
            val centreId = backStackEntry.arguments?.getInt("centreId") ?: 1
            LiveQueueScreen(
                centreId = centreId,
                queueViewModel = queueViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "qr_pass/{token}",
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            QrPassScreen(
                token = token,
                onBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToDiagnostics = { navController.navigate("diagnostics") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("diagnostics") {
            ConnectionDiagnosticsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "operator/{centreId}",
            arguments = listOf(navArgument("centreId") { type = NavType.IntType })
        ) { backStackEntry ->
            val centreId = backStackEntry.arguments?.getInt("centreId") ?: 1
            OperatorDashboardScreen(
                centreId = centreId,
                queueViewModel = queueViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
