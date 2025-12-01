package org.example.project.network

import Pet
import RegisterResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
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
    suspend fun login(email: String, password: String): LoginResponse {
        val req = LoginRequest(email, password)

        return client.post("$BASE/api/login") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
    }

    suspend fun getPetsFromShelter(shelterId: Long): List<Pet> {
        return client.get("$BASE/api/pets?shelterId=$shelterId").body()
    }

    suspend fun register(shelter: Shelter): Long {
        val response = client.post("$BASE/api/register") {
            contentType(ContentType.Application.Json)
            setBody(shelter)
        }

        return response.body<RegisterResponse>().shelterId
    }

    suspend fun getPets(): List<Pet> {
        return client.get("$BASE/api/pets") {
            contentType(ContentType.Application.Json)
        }.body()
    }
    suspend fun deletePet(petId: Long) {
        client.delete("$BASE/api/pets/$petId")
    }

    suspend fun addPet(req: PetCreateRequest, shelterId: Long): Pet {
        val response = client.post("$BASE/api/pets") {
            contentType(ContentType.Application.Json)
            url {
                parameters.append("shelterId", shelterId.toString())
            }
            setBody(req)
        }
        if (!response.status.isSuccess()) {
            val errorText = response.body<String>()
            throw Exception("HTTP ${response.status}: $errorText")
        }

        val pet = response.body<Pet>()
        return pet
    }
}
