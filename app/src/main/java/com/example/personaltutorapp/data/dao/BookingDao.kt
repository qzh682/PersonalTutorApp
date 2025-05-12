package com.example.personaltutorapp.data.dao

import androidx.room.*
import com.example.personaltutorapp.model.BookingEntity
import com.example.personaltutorapp.model.AvailabilityEntity

@Dao
interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Query("SELECT * FROM bookings WHERE studentId = :studentId")
    suspend fun getBookingsForStudent(studentId: String): List<BookingEntity>

    @Query("SELECT * FROM bookings WHERE tutorId = :tutorId")
    suspend fun getBookingsForTutor(tutorId: String): List<BookingEntity>

    @Delete
    suspend fun deleteBooking(booking: BookingEntity)

    @Transaction
    suspend fun bookSlot(
        availability: AvailabilityEntity,
        studentId: String,
        availabilityDao: AvailabilityDao
    ) {
        if (availability.isBooked) {
            throw IllegalStateException("This time slot is already booked")
        }

        val updatedAvailability = availability.copy(isBooked = true)
        availabilityDao.updateAvailability(updatedAvailability)

        val booking = BookingEntity(
            id = java.util.UUID.randomUUID().toString(),
            availabilityId = availability.id,
            studentId = studentId,
            tutorId = availability.tutorId,
            startTime = availability.startTime,
            endTime = availability.endTime
        )
        insertBooking(booking)
    }
}