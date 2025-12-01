package org.example.project

import org.example.project.screens.AddPetScreen
import androidx.compose.runtime.*
import androidx.compose.material3.*
import org.example.project.screens.LoginScreen
import org.example.project.screens.PetListScreen
import org.example.project.screens.RegisterScreen

@Composable
fun App() {
    MaterialTheme {
        AppContent()
    }
}

@Composable
fun AppContent() {
    var screen by remember { mutableStateOf("login") }
    var shelterId by remember { mutableStateOf<Long?>(null) }

    when (screen) {
        "login" -> LoginScreen(
            onSuccess = { id ->
                shelterId = id
                screen = "pets"
            },
            onRegister = { screen = "register" }
        )

        "register" -> RegisterScreen(
            onSuccess = { id ->
                shelterId = id
                screen = "pets"
            },
            onBack = { screen = "login" }
        )

        "pets" -> PetListScreen(
            shelterId = shelterId!!,
            onLogout = {
                shelterId = null
                screen = "login"
            },
            onAddPet = { screen = "addPet" }
        )

        "addPet" -> AddPetScreen(
            shelterId = shelterId!!,
            onSaved = { screen = "pets" },
            onBack = { screen = "pets" }
        )
    }
}