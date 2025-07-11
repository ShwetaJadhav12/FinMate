package com.example.finmate.pages

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.finmate.model.DrawerItems
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var userName by remember { mutableStateOf("User") }
    val currentDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
    }

    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: "User"
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load user", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val drawerItems = listOf(
        DrawerItems("Home", "home"),
        DrawerItems("Analytics", "analytics"),
        DrawerItems("Budget", "budget"),
        DrawerItems("Logout", "logout")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F1F1))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item.route == "logout") {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "${item.title} clicked", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .fillMaxWidth()
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "FinMate",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 23.sp
                        )
                    },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(start = 12.dp, end = 12.dp)
                                .clickable {
                                    scope.launch { drawerState.open() }
                                }
                        )
                    },
                    actions = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable {
                                    Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF4CAF50),
                        titleContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color(0xFFE8F5E9)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentDate,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(18.dp)
                        )
                        Text(
                            text = "You have used 90% of your budget. Try saving in Food",
                            color = Color(0xFF4D1605),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GradientButton(
                        text = "Predict Budget",
                        onClick = {
                            Toast.makeText(context, "Predict Budget Clicked", Toast.LENGTH_SHORT).show()
                        },
                        gradientColors = listOf(Color(0xFF1A481D), Color(0xFF478049)),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                DashboardGrid()

                Spacer(modifier = Modifier.height(10.dp))
                GradientBox(
                    text = "Top Spending Categories: Food, Groceries",
                    gradientColors = listOf(Color(0xFF641E08), Color(0xFFC97960)),
                    fontSize = 18.sp,
                    fontColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(horizontal = 6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                GradientBox(
                    text = "Add Expenses",
                    gradientColors = listOf(Color(0xFF0D47A1), Color(0xFF42A5F5)),
                    fontSize = 18.sp,
                    fontColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(horizontal = 6.dp)
                        .clickable {
                            // Navigate to Add Expense screen
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GradientButton(
                        text = "Set Budget",
                        onClick = {
                            Toast.makeText(context, "Set Budget Clicked", Toast.LENGTH_SHORT).show()
                        },
                        gradientColors = listOf(Color(0xFF1A481D), Color(0xFF478049)),
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )

                    GradientButton(
                        text = "Add Income",
                        onClick = {
                            Toast.makeText(context, "Add Income Clicked", Toast.LENGTH_SHORT).show()
                        },
                        gradientColors = listOf(Color(0xFF1A481D), Color(0xFF478049)),
                        modifier = Modifier.weight(1f)
                    )


                }
            }
        }
    }
}

@Composable
fun DashboardGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp), // Reduced horizontal padding
        verticalArrangement = Arrangement.spacedBy(8.dp) // Less space between rows
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Less space between columns
        ) {
            GradientDashboardCard(
                title = "Expenses",
                t1 = "10000",
                gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp) // Slightly shorter height
            )
            GradientDashboardCard(
                title = "Remaining",
                t1 = "20000",
                gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6)),
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GradientDashboardCard(
                title = "Budget",
                t1 = "80000",
                gradientColors = listOf(Color(0xFF8E24AA), Color(0xFFCE93D8)),
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
            )
            GradientDashboardCard(
                title = "Income",
                t1 = "100000",
                gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFFCC80)),
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
            )
        }
    }
}
