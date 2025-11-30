package org.example.project.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.db.AppDatabase
import org.example.project.models.Pet
import org.example.project.models.PetCreateRequest

class PetRepository(private val db: AppDatabase) {

    suspend fun getPetsByShelter(shelterId: Long): List<Pet> =
        withContext(Dispatchers.IO) {
            db.petQueries.selectByShelter(shelterId)
                .executeAsList()
                .map { it.toModel() }
        }

    suspend fun addPet(req: PetCreateRequest, shelterId: Long): Pet =
        withContext(Dispatchers.IO) {

            db.petQueries.insertPet(
                req.name,
                req.species,
                req.breed,
                req.age.toLong(),      // <-- poprawione
                req.gender,
                req.size,
                req.description,
                "[]",
                shelterId,
                1L                      // <-- LONG
            )

            // bierzemy rekord z końca (SQLite zwróci ostatni dodany)
            db.petQueries.selectAll()
                .executeAsList()
                .last()
                .toModel()
        }

    suspend fun deletePet(id: Long) =
        withContext(Dispatchers.IO) {
            db.petQueries.deleteById(id)
        }
}

private fun org.example.project.db.Pet.toModel() = Pet(
    id = id.toLong(),
    name = name,
    species = species,
    breed = breed,
    age = age.toLong(),  // <-- NULLABLE OK
    gender = gender,
    size = size,
    description = description,
    photos = emptyList(),
    shelterId = shelterId.toLong(),
    isAvailable = isAvailable == 1L
)
