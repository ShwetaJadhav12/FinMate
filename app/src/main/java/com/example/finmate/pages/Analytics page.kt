package com.example.finmate.pages

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finmate.R
import com.example.finmate.components.captureViewAsBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.YearMonth

// ----------------------------- Data Models -----------------------------
data class CategoryExpense(
    val category: String,
    val amount: Int,
    val color: Color,
    val date: String
)

data class MonthData(
    val expenses: List<CategoryExpense>,
    val budget: Float,
    val remaining: Float
)

// ----------------------------- Firestore Listener -----------------------------
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun rememberMonthlyData(
    categoryColors: Map<String, Color>,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
): Map<YearMonth, MonthData> {
    var monthlyData by remember {
        mutableStateOf<Map<YearMonth, MonthData>>(emptyMap())
    }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            firestore.collection("users")
                .document(uid)
                .collection("summary_data")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    val tempData = mutableMapOf<YearMonth, MonthData>()

                    snapshot?.documents?.forEach { doc ->
                        val monthId = doc.id // e.g. "2025-09"
                        try {
                            val parts = monthId.split("-")
                            if (parts.size == 2) {
                                val year = parts[0].toInt()
                                val month = parts[1].toInt()
                                val ym = YearMonth.of(year, month)

                                val budget = doc.get("budget").toIntAmount().toFloat()
                                val remaining = doc.get("remaining").toIntAmount().toFloat()

                                firestore.collection("users")
                                    .document(uid)
                                    .collection("summary_data")
                                    .document(monthId)
                                    .collection("expenses")
                                    .addSnapshotListener { expenseSnap, _ ->
                                        val expenses = expenseSnap?.documents?.map { expDoc ->
                                            val amount = expDoc.get("amount").toIntAmount()
                                            val category = expDoc.getString("category") ?: "Others"
                                            CategoryExpense(
                                                category,
                                                amount,
                                                categoryColors[category] ?: Color.Gray,
                                                ""
                                            )
                                        } ?: emptyList()

                                        tempData[ym] = MonthData(expenses, budget, remaining)
                                        monthlyData = tempData.toSortedMap(compareByDescending { it })
                                    }
                            }
                        } catch (ex: Exception) {
                            Log.e("MonthlyData", "Failed to parse monthId: $monthId", ex)
                        }
                    }
                }
        }
    }
    return monthlyData
}

// ----------------------------- Pie Chart -----------------------------
@Composable
fun PieChart(data: List<CategoryExpense>, modifier: Modifier = Modifier) {
    val total = data.sumOf { it.amount }.toFloat()
    var startAngle = -90f
    Canvas(modifier = modifier) {
        data.forEach { item ->
            val sweep = if (total == 0f) 0f else (item.amount / total) * 360f
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(size.width, size.height)
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
                Text("${item.category}: ${"%.1f".format(percent)}%", fontSize = 14.sp)
            }
        }
    }
}

