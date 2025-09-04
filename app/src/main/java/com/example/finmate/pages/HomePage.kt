package com.example.finmate.pages

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finmate.R
import com.example.finmate.SharedMonthViewModelnew
import com.example.finmate.model.Expenses
import com.example.finmate.viewmodel.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

// ===================== Colors =====================
val incomeColor = Color(0xFF4CAF50)
val expensesColor = Color(0xFFF44336)
val budgetColor = Color(0xFFFF9800)
val remainingColor = Color(0xFF2196F3)

// ===================== Notification =====================
fun showBudgetNotification(context: Context) {
    val channelId = "budget_alert"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Budget Alerts", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)
    }
    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("Budget Alert")
        .setContentText("Your monthly budget is 95% full!")
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .build()
    manager.notify(1001, notification)
}

// ===================== Firebase Save Helpers =====================
@RequiresApi(Build.VERSION_CODES.O)
private fun saveIncome(
    newAmount: Int,
    selectedDate: YearMonth,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val monthId = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    val summaryRef = FirebaseFirestore.getInstance()
        .collection("users").document(uid)
        .collection("summary_data").document(monthId)

    summaryRef.set(mapOf("income" to newAmount), SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun saveBudget(
    newAmount: Int,
    selectedDate: YearMonth,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val monthId = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    val summaryRef = FirebaseFirestore.getInstance()
        .collection("users").document(uid)
        .collection("summary_data").document(monthId)

    summaryRef.set(mapOf("budget" to newAmount), SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun saveExpense(
    expense: Expenses,
    selectedDate: YearMonth,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val monthId = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    val documentId = expense.id.ifEmpty { System.currentTimeMillis().toString() }

    val db = FirebaseFirestore.getInstance()
    val summaryRef = db.collection("users").document(uid)
        .collection("summary_data").document(monthId)

    val expenseRef = summaryRef.collection("expenses").document(documentId)

    db.runBatch { batch ->
        batch.set(expenseRef, expense)
        batch.set(summaryRef, mapOf("lastUpdated" to System.currentTimeMillis()), SetOptions.merge())
    }.addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}

// ===================== HomeScreen =====================
@SuppressLint("SuspiciousIndentation")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val sharedMonthViewModel: SharedMonthViewModelnew = viewModel()
    var userName by remember { mutableStateOf("User") }
    var initialLetter by remember { mutableStateOf("U") }

    var showIncomeDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showManualExpenseDialog by remember { mutableStateOf(false) }

    val selectedDate by sharedMonthViewModel.selectedMonth.collectAsState()
    val backgroundColor =
        if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Color(0xFFF5F5F5) else Color(0xFF121212)
    val context = LocalContext.current

    val dashboardVM: DashboardViewModel = viewModel()

    // Load user info
    LaunchedEffect(Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val displayName = firebaseUser?.displayName
        val uid = firebaseUser?.uid
        if (!displayName.isNullOrBlank()) {
            userName = displayName
            initialLetter = displayName.first().uppercaseChar().toString()
        } else if (uid != null) {
            try {
                val userDoc =
                    FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
                val nameFromDb = userDoc.getString("name") ?: "User"
                userName = nameFromDb
                initialLetter = nameFromDb.first().uppercaseChar().toString()
            } catch (_: Exception) {
            }
        }
    }

    // Load summary data when month changes
    LaunchedEffect(selectedDate) {
        dashboardVM.loadData(selectedDate)
    }

    // Trigger 95% budget notification
    LaunchedEffect(dashboardVM.budget, dashboardVM.expenses) {
        if (dashboardVM.budget > 0 &&
            dashboardVM.expenses >= (dashboardVM.budget * 0.95).toInt()
        ) {
            showBudgetNotification(context)
        }
    }

    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FinMate", color = Color.White, fontWeight = FontWeight.SemiBold) },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .border(0.5.dp, Color.White, CircleShape)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { navController.navigate("profilepage") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initialLetter, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF2196F3)) {
                val items = listOf("Home", "Analytics", "Category", "Settings")
                val icons = listOf(
                    Icons.Default.Home,
                    R.drawable.baseline_auto_graph_24,
                    R.drawable.baseline_category_24,
                    Icons.Default.Settings
                )

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            when (index) {
                                0 -> navController.navigate("home")
                                1 -> navController.navigate("analytics")
                                2 -> navController.navigate("categorypage")
                                3 -> navController.navigate("profilepage")
                            }
                        },
                        icon = {
                            if (icons[index] is ImageVector) {
                                Icon(icons[index] as ImageVector, contentDescription = item)
                            } else {
                                Icon(
                                    painter = painterResource(id = icons[index] as Int),
                                    contentDescription = item
                                )
                            }
                        },
                        label = { Text(item, fontSize = 12.sp) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF2196F3),
                            selectedIconColor = Color(0xFF194365),
                            selectedTextColor = Color(0xFF194365),
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .background(backgroundColor),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonthSelector(sharedMonthViewModel, selectedDate)
            Row(horizontalArrangement = Arrangement.spacedBy(13.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardCard("Income", dashboardVM.income, incomeColor)
                DashboardCard("Expenses", dashboardVM.expenses, expensesColor)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(13.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardCard("Budget", dashboardVM.budget, budgetColor)
                DashboardCard("Remaining", dashboardVM.remaining, remainingColor)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showIncomeDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = incomeColor)
                ) { Text("Add Income", color = Color.White) }

                Button(
                    onClick = { showBudgetDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = budgetColor)
                ) { Text("Set Budget", color = Color.White) }

                Button(
                    onClick = { showExpenseDialog = true },
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = expensesColor)
                ) { Text("Add Expense", color = Color.White) }
            }
        }
    }

    // ================= Dialogs =================
    if (showIncomeDialog) {
        AmountInputDialog(
            title = "Set Income",
            initial = dashboardVM.income.toString(),
            onConfirm = { value ->
                saveIncome(value, selectedDate,
                    onSuccess = {
                        dashboardVM.updateIncome(value)
                        showIncomeDialog = false
                    },
                    onFailure = { showIncomeDialog = false })
            },
            onDismiss = { showIncomeDialog = false }
        )
    }

    if (showBudgetDialog) {
        AmountInputDialog(
            title = "Set Budget",
            initial = dashboardVM.budget.toString(),
            onConfirm = { value ->
                saveBudget(value, selectedDate,
                    onSuccess = {
                        dashboardVM.updateBudget(value)
                        showBudgetDialog = false
                    },
                    onFailure = { showBudgetDialog = false })
            },
            onDismiss = { showBudgetDialog = false }
        )
    }

    if (showExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("Add Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showManualExpenseDialog = true
                            showExpenseDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Manually")
                    }
                    Button(
                        onClick = { showExpenseDialog = false /* TODO voice input */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Using Voice")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExpenseDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showManualExpenseDialog) {
        ExpenseInputDialog(
            onConfirm = { title, category, amount, date, time ->
                val expense = Expenses(
                    title = title,
                    category = category,
                    amount = amount.toString(),
                    date = date,
                    time = time
                )
                saveExpense(expense, selectedDate,
                    onSuccess = {
                        dashboardVM.addExpense(amount)
                        showManualExpenseDialog = false
                    },
                    onFailure = { showManualExpenseDialog = false })
            },
            onDismiss = { showManualExpenseDialog = false }
        )
    }
}

// ===================== Components =====================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthSelector(sharedMonthViewModel: SharedMonthViewModelnew, selectedDate: YearMonth) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { sharedMonthViewModel.previousMonth() }) { Icon(Icons.Default.ArrowBack, "Prev") }
        Text(selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = { sharedMonthViewModel.nextMonth() }) { Icon(Icons.Default.ArrowForward, "Next") }
    }
}

@Composable
fun DashboardCard(title: String, amount: Int, color: Color) {
    Box(
        modifier = Modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(amount.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AmountInputDialog(title: String, initial: String, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Amount") },
                singleLine = true
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text.toIntOrNull() ?: 0) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ExpenseInputDialog(
    onConfirm: (String, String, Int, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    val categories = listOf("Food", "Shopping", "Travel", "Utensils", "Health", "Education")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
                Spacer(Modifier.height(4.dp))
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = {
                                category = cat
                                expanded = false
                            })
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, singleLine = true)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(date, { date = it }, label = { Text("Date (YYYY-MM-DD)") }, singleLine = true)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(time, { time = it }, label = { Text("Time (HH:MM)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, category, amount.toIntOrNull() ?: 0, date, time) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
