package com.example.finmate.components

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun saveMonthlyBudgetToFirebase(
    userId: String,
    monthId: String,  // "2025-07"
    amount: String,
    startDay: Int,
    startDate: LocalDate,
    endDate: LocalDate,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    val budgetData = hashMapOf(
        "amount" to amount,
        "startDay" to startDay,
        "startDate" to startDate.toString(),
        "endDate" to endDate.toString(),
        "type" to "Monthly"
    )

    db.collection("users")
        .document(userId)
        .collection(monthId)
        .document("summaryt")  // ⚠️ this makes it a document, not a collection!
        .set(budgetData)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e) }
}
@RequiresApi(Build.VERSION_CODES.O)
fun fetchMonthlyBudget(
    userId: String,
    onResult: (Int) -> Unit
) {
    // Use correct month key, based on your Firestore structure
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val currentMonthKey = LocalDate.now().format(formatter)

    val summaryRef = Firebase.firestore
        .collection("users")
        .document(userId)
        .collection(currentMonthKey)
        .document("summaryt")

    summaryRef.get()
        .addOnSuccessListener { doc ->
            val amountString = doc.getString("amount") ?: "0"
            val amount = amountString.toDoubleOrNull()?.toInt() ?: 0
            onResult(amount)
        }
}
