package com.example.personaltutorapp.data.dao

import androidx.room.*
import com.example.personaltutorapp.model.QuizSubmissionEntity

@Dao
interface QuizSubmissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: QuizSubmissionEntity)

    @Query("SELECT * FROM quiz_submissions WHERE courseId = :courseId")
    suspend fun getSubmissionsForCourse(courseId: String): List<QuizSubmissionEntity>

    @Query("SELECT * FROM quiz_submissions WHERE courseId = :courseId AND userId = :userId")
    suspend fun getSubmission(courseId: String, userId: String): QuizSubmissionEntity?

    @Delete
    suspend fun deleteSubmission(submission: QuizSubmissionEntity)
}

