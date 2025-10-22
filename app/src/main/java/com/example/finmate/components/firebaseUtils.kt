package com.example.finmate.components

import com.example.finmate.model.Expenses
import com.example.finmate.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
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



/**
 * Saves an expense (either voice-parsed or manual) to Firestore using the
 * correct collection structure (users/{uid}/summary_data/{yyyy-MM}/expenses/{docId}).
 * This ensures consistency with your manual entry format.
 */
fun saveExpenseToFirestoreVoice(
    expense: Expenses,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid == null) {
        onFailure(Exception("User not logged in"))
        return
    }

    // 1. Determine the monthId from the expense date.
    // Assuming expense.date format is "dd/MM/yyyy" (based on your DateAndTimePicker.kt).
    val monthId = try {
        // Input format matches the date string in the Expenses object
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(expense.date)

        // Output format is the document ID for the month (e.g., "2024-03")
        val outputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        // Fallback to the current month if date parsing fails
        val calendar = Calendar.getInstance()
        val defaultFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        defaultFormat.format(calendar.time)
    }

    val db = FirebaseFirestore.getInstance()
    // It is best practice to use a copy without a local ID, letting Firestore generate the ID
    val expenseData = expense.copy(id = "")

    // 2. Save to the correct, nested Firestore path
    db.collection("users")
        .document(uid)
        .collection("summary_data")
        .document(monthId) // Document ID for the Month (e.g., "2024-03")
        .collection("expenses")
        .add(expenseData) // Firestore generates a unique ID for the document
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener {
            onFailure(it)
        }
}