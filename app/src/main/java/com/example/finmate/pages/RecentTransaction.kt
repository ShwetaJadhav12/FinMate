package com.example.finmate.pages

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finmate.GlobNavigation.navController
import com.example.finmate.model.Expenses
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// Transaction data class
data class Transaction(
    val title: String,
    val amount: Double,
    val category: String,
    val date: String
)

@Composable
fun RecentTransactionsSection() {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val expenses = remember { mutableStateListOf<Expenses>() }
    val context = LocalContext.current

    LaunchedEffect(uid) {
        if (uid != null) {
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("expenses")
                    .get()
                    .await()

                val fetchedExpenses = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Expenses::class.java)
                }

                // Sort by date + time (latest first)
                val sorted = fetchedExpenses.sortedByDescending { expense ->
                    val dateTimeString = "${expense.date} ${expense.time}"
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(dateTimeString)
                }

                expenses.clear()
                expenses.addAll(sorted.take(3)) // only top 2
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching transactions", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recent Transactions",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            Text(
                text = "Show More",
                color = Color(0xFF2196F3),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { navController.navigate("showMoreTransactions") }
            )
        }

        LazyColumn {
            items(expenses) { expense ->
                TransactionCard(expense)
            }
        }

    }
}

@Composable
fun TransactionCard(expense: Expenses) {
    val isDark = isSystemInDarkTheme() // true if dark theme, false if light

    val cardBackground = if (isDark) Color(0xFF2A3A4F) else Color(0xFFC9E5FA)
    val textTitleColor = if (isDark) Color(0xFFB3E5FC) else Color.Black
    val textSecondaryColor = if (isDark) Color(0xFFB0B0B0) else Color(0xFF154470)
    val dateColor = if (isDark) Color(0xFFB2C5D7) else Color(0xFF3A3939)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(expense.title, fontWeight = FontWeight.Bold, color = textTitleColor)
                Text(expense.category, fontSize = 12.sp, color = textSecondaryColor,
                    fontWeight = FontWeight.SemiBold)
                Text("${expense.date} ${expense.time}", fontSize = 12.sp, color = dateColor)
            }
            Text(
                text = "₹${expense.amount}",
                color = if ((expense.amount.toDoubleOrNull() ?: 0.0) >= 0) Color(0xFF348A37) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
