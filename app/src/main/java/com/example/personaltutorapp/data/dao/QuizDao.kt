package com.example.personaltutorapp.data.dao

import androidx.room.*
import com.example.personaltutorapp.model.QuizEntity

@Dao
interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM quizzes WHERE courseId = :courseId LIMIT 1")
    suspend fun getQuizForCourse(courseId: String): QuizEntity?

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    // 添加事务方法，确保删除测验时同时删除相关问题
    @Transaction
    suspend fun deleteQuizAndQuestions(quiz: QuizEntity, quizQuestionDao: QuizQuestionDao) {
        quizQuestionDao.deleteQuestionsForQuiz(quiz.id)
        deleteQuiz(quiz)
    }
}