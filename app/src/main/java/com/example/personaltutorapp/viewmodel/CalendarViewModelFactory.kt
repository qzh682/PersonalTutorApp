package com.example.personaltutorapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.personaltutorapp.data.dao.AvailabilityDao
import com.example.personaltutorapp.data.dao.BookingDao

class CalendarViewModelFactory(
    private val availabilityDao: AvailabilityDao,
    private val bookingDao: BookingDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(availabilityDao, bookingDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}