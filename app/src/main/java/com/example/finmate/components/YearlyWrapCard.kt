import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
    val isDarkTheme = isSystemInDarkTheme()

    val gradientBrush = if (isDarkTheme) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF0D47A1), // Dark Blue
                Color(0xFF33A3DC)  // Almost Black Blue
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF90CAF9), // Light Blue
                Color(0xFF1E88E5)  // Primary Blue
            )
        )
    }

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val buttonBg = if (isDarkTheme) Color.White else Color(0xFF0A375E)
    val buttonText = if (isDarkTheme) Color(0xFF82A9E5) else Color.White

    LaunchedEffect(year) {
        viewModel.loadYearlyWrap(year)
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush, RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {

            Text(
                text = "Your $year Money Wrapped 💙",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("💸 You spent ₹${viewModel.yearlyTotal}", color = textColor)
            Text("🔥 Highest month: ${viewModel.highestMonth}", color = textColor)
            Text("🍔 Top category: ${viewModel.topCategory}", color = textColor)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = buttonBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", color = buttonText)
            }
        }
    }
}
