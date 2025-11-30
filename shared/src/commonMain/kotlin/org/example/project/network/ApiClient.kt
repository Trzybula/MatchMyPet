package org.example.project.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.project.models.*

object ApiClient {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    private const val BASE = "http://localhost:8080"

    // --------------------------------------------
    // BEZ ogólnych genericów — tylko konkretne API
    // --------------------------------------------

    suspend fun login(email: String, password: String): Boolean {
        val req = LoginRequest(email, password)

        val resp = client.post("$BASE/api/login") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        return resp.status.value in 200..299
    }

    suspend fun register(shelter: Shelter) {
        client.post("$BASE/api/register") {
            contentType(ContentType.Application.Json)
            setBody(shelter)
        }
    }

    suspend fun getPets(): List<Pet> {
        return client.get("$BASE/api/pets") {
            contentType(ContentType.Application.Json)
        }.body()
    }
}
