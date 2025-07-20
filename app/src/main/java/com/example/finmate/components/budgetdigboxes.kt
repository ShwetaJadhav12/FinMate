package com.example.finmate.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.TextStyle
import java.util.Locale

@SuppressLint("NewApi")
@Composable
fun AddMonthlyBudgetForm(onSave: () -> Unit, onCancel: () -> Unit) {
    var amount by remember { mutableStateOf("") }

    // Get current month and year
    val currentMonth = remember {
        val now = LocalDate.now()
        val month = now.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) // "July"
        "$month ${now.year}" // "July 2025"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        // Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Total Monthly Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        // Month (Auto-filled & Read-only)
        OutlinedTextField(
            value = currentMonth,
            onValueChange = {},
            readOnly = true,
            label = { Text("Month") },
            modifier = Modifier.fillMaxWidth()
        )

        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onSave) { Text("Save") }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("NewApi")
@Composable
fun AddCategoryBudgetForm(
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    var category by remember { mutableStateOf("Food") }
    var amount by remember { mutableStateOf("") }
    val categories = listOf("Food", "Travel", "Shopping", "Bills", "Health")
    var expanded by remember { mutableStateOf(false) }

    // Get current month & year
    val currentMonth = remember {
        val current = now()
        "${current.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${current.year}"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            category = item
                            expanded = false
                        }
                    )
                }
            }
        }

        // Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        // Show Month (Read-only)
        OutlinedTextField(
            value = currentMonth,
            onValueChange = {},
            readOnly = true,
            label = { Text("Month") },
            modifier = Modifier.fillMaxWidth()
        )

        // Buttons
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) { Text("Save") }
        }
    }
}
