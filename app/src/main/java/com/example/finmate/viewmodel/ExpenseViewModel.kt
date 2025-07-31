package com.example.finmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.finmate.model.Expenses
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseViewModel : ViewModel() {

    private val _categoryData = mutableStateOf<Map<String, Double>>(emptyMap())
    val categoryData: State<Map<String, Double>> = _categoryData

    fun fetchAndGroupByCategory(uid: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("expenses")
            .get()
            .addOnSuccessListener { snapshot ->
                val map = mutableMapOf<String, Double>()

                for (doc in snapshot) {
                    val category = doc.getString("category") ?: "Other"
                    val amount = doc.getDouble("amount") ?: 0.0

                    map[category] = map.getOrDefault(category, 0.0) + amount
                }

                _categoryData.value = map
            }
    }
}
