package com.example.finmate.viewmodel


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MonthViewModel : ViewModel() {
    val selectedMonth = mutableStateOf("")
}
