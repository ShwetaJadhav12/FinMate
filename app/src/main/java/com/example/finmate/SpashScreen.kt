package com.example.finmate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finmate.components.OnboardingPrefs
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {

    var visible by remember { mutableStateOf(true) }

    val scale = animateFloatAsState(
        targetValue = if (visible) 1f else 2f,
        animationSpec = tween(1000),
        label = "scale"
    )

    val alpha = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1000),
        label = "alpha"
    )

    LaunchedEffect(Unit) {

        delay(1500)
        visible = false
        delay(1000)

        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        val onboardingDone = OnboardingPrefs.isOnboardingCompleted(navController.context)

        val nextRoute = when {
            isLoggedIn -> "home"
            !onboardingDone -> "onboarding"
            else -> "auth"
        }

        navController.navigate(nextRoute) {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2196F3)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = true) {
            Text(
                text = "FinMate",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            )
        }
    }
}
