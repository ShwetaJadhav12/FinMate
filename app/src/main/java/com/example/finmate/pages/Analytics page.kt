package com.example.finmate.pages

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finmate.R
import com.example.finmate.model.CategoryExpense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------------------------
// Helper: Safe amount conversion
// ---------------------------
private fun Any?.toIntAmount(): Int = when (this) {
    is Number -> this.toInt()
    is String -> this.toDoubleOrNull()?.toInt() ?: 0
    else -> 0
}

// ---------------------------
// Firebase: Category Expenses
// ---------------------------
@Composable
fun rememberCategoryExpenses(categoryColors: Map<String, Color>): List<CategoryExpense> {
    var categories by remember { mutableStateOf(listOf<CategoryExpense>()) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("expenses")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener

                    val categoryMap = snapshot?.documents
                        ?.groupBy { it.getString("category") ?: "Others" }
                        ?.map { (cat, docs) ->
                            CategoryExpense(
                                category = cat,
                                amount = docs.sumOf { it.get("amount").toIntAmount() },
                                color = categoryColors[cat] ?: Color.Gray,
                                date = docs.first().getString("date") ?: ""
                            )
                        } ?: emptyList()
                    categories = categoryMap
                }
        }
    }
    return categories
}

// ---------------------------
// Firebase: Monthly Expenses
// ---------------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun rememberMonthlyExpenses(): Map<String, Float> {
    var monthlyTotals by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("expenses")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener

                    val monthMap = mutableMapOf<String, Float>()
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

                    snapshot?.documents?.forEach { doc ->
                        val amount = doc.get("amount").toIntAmount().toFloat()
                        val dateStr = doc.getString("date") ?: return@forEach
                        try {
                            val parsedDate = LocalDate.parse(dateStr, formatter)
                            val monthYear = parsedDate.month.name.lowercase()
                                .replaceFirstChar { it.titlecase(Locale.getDefault()) } +
                                    " " + parsedDate.year

                            monthMap[monthYear] =
                                monthMap.getOrDefault(monthYear, 0f) + amount
                        } catch (_: Exception) {}
                    }

                    monthlyTotals = monthMap.toSortedMap(compareByDescending { it }) // latest first
                }
        }
    }

    return monthlyTotals
}

// ---------------------------
// Gradient Button
// ---------------------------
@Composable
fun GradientButton(text: String, gradientColors: List<Color>, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ---------------------------
// Pie Chart
// ---------------------------
@Composable
fun PieChart(
    data: List<CategoryExpense>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.amount }.toFloat()
    var startAngle = -90f

    Canvas(modifier = modifier) {
        data.forEach { item ->
            val sweep = if (total == 0f) 0f else (item.amount / total) * 360f
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }
    }
}

@Composable
fun PieChartLegend(data: List<CategoryExpense>) {
    val total = data.sumOf { it.amount }.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        data.forEach { item ->
            val percent = if (total == 0f) 0f else (item.amount / total) * 100
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(item.color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "${item.category}: ${"%.1f".format(percent)}%",
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ---------------------------
// Month Card
// ---------------------------
@Composable
fun MonthCard(month: String, total: Float) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = month,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "₹${total.toInt()}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// ---------------------------
// Month Selector Buttons
// ---------------------------
@Composable
fun MonthSelector(
    monthlyExpenses: Map<String, Float>,
    selectedMonth: String?,
    onMonthSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        monthlyExpenses.keys.forEach { month ->
            Button(
                onClick = { onMonthSelected(month) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMonth == month) Color(0xFF2196F3) else Color.Gray
                )
            ) {
                Text(month, color = Color.White)
            }
        }
    }
}

// ---------------------------
// Full AnalyticsPage
// ---------------------------
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsPage(
    selectedIndex: Int = 1,
    onTabSelected: (Int) -> Unit = {},
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val monthlyExpenses = rememberMonthlyExpenses()
    var selectedMonth by remember { mutableStateOf<String?>(monthlyExpenses.keys.firstOrNull()) }

    var selectedTabIndex by remember { mutableStateOf(selectedIndex) }
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Monthly Analysis") }

    val gradientColors = if (isSystemInDarkTheme()) {
        listOf(Color(0xFF3F6FB8), Color(0xFF5A8FD0))
    } else {
        listOf(Color(0xFF2E5D9F), Color(0xFF4A7CC3))
    }

    val options = listOf(
        "Monthly Analysis",
        "Category wise pie chart analysis"
    )

    val categoryColors = mapOf(
        "Food" to Color(0xFFFF7043),
        "Groceries" to Color(0xFF4CAF50),
        "Travel" to Color(0xFF2196F3),
        "Shopping" to Color(0xFFFFC107),
        "Health" to Color(0xFFE91E63),
        "Education" to Color(0xFF9C27B0),
        "Others" to Color(0xFF607D8B)
    )

    val categoryExpenses = rememberCategoryExpenses(categoryColors)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
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
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
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
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Spending Overview",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton("$selectedOption ▼", gradientColors) {
                expanded = !expanded
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Month Selector
            MonthSelector(monthlyExpenses, selectedMonth) { month ->
                selectedMonth = month
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedOption) {
                "Category wise pie chart analysis" -> {
                    val filteredExpenses = if (selectedMonth != null) {
                        categoryExpenses.filter { exp ->
                            val dateStr = exp.date
                            try {
                                val parsedDate = LocalDate.parse(
                                    dateStr,
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                                )
                                val monthYear = parsedDate.month.name.lowercase()
                                    .replaceFirstChar { it.titlecase(Locale.getDefault()) } +
                                        " " + parsedDate.year
                                monthYear == selectedMonth
                            } catch (_: Exception) { false }
                        }
                    } else categoryExpenses

                    PieChart(
                        data = filteredExpenses,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PieChartLegend(filteredExpenses)
                }

                "Monthly Analysis" -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        monthlyExpenses.forEach { (month, total) ->
                            if (total > 0f) {
                                MonthCard(month = month, total = total)
                            }
                        }
                    }
                }
            }
        }
    }
}
