package com.example.finmate.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun GradientBox(
    text: String,
    gradientColors: List<Color>,
    fontSize: TextUnit = 16.sp,
    fontColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(gradientColors))
            .padding(16.dp), // internal padding
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fontColor,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}
