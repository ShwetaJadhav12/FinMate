package com.example.finmate.components

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.*

@SuppressLint("NewApi", "RememberReturnType")
@Composable
fun AddMonthlyBudgetForm(
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val context = LocalContext.current

    val datePickerDialog = remember {
        val today = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
    }

    val endDate = selectedDate?.plusDays(30)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(20.dp).fillMaxWidth()
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = {
                if (it.matches(Regex("^\\d{0,6}(\\.\\d{0,2})?$"))) amount = it
            },
            label = { Text("Total Budget (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { datePickerDialog.show() }.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedDate?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Start Date") },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick date")
                    }
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = endDate?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("End Date") },
                enabled = false,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {

                }
            )
        {
                Text("Save") }

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

    val today = remember { LocalDate.now() }
    val startDate = remember { today }
    val endDate = remember { today.plusDays(30) }

    val displayMonth = remember {
        "${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.year}"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
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
                modifier = Modifier.fillMaxWidth().menuAnchor()
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

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = displayMonth,
            onValueChange = {},
            readOnly = true,
            label = { Text("Month") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("From ${startDate} to ${endDate}", style = MaterialTheme.typography.bodyMedium)

        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {

            }) {
                Text("Save")
            }
        }
    }
}