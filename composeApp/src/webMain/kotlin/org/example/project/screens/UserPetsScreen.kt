package org.example.project.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import Pet
import org.example.project.network.ApiClient
import org.example.project.models.MessageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPetsScreen(
    onLogout: () -> Unit,
    currentUserId: Long,
    currentUserName: String,
    currentUserEmail: String,
    currentUserPhone: String? = null
) {
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var selectedSpecies by remember { mutableStateOf<String?>(null) }
    var selectedSize by remember { mutableStateOf<String?>(null) }
    var filterAgeMin by remember { mutableStateOf("") }
    var filterAgeMax by remember { mutableStateOf("") }

    fun loadPets() {
        scope.launch {
            try {
                loading = true
                error = null
                pets = ApiClient.getAvailablePets()
            } catch (e: Exception) {
                error = "Błąd ładowania zwierząt: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPets()
    }

    val filteredPets = remember(pets, selectedSpecies, selectedSize, filterAgeMin, filterAgeMax) {
        pets.filter { pet ->
            val matchesSpecies = selectedSpecies == null || pet.species == selectedSpecies
            val matchesSize = selectedSize == null || pet.size == selectedSize

            val ageMin = filterAgeMin.toIntOrNull() ?: 0
            val ageMax = filterAgeMax.toIntOrNull() ?: Int.MAX_VALUE
            val matchesAge = pet.age in ageMin..ageMax

            matchesSpecies && matchesSize && matchesAge
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dostępne zwierzęta") },
                actions = {
                    Button(onClick = onLogout) {
                        Text("Wyloguj")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var expandedSpecies by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            FilterChip(
                                selected = selectedSpecies != null,
                                onClick = { expandedSpecies = true },
                                label = {
                                    Text(
                                        selectedSpecies ?: "Gatunek",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (selectedSpecies != null)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                ),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            DropdownMenu(
                                expanded = expandedSpecies,
                                onDismissRequest = { expandedSpecies = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Wszystkie",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    onClick = {
                                        selectedSpecies = null
                                        expandedSpecies = false
                                    }
                                )
                                listOf("Pies", "Kot", "Inne").forEach { species ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                species,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        onClick = {
                                            selectedSpecies = species
                                            expandedSpecies = false
                                        }
                                    )
                                }
                            }
                        }

                        var expandedSize by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            FilterChip(
                                selected = selectedSize != null,
                                onClick = { expandedSize = true },
                                label = {
                                    Text(
                                        selectedSize ?: "Rozmiar",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (selectedSize != null)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                ),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            DropdownMenu(
                                expanded = expandedSize,
                                onDismissRequest = { expandedSize = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Wszystkie",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    onClick = {
                                        selectedSize = null
                                        expandedSize = false
                                    }
                                )
                                listOf("Mały", "Średni", "Duży").forEach { size ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                size,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        onClick = {
                                            selectedSize = size
                                            expandedSize = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            "${filteredPets.size}/${pets.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Wiek:",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(30.dp)
                        )

                        OutlinedTextField(
                            value = filterAgeMin,
                            onValueChange = { filterAgeMin = it },
                            label = null,
                            placeholder = { Text("Od", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = MaterialTheme.shapes.extraSmall
                        )

                        Text(
                            "-",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        OutlinedTextField(
                            value = filterAgeMax,
                            onValueChange = { filterAgeMax = it },
                            label = null,
                            placeholder = { Text("Do", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = MaterialTheme.shapes.extraSmall
                        )

                        TextButton(
                            onClick = {
                                selectedSpecies = null
                                selectedSize = null
                                filterAgeMin = ""
                                filterAgeMax = ""
                            },
                            modifier = Modifier.padding(start = 4.dp),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "Wyczyść",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { loadPets() },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Spróbuj ponownie")
                            }
                        }
                    }
                }

                filteredPets.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (pets.isEmpty()) {
                                Text(
                                    "Brak dostępnych zwierząt",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Button(
                                    onClick = { loadPets() },
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Odśwież")
                                }
                            } else {
                                Text(
                                    "Brak zwierząt pasujących do filtrów",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        items(filteredPets) { pet ->
                            UserPetCard(
                                pet = pet,
                                currentUserId = currentUserId,
                                currentUserName = currentUserName,
                                currentUserEmail = currentUserEmail,
                                currentUserPhone = currentUserPhone,
                                onMessageSent = { loadPets() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserPetCard(
    pet: Pet,
    currentUserId: Long,
    currentUserName: String,
    currentUserEmail: String,
    currentUserPhone: String? = null,
    onMessageSent: () -> Unit = {}
) {
    var showContactDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var sendingError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    pet.name ?: "Bez imienia",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        "Dostępny",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Gatunek:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            pet.species ?: "Nieznany",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Rasa:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            pet.breed ?: "Mieszaniec",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Wiek:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${pet.age ?: "?"} lat",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Płeć:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            pet.gender ?: "?",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            pet.size ?: "?",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (!pet.description.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    pet.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 0.9,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    showContactDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = MaterialTheme.shapes.small,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 1.dp
                )
            ) {
                Text(
                    "SKONTAKTUJ SIĘ ZE SCHRONISKIEM",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoading) {
                    showContactDialog = false
                    sendingError = null
                }
            },
            title = {
                Text(
                    "Wyślij wiadomość",
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (sendingError != null) {
                        Text(
                            sendingError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        "W sprawie: ${pet.name ?: "Bez imienia"}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("Twoja wiadomość") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Napisz wiadomość do schroniska") },
                        maxLines = 4,
                        enabled = !isLoading,
                        textStyle = MaterialTheme.typography.bodySmall,
                        shape = MaterialTheme.shapes.small
                    )

                    if (isLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Wysyłanie",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (messageText.isNotBlank() && !isLoading) {
                            isLoading = true
                            sendingError = null

                            val petId = pet.id ?: run {
                                sendingError = "Błąd: Brak ID zwierzęcia"
                                isLoading = false
                                return@Button
                            }

                            val messageRequest = MessageRequest(
                                petId = petId,
                                shelterId = pet.shelterId,
                                userId = currentUserId,
                                userName = currentUserName,
                                userEmail = currentUserEmail,
                                userPhone = currentUserPhone ?: "Nie podano",
                                messageText = messageText
                            )

                            scope.launch {
                                try {
                                    ApiClient.sendMessage(messageRequest)

                                    showContactDialog = false
                                    showSuccessDialog = true
                                    messageText = ""
                                    onMessageSent()
                                } catch (e: Exception) {
                                    sendingError = "Błąd wysyłania wiadomości: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = messageText.isNotBlank() && !isLoading,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Wyślij wiadomość")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isLoading) {
                            showContactDialog = false
                            sendingError = null
                        }
                    },
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Anuluj")
                }
            },
            shape = MaterialTheme.shapes.small
        )
    }
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    "Sukces!",
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                Text(
                    "Twoja wiadomość została wysłana do schroniska.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("OK")
                }
            },
            shape = MaterialTheme.shapes.small
        )
    }
}