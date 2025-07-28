package com.example.finmate.components


import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import com.example.finmate.GlobNavigation
import com.example.finmate.R

@Composable
fun BottomNavBarMaterial3() {
    val items = listOf("Home", "Analytics", "Category", "Settings")
    val icons = listOf(
        Icons.Default.Home, // Vector
        R.drawable.baseline_auto_graph_24, // Drawable
        R.drawable.baseline_category_24,
        Icons.Default.Settings // Vector
    )

    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar(
        containerColor = Color(0xFF2196F3),
        contentColor = Color(0xFFFCFDFD)
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItem == index,
                onClick = { selectedItem = index

                        when (index) {
                            0 -> GlobNavigation.navController.navigate("home") {
                                popUpTo("home") { inclusive = true }}
                            1 -> GlobNavigation.navController.navigate("analytics")
                            2 -> {
                                GlobNavigation.navController.navigate("categorypage")} // Already on Category page
                            3 -> {}
                        }
                    },
                icon = {
                    if (icons[index] is ImageVector) {
                        Icon(
                            imageVector = icons[index] as ImageVector,
                            contentDescription = item,
                            tint = if (selectedItem == index) Color(0xFF0F395B) else Color.White
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = icons[index] as Int),
                            contentDescription = item,
                            tint = if (selectedItem == index) Color(0xFF0F395B) else Color.White
                        )
                    }
                },
                label = {
                    Text(
                        text = item,
                        color = if (selectedItem == index) Color(0xFF0F395B) else Color.White
                    )
                }
            )
        }
    }
}
