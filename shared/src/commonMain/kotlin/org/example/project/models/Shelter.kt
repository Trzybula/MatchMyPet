package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class Shelter(
    val id: Long? = null,
    val name: String,
    val email: String,
    val passwordHash: String,
    val address: String,
    val phone: String,
    val description: String?
)
