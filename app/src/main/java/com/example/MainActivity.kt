package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.RuralSyncViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: RuralSyncViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("login") {
                            LoginScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("register") {
                            RegisterScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("forgot_password") {
                            ForgotPasswordScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("permissions_onboarding") {
                            PermissionsOnboardingScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("profile") {
                            ProfileScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("dashboard") {
                            DashboardScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("submit_complaint") {
                            SubmitComplaintScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("cloud_pipeline") {
                            CloudSyncPipelineScreen(navController = navController, viewModel = viewModel)
                        }
                    }
            }
        }
    }
}
