package com.example.finmate.pages

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
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
    var searchQuery by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // 🎨 THEME COLORS
    val primaryBlue = if (isDark) Color(0xFF90CAF9) else Color(0xFF2196F3)
    val cardBg = if (isDark) Color(0xFF2A2E52) else Color(0xFFBBDEFB)
    val textPrimary = if (isDark) Color.White else Color.Black
    val textSecondary = if (isDark) Color.LightGray else Color.DarkGray

    // Format month as yyyy-MM
    val monthId = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

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
                    Toast.makeText(
                        context,
                        "Failed to fetch: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    isLoading = false
                }
            )
        }
    }

    fun totalSpent(): Int = expenses.sumOf { it.amount.toIntOrNull() ?: 0 }

    val filteredExpenses = expenses.filter { expense ->
        searchQuery.isBlank() ||
                expense.title.contains(searchQuery, true) ||
                expense.amount.contains(searchQuery) ||
                expense.date.contains(searchQuery) ||
                expense.time.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$categoryName Expenses ($monthId)",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryBlue
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(12.dp)
        ) {

            // 💳 Total Spent Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Total Spent",
                        style = MaterialTheme.typography.titleMedium,
                        color = textSecondary
                    )
                    Text(
                        "₹ ${totalSpent()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🔍 SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search expenses...", color = textSecondary) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = textPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = textSecondary,
                    cursorColor = primaryBlue
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(color = primaryBlue)
                }
                filteredExpenses.isEmpty() -> {
                    Text(
                        "No matching expenses found",
                        color = textSecondary
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredExpenses) { expense ->
                            ExpenseCard(
                                expense = expense,
                                onExpenseDeleted = { _ ->
                                    expenses = expenses.filter { it.id != expense.id }
                                },
                                onExpenseUpdated = { newAmount, _ ->
                                    expenses = expenses.map {
                                        if (it.id == expense.id)
                                            it.copy(amount = newAmount.toString())
                                        else it
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
