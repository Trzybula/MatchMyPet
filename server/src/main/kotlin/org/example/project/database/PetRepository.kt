package org.example.project.database

import org.example.project.db.AppDatabase
import org.example.project.db.Pet as DbPet
import Pet as ModelPet
import org.example.project.models.PetCreateRequest
import org.example.project.models.PetUpdateRequest

class PetRepository(private val db: AppDatabase) {

    private val queries = db.petQueries

    fun getAvailablePets(): List<ModelPet> {

        val results = queries.getAvailablePets().executeAsList()

        return results.map { it.toDTO() }
    }

    fun getAllPets(): List<ModelPet> {

        val results = queries.selectAll().executeAsList()

        return results.map { it.toDTO() }
    }

    fun getAvailablePetsFiltered(
        species: String? = null,
        size: String? = null,
        gender: String? = null
    ): List<ModelPet> {


        val allAvailable = queries.getAvailablePets().executeAsList()

        val filtered = allAvailable.filter { pet ->
            (species == null || pet.species == species) &&
                    (size == null || pet.size == size) &&
                    (gender == null || pet.gender == gender)
        }

        return filtered.map { it.toDTO() }
    }

    fun updatePet(id: Long, update: PetUpdateRequest): ModelPet {

        val currentPet = queries.selectById(id).executeAsOneOrNull()
        if (currentPet == null) {
            throw Exception("Pet not found")
        }

        val name = update.name ?: currentPet.name
        val species = update.species ?: currentPet.species
        val breed = update.breed ?: currentPet.breed
        val age = update.age?.toLong() ?: currentPet.age
        val gender = update.gender ?: currentPet.gender
        val size = update.size ?: currentPet.size
        val description = update.description ?: currentPet.description
        val isAvailable = update.isAvailable?.let { if (it) 1 else 0 } ?: currentPet.isAvailable

        queries.update(
            id = id,
            name = name,
            species = species,
            breed = breed,
            age = age,
            gender = gender,
            size = size,
            description = description,
            isAvailable = isAvailable
        )

        return queries.selectById(id).executeAsOne().toDTO()
    }

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