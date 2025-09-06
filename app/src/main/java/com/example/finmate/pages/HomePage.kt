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
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import androidx.core.app.NotificationCompat
import com.example.finmate.components.DateAndTimePicker
import java.time.LocalDate
import java.time.LocalDateTime

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
private fun saveIncome(newAmount: Int, selectedDate: YearMonth, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run { onFailure(Exception("User not logged in")); return }
    val monthId = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    FirebaseFirestore.getInstance()
        .collection("users").document(uid)
        .collection("summary_data")
        .document(monthId)
        .collection("income_expenses")
        .document("income")
        .set(mapOf("amount" to newAmount.toString()))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun saveBudget(newAmount: Int, selectedDate: YearMonth, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run { onFailure(Exception("User not logged in")); return }
    val monthId = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    FirebaseFirestore.getInstance()
        .collection("users").document(uid)
        .collection("summary_data")
        .document(monthId)
        .set(mapOf("budget" to newAmount.toString()), SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun saveExpense(expense: Expenses, selectedDate: YearMonth, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run { onFailure(Exception("User not logged in")); return }
    val monthId = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    val documentId = expense.id.ifEmpty { System.currentTimeMillis().toString() }
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .collection("summary_data")
        .document(monthId)
        .collection("expenses")
        .document(documentId)
        .set(expense)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}

// ===================== HomeScreen =====================
@SuppressLint("SuspiciousIndentation")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    sharedMonthViewModel: SharedMonthViewModelnew   // ✅ use same VM from AppNavigation
) {
    var userName by remember { mutableStateOf("User") }
    var initialLetter by remember { mutableStateOf("U") }

    var showIncomeDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showManualExpenseDialog by remember { mutableStateOf(false) }

    val selectedDate by sharedMonthViewModel.selectedMonth.collectAsState()
    val backgroundColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Color(0xFFF5F5F5) else Color(0xFF121212)
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
                val userDoc = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
                val nameFromDb = userDoc.getString("name") ?: "User"
                userName = nameFromDb
                initialLetter = nameFromDb.first().uppercaseChar().toString()
            } catch (_: Exception) {}
        }
    }

    // Load dashboard data per month
    LaunchedEffect(selectedDate) { dashboardVM.loadData(selectedDate) }

    // Budget notification 95%
    LaunchedEffect(dashboardVM.budget, dashboardVM.expenses) {
        if (dashboardVM.budget > 0 && dashboardVM.expenses >= (dashboardVM.budget * 0.95).toInt()) {
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
                            if (icons[index] is ImageVector) Icon(icons[index] as ImageVector, contentDescription = item)
                            else Icon(painter = painterResource(id = icons[index] as Int), contentDescription = item)
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
            BudgetProgressBox(dashboardVM.expenses, dashboardVM.budget)
            MonthSelector(sharedMonthViewModel, selectedDate)

            Row(
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DashboardCard(
                    "Budegt",
                    dashboardVM.budget,
                    incomeColor,
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    "Expenses",
                    dashboardVM.expenses,
                    expensesColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remaining",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp

                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("₹${dashboardVM.budget - dashboardVM.expenses}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = remainingColor
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showBudgetDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = budgetColor)
                    ) {
                        Text("Set Budget", color = Color.White)
                    }

                    Button(
                        onClick = { showExpenseDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = expensesColor)
                    ) {
                        Text("Add Expense", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ✅ Show Top Spending Category
                TopSpendingCategoryBox(

                    selectedMonth = selectedDate,
                )
            }


// ================= Recent Transactions =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Recent Transactions",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                Text(
                    text = "Show More",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        // Navigate to AllTransactionsScreen
                        navController.navigate("showMoreTransactions/${selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))}")
                    }
                )
            }


// Flexible formatter for d/M/yyyy
            val filteredExpenses = dashboardVM.expenseList
                .mapNotNull { expense ->
                    try {
                        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                        val localDate = LocalDate.parse(expense.date, formatter)
                        val expenseMonth = YearMonth.from(localDate)
                        if (expenseMonth == selectedDate) expense else null
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedByDescending {
                    try {
                        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm")
                        LocalDateTime.parse("${it.date} ${it.time}", formatter)
                    } catch (e: Exception) {
                        LocalDateTime.MIN
                    }
                }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredExpenses) { expense ->
                    TransactionItem(expense)
                }
            }
        }
    }

    // ✅ Dialogs unchanged...
    if (showIncomeDialog) {
        AmountInputDialog(
            title = "Set Income",
            initial = dashboardVM.income.toString(),
            onConfirm = { value -> saveIncome(value, selectedDate, { dashboardVM.updateIncome(value); showIncomeDialog=false }, { showIncomeDialog=false }) },
            onDismiss = { showIncomeDialog = false }
        )
    }
    if (showBudgetDialog) {
        AmountInputDialog(
            title = "Set Budget",
            initial = dashboardVM.budget.toString(),
            onConfirm = { value -> saveBudget(value, selectedDate, { dashboardVM.updateBudget(value); showBudgetDialog=false }, { showBudgetDialog=false }) },
            onDismiss = { showBudgetDialog = false }
        )
    }
    if (showExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showExpenseDialog=false },
            title = { Text("Add Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showManualExpenseDialog = true; showExpenseDialog=false }, modifier = Modifier.fillMaxWidth()) { Text("Add Manually") }
                    Button(onClick = { showExpenseDialog=false }, modifier = Modifier.fillMaxWidth()) { Text("Add Using Voice") }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showExpenseDialog=false }) { Text("Cancel") } }
        )
    }
    if (showManualExpenseDialog) {
        ExpenseInputDialog(
            onConfirm = { title, category, amount, date, time ->
                val expense = Expenses(title=title, category=category, amount=amount.toString(), date=date, time=time)
                saveExpense(expense, selectedDate, { dashboardVM.addExpense(expense); showManualExpenseDialog=false }, { showManualExpenseDialog=false })
            },
            onDismiss = { showManualExpenseDialog=false }
        )
    }
}
// ===================== Components =====================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthSelector(sharedMonthViewModel: SharedMonthViewModelnew, selectedDate: YearMonth) {
    val currentMonth = remember { YearMonth.now() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { sharedMonthViewModel.previousMonth() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Prev")
        }
        Text(
            selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (selectedDate < currentMonth) {
            IconButton(onClick = { sharedMonthViewModel.nextMonth() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "₹$value", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun AmountInputDialog(title: String, initial: String, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value=text, onValueChange={text=it}, label={Text("Amount")}, singleLine=true) },
        confirmButton = { TextButton(onClick={ onConfirm(text.toIntOrNull() ?: 0) }) { Text("Save") } },
        dismissButton = { TextButton(onClick=onDismiss){ Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }

    val categories = listOf("Food", "Shopping", "Travel", "Utensils", "Health", "Education")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("Title") },
                    singleLine = true,
                    isError = titleError
                )
                if (titleError) {
                    Text("Title is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = false },
                    label = { Text("Amount") },
                    singleLine = true,
                    isError = amountError
                )
                if (amountError) {
                    Text("Enter a valid amount", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(8.dp))
                DateAndTimePicker(date, time, { date = it }, { time = it })
                if (dateError) Text("Date is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                if (timeError) Text("Time is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                var valid = true
                if (title.isBlank()) { titleError = true; valid = false }
                if (amount.toIntOrNull() == null || amount.isBlank()) { amountError = true; valid = false }
                if (date.isBlank()) { dateError = true; valid = false }
                if (time.isBlank()) { timeError = true; valid = false }
                if (valid) { onConfirm(title, category, amount.toInt(), date, time) }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TransactionItem(expense: Expenses) {
    // Light blue background same as your ShowMoreTransactionsScreen
    val isDarkTheme = isSystemInDarkTheme()

    // Light blue background for light theme, darker blue for dark theme
    val backgroundColor = if (!isDarkTheme) Color(0xFFA0CEEE) else Color(0xFF325279)

    // Text colors adapt based on background luminance
    val textColor = if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
    val secondaryTextColor = textColor.copy(alpha = 0.7f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = expense.title,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = expense.category,
                    fontSize = 12.sp,
                    color = secondaryTextColor
                )
            }
            Text(
                text = "₹ ${expense.amount}",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetProgressBox(expenses: Int, budget: Int) {
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))
    val percentage = if (budget > 0) ((expenses.toFloat() / budget) * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFF2196F3), Color(0xFF26A69A))))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Text(today, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Column {
                Text("Budget used: $percentage%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                LinearProgressIndicator(
                    progress = (percentage.coerceIn(0, 100) / 100f),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).padding(top = 4.dp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TopSpendingCategoryBox(selectedMonth: YearMonth) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var topCategory by remember { mutableStateOf<Pair<String, Int>?>(null) }
    val context = LocalContext.current

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
                val expenses = snapshot.documents.mapNotNull { it.toObject(Expenses::class.java) }
                val categoryTotals = expenses.groupBy { it.category ?: "Other" }
                    .mapValues { entry -> entry.value.sumOf { it.amount.toIntOrNull() ?: 0 } }
                topCategory = categoryTotals.maxByOrNull { it.value }?.toPair()
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching top category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Top Spending Category: ", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.width(6.dp))
            Text(topCategory?.let { "${it.first} - ₹${it.second}" } ?: "No data", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        }
    }
}
