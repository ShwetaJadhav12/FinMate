package com.example.finmate.pages

import YearlyWrapDialog
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.finmate.R
import com.example.finmate.SharedMonthViewModelnew
import com.example.finmate.components.EditProfileDialog
import com.example.finmate.components.fetchUserData
import com.example.finmate.viewmodel.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.YearMonth

// -------------------
// Data Class
// -------------------
data class MonthlySummary(
    val income: Double = 0.0,
    val budget: Double = 0.0,
    val expenses: Double = 0.0,
    val remaining: Double = 0.0
)

// -------------------
// Firestore Fetch
// -------------------
suspend fun fetchMonthlySummary(month: String): MonthlySummary? {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
    val docRef = FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .collection("summary_data")
        .document(month)

    val snapshot = docRef.get().await()
    return if (snapshot.exists()) {
        val budget = snapshot.getDouble("budget") ?: 0.0
        val expenses = snapshot.getDouble("expenses") ?: 0.0
        MonthlySummary(
            income = snapshot.getDouble("income") ?: 0.0,
            budget = budget,
            expenses = expenses,
            remaining = budget - expenses
        )
    } else null
}

// -------------------
// Summary Dialog
// -------------------
@Composable
fun SummaryCardDialog(
    month: String,
    summary: MonthlySummary,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Summary - $month") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Income: ₹${summary.income}", fontSize = 16.sp)
                Text("Budget: ₹${summary.budget}", fontSize = 16.sp)
                Text("Expenses: ₹${summary.expenses}", fontSize = 16.sp)
                Text(
                    "Remaining: ₹${summary.remaining}",
                    fontSize = 16.sp,
                    color = if (summary.remaining >= 0) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// -------------------
// Profile Page
// -------------------
@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    dashboardViewModel: DashboardViewModel,   // ✅ ADD THIS

    navController: NavController,
    sharedMonthViewModel: SharedMonthViewModelnew, // 🔹 take selected month from HomeScreen
    selectedIndex: Int = 3,
    onTabSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(3) }

    var showeditpage by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("Loading...") }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val buttonColor = if (isDarkTheme) Color(0xFF12648D) else Color(0xFF2196F3)

    // Summary states
    var showSummaryDialog by remember { mutableStateOf(false) }
    var summaryData by remember { mutableStateOf<MonthlySummary?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // 🔹 Get selected month from SharedMonthViewModel
    val selectedMonth by sharedMonthViewModel.selectedMonth.collectAsState()

    LaunchedEffect(Unit) {
        fetchUserData(
            onSuccess = {
                userName = it.name
                userEmail = it.email
            },
            onFailure = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = onPrimaryColor
                )
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture with gradient
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                    fontSize = 40.sp,
                    color = onPrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
            Text(text = userEmail, fontSize = 16.sp, color = onSurfaceColor.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(32.dp))

            if (showeditpage) {
                EditProfileDialog(
                    onDismiss = { showeditpage = false },
                    onUserUpdated = {
                        userName = it.name
                        userEmail = it.email
                    }
                )
            }

            Button(
                onClick = { showeditpage = true },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text("Edit Profile", color = Color.White)
            }

            var showYearlyWrap by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    showYearlyWrap = true
                }
            ) {
                Text("Show Yearly Card")
            }

            if (showYearlyWrap) {
                YearlyWrapDialog(
                    viewModel = dashboardViewModel,
                    year = selectedMonth.year,
                    onDismiss = { showYearlyWrap = false }
                )
            }





            Spacer(modifier = Modifier.height(16.dp))
//            Button(
//                onClick = {
//                    coroutineScope.launch {
//                        val result = fetchMonthlySummary(selectedMonth.toString())
//                        if (result != null) {
//                            summaryData = result
//                            showSummaryDialog = true
//                        } else {
//                            Toast.makeText(context, "No summary data found", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                },
//                modifier = Modifier.fillMaxWidth(0.8f),
//                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
//            ) {
//                Text("Summary Card", color = Color.White)
//            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Logout", color = Color.White)
            }
        }
    }

    // Show dialog when summary is ready
    if (showSummaryDialog && summaryData != null) {
        SummaryCardDialog(
            month = selectedMonth.toString(),
            summary = summaryData!!,
            onDismiss = { showSummaryDialog = false }
        )
    }
}