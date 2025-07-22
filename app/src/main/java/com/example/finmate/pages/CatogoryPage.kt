package com.example.finmate.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.finmate.GlobNavigation.navController
import com.example.finmate.model.Category
import com.google.firebase.firestore.FirebaseFirestore

// Fetch categories from Firestore
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
        .addOnFailureListener {
            onFailure(it)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryGridScreen() {
    var selectedIndex by remember { mutableStateOf(2) } // Default to Category page

    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }

    // Fetch categories once
    LaunchedEffect(Unit) {
        fetchCategoriesFromFirestore(
            onSuccess = { categories = it },
            onFailure = { e -> Log.e("CategoryFetch", "Error: ${e.message}") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleLarge)
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF80B2AE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                                0 -> navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                                1 -> navController.navigate("analytics")
                                2 -> {} // Already on Category page
                                3 -> navController.navigate("profilepage")
                            }
                        },
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 12.sp) },
                        alwaysShowLabel = true
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
                        .fillMaxWidth().clickable {
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
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF80B2AE),
                                        Color(0xFF577C77)
                                    )
                                )
                            )
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
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = category.name,
                                fontSize = 20.sp,
                                color = Color.White,
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
