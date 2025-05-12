package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription // 导入 contentDescription
import androidx.compose.ui.semantics.semantics // 导入 semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.personaltutorapp.data.AppDatabase
import com.example.personaltutorapp.model.AvailabilityEntity
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.CalendarViewModel
import com.example.personaltutorapp.viewmodel.CalendarViewModelFactory
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StudentBookingScreen(
    tutorId: String,
    studentId: String,
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CalendarViewModelFactory(database.availabilityDao(), database.bookingDao())
    )

    val availableSlots by viewModel.availableSlots.collectAsState()
    val bookingError by viewModel.bookingError.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val coroutineScope = rememberCoroutineScope()

    // 日历状态
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) } // 1-12
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // 计算当前月有多少天
    val daysInMonth = calendar.apply {
        set(selectedYear, selectedMonth - 1, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

    // 生成日期列表
    val days = (1..daysInMonth).toList()

    // 使用 produceState 在协程中加载当前月份的可用时间段
    val availableSlotsForMonth by produceState<List<AvailabilityEntity>>(
        initialValue = emptyList(),
        key1 = selectedYear,
        key2 = selectedMonth
    ) {
        value = viewModel.loadAvailableSlotsForMonth(tutorId, selectedYear, selectedMonth)
    }

    // 确定哪些日期是可用的（有未被预约的时间段）
    val availableDays = days.associateWith { day ->
        val daySlots = availableSlotsForMonth.filter { slot ->
            val slotCalendar = Calendar.getInstance().apply { time = slot.startTime }
            slotCalendar.get(Calendar.DAY_OF_MONTH) == day
        }
        daySlots.isNotEmpty()
    }

    // 加载所有可用时间段
    LaunchedEffect(tutorId) {
        try {
            viewModel.loadAvailableSlots(tutorId)
            val slots = database.availabilityDao().getAvailableSlots(tutorId)
            println("DEBUG: Available slots in database for tutorId=$tutorId: $slots")
            if (slots.isEmpty()) {
                loadError = "No slots found in database for tutorId=$tutorId"
            }
        } catch (e: Exception) {
            println("ERROR: Failed to load available slots: ${e.message}")
            loadError = "Failed to load available slots: ${e.message}"
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Book a Meeting", style = MaterialTheme.typography.headlineMedium)

        // 成功提示
        if (showSuccessMessage) {
            Text(
                text = "Booking successful!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.semantics {
                        contentDescription = "Loading available slots"
                    }
                )
            }
        } else if (loadError != null) {
            Text(
                text = loadError ?: "Error loading slots",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            // 显示错误信息
            bookingError?.let { error ->
                Text(
                    text = "Error: $error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 年份选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Year: $selectedYear", style = MaterialTheme.typography.bodyLarge)
                Row {
                    TextButton(onClick = { if (selectedYear > 2025) selectedYear-- }) {
                        Text("-")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { selectedYear++ }) {
                        Text("+")
                    }
                }
            }

            // 月份选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Month: $selectedMonth", style = MaterialTheme.typography.bodyLarge)
                Row {
                    TextButton(onClick = { if (selectedMonth > 1) selectedMonth-- }) {
                        Text("-")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { if (selectedMonth < 12) selectedMonth++ }) {
                        Text("+")
                    }
                }
            }

            // 日期选择（日历表格）
            Text("Day:", style = MaterialTheme.typography.bodyLarge)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(days.size) { index ->
                    val day = days[index]
                    val isAvailable = availableDays[day] == true
                    val isSelected = selectedDay == day
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable(
                                enabled = isAvailable,
                                onClick = { selectedDay = day }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isAvailable -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 显示选中日期的可用时间段
            if (selectedDay != null) {
                val selectedDateSlots = availableSlotsForMonth.filter { slot ->
                    val slotCalendar = Calendar.getInstance().apply { time = slot.startTime }
                    slotCalendar.get(Calendar.DAY_OF_MONTH) == selectedDay
                }

                if (selectedDateSlots.isEmpty()) {
                    Text(
                        text = "No available time slots for this date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Available Time Slots for $selectedYear-$selectedMonth-$selectedDay:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    selectedDateSlots.forEach { slot ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${dateFormat.format(slot.startTime)} - ${dateFormat.format(slot.endTime)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.bookSlot(slot, studentId)
                                        if (bookingError == null) {
                                            showSuccessMessage = true
                                            // 刷新日历
                                            selectedDay = null
                                        }
                                    }
                                }
                            ) {
                                Text("Book")
                            }
                        }
                    }
                }
            }
        }
    }
}