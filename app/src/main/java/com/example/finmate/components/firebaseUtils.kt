package com.example.finmate.components

import com.example.finmate.model.Expenses
import com.example.finmate.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

suspend fun fetchExpensesByCategory(
    uid: String,
    monthId: String,
    category: String,
    onSuccess: (List<Expenses>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    try {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("summary_data")
            .document(monthId)  // ✅ Only this month
            .collection("expenses")
            .whereEqualTo("category", category)
            .get()
            .await()

        val expenses = snapshot.documents.mapNotNull { it.toObject(Expenses::class.java)?.copy(id = it.id) }
        onSuccess(expenses)
    } catch (e: Exception) {
        onFailure(e)
    }
}


fun fetchUserData(
    onSuccess: (User) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .get()
        .addOnSuccessListener { document ->
            val user = document.toObject(User::class.java)
            if (user != null) onSuccess(user)
        }
        .addOnFailureListener { onFailure(it) }
}

fun updateUserData(
    name: String,
    email: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val userMap = mapOf(
        "name" to name,
        "email" to email
    )

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .update(userMap)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}
