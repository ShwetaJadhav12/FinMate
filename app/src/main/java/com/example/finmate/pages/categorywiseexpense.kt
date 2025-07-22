package com.example.finmate.pages

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.finmate.GlobNavigation.navController
import com.example.finmate.components.ExpenseCard
import com.example.finmate.model.Expenses
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExpensesScreen(categoryName: String) {
    var expenses by remember { mutableStateOf<List<Expenses>>(emptyList()) }

    // Fetch expenses
    LaunchedEffect(categoryName) {
        FirebaseFirestore.getInstance()
            .collection("expenses")
            .whereEqualTo("categories", categoryName)
            .get()
            .addOnSuccessListener { result ->
                expenses = result.map {
                    it.toObject(Expenses::class.java)
                }
            }
            .addOnFailureListener {
                Log.e("FetchExpenses", "Error: ${it.message}")
            }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF80B2AE))
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(expenses) {
                expense ->
                ExpenseCard(expense = expense)
            }
        }
    }
}
