package org.example.project

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontFamily
import org.example.project.screens.LoginScreen
import org.example.project.screens.PetListScreen
import org.example.project.screens.RegisterScreen

@Composable
fun App() {
    // Dodaj MaterialTheme provider
    MaterialTheme {
        AppContent()
    }
}

@Composable
fun AppContent() {
    var screen by remember { mutableStateOf("login") }

    when (screen) {
        "login" -> LoginScreen(
            onSuccess = { screen = "pets" },
            onRegister = { screen = "register" }
        )

        "register" -> RegisterScreen(
            onRegisterSuccess = { screen = "login" },
            onBackToLogin = { screen = "login" }
        )

        "pets" -> PetListScreen(
            onLogout = { screen = "login" }
        )
    }
}