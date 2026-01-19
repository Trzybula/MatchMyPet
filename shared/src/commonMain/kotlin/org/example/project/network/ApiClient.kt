package org.example.project.network

import Pet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.project.models.*
import org.example.project.models.SendMessageResponse

object ApiClient {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }

    private const val BASE_URL = "http://localhost:8081"

    suspend fun login(email: String, password: String): LoginResponse {
        val req = LoginRequest(email, password)

        val response = client.post("$BASE_URL/api/login") {
            setBody(req)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Login failed: ${response.status}")
        }

        return response.body<LoginResponse>()
    }

    suspend fun registerUser(user: User): Long? {
        val response = client.post("$BASE_URL/api/registerUser") {
            setBody(user)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.body<String>()
            throw Exception("Registration failed: $errorBody")
        }

        val registerResponse = response.body<RegisterResponse>()
        return registerResponse.id
    }

    suspend fun getPets(): List<Pet> {
        return client.get("$BASE_URL/api/pets").body()
    }

    suspend fun registerShelter(shelter: Shelter): Long? {
        val response = client.post("$BASE_URL/api/registerShelter") {
            setBody(shelter)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Shelter registration failed")
        }

        val registerResponse = response.body<RegisterResponse>()
        return registerResponse.id
    }

    suspend fun getPetsFromShelter(shelterId: Long): List<Pet> {
        return client.get("$BASE_URL/api/pets") {
            url {
                parameters.append("shelterId", shelterId.toString())
            }
        }.body()
    }

    suspend fun getAvailablePets(): List<Pet> {
        try {
            val response = client.get("$BASE_URL/api/pets/available")

            if (!response.status.isSuccess()) {
                val errorText = response.body<String>()
                throw Exception("Failed to fetch available pets: ${response.status}")
            }

            val pets = response.body<List<Pet>>()
            return pets
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserPets(
        species: String? = null,
        size: String? = null,
        gender: String? = null
    ): List<Pet> {
        return client.get("$BASE_URL/api/pets/user") {
            url {
                if (species != null) parameters.append("species", species)
                if (size != null) parameters.append("size", size)
                if (gender != null) parameters.append("gender", gender)
            }
        }.body()
    }

    suspend fun addPet(req: PetCreateRequest, shelterId: Long): Pet {
        val response = client.post("$BASE_URL/api/pets") {
            url {
                parameters.append("shelterId", shelterId.toString())
            }
            setBody(req)
        }

        if (!response.status.isSuccess()) {
            val errorText = response.body<String>()
            throw Exception("Failed to add pet: $errorText")
        }

        return response.body<Pet>()
    }

    suspend fun deletePet(petId: Long) {
        val response = client.delete("$BASE_URL/api/pets/$petId")

        if (!response.status.isSuccess()) {
            val errorText = response.body<String>()
            throw Exception("Failed to delete pet: $errorText")
        }
    }

    suspend fun updatePet(petId: Long, update: PetUpdateRequest): Pet {
        val response = client.put("$BASE_URL/api/pets/$petId") {
            setBody(update)
        }

        if (!response.status.isSuccess()) {
            val errorText = response.body<String>()
            throw Exception("Failed to update pet: $errorText")
        }

        return response.body<Pet>()
    }

    suspend fun sendMessage(request: MessageRequest): Long {
        val response = client.post("$BASE_URL/api/messages") {
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            val errorText = response.body<String>()
            throw Exception("Failed to send message: $errorText")
        }

        val result = response.body<SendMessageResponse>()
        return result.messageId
    }


    suspend fun getShelterMessages(shelterId: Long): List<Message> {
        return client.get("$BASE_URL/api/messages/shelter/$shelterId").body()
    }

    suspend fun getUserMessages(userId: Long): List<Message> {
        return client.get("$BASE_URL/api/messages/user/$userId").body()
    }

    suspend fun markMessageAsRead(messageId: Long, isRead: Boolean) {
        client.put("$BASE_URL/api/messages/$messageId/read") {
            setBody(MarkAsReadRequest(messageId, isRead))
        }
    }

    suspend fun deleteMessage(messageId: Long) {
        client.delete("$BASE_URL/api/messages/$messageId")
    }

    suspend fun getUserById(userId: Long?): User {
        val response = client.get("$BASE_URL/api/users/$userId")
        if (!response.status.isSuccess()) {
            val err = response.body<String>()
            throw Exception("Failed to fetch user: $err")
        }
        return response.body<User>()
    }

}