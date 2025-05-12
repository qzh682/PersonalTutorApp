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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.personaltutorapp.data.AppDatabase
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.CalendarViewModel
import com.example.personaltutorapp.viewmodel.CalendarViewModelFactory
import com.example.personaltutorapp.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TutorAvailabilityScreen(
    tutorId: String,
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CalendarViewModelFactory(database.availabilityDao(), database.bookingDao())
    )

    val selectedDateTime by viewModel.selectedDateTime.collectAsState()
    var duration by remember { mutableStateOf("30") } // Default duration: 30 minutes
    var showSuccessDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // 日历状态
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) } // 1-12
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }

    // 计算当前月有多少天
    val daysInMonth = calendar.apply {
        set(selectedYear, selectedMonth - 1, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

    // 生成日期、小时和分钟列表
    val days = (1..daysInMonth).toList()
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // 添加滑动功能
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Set Availability", style = MaterialTheme.typography.headlineMedium)

        // 显示已选择的日期和时间
        Text(
            text = "Selected Date and Time: ${
                selectedDateTime?.let { dateFormat.format(it) } ?: "Not selected"
            }",
            style = MaterialTheme.typography.bodyLarge
        )

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
                .height(150.dp), // 减小高度，使表格更紧凑
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(days.size) { index ->
                val day = days[index]
                val isSelected = selectedDay == day
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable {
                            selectedDay = day
                            // 如果小时和分钟已选择，更新 selectedDateTime
                            if (selectedHour != null && selectedMinute != null) {
                                viewModel.updateSelectedDateTime(
                                    selectedYear,
                                    selectedMonth - 1,
                                    selectedDay!!,
                                    selectedHour!!,
                                    selectedMinute!!
                                )
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            style = MaterialTheme.typography.bodySmall, // 减小字体，使内容更紧凑
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 小时选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Hour: ${selectedHour ?: "Not selected"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Row {
                TextButton(onClick = {
                    selectedHour = (selectedHour ?: 0).let { if (it > 0) it - 1 else 23 }
                    if (selectedDay != null && selectedMinute != null) {
                        viewModel.updateSelectedDateTime(
                            selectedYear,
                            selectedMonth - 1,
                            selectedDay!!,
                            selectedHour!!,
                            selectedMinute!!
                        )
                    }
                }) {
                    Text("-")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    selectedHour = (selectedHour ?: 0).let { if (it < 23) it + 1 else 0 }
                    if (selectedDay != null && selectedMinute != null) {
                        viewModel.updateSelectedDateTime(
                            selectedYear,
                            selectedMonth - 1,
                            selectedDay!!,
                            selectedHour!!,
                            selectedMinute!!
                        )
                    }
                }) {
                    Text("+")
                }
            }
        }

        // 分钟选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Minute: ${selectedMinute ?: "Not selected"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Row {
                TextButton(onClick = {
                    selectedMinute = (selectedMinute ?: 0).let { if (it > 0) it - 1 else 59 }
                    if (selectedDay != null && selectedHour != null) {
                        viewModel.updateSelectedDateTime(
                            selectedYear,
                            selectedMonth - 1,
                            selectedDay!!,
                            selectedHour!!,
                            selectedMinute!!
                        )
                    }
                }) {
                    Text("-")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    selectedMinute = (selectedMinute ?: 0).let { if (it < 59) it + 1 else 0 }
                    if (selectedDay != null && selectedHour != null) {
                        viewModel.updateSelectedDateTime(
                            selectedYear,
                            selectedMonth - 1,
                            selectedDay!!,
                            selectedHour!!,
                            selectedMinute!!
                        )
                    }
                }) {
                    Text("+")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Duration input
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val durationMinutes = duration.toIntOrNull() ?: 30
                viewModel.saveAvailability(tutorId, durationMinutes)
            },
            enabled = selectedDateTime != null
        ) {
            Text("Save Availability")
        }
    }

    // 保存成功提示
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            showSuccessDialog = true
            viewModel.resetSaveState()
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Availability Set") },
            text = { Text("Your availability has been set successfully.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigate(NavRoutes.TutorDashboard.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}