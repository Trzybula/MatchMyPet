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
import org.example.project.models.Message
import org.example.project.network.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterMessagesScreen(
    shelterId: Long,
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadMessages() {
        scope.launch {
            try {
                loading = true
                error = null
                println("Loading messages for shelter: $shelterId")
                messages = ApiClient.getShelterMessages(shelterId)
                println("Loaded ${messages.size} messages")
            } catch (e: Exception) {
                error = "Błąd ładowania wiadomości: ${e.message}"
                println("ERROR: $error")
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    fun markMessageAsRead(messageId: Long, isRead: Boolean) {
        scope.launch {
            try {
                println("Marking message $messageId as read: $isRead")
                ApiClient.markMessageAsRead(messageId, isRead)
                // Odśwież listę
                loadMessages()
            } catch (e: Exception) {
                error = "Błąd aktualizacji wiadomości: ${e.message}"
                println("ERROR: $error")
            }
        }
    }

    LaunchedEffect(shelterId) {
        println("ShelterMessagesScreen initialized for shelter: $shelterId")
        loadMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Wiadomości")
                        // Liczba nieprzeczytanych
                        val unreadCount = messages.count { !it.isRead }
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text(unreadCount.toString())
                            }
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Wróć")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { loadMessages() }) {
                Text("⟳")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                            Text("Ładowanie wiadomości...")
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
                            Button(onClick = { loadMessages() }) {
                                Text("Spróbuj ponownie")
                            }
                        }
                    }
                }

                messages.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "📭",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Text("Brak wiadomości")
                            Text(
                                "Użytkownicy będą się tu kontaktować w sprawie zwierząt",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    // Statystyki
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Wiadomości: ${messages.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val unreadCount = messages.count { !it.isRead }
                        Text(
                            "Nieprzeczytane: $unreadCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (unreadCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { message ->
                            MessageCard(
                                message = message,
                                onMarkAsRead = { isRead ->
                                    val mid = message.id
                                    if (mid != null) {
                                        markMessageAsRead(mid, isRead)
                                    } else {
                                        error = "Błąd: wiadomość nie ma ID"
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageCard(
    message: Message,
    onMarkAsRead: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isRead)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nagłówek - nadawca
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        message.userName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        message.userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tel: ${message.userPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status wiadomości
                if (!message.isRead) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text("NOWA")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Informacja o zwierzęciu
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Zwierzę ID: ${message.petId}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Schronisko ID: ${message.shelterId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Treść wiadomości
            Text(
                "Wiadomość:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Text(
                message.messageText.take(if (expanded) Int.MAX_VALUE else 150),
                style = MaterialTheme.typography.bodyMedium
            )

            // Przycisk "pokaż więcej/niej"
            if (message.messageText.length > 150) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(if (expanded) "Pokaż mniej" else "Pokaż więcej")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Stopka - data i przyciski akcji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Data
                Text(
                    message.createdAt ?: "-",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Przyciski akcji
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Przycisk oznacz jako przeczytane/nieprzeczytane
                    Button(
                        onClick = { onMarkAsRead(!message.isRead) },
                        modifier = Modifier.height(32.dp),
                        colors = if (message.isRead) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    ) {
                        Text(if (message.isRead) "Oznacz jako nieprz." else "Oznacz jako przecz.")
                    }

                    // Przycisk odpowiedz
                    OutlinedButton(
                        onClick = {
                            println("Odpowiadanie na wiadomość: ${message.id}")
                            // TODO: Dodać odpowiedź
                        },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Odpowiedz")
                    }
                }
            }
        }
    }
}