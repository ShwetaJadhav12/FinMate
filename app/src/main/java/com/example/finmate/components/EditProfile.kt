package com.example.finmate.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finmate.model.User
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EditProfileDialog(
    onDismiss: () -> Unit,
    onUserUpdated: (User) -> Unit // Callback to update profile page
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        fetchUserData(
            onSuccess = {
                name = it.name
                email = it.email
                isLoading = false
            },
            onFailure = {
                Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )
    }

    if (!isLoading) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Edit Profile",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateUserData(name, email,
                            onSuccess = {
                                val updatedUser = User(name, email, FirebaseAuth.getInstance().currentUser?.uid)
                                Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                                onUserUpdated(updatedUser)
                                onDismiss()
                            },
                            onFailure = {
                                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
// Transaction data class
data class Transaction(
    val title: String,
    val amount: Double,
    val category: String,
    val date: String
)

@Composable
fun RecentTransactionsSection() {
    val transactions = listOf(
        Transaction("Coffee at Starbucks", 250.0, "Food & Drink", "2025-08-10"),
        Transaction("Groceries", 1800.0, "Groceries", "2025-08-09"),

    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent Transactions",
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp
        )

        Text(
            text = "Show more",
            color = Color(0xFF1976D2), // Blue theme
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable {
                // TODO: Navigate to all transactions screen
            }
        )
    }
        LazyColumn {
            items(transactions) { transaction ->
                TransactionCard(transaction)
            }
        }
    }


@Composable
fun TransactionCard(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD) // Light blue background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0D47A1) // Dark blue text
                )
                Text(
                    text = transaction.category,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = transaction.date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "₹${transaction.amount}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1976D2) // Blue amount
            )
        }
    }
}
