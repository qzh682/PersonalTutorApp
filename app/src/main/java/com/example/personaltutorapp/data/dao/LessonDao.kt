package com.example.personaltutorapp.data.dao

import androidx.room.*
import com.example.personaltutorapp.model.LessonEntity

@Dao
interface LessonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Query("SELECT * FROM lessons WHERE courseId = :courseId")
    suspend fun getLessonsForCourse(courseId: String): List<LessonEntity>

    @Query("SELECT * FROM lessons")
    suspend fun getAllLessons(): List<LessonEntity>
}
