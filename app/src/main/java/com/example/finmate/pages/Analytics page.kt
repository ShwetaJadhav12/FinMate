package com.example.finmate.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finmate.GlobNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsPage(
    selectedIndex: Int = 1,
    onTabSelected: (Int) -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf(1) } // Default to Category page

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
            NavigationBar(containerColor = Color(0xFFF7F7F7)) {
                val items = listOf("Home", "Analytics", "Category", "Settings")
                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.Add,
                    Icons.Default.Favorite,
                    Icons.Default.Settings
                )
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            when (index) {
                                0 -> GlobNavigation.navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                                1 -> {}
                                2 -> {GlobNavigation.navController.navigate("categorypage")} // Already on Category page
                                3 -> GlobNavigation.navController.navigate("profilepage")
                            }
                        },
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 12.sp) },
                        alwaysShowLabel = true
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Spending Overview",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
//                    Toast.makeText(context,"cliked on weekly bar chart")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF11446C),
                    contentColor = Color.White
                )
            ) {
                Text("Weekly Bar chart")
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
//                    Toast.makeText(context,"cliked on weekly bar chart")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF11446C),
                    contentColor = Color.White
                )
            ) {
                Text("Monthly Analysis")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
//                    Toast.makeText(context,"cliked on weekly bar chart")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF11446C),
                    contentColor = Color.White
                )
            ) {
                Text("Category wise pie chart analysis")
            }
        }
    }
}
