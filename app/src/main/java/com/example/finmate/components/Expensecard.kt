package com.example.finmate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finmate.model.Expenses

@Composable
fun ExpenseCard(
    expense: Expenses
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = expense.title ?: "No Title",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${expense.amount}",
                    fontSize = 16.sp,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Date: ${expense.date ?: "N/A"}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Time: ${expense.time ?: "N/A"}",
                    fontSize = 14.sp
                )

            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = { /* Handle delete action */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
            IconButton(
                onClick = { /* Handle delete action */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Delete",
                    tint = Color.DarkGray
                )
            }


        }
    }
}
