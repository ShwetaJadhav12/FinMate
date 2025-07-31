package com.example.finmate.pages

import AddCategoryBudgetForm
import AddMonthlyBudgetForm
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finmate.R
import com.example.finmate.components.AddIncomeToDashBoard
import com.example.finmate.components.ExpenseEntryOptionsDialog
import com.example.finmate.components.GradientBox
import com.example.finmate.components.GradientButton
import com.example.finmate.components.GradientDashboardCard
import com.example.finmate.components.fetchMonthlyBudget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {

    val context = LocalContext.current
    var userName by remember { mutableStateOf("User") }
    val initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    var incomemain by remember { mutableStateOf(0) }
    var expensemain by remember { mutableStateOf(0) }
    var budetmain by remember { mutableStateOf(0) }
    var remaining by remember { mutableStateOf(0) }
    var showIncomeDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            // 1. Get user name
            Firebase.firestore.collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: "User"
                }

            // 2. Get budget from Firestore
            fetchMonthlyBudget(it) { fetchedBudget ->
                budetmain = fetchedBudget
                remaining = fetchedBudget - expensemain
            }
            println("Remaining: $remaining")
            println("butemain : $budetmain")

        }
    }

    var showDialog by remember { mutableStateOf(false) }
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

    var showMainDialog by remember { mutableStateOf(false) }
    var showMonthlyDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

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

    if (showMonthlyDialog) {
        AlertDialog(
            onDismissRequest = { showMonthlyDialog = false },
            title = { Text("Add Monthly Budget") },
            text = {
                AddMonthlyBudgetForm(
                    onSave = { showMonthlyDialog = false },
                    onCancel = { showMonthlyDialog = false },
                )},

            confirmButton = {},
            dismissButton = {}
        )
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Add Category-wise Budget") },
            text = {
                AddCategoryBudgetForm(
                    onSave = { showCategoryDialog = false },
                    onCancel = { showCategoryDialog = false },
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(

                title = {
                    Text("FinMate", color = Color.White, fontWeight = FontWeight.SemiBold)
                },


                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .border(width = 0.5.dp, color = Color.White, shape = CircleShape)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable {
                                navController.navigate("profilepage")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = initial, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF2196F3)) {
                val items = listOf("Home", "Analytics", "Category", "Settings")
                val icons = listOf(
                    Icons.Default.Home, // Vector
                    R.drawable.baseline_auto_graph_24, // Drawable
                    R.drawable.baseline_category_24,
                    Icons.Default.Settings // Vector
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
                .padding(16.dp).verticalScroll(rememberScrollState())

                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFB8BABB))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = currentDate, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have used 90% of your budget.\nTry saving in Food.",
                        fontSize = 15.sp,
                        color = Color(0xFF4E342E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                GradientButton(
                    text = "Predict Budget",
                    onClick = { Toast.makeText(context, "Predict Budget Clicked", Toast.LENGTH_SHORT).show() },
                    gradientColors = listOf(Color(0xFF12648D), Color(0xFF12648D)),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            DashboardGrid(
                expensemain = expensemain,
                remaining = remaining,
                budetmain = budetmain,
                incomemain = incomemain
            )

            Spacer(modifier = Modifier.height(18.dp))
            GradientBox(
                text = "Top Spending Categories: Food, Groceries",
                gradientColors = listOf(Color(0xFF12648D), Color(0xFF12648D)),
                fontSize = 16.sp,
                fontColor = Color.White,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))
            Text(text = "Add New", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                val buttonColors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White)
                Button(onClick = { showDialog = true }, modifier = Modifier.weight(1f).height(45.dp), colors = buttonColors) {
                    Text("Expenses", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Button(onClick = { showMainDialog = true }, modifier = Modifier.weight(1f).height(45.dp), colors = buttonColors) {
                    Text("Budget", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Button(onClick = {
                    showIncomeDialog = true


                }, modifier = Modifier.weight(1f).height(45.dp), colors = buttonColors) {
                    Text("Income", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            AddIncomeToDashBoard(
                showDialog = showIncomeDialog,
                onDismiss = { showIncomeDialog = false },
                onSave = { incomemain = it },
            )



            Spacer(modifier = Modifier.height(18.dp))
            Text(text = "Recent Transactions", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp).clip(MaterialTheme.shapes.medium).background(Color(0xFFB8BABB)).padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Domino's Pizza", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(
                            0xFF133C60
                        )
                        )
                        Text("₹350 • Food & Dining", fontSize = 14.sp, color = Color.DarkGray)
                    }
                    Button(onClick = { Toast.makeText(context, "Show All Clicked", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12648D), contentColor = Color.White), modifier = Modifier.height(36.dp)) {
                        Text("Show All", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardGrid(
    expensemain: Int = 0,
    remaining: Int = 0,
    budetmain: Int = 0,
    incomemain: Int = 0,

    ) {


    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientDashboardCard("Expenses",
                expensemain.toString(), listOf(Color(0xFFB8BABB), Color(0xFFB8BABB)), Modifier.weight(1f).height(100.dp))
            GradientDashboardCard("Remaining", remaining.toString(), listOf(Color(0xFFB8BABB), Color(
                0xFFB8BABB
            )
            ), Modifier.weight(1f).height(100.dp))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientDashboardCard("Budget", budetmain.toString(), listOf(Color(0xFFB8BABB), Color(
                0xFFB8BABB
            )
            ), Modifier.weight(1f).height(100.dp))
            GradientDashboardCard("Income", incomemain.toString(), listOf(Color(0xFFB8BABB), Color(
                0xFFB8BABB
            )
            ), Modifier.weight(1f).height(100.dp))
        }
    }
}