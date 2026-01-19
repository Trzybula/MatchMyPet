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
import Pet
import org.example.project.network.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    shelterId: Long? = null,
    onLogout: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    var allPets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var onlyAvailable by remember { mutableStateOf(true) }
    var ageMin by remember { mutableStateOf("0") }
    var ageMax by remember { mutableStateOf("30") }
    var gender by remember { mutableStateOf<String?>(null) }
    var species by remember { mutableStateOf<String?>(null) }
    var size by remember { mutableStateOf<String?>(null) }

    fun ageOr(default: Int, s: String): Int = s.toIntOrNull() ?: default

    fun filtered(pets: List<Pet>): List<Pet> {
        val minA = ageOr(0, ageMin)
        val maxA = ageOr(30, ageMax)

        return pets.asSequence()
            .filter { it.age in minA..maxA }
            .filter { gender == null || it.gender.equals(gender, ignoreCase = true) }
            .filter { species == null || it.species.equals(species, ignoreCase = true) }
            .filter { size == null || it.size.equals(size, ignoreCase = true) }
            .filter { !onlyAvailable || it.isAvailable }
            .toList()
    }

    @Composable
    fun SimpleDropdown(
        label: String,
        value: String?,
        options: List<String>,
        onChange: (String?) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value ?: "Wszystkie",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Wszystkie") },
                    onClick = { onChange(null); expanded = false }
                )
                options.distinct().forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = { onChange(opt); expanded = false }
                    )
                }
            }
        }
    }

    suspend fun loadPets() {
        isLoading = true
        error = null
        try {
            allPets = if (shelterId != null) {
                ApiClient.getPetsFromShelter(shelterId)
            } else {
                ApiClient.getPets()
            }
        } catch (e: Exception) {
            error = "Błąd pobierania: ${e.message ?: "Nieznany błąd"}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(shelterId) { loadPets() }

    val speciesOptions = remember(allPets) { allPets.map { it.species }.distinct() }
    val genderOptions = remember(allPets) { allPets.map { it.gender }.distinct() }
    val sizeOptions = remember(allPets) { allPets.map { it.size }.distinct() }

    val shownPets = remember(allPets, onlyAvailable, ageMin, ageMax, gender, species, size) {
        filtered(allPets)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Dashboard użytkownika", style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (shelterId == null) "Wszystkie schroniska" else "Schronisko ID: $shelterId",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) TextButton(onClick = onBack) { Text("Wróć") }
                TextButton(onClick = onLogout) { Text("Wyloguj") }
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Filtry", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = onlyAvailable, onCheckedChange = { onlyAvailable = it })
                        Text("Tylko dostępne")
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = ageMin,
                        onValueChange = { ageMin = it },
                        label = { Text("Wiek od") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ageMax,
                        onValueChange = { ageMax = it },
                        label = { Text("Wiek do") },
                        modifier = Modifier.weight(1f)
                    )
                }

                SimpleDropdown("Gatunek", species, speciesOptions) { species = it }
                SimpleDropdown("Płeć", gender, genderOptions) { gender = it }
                SimpleDropdown("Rozmiar", size, sizeOptions) { size = it }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        onlyAvailable = true
                        ageMin = "0"
                        ageMax = "30"
                        gender = null
                        species = null
                        size = null
                    }) { Text("Reset") }

                    Button(onClick = { scope.launch { loadPets() } }) { Text("Odśwież") }
                }
            }
        }

        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)

        Text("Wyniki: ${shownPets.size}", style = MaterialTheme.typography.bodyMedium)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shownPets) { pet ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(pet.name, style = MaterialTheme.typography.titleLarge)
                        Text("${pet.species} • ${pet.breed ?: "Brak rasy"}")
                        Text("Wiek: ${pet.age} • Płeć: ${pet.gender} • Rozmiar: ${pet.size}")
                        Text(pet.description ?: "Brak opisu")
                        Text(if (pet.isAvailable) "Dostępny" else "Niedostępny")
                    }
                }
            }
        }
    }
}
