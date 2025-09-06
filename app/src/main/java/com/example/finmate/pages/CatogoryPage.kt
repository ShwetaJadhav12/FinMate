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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.finmate.GlobNavigation.navController
import com.example.finmate.R
import com.example.finmate.SharedMonthViewModelnew
import com.example.finmate.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import java.time.format.DateTimeFormatter

fun fetchCategoriesFromFirestore(
    onSuccess: (List<Category>) -> Unit,
    onFailure: (Exception) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("categories")
        .get()
        .addOnSuccessListener { result ->
            val categoryList = result.map { it.toObject(Category::class.java) }
            onSuccess(categoryList)
        }
        .addOnFailureListener { onFailure(it) }
}

@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryGridScreen() {
    var selectedIndex by remember { mutableStateOf(2) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }

    val isDark = isSystemInDarkTheme()
    val sharedMonthViewModelnew: SharedMonthViewModelnew = viewModel()
    val selectedMonth by sharedMonthViewModelnew.selectedMonth.collectAsState()

    val monthId = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    // Adaptive card colors
    val cardGradientColors = if (isDark) {
        listOf(Color(0xFF2B2F3B), Color(0xFF3A4050)) // dark gray-blue gradient
    } else {
        listOf(Color(0xFFD0E4F5), Color(0xFFE6F0FA)) // light blue gradient
    }

    // Adaptive text color
    val textColor = if (isDark) Color(0xFFBBDEFB) else Color(0xFF0D47A1)

    LaunchedEffect(Unit) {
        fetchCategoriesFromFirestore(
            onSuccess = { categories = it },
            onFailure = { e -> Log.e("CategoryFetch", "Error: ${e.message}") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3198F1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            itemsIndexed(categories) { _, category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("categoryExpenses/${category.name}")
                        }
                        .aspectRatio(1f)
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = Brush.verticalGradient(cardGradientColors))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = category.imageurl,
                                contentDescription = category.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = category.name,
                                fontSize = 20.sp,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
