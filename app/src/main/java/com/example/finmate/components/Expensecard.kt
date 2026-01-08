package com.example.finmate.components

import android.os.Build
import androidx.annotation.RequiresApi
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseCard(
    expense: Expenses,
    onExpenseDeleted: (amount: Int) -> Unit,
    onExpenseUpdated: (newAmount: Int, oldAmount: Int) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    // 🎨 Theme colors (FINAL)
    val cardBg = if (isDark) Color(0xFF1F2A33) else Color(0xFFE3F2FD)
    val titleColor = if (isDark) Color(0xFFBBDEFB) else Color(0xFF1565C0)
    val secondaryText = if (isDark) Color(0xFF90A4AE) else Color(0xFF37474F)
    val amountColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
    val errorColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F)
    val editIconColor = if (isDark) Color(0xFFB0BEC5) else Color(0xFF455A64)


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
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
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${expense.amount}",
                    fontSize = 16.sp,
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Date: ${expense.date}",
                    fontSize = 14.sp,
                    color = secondaryText
                )
                Text(
                    text = "Time: ${expense.time}",
                    fontSize = 14.sp,
                    color = secondaryText
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                // 🗑 Delete
                IconButton(
                    onClick = {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@IconButton
                        val db = FirebaseFirestore.getInstance()
                        val amountInt = expense.amount.toIntOrNull() ?: 0
                        val monthId = getMonthIdFromDate(expense.date)

                        db.collection("users").document(uid)
                            .collection("summary_data")
                            .document(monthId)
                            .collection("expenses")
                            .document(expense.id)
                            .delete()
                            .addOnSuccessListener { onExpenseDeleted(amountInt) }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }

                // ✏ Edit
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = editIconColor)
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
                val monthId = getMonthIdFromDate(newDate)

                val updatedExpense = expense.copy(
                    title = newTitle,
                    amount = newAmount,
                    date = newDate,
                    time = newTime
                )

                // ✅ FIXED PATH
                db.collection("users").document(uid)
                    .collection("summary_data")
                    .document(monthId)
                    .collection("expenses")
                    .document(expense.id)
                    .set(updatedExpense)
                    .addOnSuccessListener {
                        onExpenseUpdated(newAmountInt, oldAmount)
                        showEditDialog = false
                    }
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

@RequiresApi(Build.VERSION_CODES.O)
fun getMonthIdFromDate(date: String): String {
    return try {
        val normalized = date.replace("/", "-")
        val localDate = LocalDate.parse(
            normalized,
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )
        YearMonth.from(localDate).format(DateTimeFormatter.ofPattern("yyyy-MM"))
    } catch (e: Exception) {
        YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }
}
