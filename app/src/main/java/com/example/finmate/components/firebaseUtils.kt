package com.example.finmate.components

import com.example.finmate.model.Expenses
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun fetchExpensesByCategory(
    category: String,
    onSuccess: (List<Expenses>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .collection("expenses")
        .whereEqualTo("category", category)
        .get()
        .addOnSuccessListener { result ->
            val expenses = result.mapNotNull { it.toObject(Expenses::class.java) }
            onSuccess(expenses)
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

