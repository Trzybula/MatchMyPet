package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val shelter: Shelter
)
