package com.example.finmate.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finmate.GlobNavigation.navController

@Composable
fun HomeScreenHelper(
    innerPadding: androidx.compose.ui.unit.Dp,
    context: android.content.Context,
    currentDate: String,
    showDialog: Boolean,
){
Column(
modifier = Modifier
.padding(innerPadding)
.padding(16.dp)
.fillMaxSize()
) {
    // Top Card

    // Show the dialog conditionally
    if (showDialog) {
        ExpenseEntryOptionsDialog(
            onDismiss = { !showDialog },
            onAddManually = {
                !showDialog
                // Navigate to manual form screen
                navController.navigate("addExpense")
            },
            onScanReceipt = {
                !showDialog
                // Navigate to scan camera screen
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8F5E9))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentDate,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You have used 90% of your budget.\nTry saving in Food.",
                fontSize = 15.sp,
                color = Color(0xFF4D1605),
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Buttons
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        GradientButton(
            text = "Predict Budget",
            onClick = { Toast.makeText(context, "Predict Budget Clicked", Toast.LENGTH_SHORT).show() },
            gradientColors = listOf(Color(0xFF1A481D), Color(0xFF478049)),
            modifier = Modifier.weight(1f)
        )

    }

    Spacer(modifier = Modifier.height(16.dp))
    DashboardGrid()

    Spacer(modifier = Modifier.height(18.dp))

    // Top Categories
    GradientBox(
        text = "Top Spending Categories: Food, Groceries",
        gradientColors = listOf(Color(0xFF641E08), Color(0xFFC97960)),
        fontSize = 16.sp,
        fontColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    )

    Spacer(modifier = Modifier.height(18.dp))
    Text(
        text = "Add New",
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = {
                showDialog
            },
            modifier = Modifier
                .weight(1f)
                .height(45.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF93B694),
                contentColor = Color(0xFF053607)
            )
        ) {
            Text(
                text = "Expenses",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Button(
            onClick = {
                Toast.makeText(context, "Set Budget Clicked", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .weight(1f)
                .height(45.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF93B694),
                contentColor = Color(0xFF053607)
            )
        ) {
            Text(
                text = "Budget",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Button(
            onClick = {
                Toast.makeText(context, "Add Income Clicked", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .weight(1f)
                .height(45.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF93B694),
                contentColor = Color(0xFF053607)
            )
        ) {
            Text(
                text = "Income",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
    Spacer(modifier = Modifier.height(18.dp))
    Text(
        text = "Recent Transactions",
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFFE8F5E9))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Left side - transaction details
            Column {
                Text(
                    text = "Domino's Pizza",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "₹350 • Food & Dining",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            // Right side - show all button
            Button(
                onClick = {
                    Toast.makeText(context, "Show All Clicked", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Show All", fontSize = 13.sp)
            }
        }
    }



}
}
@Composable
fun DashboardGrid() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientDashboardCard(
                title = "Expenses",
                t1 = "₹10,000",
                gradientColors = listOf(Color(0xFFD21E60), Color(0xFFD0839F)),
                modifier = Modifier.weight(1f).height(100.dp)
            )
            GradientDashboardCard(
                title = "Remaining",
                t1 = "₹20,000",
                gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6)),
                modifier = Modifier.weight(1f).height(100.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientDashboardCard(
                title = "Budget",
                t1 = "₹80,000",
                gradientColors = listOf(Color(0xFF8E24AA), Color(0xFFCE93D8)),
                modifier = Modifier.weight(1f).height(100.dp)
            )
            GradientDashboardCard(
                title = "Income",
                t1 = "₹1,00,000",
                gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFFCC80)),
                modifier = Modifier.weight(1f).height(100.dp)
            )
        }
    }
}
