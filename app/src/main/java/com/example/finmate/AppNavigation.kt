package com.example.finmate

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finmate.pages.*
import com.example.finmate.speechtotext.SpeechToTextScreen
import com.example.finmate.viewmodel.DashboardViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.time.YearMonth

@SuppressLint("ComposableDestinationInComposeScope", "StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    GlobNavigation.navController = navController
    val dashboardVM: DashboardViewModel = viewModel()
    val sharedMonthViewModel: SharedMonthViewModelnew = viewModel()

    // check if user is already logged in
    val isLoggedIn = Firebase.auth.currentUser != null
    val firstPage = if (isLoggedIn) "home" else "auth"

    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = "splash"
    ) {
        // Splash screen
        composable("splash") {
            SplashScreen(navController)
        }

        // Authentication
        composable("auth") { AuthScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }

        // Home
        composable("home") {
            HomeScreen(
                navController = navController,
                sharedMonthViewModel = sharedMonthViewModel
            )
        }

        // Category Page
        composable("categorypage") {
            CategoryGridScreen()
        }

        // Profile/Settings Page
        composable("profilepage") {
            ProfilePage(
                dashboardViewModel = dashboardVM,
                navController = navController,
                selectedIndex = 3, // Settings selected
                onTabSelected = { index ->
                    when (index) {
                        0 -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        1 -> navController.navigate("analytics")
                        2 -> navController.navigate("categorypage")
                        3 -> {} // already on profile/settings
                    }
                },
                sharedMonthViewModel = sharedMonthViewModel
            )
        }

        // Analytics
        composable("analytics") {
            AnalyticsPage(
                navController = navController,
            )
        }

        // More transactions page
        composable("showMoreTransactions/{monthId}") { backStackEntry ->
            val monthId = backStackEntry.arguments?.getString("monthId") ?: YearMonth.now().toString()
            val selectedMonth = YearMonth.parse(monthId) // yyyy-MM
            ShowMoreTransactionsScreen(
                selectedMonth = selectedMonth,
            )
        }

        // Add Expense
        composable("addexpense") {
            AddExpenseScreen(
                navController = navController,
            )
        }

        // Category detail expenses
        composable("categoryExpenses/{categoryName}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val selectedMonth by sharedMonthViewModel.selectedMonth.collectAsState()

            if (categoryName.isNotEmpty()) {
                CategoryExpensesScreen(
                    categoryName = categoryName,
                    selectedMonth = selectedMonth
                )
            }
        }


        composable("speechtotext") {
            SpeechToTextScreen(
                navController = navController,
                dashboardVM = dashboardVM   // <- IMPORTANT
            )
        }

    }
}

// Global navigation holder
object GlobNavigation {
    @SuppressLint("StaticFieldLeak")
    lateinit var navController: NavHostController
}
