package com.example.finmate.components

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.finmate.model.Income
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun saveMonthlyBudgetToFirebase(
    userId: String,
    amount: String,
    startDate: LocalDate,
    endDate: LocalDate,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val monthId = "${startDate.year}-${startDate.monthValue.toString().padStart(2, '0')}-${startDate.dayOfMonth.toString().padStart(2, '0')}"

    val budgetData = mapOf(
        "amount" to amount,
        "startDate" to startDate.toString(),
        "endDate" to endDate.toString()
    )

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .collection(monthId)
        .document("summaryt")
        .set(budgetData)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}


@RequiresApi(Build.VERSION_CODES.O)
fun fetchMonthlyBudget(
    userId: String,
    startDate: String,
    onResult: (Int) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val monthId = startDate.format(formatter)
    println("monthId: $monthId")

    val summaryRef = Firebase.firestore
        .collection("users")
        .document(userId)
        .collection(monthId)
        .document("summaryt")

    summaryRef.get()
        .addOnSuccessListener { doc ->
            val amountString = doc.getString("amount") ?: "0"
            val amount = amountString.toDoubleOrNull()?.toInt() ?: 0
            onResult(amount)
        }
}
fun saveIncomeToFirestore(
    income: Income,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid != null) {
        val db = FirebaseFirestore.getInstance()
        val incomeRef = db.collection("users")
            .document(uid)
            .collection("income")
            .document("incomeData") // single doc for income

        incomeRef.set(income)  // overwrite if already exists
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    } else {
        onFailure(Exception("User not logged in"))
    }
}
fun getIncomeFromFirestore(
    onSuccess: (Income?) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid != null) {
        val db = FirebaseFirestore.getInstance()
        val incomeRef = db.collection("users")
            .document(uid)
            .collection("income")
            .document("incomeData")

        incomeRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val income = document.toObject(Income::class.java)
                    onSuccess(income)
                } else {
                    onSuccess(null) // no income yet
                }
            }
            .addOnFailureListener { onFailure(it) }
    } else {
        onFailure(Exception("User not logged in"))
    }
}