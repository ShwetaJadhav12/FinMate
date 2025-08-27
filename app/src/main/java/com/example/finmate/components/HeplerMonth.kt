package com.example.finmate.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.finmate.model.CategoryExpense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Safely read an "amount" that might be Number or String
private fun Any?.toIntAmount(): Int = when (this) {
    is Number -> this.toInt()
    is String -> this.toDoubleOrNull()?.toInt() ?: 0
    else -> 0
}

@Composable
fun rememberCategoryExpenses(): List<CategoryExpense> {
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
                        ?.groupBy { it.getString("category") ?: "Other" }
                        ?.map { (cat, docs) ->
                            CategoryExpense(
                                category= cat,
                                amount = docs.sumOf { it.get("amount").toIntAmount() },
                                color = Color(
                                    red = (0..255).random() / 255f,
                                    green = (0..255).random() / 255f,
                                    blue = (0..255).random() / 255f
                                ),
                                date = docs.first().getString("date") ?: ""
                            )
                        } ?: emptyList()
                    categories = categoryMap
                }
        }
    }

    return categories
}
