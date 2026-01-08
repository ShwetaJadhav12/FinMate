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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
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
    selectedMonth: YearMonth
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val expenses = remember { mutableStateListOf<Expenses>() }
    var searchQuery by remember { mutableStateOf("") }   // ✅ ADDED
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    // 🎨 THEME COLORS
    val topBarColor = if (isDark) Color(0xFF1F2A33) else Color(0xFF3198F1)
    val searchBorder = if (isDark) Color(0xFF90CAF9) else Color(0xFF2196F3)
    val searchText = if (isDark) Color.White else Color.Black
    val searchHint = if (isDark) Color(0xFFB0BEC5) else Color.Gray

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

    // ✅ FILTERED LIST (ADDED)
    val filteredExpenses = expenses.filter { expense ->
        searchQuery.isBlank() ||
                expense.title.contains(searchQuery, true) ||
                expense.amount.contains(searchQuery) ||
                expense.category.contains(searchQuery, true) ||
                expense.date.contains(searchQuery) ||
                expense.time.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Transactions", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Oldest Date First") },
                            onClick = {
                                expanded = false
                                expenses.sortBy {
                                    try {
                                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                            .parse("${it.date} ${it.time}")
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
                                expenses.sortByDescending {
                                    try {
                                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                            .parse("${it.date} ${it.time}")
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("A → Z") },
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
                            onClick = { expanded = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {

            // 🔍 SEARCH BAR (ADDED)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search transactions...", color = searchHint) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = searchText),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = searchBorder,
                    unfocusedBorderColor = searchHint,
                    cursorColor = searchBorder
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(filteredExpenses) { expense ->
                    TransactionCard(expense)
                }
            }
        }
    }
}
