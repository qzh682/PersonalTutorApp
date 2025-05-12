package com.example.personaltutorapp.data.dao

import androidx.room.*
import com.example.personaltutorapp.model.AvailabilityEntity
import java.util.Date

@Dao
interface AvailabilityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailability(availability: AvailabilityEntity)

    @Query("SELECT * FROM availabilities WHERE tutorId = :tutorId AND startTime >= :start AND endTime <= :end")
    suspend fun getAvailabilitiesForTutor(tutorId: String, start: Date, end: Date): List<AvailabilityEntity>

    @Query("SELECT * FROM availabilities WHERE tutorId = :tutorId AND isBooked = 0")
    suspend fun getAvailableSlots(tutorId: String): List<AvailabilityEntity>

    @Update
    suspend fun updateAvailability(availability: AvailabilityEntity)

    @Delete
    suspend fun deleteAvailability(availability: AvailabilityEntity)

    // 新增方法：根据 availabilityId 查询 AvailabilityEntity
    @Query("SELECT * FROM availabilities WHERE id = :availabilityId")
    suspend fun getAvailabilityById(availabilityId: String): AvailabilityEntity?
}