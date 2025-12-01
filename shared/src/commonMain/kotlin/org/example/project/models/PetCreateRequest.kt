package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class PetCreateRequest(
    val name: String,
    val species: String,
    val breed: String?,
    val age: Int,
    val gender: String,
    val size: String,
    val description: String?,
    val photos: List<String>,
    val isAvailable: Boolean = true
)
