package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "availabilities")
data class AvailabilityEntity(
    @PrimaryKey val id: String,
    val tutorId: String,
    val startTime: Date,
    val endTime: Date,
    val isBooked: Boolean = false
) {
    init {
        require(id.isNotBlank()) { "Availability ID cannot be blank" }
        require(tutorId.isNotBlank()) { "Tutor ID cannot be blank" }
        require(startTime.before(endTime)) { "Start time must be before end time" }
    }
}