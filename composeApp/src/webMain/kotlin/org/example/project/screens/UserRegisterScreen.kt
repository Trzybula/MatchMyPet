package org.example.project.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.network.ApiClient
import org.example.project.models.User

@Composable
fun UserRegisterScreen(
    onSuccess: (Long) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Rejestracja użytkownika", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Imię *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text("Nazwisko *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Adres *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefon *") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank() ||
                    address.isBlank() || phone.isBlank()) {
                    error = "Wypełnij wszystkie wymagane pola (*)"
                    return@Button
                }

                isLoading = true
                error = null

                scope.launch {
                    try {
                        val user = User(
                            id = 0,
                            name = name.trim(),
                            surname = surname.trim(),
                            email = email.trim(),
                            passwordHash = password,
                            address = address.trim(),
                            phone = phone.trim()
                        )

                        val userId = ApiClient.registerUser(user)
                        println("Zarejestrowano user z ID: $userId")

                        onSuccess(userId!!)

                    } catch (e: Exception) {
                        error = "Błąd rejestracji: ${e.message ?: "Nieznany błąd"}"
                        println(error)
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) Text("Rejestracja jest w toku") else Text("Zarejestruj się")
        }

        if (error != null) {
            Text(
                error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        TextButton(onClick = onBack) {
            Text("Masz już konto? Zaloguj się")
        }

        Text(
            "* - pola wymagane",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}