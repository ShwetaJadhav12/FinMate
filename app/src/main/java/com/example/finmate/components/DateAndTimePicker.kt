package com.example.finmate.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun DateAndTimePicker(
    date: String,
    time: String,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Date Picker
        OutlinedButton(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        onDateChange("$day/${month + 1}/$year")
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
            Spacer(Modifier.width(8.dp))
            Text(if (date.isNotEmpty()) "Date: $date" else "Pick a Date")
        }

        Spacer(Modifier.height(16.dp))

        // Time Picker
        OutlinedButton(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onTimeChange(String.format("%02d:%02d", hour, minute))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.DateRange, contentDescription = "Pick Time")
            Spacer(Modifier.width(8.dp))
            Text(if (time.isNotEmpty()) "Time: $time" else "Pick a Time")
        }
    }
}
