package com.example.finmate.pages


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finmate.R
import com.example.finmate.model.OnboardingPage
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(navController: NavHostController) {

    val isDark = isSystemInDarkTheme()

    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF0A192F),
                Color(0xFF112240),
                Color(0xFF1F3A5F)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFFDFEFF),
                Color(0xFFF1F7FF),
                Color(0xFFE0ECFF)
            )
        )
    }

    val onboardingPages = remember {
        listOf(
            OnboardingPage(
                R.drawable.onboarding0,
                "Welcome to FinMate",
                "Your smart companion for managing money"
            ),
            OnboardingPage(
                R.drawable.onboarding1,
                "Set Budget",
                "Plan your monthly spending with ease"
            ),
            OnboardingPage(
                R.drawable.onboarding2,
                "Add Expenses",
                "Add expenses manually or using voice input"
            ),
            OnboardingPage(
                R.drawable.onboarding3,
                "Track Expenses",
                "Visual insights with charts and analytics"
            ),
            OnboardingPage(
                R.drawable.onboarding4,
                "Summary Card",
                "Get detailed summaries of your spending"
            )
        )
    }

    val pagerState = rememberPagerState { onboardingPages.size }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingItem(
                page = onboardingPages[page],
                isDark = isDark
            )
        }

        // ✨ Smooth Indicators (same place, prettier)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(onboardingPages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .height(8.dp)
                        .width(if (pagerState.currentPage == index) 28.dp else 8.dp)
                        .background(
                            if (pagerState.currentPage == index)
                                Color(0xFF3B82F6)
                            else
                                Color.LightGray.copy(alpha = 0.6f),
                            CircleShape
                        )
                )
            }
        }

        // 🔘 Bottom Actions (same layout, refined)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextButton(onClick = {
                navController.navigate("auth") {
                    popUpTo("onboarding") { inclusive = true }
                }
            }) {
                Text(
                    "Skip",
                    fontSize = 16.sp,
                    color = if (isDark) Color(0xFFCBD5E1) else Color.Gray
                )
            }

            Button(
                onClick = {
                    if (pagerState.currentPage == onboardingPages.lastIndex) {
                        navController.navigate("auth") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 4.dp
                ),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
@Composable
fun OnboardingItem(
    page: OnboardingPage,
    isDark: Boolean
) {
    // Subtle animation
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "alpha"
    )

    val animatedOffset by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = tween(600),
        label = "offset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .alpha(animatedAlpha)
            .offset(y = animatedOffset),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // 🖼️ Illustration
        Image(
            painter = painterResource(page.image),
            contentDescription = null,
            modifier = Modifier
                .height(280.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        // 🌫️ Glow shadow (fake but beautiful)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isDark)
                        Color(0xFF2563EB).copy(alpha = 0.12f)
                    else
                        Color(0xFF93C5FD).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(6.dp)
        ) {

            // 💎 Main Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark)
                        Color(0xFF162A3A)
                    else
                        Color.White
                ),
                elevation = CardDefaults.cardElevation(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 22.dp,
                        vertical = 26.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // 🎯 Accent line
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .background(
                                Color(0xFF3B82F6),
                                RoundedCornerShape(50)
                            )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 📝 Title
                    Text(
                        text = page.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark)
                            Color.White
                        else
                            Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 📄 Description
                    Text(
                        text = page.description,
                        fontSize = 16.sp,
                        color = if (isDark)
                            Color(0xFFCBD5E1)
                        else
                            Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 💡 Hint text (tiny but classy)
                    Text(
                        text = "Swipe to continue →",
                        fontSize = 13.sp,
                        color = Color(0xFF3B82F6)
                    )
                }
            }
        }
    }
}