// ----------------------------- Analytics Screen -----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnalyticsPage(
    navController: NavHostController
) {
    var selectedIndex by remember { mutableStateOf(1) }

    val isDark = isSystemInDarkTheme()

    // Define adaptive colors
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFF5F5F5)
    val cardBackgroundColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE3F2FD)
    val textPrimaryColor = if (isDark) Color(0xFFBBDEFB) else Color(0xFF1565C0)
    val textSecondaryColor = if (isDark) Color(0xFFEEEEEE) else Color.Black
    val errorColor = if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F)

    val buttonBg = if (isDark) Color(0xFF1E88E5) else Color(0xFF2196F3)

    val dropdownBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val dropdownText = if (isDark) Color.White else Color.Black
    val dropdownBorder = if (isDark) Color(0xFF424242) else Color(0xFFDDDDDD)

    val categoryColors = mapOf(
        "Food" to Color(0xFF4CAF50),
        "Shopping" to Color(0xFFFF9800),
        "Travel" to Color(0xFF2196F3),
        "Health" to Color(0xFFE91E63),
        "Education" to Color(0xFF9C27B0),
        "Others" to Color(0xFF9E9E9E)
    )

    val monthlyData = rememberMonthlyData(categoryColors)
    val textColor = if (isDark) Color.White else Color(0xFF0D47A1)
    var selectedOption by remember { mutableStateOf("Monthly Analysis") }
    val options = listOf("Monthly Analysis", "Category-wise Distribution")

    var expanded by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf(monthlyData.keys.firstOrNull()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("FinMate", color = Color.White, fontWeight = FontWeight.SemiBold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3)
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
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dropdown for analysis type
            Box {
                // 🔘 Button with Arrow
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF1E88E5) else Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedOption,
                            color = Color.White,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.White
                        )
                    }
                }

                // 📂 Dropdown Menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(
                            if (isDark) Color(0xFF1E1E1E) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    color = if (isDark) Color.White else Color.Black,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedOption = option
                                expanded = false
                            }
                        )
                    }
                }
            }



            Spacer(modifier = Modifier.height(16.dp))

            when (selectedOption) {
                "Monthly Analysis" -> {
                    if (monthlyData.isEmpty()) {
                        Text("No monthly data available", color = textPrimaryColor)
                    } else {
                        var showDialog by remember { mutableStateOf(false) }
                        var selectedMonthData by remember { mutableStateOf<Pair<YearMonth, MonthData>?>(null) }

                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            monthlyData.forEach { (month, data) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedMonthData = month to data
                                            showDialog = true
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {

                                        // 🔹 Header: Month
                                        Text(
                                            text = month.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimaryColor
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // 🔹 Budget + Remaining (side by side)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Budget",
                                                    fontSize = 13.sp,
                                                    color = textSecondaryColor
                                                )
                                                Text(
                                                    text = "₹${data.budget.toInt()}",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textPrimaryColor
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Remaining",
                                                    fontSize = 13.sp,
                                                    color = textSecondaryColor
                                                )
                                                Text(
                                                    text = "₹${data.budget - data.expenses.sumOf { it.amount }}",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textPrimaryColor
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Divider(color = Color.LightGray.copy(alpha = 0.4f))

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // 🔹 Highlight: Total Expenses
                                        val totalExpenses = data.expenses.sumOf { it.amount }

                                        Text(
                                            text = "Total Expenses",
                                            fontSize = 13.sp,
                                            color = textSecondaryColor
                                        )

                                        Text(
                                            text = "₹$totalExpenses",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = errorColor
                                        )
                                    }
                                }

                            }
                        }

                        if (showDialog && selectedMonthData != null) {
                            val (month, data) = selectedMonthData!!
                            val context = LocalContext.current
                            val view = LocalView.current

                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                confirmButton = {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("Close")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        val bitmap = captureViewAsBitmap(view)
                                        val filename = "Summary_${month}.png"

                                        val resolver = context.contentResolver
                                        val contentValues = android.content.ContentValues().apply {
                                            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, filename)
                                            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                                            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FinMate")
                                        }

                                        val imageUri = resolver.insert(
                                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            contentValues
                                        )

                                        imageUri?.let { uri ->
                                            resolver.openOutputStream(uri)?.use { stream ->
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                            }

                                            Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
                                        } ?: run {
                                            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                                        }

                                        showDialog = false

                                    }) {
                                        Text("Download")
                                    }
                                },
                                title = { Text("Summary - $month", color = textPrimaryColor) },
                                text = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Budget: ₹${data.budget.toInt()}", fontSize = 16.sp, color = textSecondaryColor)
                                        Text(
                                            "Remaining: ₹${data.budget - data.expenses.sumOf { it.amount }}",
                                            fontSize = 16.sp,
                                            color = textSecondaryColor
                                        )
                                        val totalExpenses = data.expenses.sumOf { it.amount }
                                        Text("Total Expenses: ₹$totalExpenses", fontSize = 16.sp, color = errorColor)

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Category Breakdown:", fontWeight = FontWeight.SemiBold, color = textSecondaryColor)

                                        data.expenses.groupBy { it.category }.forEach { (category, items) ->
                                            val sum = items.sumOf { it.amount }
                                            Text("- $category: ₹$sum", color = textSecondaryColor)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }


                "Category-wise Distribution" -> {
                    if (monthlyData.isEmpty()) {
                        Text("No expenses available", color = textPrimaryColor)
                    } else {
                        var monthExpanded by remember { mutableStateOf(false) }
                        Box {
                            // 🔘 ORIGINAL BUTTON (with arrow)
                            Button(
                                onClick = { monthExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = buttonBg),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedMonth?.toString() ?: "Select Month",
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = Color.White
                                    )
                                }
                            }

                            // 📂 DROPDOWN MENU (VISIBLE + THEMED)
                            DropdownMenu(
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false },
                                modifier = Modifier
                                    .background(dropdownBg, RoundedCornerShape(12.dp))
                                    .width(IntrinsicSize.Min)
                            ) {
                                monthlyData.keys.forEach { month ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = month.toString(),
                                                color = textColor,
                                                fontSize = 14.sp
                                            )
                                        },

                                        onClick = {
                                            selectedMonth = month
                                            monthExpanded = false
                                        }
                                    )
                                }
                            }
                        }


                        Spacer(modifier = Modifier.height(16.dp))

                        selectedMonth?.let { month ->
                            val expenses = monthlyData[month]?.expenses ?: emptyList()
                            if (expenses.isEmpty()) {
                                Text("No expenses in $month", color = textSecondaryColor)
                            } else {
                                val grouped = expenses.groupBy { it.category }.map { (category, items) ->
                                    CategoryExpense(
                                        category = category,
                                        amount = items.sumOf { it.amount },
                                        color = items.first().color,
                                        date = ""
                                    )
                                }

                                PieChart(
                                    grouped,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                PieChartLegend(grouped)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------- Helper -----------------------------
fun Any?.toIntAmount(): Int {
    return when (this) {
        is Number -> this.toInt()
        is String -> this.toIntOrNull() ?: 0
        else -> 0
    }
}

@Composable
fun CategoryExpenseDialog(
    category: String,
    total: Int,
    details: List<CategoryExpense>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("$category - ₹$total") },
        text = {
            Column {
                details.forEach {
                    Text("- ${it.date}: ₹${it.amount}")
                }
            }
        }
    )
}
