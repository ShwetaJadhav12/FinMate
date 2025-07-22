package com.example.finmate.pages

import android.widget.Toast
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
import com.example.finmate.components.fetchExpensesByCategory
import com.example.finmate.model.Expenses

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExpensesScreen(categoryName: String) {
    var expenses by remember { mutableStateOf<List<Expenses>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // 🔁 Fetch category-specific expenses from Firestore
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

    // 🧾 UI layout with Top Bar and list
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "$categoryName Expenses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF80B2AE))
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(innerPadding).padding(16.dp))
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(expenses) { expense ->
                    ExpenseCard(expense = expense)
                }
            }
        }
    }
}
