package com.example.personaltutorapp.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.personaltutorapp.data.dao.*
import com.example.personaltutorapp.model.*

@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        LessonEntity::class,
        LessonPageEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        QuizSubmissionEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun lessonPageDao(): LessonPageDao
    abstract fun quizDao(): QuizDao
    abstract fun quizQuestionDao(): QuizQuestionDao
    abstract fun quizSubmissionDao(): QuizSubmissionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Define migration from version 1 to 2 to add isPublished column to quizzes table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("Migrating database from version 1 to 2")
                // Add isPublished column to quizzes table
                database.execSQL("ALTER TABLE quizzes ADD COLUMN isPublished INTEGER NOT NULL DEFAULT 0")
                println("Added isPublished column to quizzes table")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_tutor_app_database"
                )
                    .addMigrations(MIGRATION_1_2) // Add migration strategy
                    .fallbackToDestructiveMigrationOnDowngrade() // Clear data on downgrade
                    .build()
                INSTANCE = instance
                println("Database instance created with version ${instance.openHelper.writableDatabase.version}")
                instance
            }
        }

        // Provide a method to close the database
        fun closeDatabase() {
            INSTANCE?.let {
                if (it.isOpen) {
                    it.close()
                    println("Database instance closed")
                }
                INSTANCE = null
            }
        }
    }
}