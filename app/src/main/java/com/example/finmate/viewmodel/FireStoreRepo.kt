package com.example.finmate.viewmodel

import com.example.finmate.model.Expenses
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"

    private fun getCurrentMonthId(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())  // Example: "2025-08"
    }

    fun addExpense(expense: Expenses, onResult: (Boolean) -> Unit) {
        val monthId = getCurrentMonthId()
        val expenseRef = db.collection("users")
            .document(userId)
            .collection("summary_data")
            .document(monthId)
            .collection("expenses")
            .document()

        expenseRef.set(expense)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun addIncome(amount: Double, source: String, onResult: (Boolean) -> Unit) {
        val monthId = getCurrentMonthId()
        val incomeData = mapOf(
            "amount" to amount,
            "source" to source,
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        val incomeRef = db.collection("users")
            .document(userId)
            .collection("summary_data")
            .document(monthId)
            .collection("income")
            .document()

        incomeRef.set(incomeData)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
