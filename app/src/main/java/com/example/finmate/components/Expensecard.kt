package com.example.finmate.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finmate.model.Expenses
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ExpenseCard(
    expense: Expenses,
    onExpenseDeleted: (amount: Int) -> Unit,
    onExpenseUpdated: (newAmount: Int, oldAmount: Int) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) Color(0xFFB3E5FC) else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${expense.amount}",
                    fontSize = 16.sp,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Date: ${expense.date}",
                    fontSize = 14.sp,
                    color = if (isSystemInDarkTheme()) Color(0xFFB0B0B0) else Color.Gray
                )
                Text(
                    text = "Time: ${expense.time}",
                    fontSize = 14.sp,
                    color = if (isSystemInDarkTheme()) Color(0xFFB0B0B0) else Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                // Delete
                IconButton(
                    onClick = {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@IconButton
                        val db = FirebaseFirestore.getInstance()
                        val amountInt = expense.amount.toIntOrNull() ?: 0

                        db.collection("users").document(uid)
                            .collection("expenses")
                            .document(expense.id)
                            .delete()
                            .addOnSuccessListener { onExpenseDeleted(amountInt) }
                            .addOnFailureListener { it.printStackTrace() }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }

                // Edit
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.DarkGray)
                }
            }
        }
    }

    if (showEditDialog) {
        EditExpenseDialog(
            expense = expense,
            onDismiss = { showEditDialog = false },
            onUpdate = { newTitle, newAmount, newDate, newTime ->
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@EditExpenseDialog
                val db = FirebaseFirestore.getInstance()
                val oldAmount = expense.amount.toIntOrNull() ?: 0
                val newAmountInt = newAmount.toIntOrNull() ?: 0

                val updatedExpense = expense.copy(
                    title = newTitle,
                    amount = newAmount,
                    date = newDate,
                    time = newTime
                )

                db.collection("users").document(uid)
                    .collection("expenses")
                    .document(expense.id)
                    .set(updatedExpense)
                    .addOnSuccessListener {
                        onExpenseUpdated(newAmountInt, oldAmount)
                        showEditDialog = false
                    }
                    .addOnFailureListener { it.printStackTrace() }
            }
        )
    }
}
@Composable
fun EditExpenseDialog(
    expense: Expenses,
    onDismiss: () -> Unit,
    onUpdate: (title: String, amount: String, date: String, time: String) -> Unit
) {
    var title by remember { mutableStateOf(expense.title) }
    var amount by remember { mutableStateOf(expense.amount) }
    var date by remember { mutableStateOf(expense.date) }
    var time by remember { mutableStateOf(expense.time) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") })
            }
        },
        confirmButton = {
            Button(onClick = { onUpdate(title, amount, date, time) }) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

