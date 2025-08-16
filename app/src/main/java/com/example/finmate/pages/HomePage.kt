package com.example.finmate.pages

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finmate.R
import com.example.finmate.components.*
import com.example.finmate.viewmodel.SharedMonthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ===================== Helpers & Models =====================

fun Color.adjustBrightness(factor: Float): Color {
    return copy(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f)
    )
}

private data class Income(val amount: String = "")

private suspend fun loadIncome(): Int {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return 0
    val snap = FirebaseFirestore.getInstance()
        .collection("users").document(uid)
        .collection("user_data").document("income")
        .get().await()
    val amountStr = snap.getString("amount") ?: return 0
    return amountStr.toIntOrNull() ?: 0
}

private fun saveIncome(
    newAmount: Int,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid == null) {
        onFailure(Exception("User not logged in")); return
    }
    FirebaseFirestore.getInstance()
        .collection("users").document(uid)
        .collection("user_data").document("income")
        .set(Income(amount = newAmount.toString()))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}

// ===================== Screen =====================

@SuppressLint("SuspiciousIndentation")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val sharedMonthViewModel: SharedMonthViewModel = viewModel()
    val context = LocalContext.current

    var userName by remember { mutableStateOf("User") }
    var initialLetter by remember { mutableStateOf("U") }

    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

    var incomemain by remember { mutableStateOf(0) }
    var expensemain by remember { mutableStateOf(0) }
    var budetmain by remember { mutableStateOf(0) }
    var remaining by remember { mutableStateOf(0) }

    var showIncomeDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showMainDialog by remember { mutableStateOf(false) }
    var showMonthlyDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val selectedDate by sharedMonthViewModel.selectedMonthStartDate.collectAsState()

    val colorScheme = MaterialTheme.colorScheme
    val isLightTheme = colorScheme.background.luminance() > 0.5f

    // Define theme-aware colors (unchanged visually)
    val backgroundColor = if (isLightTheme) Color(0xFFF5F5F5) else Color(0xFF121212)
    val cardColor = if (isLightTheme) Color(0xFFFFFFFF) else Color(0xFF1E1E1E)
    val primaryText = if (isLightTheme) Color(0xE10E0101) else Color(0xE10E0101)
    val secondaryText = if (isLightTheme) Color(0xFF492B04) else Color(0xFFAAAAAA)
    val accentColor = if (isLightTheme) Color(0xFF2196F3) else Color(0xFF2196F3)
    val positiveColor = if (isLightTheme) Color(0xFF4CAF50) else Color(0xFF4CAF50)

    // ===================== Fetch budget (existing) =====================
    LaunchedEffect(selectedDate) {
        val date = selectedDate ?: return@LaunchedEffect
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        val monthId = date.toString()
        try {
            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection(monthId)
                .document("summaryt")
                .get()
                .await()

            budetmain = if (document.exists()) {
                document.getString("amount")?.toDouble()?.toInt() ?: 0
            } else 0
        } catch (e: Exception) {
            budetmain = 0
            Log.e("HomeScreen", "Error loading budget", e)
        }
    }

    // ===================== Fetch username (existing) =====================
    LaunchedEffect(Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid

        val displayName = firebaseUser?.displayName
        if (!displayName.isNullOrBlank()) {
            userName = displayName
            initialLetter = displayName.first().uppercaseChar().toString()
        } else if (uid != null) {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()

                val nameFromDb = userDoc.getString("name") ?: "User"
                userName = nameFromDb
                initialLetter = nameFromDb.first().uppercaseChar().toString()
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error fetching username", e)
                userName = "User"
                initialLetter = "U"
            }
        }
    }

    // ===================== Load income on app open =====================
    LaunchedEffect(Unit) {
        try {
            incomemain = loadIncome()
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error loading income", e)
        }
    }

    // ===================== Existing dialogs =====================
    if (showDialog) {
        ExpenseEntryOptionsDialog(
            onDismiss = { showDialog = false },
            onAddManually = {
                showDialog = false
                navController.navigate("addExpense")
            },
            onScanReceipt = { showDialog = false }
        )
    }

    if (showMainDialog) {
        AlertDialog(
            onDismissRequest = { showMainDialog = false },
            title = { Text("Choose Budget Type") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        showMainDialog = false
                        showMonthlyDialog = true
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Add Monthly Budget")
                    }
                    Button(onClick = {
                        showMainDialog = false
                        showCategoryDialog = true
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Add Category-wise Budget")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    var selectedIndex by remember { mutableStateOf(0) }

    // Define gradient for Top Spending (existing)
    val topSpendingGradient = if (isLightTheme) {
        listOf(Color(0xFF2E5D9F), Color(0xFF4A7CC3))
    } else {
        listOf(Color(0xFF3F6FB8), Color(0xFF5A8FD0))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FinMate", color = Color.White, fontWeight = FontWeight.SemiBold) },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .border(width = 0.5.dp, color = Color.White, shape = CircleShape)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { navController.navigate("profilepage") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = initialLetter, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.primary)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = colorScheme.primary) {
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
                                Icon(
                                    imageVector = icons[index] as ImageVector,
                                    contentDescription = item
                                )
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
                            indicatorColor = colorScheme.primary,
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
                .background(backgroundColor)
        ) {
            // Top Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor.adjustBrightness(if (isLightTheme) 0.9f else 1.1f))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = currentDate, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = primaryText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have used 90% of your budget.\nTry saving in Food.",
                        fontSize = 15.sp,
                        color = secondaryText.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dashboard Grid (unchanged; shows incomemain)
            DashboardGrid(
                expensemain = expensemain,
                remaining = remaining,
                budetmain = budetmain,
                incomemain = incomemain,
                isLightTheme = isLightTheme,
                cardColor = cardColor
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Remaining : $remaining",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = positiveColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))
            GradientBox(
                text = "Top Spending Categories: Food, Groceries",
                gradientColors = topSpendingGradient,
                fontSize = 16.sp,
                fontColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Add New",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = primaryText,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val buttonColors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                )
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.weight(1f).height(45.dp),
                    colors = buttonColors
                ) {
                    Text("Expenses", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Button(
                    onClick = { showIncomeDialog = true },
                    modifier = Modifier.weight(1f).height(45.dp),
                    colors = buttonColors
                ) {
                    Text("Income", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            RecentTransactionsSection()

            // ===== Income dialog flow (only addition/change) =====
            AddIncomeToDashBoard(
                showDialog = showIncomeDialog,
                currentIncome = incomemain,
                onDismiss = { showIncomeDialog = false },
                onSaved = { newIncome ->
                    incomemain = newIncome
                    showIncomeDialog = false
                }
            )
        }
    }
}

// ===================== Dashboard cards (unchanged except textColor is dynamic) =====================

@Composable
fun DashboardGrid(
    expensemain: Int,
    remaining: Int,
    budetmain: Int,
    incomemain: Int,
    isLightTheme: Boolean,
    cardColor: Color
) {
    val textColor = if (isSystemInDarkTheme()) Color(0xFFB3E5FC) else Color(0xFF17354F)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GradientDashboardCard(
                title = "Expenses",
                t1 = expensemain.toString(),
                gradientColors = listOf(
                    cardColor.adjustBrightness(if (isLightTheme) 0.9f else 1.1f),
                    cardColor.adjustBrightness(if (isLightTheme) 0.85f else 1.15f)
                ),
                modifier = Modifier.weight(1f).height(100.dp),
                textColor = textColor
            )
            GradientDashboardCard(
                title = "Income",
                t1 = incomemain.toString(),
                gradientColors = listOf(
                    cardColor.adjustBrightness(if (isLightTheme) 0.9f else 1.1f),
                    cardColor.adjustBrightness(if (isLightTheme) 0.85f else 1.15f)
                ),
                modifier = Modifier.weight(1f).height(100.dp),
                textColor = textColor
            )
        }
    }
}

// ===================== Income Dialog(s) =====================

@Composable
private fun AddIncomeToDashBoard(
    showDialog: Boolean,
    currentIncome: Int,
    onDismiss: () -> Unit,
    onSaved: (Int) -> Unit
) {
    if (!showDialog) return

    // If no income yet → open input dialog directly
    if (currentIncome <= 0) {
        IncomeInputDialog(
            title = "Set Income",
            initial = "",
            onCancel = onDismiss,
            onConfirm = { entered ->
                val value = entered.toIntOrNull() ?: 0
                saveIncome(
                    newAmount = value,
                    onSuccess = { onSaved(value) },
                    onFailure = { onDismiss() }
                )
            }
        )
        return
    }

    // Else ask whether to edit
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Income?") },
        text = { Text("Income is already set to ₹$currentIncome. Do you want to edit it?") },
        confirmButton = {
            var showEdit by remember { mutableStateOf(false) }
            Button(onClick = { showEdit = true }) { Text("Yes") }

            if (showEdit) {
                IncomeInputDialog(
                    title = "Edit Income",
                    initial = currentIncome.toString(),
                    onCancel = onDismiss,
                    onConfirm = { entered ->
                        val value = entered.toIntOrNull() ?: currentIncome
                        saveIncome(
                            newAmount = value,
                            onSuccess = { onSaved(value) },
                            onFailure = { onDismiss() }
                        )
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("No") }
        }
    )
}

@Composable
private fun IncomeInputDialog(
    title: String,
    initial: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var input by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { ch -> ch.isDigit() } },
                label = { Text("Amount") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                enabled = input.isNotBlank(),
                onClick = { onConfirm(input) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}
