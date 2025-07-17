package com.example.finmate.pages

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

// Composable Grid View
@Composable
fun CategoryGridScreen() {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }

    // Load once on composition
    LaunchedEffect(Unit) {
        fetchCategoriesFromFirestore(
            onSuccess = { categories = it },
            onFailure = { e -> Log.e("CategoryFetch", "Error: ${e.message}") }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Use index if no unique key (like 'id')
        itemsIndexed(categories) { _, category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = category.imageurl,  // ✅ Corrected from mageurl
                        contentDescription = category.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = category.name,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
