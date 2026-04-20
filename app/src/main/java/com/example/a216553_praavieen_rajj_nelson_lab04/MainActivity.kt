package com.example.a216553_praavieen_rajj_nelson_lab04

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.a216553_praavieen_rajj_nelson_lab04.ui.theme.A216553_PRAAVIEEN_RAJJ_NELSON_LAB04Theme

enum class Screen { LOGIN, REGISTER, HOME, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            A216553_PRAAVIEEN_RAJJ_NELSON_LAB04Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigator()
                }
            }
        }
    }
}

@Composable
fun AppNavigator(
    navController: NavHostController = rememberNavController(),
    userViewModel: UserViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = Screen.LOGIN.name) {

        composable(route = Screen.LOGIN.name) {
            LoginScreen(
                onLoginSuccess = { username ->
                    userViewModel.updateFromLogin(username)
                    navController.navigate(Screen.HOME.name)
                },
                onGoToRegister = { navController.navigate(Screen.REGISTER.name) }
            )
        }

        composable(route = Screen.REGISTER.name) {
            RegisterScreen(
                onRegisterSuccess = { fullName, username, email, phone ->
                    userViewModel.updateFromRegister(fullName, username, email, phone)
                    navController.navigate(Screen.HOME.name) {
                        popUpTo(Screen.LOGIN.name) { inclusive = false }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(route = Screen.HOME.name) {
            val uiState by userViewModel.uiState.collectAsState()
            HomeScreen(
                profile = uiState,
                onViewProfile = { navController.navigate(Screen.PROFILE.name) },
                onLogout = {
                    userViewModel.clear()
                    navController.navigate(Screen.LOGIN.name) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(route = Screen.PROFILE.name) {
            val uiState by userViewModel.uiState.collectAsState()
            ProfileScreen(profile = uiState, onBack = { navController.popBackStack() })
        }
    }
}
