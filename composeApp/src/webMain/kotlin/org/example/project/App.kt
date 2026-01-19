package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.example.project.network.ApiClient
import org.example.project.screens.*

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
    var userId by remember { mutableStateOf<Long?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }
    var userEmail by remember { mutableStateOf<String?>(null) }
    var userPhone by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    when (screen) {
        "login" -> LoginScreen(
            onSuccess = { response ->
                when (response.role) {
                    "SHELTER" -> {
                        shelterId = response.id
                        userId = null
                        screen = "pets"
                    }
                    "USER" -> {
                        userId = response.id
                        shelterId = null

                        scope.launch {
                            try {
                                val u = ApiClient.getUserById(response.id)
                                userName = u.name
                                userEmail = u.email
                                userPhone = u.phone
                                screen = "userPets"
                            } catch (e: Exception) {
                                userName = "Użytkownik"
                                userEmail = "unknown@example.com"
                                userPhone = null
                                screen = "userPets"
                            }
                        }
                    }
                    else -> {
                        shelterId = null
                        userId = null
                        screen = "login"
                    }
                }
            },
            onRegisterShelter = { screen = "register" },
            onRegisterUser = { screen = "registerUser" }
        )

        "register" -> RegisterScreen(
            onSuccess = { id ->
                shelterId = id
                userId = null
                screen = "pets"
            },
            onBack = { screen = "login" }
        )

        "registerUser" -> UserRegisterScreen(
            onSuccess = { id ->
                userId = id
                shelterId = null
                scope.launch {
                    try {
                        val u = ApiClient.getUserById(id)
                        userName = u.name
                        userEmail = u.email
                        userPhone = u.phone
                    } catch (_: Exception) {
                        userName = "Użytkownik"
                        userEmail = "unknown@example.com"
                        userPhone = null
                    }
                    screen = "userPets"
                }
            },
            onBack = { screen = "login" }
        )

        "pets" -> PetListScreen(
            shelterId = shelterId!!,
            onLogout = {
                shelterId = null
                screen = "login"
            },
            onAddPet = { screen = "addPet" },
            onMessages = { screen = "shelterMessages" }
        )

        "userPets" -> UserPetsScreen(
            onLogout = {
                userId = null
                screen = "login"
            },
            currentUserId = userId!!,
            currentUserName = userName ?: "Użytkownik",
            currentUserEmail = userEmail ?: "unknown@example.com",
            currentUserPhone = userPhone
        )

        "userDashboard" -> UserDashboardScreen(
            shelterId = null,
            onLogout = {
                userId = null
                screen = "login"
            },
            onBack = { screen = "login" }
        )

        "addPet" -> AddPetScreen(
            shelterId = shelterId!!,
            onSaved = { screen = "pets" },
            onBack = { screen = "pets" }
        )

        "shelterMessages" -> ShelterMessagesScreen(
            shelterId = shelterId!!,
            onBack = { screen = "pets" }
        )
    }
}
