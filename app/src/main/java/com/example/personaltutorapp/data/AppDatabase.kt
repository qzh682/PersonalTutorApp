package com.example.personaltutorapp.data

import android.content.Context
import androidx.room.*
import com.example.personaltutorapp.data.dao.CourseDao
import com.example.personaltutorapp.data.dao.LessonDao
import com.example.personaltutorapp.data.dao.LessonPageDao
import com.example.personaltutorapp.data.dao.UserDao
import com.example.personaltutorapp.db.Converters
import com.example.personaltutorapp.model.CourseEntity
import com.example.personaltutorapp.model.LessonEntity
import com.example.personaltutorapp.model.LessonPageEntity
import com.example.personaltutorapp.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        LessonEntity::class,
        LessonPageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun lessonPageDao(): LessonPageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_tutor_app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
