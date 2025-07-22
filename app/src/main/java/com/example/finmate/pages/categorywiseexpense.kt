package com.example.finmate.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.finmate.components.GradientDashboardCard
import com.example.finmate.components.fetchExpensesByCategory
import com.example.finmate.model.Expenses

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExpensesScreen(categoryName: String) {
    var expenses by remember { mutableStateOf<List<Expenses>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // 🔁 Fetch expenses by category
    LaunchedEffect(categoryName) {
        fetchExpensesByCategory(
            category = categoryName,
            onSuccess = {
                expenses = it
                isLoading = false
            },
            onFailure = {
                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "$categoryName Expenses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3198F1))
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(12.dp)) {

            // ✅ Add Budget & Spent cards here
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD6EAF8))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Budget")
                        Text("₹ 5000") // Replace with dynamic value if needed
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9E79F))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Spent")
                        Text("₹ ${expenses.sumOf { it.amount.toInt() }}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(expenses) { expense ->
                        ExpenseCard(expense = expense)
                    }
                }
            }
        }
    }
}
