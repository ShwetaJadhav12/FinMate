package com.example.finmate.components

import android.util.Log
import com.example.finmate.model.Expenses
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

fun saveSummaryDataForMonth(
    month: String, // 👈 This will be the user-selected month like "2025-07"
    income: Double? = null,
    expenses: Double? = null,
    remaining: Double? = null,
    budget: String? = null
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    val summaryData = mutableMapOf<String, Any>()
    income?.let { summaryData["income"] = it }
    expenses?.let { summaryData["expenses"] = it }
    remaining?.let { summaryData["remaining"] = it }
    budget?.let { summaryData["budget"] = it }

    db.collection("users").document(userId)
        .collection("summary_data").document(month)
        .set(summaryData, SetOptions.merge())
}
fun saveCategoryBudgetForMonth(
    month: String,
    category: String,
    amount: String
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    val categoryData = mapOf("amount" to amount)

    db.collection("users").document(userId)
        .collection("summary_data").document(month)
        .collection("category_budgets")
        .document(category)
        .set(categoryData)
}

fun calculateAndSaveTotalExpensesForMonth(selectedMonth: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    val monthPrefix = "$selectedMonth-" // e.g. "2025-07-"

    db.collection("users").document(userId).collection("expenses")
        .get()
        .addOnSuccessListener { querySnapshot ->
            var totalExpense = 0.0

            for (document in querySnapshot.documents) {
                val date = document.getString("date") ?: continue
                if (date.startsWith(monthPrefix)) {
                    val amount = document.getDouble("amount") ?: 0.0
                    totalExpense += amount
                }
            }

            // Now fetch the income to calculate remaining
            db.collection("users").document(userId)
                .collection("summary_data").document(selectedMonth)
                .get()
                .addOnSuccessListener { docSnapshot ->
                    val income = docSnapshot.getDouble("income") ?: 0.0
                    val remaining = income - totalExpense

                    val summaryData = mapOf(
                        "expenses" to totalExpense,
                        "remaining" to remaining
                    )

                    db.collection("users").document(userId)
                        .collection("summary_data").document(selectedMonth)
                        .set(summaryData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("Firestore", "Total expenses and remaining updated.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error updating summary: ", e)
                        }
                }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching expenses: ", e)
        }
}
fun fetchMonthlyExpenses(
    selectedMonth: String,
    onResult: (List<Expenses>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    if (uid != null) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("summary_data")
            .document(selectedMonth)
            .collection("expenses")
            .get()
            .addOnSuccessListener { snapshot ->
                val expenses = snapshot.documents.mapNotNull { it.toObject(Expenses::class.java) }
                onResult(expenses)
            }
            .addOnFailureListener { onFailure(it) }
    } else {
        onFailure(Exception("User not logged in"))
    }
}
