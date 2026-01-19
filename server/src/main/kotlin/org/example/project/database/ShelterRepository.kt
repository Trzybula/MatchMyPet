package org.example.project.database

import org.example.project.db.AppDatabase
import org.example.project.db.Shelter
import org.example.project.models.Shelter as ShelterDTO

class ShelterRepository(private val db: AppDatabase) {

    private val queries = db.shelterQueries

    fun findByEmail(email: String): ShelterDTO? =
        queries.findByEmail(email).executeAsOneOrNull()?.toDTO()

    fun findById(id: Long): ShelterDTO? =
        queries.findById(id).executeAsOneOrNull()?.toDTO()

    fun insert(shelter: ShelterDTO): Long {
        db.transaction {
            queries.insert(
                name = shelter.name,
                email = shelter.email,
                passwordHash = shelter.passwordHash,
                address = shelter.address,
                phone = shelter.phone,
                description = shelter.description.toString()
            )
        }

        return queries.lastInsertId().executeAsOne()
    }
}

private fun Shelter.toDTO() = ShelterDTO(
    id = id,
    name = name,
    email = email,
    passwordHash = passwordHash,
    address = address,
    phone = phone,
    description = description
)
