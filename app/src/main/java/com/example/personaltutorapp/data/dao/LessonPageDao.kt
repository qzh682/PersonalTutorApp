package com.example.personaltutorapp.data.dao

import androidx.room.*
import com.example.personaltutorapp.model.LessonPageEntity

@Dao
interface LessonPageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: LessonPageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<LessonPageEntity>)

    @Update
    suspend fun updatePage(page: LessonPageEntity)

    @Delete
    suspend fun deletePage(page: LessonPageEntity)

    @Query("SELECT * FROM lesson_pages WHERE id = :pageId")
    suspend fun getPageById(pageId: String): LessonPageEntity?

    @Query("SELECT * FROM lesson_pages WHERE lessonId = :lessonId")
    suspend fun getPagesForLesson(lessonId: String): List<LessonPageEntity>

    @Query("SELECT * FROM lesson_pages")
    suspend fun getAllPages(): List<LessonPageEntity>
}
