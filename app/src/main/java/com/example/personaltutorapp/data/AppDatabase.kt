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
        QuizSubmissionEntity::class,
        AvailabilityEntity::class, // Add AvailabilityEntity
        BookingEntity::class // Add BookingEntity
    ],
    version = 3, // Increment version due to new entities
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
    abstract fun availabilityDao(): AvailabilityDao // Add DAO for AvailabilityEntity
    abstract fun bookingDao(): BookingDao // Add DAO for BookingEntity

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Define migration from version 1 to 2 to add isPublished column to quizzes table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("Migrating database from version 1 to 2")
                database.execSQL("ALTER TABLE quizzes ADD COLUMN isPublished INTEGER NOT NULL DEFAULT 0")
                println("Added isPublished column to quizzes table")
            }
        }

        // Define migration from version 2 to 3 to add availabilities and bookings tables
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("Migrating database from version 2 to 3")
                database.execSQL("""
                    CREATE TABLE availabilities (
                        id TEXT PRIMARY KEY,
                        tutorId TEXT NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        isBooked INTEGER NOT NULL DEFAULT 0
                    )
                """)
                database.execSQL("""
                    CREATE TABLE bookings (
                        id TEXT PRIMARY KEY,
                        availabilityId TEXT NOT NULL,
                        studentId TEXT NOT NULL,
                        tutorId TEXT NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL
                    )
                """)
                println("Added availabilities and bookings tables")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_tutor_app_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Add new migration
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                println("Database instance created with version ${instance.openHelper.writableDatabase.version}")
                instance
            }
        }

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