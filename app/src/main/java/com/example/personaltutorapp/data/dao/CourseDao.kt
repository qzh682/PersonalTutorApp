package com.example.personaltutorapp.data.dao

import androidx.room.*
import com.example.personaltutorapp.model.CourseEntity

@Dao
interface CourseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: String): CourseEntity?

    @Query("SELECT * FROM courses WHERE tutorId = :tutorId")
    suspend fun getCoursesByTutor(tutorId: String): List<CourseEntity>

    @Query("SELECT * FROM courses")
    suspend fun getAllCourses(): List<CourseEntity>
}
