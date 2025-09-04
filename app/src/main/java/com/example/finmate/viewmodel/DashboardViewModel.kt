package com.example.finmate.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DashboardViewModel : ViewModel() {

    var income by mutableStateOf(0);
    var expenses by mutableStateOf(0);
    var budget by mutableStateOf(0);
    var remaining by mutableStateOf(0);

    // Load all data for selected month from Firestore
    @SuppressLint("NewApi")
    fun loadData(selectedDate: YearMonth) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val monthId = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val firestore = FirebaseFirestore.getInstance()

                // load income (income doc inside income_expenses/income)
                val incomeSnap = firestore.collection("users")
                    .document(uid)
                    .collection("summary_data")
                    .document(monthId)
                    .collection("income_expenses")
                    .document("income")
                    .get()
                    .await()
                income = incomeSnap.getString("amount")?.toIntOrNull() ?: 0

                // load budget (document field "budget" or "amount" - adapt if needed)
                val budgetDoc = firestore.collection("users")
                    .document(uid)
                    .collection("summary_data")
                    .document(monthId)
                    .get()
                    .await()
                // try both keys for compatibility
                budget = (budgetDoc.getLong("budget")?.toInt()
                    ?: budgetDoc.getString("amount")?.toIntOrNull()
                    ?: 0)

                // load expenses (sum)
                val expensesSnap = firestore.collection("users")
                    .document(uid)
                    .collection("summary_data")
                    .document(monthId)
                    .collection("expenses")
                    .get()
                    .await()
                expenses = expensesSnap.documents.sumOf { it.getString("amount")?.toIntOrNull() ?: 0 }

                // remaining
                remaining = budget - expenses

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "loadData error", e)
                // keep existing values if error
            }
        }
    }

    // Call after a successful save of an expense
    fun addExpense(amount: Int) {
        expenses += amount
        remaining = budget - expenses
    }

    // Call after a successful income save
    fun updateIncome(newIncome: Int) {
        income = newIncome
        remaining = budget - expenses
    }

    // Call after a successful budget save
    fun updateBudget(newBudget: Int) {
        budget = newBudget
        remaining = budget - expenses
    }

    // Optional: force reload
    fun refresh(selectedDate: YearMonth) {
        loadData(selectedDate)
    }
}