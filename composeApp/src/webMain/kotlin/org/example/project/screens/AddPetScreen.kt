package org.example.project.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.network.ApiClient
import org.example.project.models.PetCreateRequest

@Composable
fun AddPetScreen(
    shelterId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Dodaj nowe zwierzę",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Imię *") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            isError = name.isBlank() && error != null
        )

        OutlinedTextField(
            value = species,
            onValueChange = { species = it },
            label = { Text("Gatunek *") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            isError = species.isBlank() && error != null
        )

        OutlinedTextField(
            value = breed,
            onValueChange = { breed = it },
            label = { Text("Rasa") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = age,
            onValueChange = {
                if (it.all { char -> char.isDigit() || it.isEmpty() }) {
                    age = it
                }
            },
            label = { Text("Wiek (lata) *") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            isError = age.isBlank() && error != null
        )

        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Płeć *") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            placeholder = { Text("np. samiec, samica") },
            isError = gender.isBlank() && error != null
        )

        OutlinedTextField(
            value = size,
            onValueChange = { size = it },
            label = { Text("Rozmiar *") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            placeholder = { Text("np. mały, średni, duży") },
            isError = size.isBlank() && error != null
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Opis") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = false,
            maxLines = 3
        )

        Button(
            onClick = {
                if (name.isBlank() || species.isBlank() || age.isBlank() ||
                    gender.isBlank() || size.isBlank()) {
                    error = "Proszę wypełnić wszystkie wymagane pola (*)"
                    return@Button
                }

                val ageInt = try {
                    age.toInt()
                } catch (e: NumberFormatException) {
                    error = "Wiek musi być liczbą całkowitą"
                    return@Button
                }

                if (ageInt <= 0 || ageInt > 50) {
                    error = "Wiek musi być liczbą dodatnią i realistyczną (1-50)"
                    return@Button
                }

                isLoading = true
                error = null

                scope.launch {
                    try {
                        println("Próba dodania zwierzęcia dla schroniska: $shelterId")

                        val petRequest = PetCreateRequest(
                            name = name.trim(),
                            species = species.trim(),
                            breed = breed.trim().takeIf { it.isNotBlank() },
                            age = ageInt,
                            gender = gender.trim(),
                            size = size.trim(),
                            description = description.trim().takeIf { it.isNotBlank() },
                            photos = emptyList(),  // TODO
                            isAvailable = true
                        )
                        val newPet = ApiClient.addPet(petRequest, shelterId)

                        name = ""
                        species = ""
                        breed = ""
                        age = ""
                        gender = ""
                        size = ""
                        description = ""
                        onSaved()

                    } catch (e: Exception) {
                        error = "Błąd: ${e.message ?: "Nieznany błąd serwera"}"
                        println("Błąd podczas dodawania zwierzęcia: ${e.message}")
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                Text("Dodawanie zwierzęcia")
            } else {
                Text("Dodaj zwierzę")
            }
        }

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Wróć do listy zwierząt")
        }

        Text(
            text = "* - pola wymagane",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}