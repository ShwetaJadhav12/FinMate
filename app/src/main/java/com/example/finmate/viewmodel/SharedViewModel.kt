package com.example.finmate.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class SharedMonthViewModel : ViewModel() {
    private val _selectedMonthStartDate = MutableStateFlow<LocalDate?>(null)
    val selectedMonthStartDate: StateFlow<LocalDate?> = _selectedMonthStartDate

    private val _budgetAmount = MutableStateFlow<Double?>(null)
    val budgetAmount: StateFlow<Double?> = _budgetAmount

    @RequiresApi(Build.VERSION_CODES.O)
    fun setSelectedMonthStartDate(date: LocalDate) {
        _selectedMonthStartDate.value = date // remove withDayOfMonth(1)
    }


    fun setBudgetAmount(amount: Double?) {
        _budgetAmount.value = amount
    }
}
