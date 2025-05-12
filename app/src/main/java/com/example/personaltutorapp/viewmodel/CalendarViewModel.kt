package com.example.personaltutorapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltutorapp.data.dao.AvailabilityDao
import com.example.personaltutorapp.data.dao.BookingDao
import com.example.personaltutorapp.model.AvailabilityEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class CalendarViewModel(
    private val availabilityDao: AvailabilityDao,
    private val bookingDao: BookingDao
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private val _selectedDateTime = MutableStateFlow<Date?>(null)
    val selectedDateTime: StateFlow<Date?> = _selectedDateTime

    private val _availableSlots = MutableStateFlow<List<AvailabilityEntity>>(emptyList())
    val availableSlots: StateFlow<List<AvailabilityEntity>> = _availableSlots

    private val _bookingError = MutableStateFlow<String?>(null)
    val bookingError: StateFlow<String?> = _bookingError

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun updateSelectedDateTime(dateTime: Date?) {
        _selectedDateTime.value = dateTime
    }

    fun updateSelectedDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        _selectedDateTime.value = calendar.time
    }

    fun saveAvailability(tutorId: String, durationMinutes: Int) {
        val startTime = _selectedDateTime.value ?: return

        val calendar = Calendar.getInstance()
        calendar.time = startTime
        calendar.add(Calendar.MINUTE, durationMinutes)
        val endTime = calendar.time

        if (!startTime.before(endTime)) {
            return
        }

        viewModelScope.launch {
            val availability = AvailabilityEntity(
                id = UUID.randomUUID().toString(),
                tutorId = tutorId,
                startTime = startTime,
                endTime = endTime
            )
            availabilityDao.insertAvailability(availability)
            _saveSuccess.value = true
        }
    }

    fun loadAvailableSlots(tutorId: String) {
        viewModelScope.launch {
            _availableSlots.value = availabilityDao.getAvailableSlots(tutorId)
        }
    }

    // 修改为 suspend 函数
    suspend fun loadAvailableSlotsForMonth(tutorId: String, year: Int, month: Int): List<AvailabilityEntity> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time

        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.time

        return try {
            availabilityDao.getAvailabilitiesForTutor(tutorId, startOfMonth, endOfMonth)
                .filter { !it.isBooked } // 只返回未被预约的时间段
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun bookSlot(availability: AvailabilityEntity, studentId: String) {
        viewModelScope.launch {
            try {
                bookingDao.bookSlot(availability, studentId, availabilityDao)
                loadAvailableSlots(availability.tutorId)
                _bookingError.value = null
            } catch (e: Exception) {
                _bookingError.value = e.message ?: "Failed to book slot"
            }
        }
    }

    fun resetSaveState() {
        _saveSuccess.value = false
    }
}