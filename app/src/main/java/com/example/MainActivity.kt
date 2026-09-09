package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AppViewModel
import com.example.ui.MainContainer
import com.example.ui.SplashScreen
import com.example.ui.OnboardingScreen
import com.example.ui.LoginScreen
import com.example.ui.RegisterScreen
import com.example.ui.MedAIYordamchiScreen
import com.example.ui.SymptomCheckerScreen
import com.example.ui.DrugInfoScreen
import com.example.ui.ReminderScreen
import com.example.ui.NotificationsScreen
import com.example.ui.AnalyticsScreen
import com.example.ui.ServicesScreen
import com.example.ui.AIDoctorScreen
import com.example.ui.AITipsScreen
import com.example.ui.LabAnalysisScreen
import com.example.ui.FamilyScreen
import com.example.ui.PremiumUpgradeScreen
import com.example.ui.HelpCenterScreen
import com.example.ui.AdminScreen
import com.example.ui.SOSScreen
import com.example.ui.theme.MedicalBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MedAIAppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun MedAIAppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalBackground)
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                viewModel = viewModel
            )
        }

        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable("home") {
            MainContainer(
                navController = navController,
                viewModel = viewModel,
                onNavigateToFeature = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable("yordamchi") {
            MedAIYordamchiScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("symptoms") {
            SymptomCheckerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("drugs") {
            DrugInfoScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("reminder") {
            ReminderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("notifications") {
            NotificationsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("analytics") {
            AnalyticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("services") {
            ServicesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToUpgrade = { navController.navigate("upgrade") }
            )
        }

        composable("ai_doctor") {
            AIDoctorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("ai_tips") {
            AITipsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("lab") {
            LabAnalysisScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("family") {
            FamilyScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("upgrade") {
            PremiumUpgradeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("help") {
            HelpCenterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("admin") {
            AdminScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("sos") {
            SOSScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
