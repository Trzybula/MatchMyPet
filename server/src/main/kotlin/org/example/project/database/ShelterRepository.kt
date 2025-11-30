package org.example.project.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.db.AppDatabase
import org.example.project.models.Shelter

class ShelterRepository(private val db: AppDatabase) {

    suspend fun login(email: String, pass: String): Shelter? =
        withContext(Dispatchers.IO) {
            val row = db.shelterQueries.findByEmail(email).executeAsOneOrNull()
            if (row != null && row.passwordHash == pass) row.toModel() else null
        }

    suspend fun insert(s: Shelter) =
        withContext(Dispatchers.IO) {
            db.shelterQueries.insertShelter(
                s.name,
                s.email,
                s.passwordHash,
                s.address,
                s.phone,
                s.description
            )
        }
}

private fun org.example.project.db.Shelter.toModel() = Shelter(
    id = id.toLong(),
    name = name,
    email = email,
    passwordHash = passwordHash,
    address = address,
    phone = phone,
    description = description
)
