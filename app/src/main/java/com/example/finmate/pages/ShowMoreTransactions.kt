package com.example.finmate.pages

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale





@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowMoreTransactionsScreen(
    selectedMonth: YearMonth   // ✅ gets month from navigation
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val expenses = remember { mutableStateListOf<Expenses>() }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) } // for 3-dots menu

    // 🔹 Load transactions for selected month
    LaunchedEffect(uid, selectedMonth) {
        if (uid != null) {
            try {
                val monthId = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("summary_data")
                    .document(monthId)
                    .collection("expenses")
                    .get()
                    .await()

                val fetchedExpenses = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Expenses::class.java)
                }

                val sorted = fetchedExpenses.sortedByDescending { expense ->
                    val dateTimeString = "${expense.date} ${expense.time}"
                    try {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .parse(dateTimeString)
                    } catch (e: Exception) {
                        null
                    }
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
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Oldest Date First") },
                            onClick = {
                                expanded = false
                                expenses.sortBy { expense ->
                                    val dateTimeString = "${expense.date} ${expense.time}"
                                    try {
                                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                            .parse(dateTimeString)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Newest Date First") },
                            onClick = {
                                expanded = false
                                expenses.sortByDescending { expense ->
                                    val dateTimeString = "${expense.date} ${expense.time}"
                                    try {
                                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                            .parse(dateTimeString)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            })
                        DropdownMenuItem(
                            text = { Text("A-->Z") },
                            onClick = {
                                expanded = false
                                expenses.sortBy { it.title }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Time") },
                            onClick = {
                                expanded = false
                                expenses.sortBy { it.time }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Category") },
                            onClick = {
                                expanded = false
                                expenses.sortBy { it.category }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Amount") },
                            onClick = {
                                expanded = false
                            }

                        )
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
                TransactionCard(expense) // ✅ your existing transaction card UI
            }
        }
    }
}
