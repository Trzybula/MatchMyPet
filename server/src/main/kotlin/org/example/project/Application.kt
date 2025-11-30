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
import org.example.project.models.*
import io.ktor.server.plugins.cors.routing.*
fun main() {
    val db = DatabaseFactory.createDatabase()
    val pets = PetRepository(db)
    val shelters = ShelterRepository(db)

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(CORS) {
            anyHost()  // ← NAJWAŻNIEJSZE, żeby wstało
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Options)

            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)

            allowCredentials = true
        }

        routing {

            post("/api/login") {
                val req = call.receive<LoginRequest>()
                val ok = shelters.login(req.email, req.passwordHash)
                if (ok != null) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.Unauthorized)
            }

            get("/api/pets") {
                call.respond(pets.getPetsByShelter(1L))   // LONG
            }

            post("/api/pets") {
                val req = call.receive<PetCreateRequest>()
                val pet = pets.addPet(req, 1L)            // LONG
                call.respond(pet)
            }

            delete("/api/pets/{id}") {
                val id = call.parameters["id"]!!.toLong() // LONG
                pets.deletePet(id)
                call.respond(HttpStatusCode.OK)
            }

            post("/api/register") {
                val req = call.receive<Shelter>()
                shelters.insert(req)
                call.respond(HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}
