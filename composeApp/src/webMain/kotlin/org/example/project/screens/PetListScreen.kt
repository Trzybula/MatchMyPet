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
import org.example.project.models.PetUpdateRequest
import org.example.project.network.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListScreen(
    shelterId: Long,
    onLogout: () -> Unit,
    onAddPet: () -> Unit,
    onMessages: () -> Unit
) {
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var petToDelete by remember { mutableStateOf<Pet?>(null) }
    var petToEdit by remember { mutableStateOf<Pet?>(null) }
    val scope = rememberCoroutineScope()

    var filterAvailable by remember { mutableStateOf<Boolean?>(null) }
    var filterSpecies by remember { mutableStateOf<String?>(null) }
    var filterAgeMin by remember { mutableStateOf("") }
    var filterAgeMax by remember { mutableStateOf("") }
    var filterSize by remember { mutableStateOf<String?>(null) }

    fun loadPets() {
        scope.launch {
            try {
                println("loadPets() called for shelterId: $shelterId")
                loading = true
                error = null
                pets = ApiClient.getPetsFromShelter(shelterId)
                println("Successfully loaded pets: ${pets.size}")
            } catch (e: Exception) {
                error = "Błąd ładowania zwierząt: ${e.message}"
                println("ERROR in loadPets: ${e.message}")
                e.printStackTrace()
            } finally {
                loading = false
                println("loadPets finished, loading: $loading")
            }
        }
    }

    fun updatePetStatus(pet: Pet, isAvailable: Boolean) {
        scope.launch {
            try {
                println("Updating pet ${pet.id} status to: $isAvailable")
                val update = PetUpdateRequest(isAvailable = isAvailable)
                val updatedPet = ApiClient.updatePet(pet.id, update)

                pets = pets.map { if (it.id == pet.id) updatedPet else it }

                println("Pet status updated successfully")
            } catch (e: Exception) {
                error = "Błąd aktualizacji statusu: ${e.message}"
                println("ERROR: $error")
                e.printStackTrace()
            }
        }
    }

    fun updatePet(update: PetUpdateRequest) {
        scope.launch {
            try {
                val petId = petToEdit?.id ?: return@launch
                println("Updating pet $petId with: $update")

                val updatedPet = ApiClient.updatePet(petId, update)
                pets = pets.map { if (it.id == petId) updatedPet else it }

                showEditDialog = false
                petToEdit = null
                println("Pet updated successfully")
            } catch (e: Exception) {
                error = "Błąd aktualizacji: ${e.message}"
                println("ERROR: $error")
                e.printStackTrace()
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
        println("LaunchedEffect triggered")
        loadPets()
    }

    val filteredPets = remember(pets, filterAvailable, filterSpecies, filterAgeMin, filterAgeMax, filterSize) {
        pets.filter { pet ->
            (filterAvailable == null || pet.isAvailable == filterAvailable) &&
                    (filterSpecies == null || pet.species == filterSpecies) &&
                    (filterSize == null || pet.size == filterSize) &&
                    (filterAgeMin.isEmpty() || pet.age >= filterAgeMin.toIntOrNull() ?: 0) &&
                    (filterAgeMax.isEmpty() || pet.age <= filterAgeMax.toIntOrNull() ?: Int.MAX_VALUE)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Twoje zwierzęta") },
                actions = {
                    Button(
                        onClick = onMessages,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Wiadomości")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = onLogout) {
                        Text("Wyloguj")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPet) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Filtry", style = MaterialTheme.typography.titleSmall)

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterAvailable == true,
                            onClick = {
                                filterAvailable = if (filterAvailable == true) null else true
                            },
                            label = { Text("Dostępne") }
                        )

                        FilterChip(
                            selected = filterAvailable == false,
                            onClick = {
                                filterAvailable = if (filterAvailable == false) null else false
                            },
                            label = { Text("Niedostępne") }
                        )

                        FilterChip(
                            selected = filterSpecies == "Pies",
                            onClick = {
                                filterSpecies = if (filterSpecies == "Pies") null else "Pies"
                            },
                            label = { Text("Psy") }
                        )

                        FilterChip(
                            selected = filterSpecies == "Kot",
                            onClick = {
                                filterSpecies = if (filterSpecies == "Kot") null else "Kot"
                            },
                            label = { Text("Koty") }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wiek:", modifier = Modifier.fillMaxWidth(0.3f))

                        OutlinedTextField(
                            value = filterAgeMin,
                            onValueChange = { filterAgeMin = it },
                            label = { Text("Od") },
                            modifier = Modifier.fillMaxWidth(0.5f),
                            singleLine = true
                        )

                        Text("-", modifier = Modifier.padding(horizontal = 4.dp))

                        OutlinedTextField(
                            value = filterAgeMax,
                            onValueChange = { filterAgeMax = it },
                            label = { Text("Do") },
                            modifier = Modifier.fillMaxWidth(0.5f),
                            singleLine = true
                        )

                        TextButton(
                            onClick = {
                                filterAvailable = null
                                filterSpecies = null
                                filterAgeMin = ""
                                filterAgeMax = ""
                                filterSize = null
                            }
                        ) {
                            Text("Wyczyść")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Zwierzęta: ${pets.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Po filtracji: ${filteredPets.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(onClick = { loadPets() }) {
                    Text("Odśwież")
                }
            }

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

                filteredPets.isEmpty() -> {
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
                            if (pets.isNotEmpty() && filteredPets.isEmpty()) {
                                Text(
                                    "Żadne zwierzę nie pasuje do filtrów",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        filterAvailable = null
                                        filterSpecies = null
                                        filterAgeMin = ""
                                        filterAgeMax = ""
                                        filterSize = null
                                    }
                                ) {
                                    Text("Wyczyść filtry")
                                }
                            } else {
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
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPets) { pet ->
                            PetCard(
                                pet = pet,
                                onEdit = {
                                    petToEdit = pet
                                    showEditDialog = true
                                },
                                onDelete = {
                                    petToDelete = pet
                                    showDeleteDialog = true
                                },
                                onToggleStatus = { isAvailable ->
                                    updatePetStatus(pet, isAvailable)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && petToEdit != null) {
        EditPetDialog(
            pet = petToEdit!!,
            onSave = { update ->
                updatePet(update)
            },
            onDismiss = {
                showEditDialog = false
                petToEdit = null
            }
        )
    }

    if (showDeleteDialog && petToDelete != null) {
        DeletePetDialog(
            pet = petToDelete!!,
            onConfirm = { deletePet(petToDelete!!) },
            onDismiss = {
                showDeleteDialog = false
                petToDelete = null
            }
        )
    }
}

@Composable
private fun PetCard(
    pet: Pet,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pet.isAvailable == true)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
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

                Row {
                    Switch(
                        checked = pet.isAvailable == true,
                        onCheckedChange = onToggleStatus
                    )

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onEdit,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Edytuj")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Usuń")
                    }
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

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.33f)) {
                    Text(
                        "Gatunek:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(pet.species ?: "Nieznany", style = MaterialTheme.typography.bodyMedium)
                }

                Column(modifier = Modifier.fillMaxWidth(0.33f)) {
                    Text(
                        "Rasa:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(pet.breed ?: "Mieszaniec", style = MaterialTheme.typography.bodyMedium)
                }

                Column(modifier = Modifier.fillMaxWidth(0.33f)) {
                    Text(
                        "Wiek:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("${pet.age ?: "?"} lat", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                    Text(
                        "Płeć:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(pet.gender ?: "?", style = MaterialTheme.typography.bodyMedium)
                }

                Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                    Text(
                        "Rozmiar:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(pet.size ?: "?", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (!pet.description.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Opis:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    pet.description ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun EditPetDialog(
    pet: Pet,
    onSave: (PetUpdateRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(pet.name ?: "") }
    var species by remember { mutableStateOf(pet.species ?: "") }
    var breed by remember { mutableStateOf(pet.breed ?: "") }
    var age by remember { mutableStateOf(pet.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(pet.gender ?: "") }
    var size by remember { mutableStateOf(pet.size ?: "") }
    var description by remember { mutableStateOf(pet.description ?: "") }
    var isAvailable by remember { mutableStateOf(pet.isAvailable == true) }
    var saving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj zwierzę") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Imię") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = species,
                        onValueChange = { species = it },
                        label = { Text("Gatunek") },
                        modifier = Modifier.fillMaxWidth(0.5f),
                        enabled = !saving
                    )

                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Rasa") },
                        modifier = Modifier.fillMaxWidth(0.5f),
                        enabled = !saving
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Wiek") },
                        modifier = Modifier.fillMaxWidth(0.33f),
                        enabled = !saving
                    )

                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Płeć") },
                        modifier = Modifier.fillMaxWidth(0.33f),
                        enabled = !saving
                    )

                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Rozmiar") },
                        modifier = Modifier.fillMaxWidth(0.33f),
                        enabled = !saving
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !saving
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        enabled = !saving
                    )
                    Text("Dostępne")
                }
            }
        },
        confirmButton = {
            if (saving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Button(
                    onClick = {
                        saving = true
                        val update = PetUpdateRequest(
                            name = name.ifBlank { null },
                            species = species.ifBlank { null },
                            breed = breed.ifBlank { null },
                            age = age.toIntOrNull(),
                            gender = gender.ifBlank { null },
                            size = size.ifBlank { null },
                            description = description.ifBlank { null },
                            isAvailable = isAvailable
                        )
                        onSave(update)
                    },
                    enabled = !saving
                ) {
                    Text("Zapisz")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !saving
            ) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
private fun DeletePetDialog(
    pet: Pet,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Usuń zwierzę") },
        text = {
            Column {
                Text("Czy na pewno chcesz usunąć to zwierzę?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${pet.name ?: "Zwierzę"} (${pet.species ?: "gatunek nieznany"})",
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
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Usuń")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}