package org.example.project
import RegisterResponse
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
import org.example.project.models.*
import io.ktor.server.plugins.cors.routing.*

fun main() {
    val db = DatabaseFactory.createDatabase()
    val pets = PetRepository(db)
    val shelters = ShelterRepository(db)

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }

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
                val req = call.receive<LoginRequest>()
                val shelter = shelters.findByEmail(req.email)

                if (shelter != null && shelter.passwordHash == req.passwordHash) {
                    call.respond(mapOf("shelterId" to shelter.id))
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            get("/api/pets") {
                val shelterId = call.request.queryParameters["shelterId"]?.toLong()
                println("GET /api/pets - shelterId: $shelterId")

                val result = if (shelterId != null) {
                    val petsList = pets.getByShelter(shelterId)
                    petsList
                } else {
                    println("No shelterId provided")
                    emptyList()
                }

                call.respond(result)
            }

            post("/api/pets") {
                try {
                    val req = call.receive<PetCreateRequest>()
                    println("POST /api/pets - Request: $req")

                    val shelterId = call.request.queryParameters["shelterId"]?.toLong()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing shelterId")

                    val pet = pets.addPet(req, shelterId)
                    call.respond(pet)
                } catch (e: Exception) {
                    println("Error adding pet: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Error adding pet: ${e.message}")
                }
            }

            delete("/api/pets/{id}") {
                val id = call.parameters["id"]!!.toLong()
                pets.deletePet(id)
                call.respond(HttpStatusCode.OK)
            }

            post("/api/register") {
                val req = call.receive<Shelter>()
                val shelterId = shelters.insert(req)

                call.respond(RegisterResponse(shelterId))
            }
        }
    }.start(wait = true)
}