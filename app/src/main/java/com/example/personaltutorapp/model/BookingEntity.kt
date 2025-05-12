package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val availabilityId: String,
    val studentId: String,
    val tutorId: String,
    val startTime: Date,
    val endTime: Date
) {
    init {
        require(id.isNotBlank()) { "Booking ID cannot be blank" }
        require(availabilityId.isNotBlank()) { "Availability ID cannot be blank" }
        require(studentId.isNotBlank()) { "Student ID cannot be blank" }
        require(tutorId.isNotBlank()) { "Tutor ID cannot be blank" }
        require(startTime.before(endTime)) { "Start time must be before end time" }
    }
}