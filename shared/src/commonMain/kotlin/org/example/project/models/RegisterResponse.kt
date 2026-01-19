package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val id: Long?
)