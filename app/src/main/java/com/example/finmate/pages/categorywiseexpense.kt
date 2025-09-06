package com.example.finmate.pages

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.finmate.GlobNavigation.navController
import com.example.finmate.components.ExpenseCard
import com.example.finmate.components.fetchExpensesByCategory
import com.example.finmate.model.Expenses
import com.google.firebase.auth.FirebaseAuth
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExpensesScreen(
    categoryName: String,
    selectedMonth: YearMonth
) {
    var expenses by remember { mutableStateOf<List<Expenses>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // Format month as yyyy-MM
    val monthId = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    // 🔁 Fetch expenses of this category for the selected month
    LaunchedEffect(categoryName, selectedMonth) {
        if (uid != null) {
            isLoading = true
            fetchExpensesByCategory(
                uid = uid,
                monthId = monthId,
                category = categoryName,
                onSuccess = { fetchedExpenses ->
                    expenses = fetchedExpenses
                    isLoading = false
                },
                onFailure = { error ->
                    Toast.makeText(context, "Failed to fetch: ${error.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            )
        }
    }

    // Total spent for this category in the month
    fun totalSpent(): Int = expenses.sumOf { it.amount.toIntOrNull() ?: 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "$categoryName Expenses ($monthId)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(12.dp)
        ) {

            // Total Spent Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Spent", style = MaterialTheme.typography.titleMedium)
                    Text("₹ ${totalSpent()}", style = MaterialTheme.typography.titleLarge)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                expenses.isEmpty() -> {
                    Text("No expenses found for $categoryName in $monthId")
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(expenses) { expense ->
                            ExpenseCard(
                                expense = expense,
                                onExpenseDeleted = { _ ->
                                    expenses = expenses.filter { it.id != expense.id }
                                },
                                onExpenseUpdated = { newAmount, _ ->
                                    expenses = expenses.map {
                                        if (it.id == expense.id) it.copy(amount = newAmount.toString()) else it
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
