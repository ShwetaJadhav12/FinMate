package com.example.finmate.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddIncomeToDashBoard(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Income") },
            text = {
                Column {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            errorMessage = null
                        },
                        label = { Text("Enter Amount (₹)") },
                        isError = errorMessage != null,
                        singleLine = true
                    )
                    errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
//                    val income = amount.toIntOrNull()
//                    if (income == null || income <= 0) {
//                        errorMessage = "Please enter a valid positive number"
//                        return@TextButton
//                    }
//
//
//                    val db = FirebaseFirestore.getInstance()
//                    val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//                    if (userId != null) {
//                        val incomeMap = mapOf("income" to income.toDouble())
//
//                        db.collection("users")
//                            .document(userId)
//                            .set(incomeMap, SetOptions.merge()) // 🔁 merge to keep budget/expenses
//                            .addOnSuccessListener {
//                                onSave(income) // notify parent
//                                onDismiss() // close dialog
//                            }
//                            .addOnFailureListener {
//                                errorMessage = "Failed to save income: ${it.message}"
//                            }
//                    } else {
//                        errorMessage = "User not logged in"
//                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
