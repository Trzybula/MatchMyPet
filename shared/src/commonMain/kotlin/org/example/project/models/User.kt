package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long? = null,
    val name: String,
    val surname: String,
    val email: String,
    val passwordHash: String,
    val address: String,
    val phone: String,
)