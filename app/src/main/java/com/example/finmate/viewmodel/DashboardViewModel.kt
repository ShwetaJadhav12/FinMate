package com.example.finmate.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finmate.model.Expenses
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DashboardViewModel : ViewModel() {

    var income by mutableStateOf(0)
    var expenses by mutableStateOf(0)
    var budget by mutableStateOf(0)
    var remaining by mutableStateOf(0)
    var expenseList by mutableStateOf(listOf<Expenses>())
        private set

    // ================= Load all data for selected month =================
    @SuppressLint("NewApi")
    fun loadData(selectedDate: YearMonth) {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val monthId = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val firestore = FirebaseFirestore.getInstance()

                // ----- Load Income -----
                val incomeSnap = firestore.collection("users")
                    .document(uid)
                    .collection("summary_data")
                    .document(monthId)
                    .collection("income_expenses")
                    .document("income")
                    .get()
                    .await()
                income = incomeSnap.getString("amount")?.toIntOrNull() ?: 0

                // ----- Load Budget -----
                val budgetDoc = firestore.collection("users")
                    .document(uid)
                    .collection("summary_data")
                    .document(monthId)
                    .get()
                    .await()
                budget = budgetDoc.getString("budget")?.toIntOrNull() ?: 0


                // ----- Load Expenses List -----
                val expensesSnap = firestore.collection("users")
                    .document(uid)
                    .collection("summary_data")
                    .document(monthId)
                    .collection("expenses")
                    .get()
                    .await()

                expenseList = expensesSnap.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Expenses::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { (it.date ?: "") + (it.time ?: "") } // Most recent first

                // ----- Calculate total expenses -----
                expenses = expenseList.sumOf { it.amount?.toIntOrNull() ?: 0 }

                // ----- Calculate remaining -----
                remaining = budget - expenses

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "loadData error", e)
                // keep existing values if error
            }
        }
    }

    // ================= Add New Expense =================
    fun addExpense(expense: Expenses) {
        val amt = expense.amount?.toIntOrNull() ?: 0
        expenseList = listOf(expense) + expenseList // add on top
        expenses += amt
        remaining = budget - expenses
    }

    // ================= Update Income =================
    fun updateIncome(newIncome: Int) {
        income = newIncome
        remaining = budget - expenses
    }

    // ================= Update Budget =================
    fun updateBudget(newBudget: Int) {
        budget = newBudget
        remaining = budget - expenses
    }

    // ================= Force Reload =================
    fun refresh(selectedDate: YearMonth) {
        loadData(selectedDate)
    }
}
