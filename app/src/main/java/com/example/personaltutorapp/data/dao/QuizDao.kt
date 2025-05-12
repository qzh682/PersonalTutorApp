package com.example.personaltutorapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.personaltutorapp.model.QuizEntity

@Dao
interface QuizDao {

    @Insert
    suspend fun insertQuiz(quiz: QuizEntity)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM quizzes WHERE courseId = :courseId LIMIT 1")
    suspend fun getQuizForCourse(courseId: String): QuizEntity?

    @Query("DELETE FROM quizzes WHERE id = :quizId")
    suspend fun deleteQuiz(quizId: String)

    @Query("DELETE FROM quiz_questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: String)

    @Transaction
    suspend fun deleteQuizAndQuestions(quiz: QuizEntity, quizQuestionDao: QuizQuestionDao) {
        deleteQuestionsForQuiz(quiz.id)
        deleteQuiz(quiz.id)
    }
}