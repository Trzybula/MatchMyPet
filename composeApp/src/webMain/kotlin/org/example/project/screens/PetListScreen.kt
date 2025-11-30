package org.example.project.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.models.Pet
import org.example.project.network.ApiClient

@Composable
fun PetListScreen(
    onLogout: () -> Unit
) {
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                loading = true
                pets = ApiClient.getPets()
                error = null
            } catch (e: Exception) {
                error = "Błąd ładowania zwierząt: ${e.message}"
                pets = emptyList()
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Zwierzęta", style = MaterialTheme.typography.headlineMedium)

            Button(onClick = onLogout) {
                Text("Wyloguj")
            }
        }

        HorizontalDivider()

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = {
                        scope.launch {
                            try {
                                loading = true
                                error = null
                                pets = ApiClient.getPets()
                            } catch (e: Exception) {
                                error = "Błąd ładowania zwierząt: ${e.message}"
                            } finally {
                                loading = false
                            }
                        }
                    }) {
                        Text("Spróbuj ponownie")
                    }
                }
            }
        } else if (pets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Brak zwierząt do wyświetlenia",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pets) { pet ->
                    PetCard(pet)
                }
            }
        }
    }
}

@Composable
private fun PetCard(pet: Pet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                pet.name ?: "Bez imienia",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(6.dp))

            Text("${pet.species ?: ""} • ${pet.breed ?: ""}",
                style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(4.dp))

            Text("Wiek: ${pet.age ?: "?"}", style = MaterialTheme.typography.bodySmall)
            Text("Płeć: ${pet.gender ?: "?"}", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))

            Text(
                pet.description ?: "Brak opisu",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}