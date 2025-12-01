package org.example.project.database

import org.example.project.db.AppDatabase
import org.example.project.db.Pet as DbPet
import Pet as ModelPet
import org.example.project.models.PetCreateRequest

class PetRepository(private val db: AppDatabase) {

    private val queries = db.petQueries

    fun getByShelter(shelterId: Long): List<ModelPet> {
        return queries.selectByShelter(shelterId)
            .executeAsList()
            .map { it.toDTO() }
    }

    fun addPet(req: PetCreateRequest, shelterId: Long): ModelPet {
        queries.insert(
            name = req.name,
            species = req.species,
            breed = req.breed ?: "",
            age = req.age.toLong(),
            gender = req.gender,
            size = req.size,
            description = req.description ?: "",
            photos = req.photos.joinToString(";"),
            shelterId = shelterId,
            isAvailable = if (req.isAvailable) 1 else 0
        )

        val id = queries.lastInsertId().executeAsOne()

        return ModelPet(
            id = id,
            name = req.name,
            species = req.species,
            breed = req.breed,
            age = req.age,
            gender = req.gender,
            size = req.size,
            description = req.description,
            photos = req.photos,
            shelterId = shelterId,
            isAvailable = req.isAvailable
        )
    }

    fun getById(id: Long): ModelPet? {
        return queries.selectById(id)
            .executeAsOneOrNull()
            ?.toDTO()
    }

    fun deletePet(id: Long) {
        queries.delete(id)
    }
}

private fun DbPet.toDTO(): ModelPet = ModelPet(
    id = id,
    name = name,
    species = species,
    breed = breed,
    age = age.toInt(),
    gender = gender,
    size = size,
    description = description,
    photos = photos?.split(";")?.filter { it.isNotBlank() } ?: emptyList(),
    shelterId = shelterId,
    isAvailable = isAvailable == 1L
)