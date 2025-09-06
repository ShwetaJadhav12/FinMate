package com.example.finmate

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.YearMonth

class SharedMonthViewModelnew : ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    @RequiresApi(Build.VERSION_CODES.O)
    fun setMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }
}
