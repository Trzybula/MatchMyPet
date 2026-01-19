package org.example.project.database

import org.example.project.db.AppDatabase
import org.example.project.db.User
import org.example.project.models.User as UserDTO

class UserRepository(private val db: AppDatabase) {

    private val queries = db.userQueries

    fun findByEmail(email: String): UserDTO? =
        queries.findByEmail(email).executeAsOneOrNull()?.toDTO()

    fun findById(id: Long): UserDTO? =
        queries.findById(id).executeAsOneOrNull()?.toDTO()

    fun insert(user: UserDTO): Long {
        db.transaction {
            queries.insert(
                name = user.name,
                surname = user.surname,
                email = user.email,
                passwordHash = user.passwordHash,
                address = user.address,
                phone = user.phone
            )
        }

        return queries.lastInsertId().executeAsOne()
    }
}

private fun User.toDTO() = UserDTO(
    id = id,
    name = name,
    surname = surname,
    email = email,
    passwordHash = passwordHash,
    address = address,
    phone = phone
)