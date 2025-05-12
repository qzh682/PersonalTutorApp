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

    // 添加事务方法，确保删除和插入提交时的一致性
    @Transaction
    suspend fun updateSubmission(courseId: String, userId: String, submission: QuizSubmissionEntity) {
        val existing = getSubmission(courseId, userId)
        if (existing != null) {
            deleteSubmission(existing)
        }
        insertSubmission(submission)
    }
}