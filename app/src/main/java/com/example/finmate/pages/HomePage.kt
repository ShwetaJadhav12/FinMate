package com.example.finmate.pages

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finmate.components.GradientBox
import com.example.finmate.components.GradientButton
import com.example.finmate.components.GradientDashboardCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
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

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            Firebase.firestore.collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: "User"
                }
        }
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable {
                                Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50)
                )
            )
        },

        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF7F7F7)) {
                val items = listOf("Home", "Analytics", "Settings")
                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.Add,
                    Icons.Default.Favorite,
                    Icons.Default.Settings
                )
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 12.sp) },
                        alwaysShowLabel = true
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
        ) {
            // Top Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentDate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have used 90% of your budget.\nTry saving in Food.",
                        fontSize = 15.sp,
                        color = Color(0xFF4D1605),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GradientButton(
                    text = "Predict Budget",
                    onClick = { Toast.makeText(context, "Predict Budget Clicked", Toast.LENGTH_SHORT).show() },
                    gradientColors = listOf(Color(0xFF1A481D), Color(0xFF478049)),
                    modifier = Modifier.weight(1f)
                )

            }

            Spacer(modifier = Modifier.height(16.dp))
            DashboardGrid()

            Spacer(modifier = Modifier.height(18.dp))

            // Top Categories
            GradientBox(
                text = "Top Spending Categories: Food, Groceries",
                gradientColors = listOf(Color(0xFF641E08), Color(0xFFC97960)),
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
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Add Expenses Clicked", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF93B694),
                        contentColor = Color(0xFF053607)
                    )
                ) {
                    Text(
                        text = "Expenses",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Set Budget Clicked", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF93B694),
                        contentColor = Color(0xFF053607)
                    )
                ) {
                    Text(
                        text = "Budget",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Add Income Clicked", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF93B694),
                        contentColor = Color(0xFF053607)
                    )
                ) {
                    Text(
                        text = "Income",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Recent Transactions",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color(0xFFE8F5E9))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Left side - transaction details
                    Column {
                        Text(
                            text = "Domino's Pizza",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "₹350 • Food & Dining",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }

                    // Right side - show all button
                    Button(
                        onClick = {
                            Toast.makeText(context, "Show All Clicked", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(text = "Show All", fontSize = 13.sp)
                    }
                }
            }



        }
    }
}

@Composable
fun DashboardGrid() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientDashboardCard(
                title = "Expenses",
                t1 = "₹10,000",
                gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
                modifier = Modifier.weight(1f).height(100.dp)
            )
            GradientDashboardCard(
                title = "Remaining",
                t1 = "₹20,000",
                gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6)),
                modifier = Modifier.weight(1f).height(100.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientDashboardCard(
                title = "Budget",
                t1 = "₹80,000",
                gradientColors = listOf(Color(0xFF8E24AA), Color(0xFFCE93D8)),
                modifier = Modifier.weight(1f).height(100.dp)
            )
            GradientDashboardCard(
                title = "Income",
                t1 = "₹1,00,000",
                gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFFCC80)),
                modifier = Modifier.weight(1f).height(100.dp)
            )
        }
    }
}
