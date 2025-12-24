import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.finmate.viewmodel.DashboardViewModel

@Composable
fun YearlyWrapDialog(
    viewModel: DashboardViewModel,
    year: Int,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // 🌈 Background Gradient (Soft & Premium)
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF9FBFF),
                Color(0xFFEAF2FF)
            )
        )
    }

    // 🎨 Typography Colors
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val valueColor = if (isDark) Color.White else Color(0xFF0F172A)

    LaunchedEffect(year) {
        viewModel.loadYearlyWrap(year)
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {

            // 🏷️ Title
            Text(
                text = "Your $year Money Wrapped",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "A simple summary of your spending",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = subtitleColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 💎 Inner Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isDark)
                            Color(0xFF020617).copy(alpha = 0.4f)
                        else
                            Color.White,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {

                WrapTextItem(
                    label = "TOTAL SPENT",
                    value = "₹${viewModel.yearlyTotal}",
                    labelColor = labelColor,
                    valueColor = valueColor
                )

                DividerSpacer()

                WrapTextItem(
                    label = "HIGHEST SPENDING MONTH",
                    value = viewModel.highestMonth,
                    labelColor = labelColor,
                    valueColor = valueColor
                )

                DividerSpacer()

                WrapTextItem(
                    label = "TOP CATEGORY",
                    value = viewModel.topCategory,
                    labelColor = labelColor,
                    valueColor = valueColor
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 🔘 Close Button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                )
            ) {
                Text(
                    text = "Close",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}
@Composable
fun WrapTextItem(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
fun DividerSpacer() {
    Spacer(modifier = Modifier.height(18.dp))
}
