package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val id: Long?,
    val role: String
)

