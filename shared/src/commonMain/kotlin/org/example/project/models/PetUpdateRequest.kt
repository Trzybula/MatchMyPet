package org.example.project.models

import kotlinx.serialization.Serializable

@Serializable
data class PetUpdateRequest(
    val name: String? = null,
    val species: String? = null,
    val breed: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val size: String? = null,
    val description: String? = null,
    val isAvailable: Boolean? = null
)
