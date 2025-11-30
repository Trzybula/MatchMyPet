package org.example.project.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.example.project.db.AppDatabase

object DatabaseFactory {

    fun createDatabase(): AppDatabase {
        val driver = JdbcSqliteDriver("jdbc:sqlite:matchmypet.db")
        AppDatabase.Schema.create(driver)
        return AppDatabase(driver)
    }
}
