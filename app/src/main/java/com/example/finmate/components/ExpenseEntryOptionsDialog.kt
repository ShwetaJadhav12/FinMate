package com.example.finmate.components

import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.airbnb.lottie.compose.*
import com.example.finmate.GlobNavigation.navController
import com.example.finmate.R

@Composable
fun ExpenseEntryOptionsDialog(
    onDismiss: () -> Unit,
    onAddManually: () -> Unit,
    onScanReceipt: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lottie Animation at the top
                val composition by rememberLottieComposition(LottieCompositionSpec.Url("https://lottie.host/6b49507f-60db-4f2e-8f92-c89a6ea45cbf/2hNKPFWZzO.json"))
                val progress by animateLottieCompositionAsState(composition)

                LottieAnimation(
                    composition,
                    progress,
                    modifier = Modifier
                        .height(120.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = "Add Expense",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAddManually,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Manual", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Manually", color = Color.White)
                }

                Button(
                    onClick = {
                        navController.navigate("speechtotext")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Scan", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Speak and Add", color = Color.White)
                }

                Button(
                    onClick = {
                        navController.navigate("imagetotext")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_upload_24),
                        contentDescription = "Scan",
                        modifier = Modifier.size(24.dp), // Optional
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan UPI screenshot and add", color = Color.White)
                }


                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}