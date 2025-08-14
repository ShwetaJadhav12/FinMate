package com.example.finmate.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.finmate.GlobNavigation.navController
import com.example.finmate.model.Expenses
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowMoreTransactionsScreen() {
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

                val sorted = fetchedExpenses.sortedByDescending { expense ->
                    val dateTimeString = "${expense.date} ${expense.time}"
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(dateTimeString)
                }

                expenses.clear()
                expenses.addAll(sorted)
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching transactions", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Transactions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3198F1))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            items(expenses) { expense ->
                TransactionCard(expense)
            }
        }
    }
}
