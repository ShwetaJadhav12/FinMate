package com.example.finmate.pages

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finmate.R

@Composable
fun AuthScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val bgColor = if (isDark) Color(0xFF101820) else Color.White
    val titleColor = if (isDark) Color(0xFF58A6FF) else Color(0xFF073156)
    val subtitleColor = if (isDark) Color(0xFF9DA5B4) else Color.Gray
    val buttonBg = if (isDark) Color(0xFF1976D2) else Color(0xFF4181D0)

    val buttonText = Color.White
    val outlineText = if (isDark) Color(0xFF58A6FF) else Color(0xFF4181D0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(id = R.drawable.a),
            contentDescription = "Banner",
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(250.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Monitor your spending and grow your savings",
                style = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = titleColor,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Track daily expenses effortlessly and save more over time",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    color = subtitleColor,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(55.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBg,
                    contentColor = buttonText
                )
            ) {
                Text(text = "Log In", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { navController.navigate("signup") },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(55.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = outlineText
                )
            ) {
                Text(text = "Sign Up", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
