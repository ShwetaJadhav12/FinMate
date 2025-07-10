package com.example.finmate.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(100.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(colors = gradientColors),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 12.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
@Composable
fun DashboardGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GradientDashboardCard(
                title = "Expenses",
                icon = Icons.Default.Menu,
                gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            GradientDashboardCard(
                title = "Analytics",
                icon = Icons.Default.Menu,
                gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6)),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GradientDashboardCard(
                title = "Budget",
                icon = Icons.Default.Menu,
                gradientColors = listOf(Color(0xFF8E24AA), Color(0xFFCE93D8)),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            GradientDashboardCard(
                title = "Settings",
                icon = Icons.Default.Settings,
                gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFFCC80)),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
        }
    }
}
