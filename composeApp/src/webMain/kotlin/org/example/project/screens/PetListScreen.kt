package org.example.project.screens

import Pet
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.network.ApiClient

@Composable
fun PetListScreen(
    shelterId: Long,
    onLogout: () -> Unit,
    onAddPet: () -> Unit
) {
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var petToDelete by remember { mutableStateOf<Pet?>(null) }
    val scope = rememberCoroutineScope()

    fun loadPets() {
        scope.launch {
            try {
                loading = true
                error = null
                pets = ApiClient.getPetsFromShelter(shelterId)
            } catch (e: Exception) {
                error = "Błąd ładowania zwierząt: ${e.message}"
                println(error)
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    fun deletePet(pet: Pet) {
        scope.launch {
            try {
                println("Usuwanie zwierzęcia: ${pet.id} - ${pet.name}")
                ApiClient.deletePet(pet.id)
                pets = pets.filter { it.id != pet.id }
                showDeleteDialog = false
                petToDelete = null

            } catch (e: Exception) {
                error = "Błąd usuwania zwierzęcia: ${e.message}"
                println(error)
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(shelterId) {
        loadPets()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Twoje zwierzęta", style = MaterialTheme.typography.headlineMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onAddPet) {
                    Text("Dodaj zwierzę")
                }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Wyloguj")
                }
            }
        }

        HorizontalDivider()

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Ładowanie zwierząt")
                    }
                }
            }

            error != null -> {
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
                        Button(onClick = { loadPets() }) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
            }

            pets.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            "Brak zwierząt do wyświetlenia",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Dodaj pierwsze zwierzę do swojego schroniska!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onAddPet) {
                            Text("Dodaj pierwsze zwierzę")
                        }
                    }
                }
            }

            else -> {
                Text(
                    "Liczba zwierząt: ${pets.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pets) { pet ->
                        PetCard(
                            pet = pet,
                            onDelete = {
                                petToDelete = pet
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && petToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                petToDelete = null
            },
            title = { Text("Usuń zwierzę") },
            text = {
                Column {
                    Text("Czy na pewno chcesz usunąć to zwierzę?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${petToDelete!!.name ?: "Zwierzę"} (${petToDelete!!.species ?: "gatunek nieznany"})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tej operacji nie można cofnąć.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { deletePet(petToDelete!!) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        petToDelete = null
                    }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
private fun PetCard(
    pet: Pet,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    pet.name ?: "Bez imienia",
                    style = MaterialTheme.typography.titleLarge
                )

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("USUŃ")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (pet.isAvailable == true) "DOSTĘPNY" else "NIEDOSTĘPNY",
                style = MaterialTheme.typography.labelMedium,
                color = if (pet.isAvailable == true) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "${pet.species ?: "Gatunek nieznany"} ${pet.breed ?: "Rasa nieznana"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(6.dp))

            Column {
                Text("• Wiek: ${pet.age ?: "?"} lat", style = MaterialTheme.typography.bodySmall)
                Text("• Płeć: ${pet.gender ?: "?"}", style = MaterialTheme.typography.bodySmall)
                Text("• Rozmiar: ${pet.size ?: "?"}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            if (!pet.description.isNullOrBlank()) {
                Text(
                    pet.description ?: "Brak opisu",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}