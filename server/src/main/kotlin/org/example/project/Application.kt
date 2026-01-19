package org.example.project

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import org.example.project.db.DatabaseFactory
import org.example.project.database.PetRepository
import org.example.project.database.ShelterRepository
import org.example.project.database.UserRepository
import org.example.project.database.MessageRepository
import org.example.project.models.*
import io.ktor.server.plugins.cors.routing.*

fun main() {
    val db = DatabaseFactory.createDatabase()
    val pets = PetRepository(db)
    val shelters = ShelterRepository(db)
    val users = UserRepository(db)
    val messages = MessageRepository(db)

    embeddedServer(Netty, port = 8081) {
        install(ContentNegotiation) {
            json()
        }

        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Options)
            allowCredentials = true
        }

        routing {
            post("/api/login") {
                try {
                    val req = call.receive<LoginRequest>()

                    val shelter = shelters.findByEmail(req.email)
                    if (shelter != null && shelter.passwordHash == req.passwordHash) {
                        call.respond(LoginResponse(shelter.id!!, "SHELTER"))
                        return@post
                    }

                    val user = users.findByEmail(req.email)
                    if (user != null && user.passwordHash == req.passwordHash) {
                        call.respond(LoginResponse(user.id!!, "USER"))
                        return@post
                    }

                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
                }
            }

            post("/api/registerUser") {
                try {
                    val req = call.receive<User>()

                    val userId = users.insert(req)

                    call.respond(RegisterResponse(userId))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            post("/api/registerShelter") {
                try {
                    val req = call.receive<Shelter>()

                    val shelterId = shelters.insert(req)

                    call.respond(RegisterResponse(shelterId))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            get("/api/pets/available") {
                try {
                    val availablePets = pets.getAvailablePets()
                    call.respond(availablePets)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            get("/api/pets/user") {
                try {
                    val species = call.request.queryParameters["species"]
                    val size = call.request.queryParameters["size"]
                    val gender = call.request.queryParameters["gender"]

                    val filteredPets = pets.getAvailablePetsFiltered(species, size, gender)
                    call.respond(filteredPets)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            get("/api/pets") {
                try {
                    val shelterId = call.request.queryParameters["shelterId"]?.toLong()

                    val result = if (shelterId != null) {
                        pets.getByShelter(shelterId)
                    } else {
                        pets.getAllPets()
                    }

                    call.respond(result)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            post("/api/pets") {
                try {
                    val req = call.receive<PetCreateRequest>()
                    val shelterId = call.request.queryParameters["shelterId"]?.toLong()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing shelterId")

                    val pet = pets.addPet(req, shelterId)
                    call.respond(pet)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            put("/api/pets/{id}") {
                try {
                    val id = call.parameters["id"]?.toLong() ?: return@put
                    val req = call.receive<PetUpdateRequest>()

                    val updatedPet = pets.updatePet(id, req)
                    call.respond(updatedPet)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            delete("/api/pets/{id}") {
                try {
                    val id = call.parameters["id"]?.toLong() ?: return@delete
                    pets.deletePet(id)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            post("/api/messages") {
                try {
                    val req = call.receive<MessageRequest>()

                    val messageId = messages.createMessage(req)

                    call.respond(SendMessageResponse(success = true, messageId = messageId))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            get("/api/messages/shelter/{shelterId}") {
                try {
                    val shelterId = call.parameters["shelterId"]?.toLong() ?: return@get

                    val messagesList = messages.getMessagesByShelter(shelterId)

                    call.respond(messagesList.map { message ->
                        mapOf(
                            "id" to message.id,
                            "petId" to message.petId,
                            "shelterId" to message.shelterId,
                            "userId" to message.userId,
                            "userName" to message.userName,
                            "userEmail" to message.userEmail,
                            "userPhone" to message.userPhone,
                            "messageText" to message.messageText,
                            "createdAt" to message.createdAt,
                            "isRead" to message.isRead
                        )
                    })
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            get("/api/messages/user/{userId}") {
                try {
                    val userId = call.parameters["userId"]?.toLong() ?: return@get

                    val messagesList = messages.getMessagesByUser(userId)
                    call.respond(messagesList)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            put("/api/messages/{id}/read") {
                try {
                    val messageId = call.parameters["id"]?.toLong() ?: return@put
                    val req = call.receive<MarkAsReadRequest>()

                    messages.markAsRead(messageId, req.isRead)
                    call.respond(mapOf("success" to true))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            delete("/api/messages/{id}") {
                try {
                    val messageId = call.parameters["id"]?.toLong() ?: return@delete

                    messages.deleteMessage(messageId)
                    call.respond(mapOf("success" to true))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }

            get("/api/users/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")

                val user = users.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")
                call.respond(user)
            }

        }
    }.start(wait = true)
}