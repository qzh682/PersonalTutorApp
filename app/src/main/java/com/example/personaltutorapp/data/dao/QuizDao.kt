package com.example.personaltutorapp.data.dao

import androidx.room.*
import com.example.personaltutorapp.model.QuizEntity

@Dao
interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM quizzes WHERE courseId = :courseId")
    suspend fun getQuizByCourseId(courseId: String): QuizEntity?

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM quizzes WHERE courseId = :courseId")
    suspend fun getQuizForCourse(courseId: String): QuizEntity?
}
