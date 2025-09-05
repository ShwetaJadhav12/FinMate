package com.example.finmate

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finmate.pages.AddExpenseScreen
import com.example.finmate.pages.AnalyticsPage
import com.example.finmate.pages.AuthScreen
import com.example.finmate.pages.CategoryExpensesScreen
import com.example.finmate.pages.CategoryGridScreen
import com.example.finmate.pages.HomeScreen
import com.example.finmate.pages.LoginScreen
import com.example.finmate.pages.ProfilePage
import com.example.finmate.pages.ShowMoreTransactionsScreen
import com.example.finmate.pages.SignupScreen
import com.example.finmate.speechtotext.SpeechToTextScreen
import com.example.finmate.viewmodel.DashboardViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.time.YearMonth

@SuppressLint("ComposableDestinationInComposeScope")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    GlobNavigation.navController = navController
    val dashboardVM: DashboardViewModel = viewModel()


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
        composable("auth") {
            AuthScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignupScreen(navController)
        }

        // Home
        composable("home") {
            HomeScreen(
                navController = navController
            )
        }

        // Category Page
        composable("categorypage") {
            CategoryGridScreen()
        }

        // Profile/Settings Page
        composable("profilepage") {
            ProfilePage(
                navController = navController,
                selectedIndex = 3, // Settings selected
                onTabSelected = { index ->
                    when (index) {
                        0 -> navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                        1 -> navController.navigate("addexpense") // ✅ fixed route
                        2 -> navController.navigate("categorypage")
                        3 -> {} // already on profile/settings
                    }
                }
            )
        }

        // Analytics
        composable("analytics") {
            AnalyticsPage(
                selectedIndex = 1,
                navController = navController,


            )
        }
        // More transactions page
        composable("showMoreTransactions/{monthId}") { backStackEntry ->
            val monthId = backStackEntry.arguments?.getString("monthId") ?: ""
            val selectedMonth = YearMonth.parse(monthId) // yyyy-MM
            ShowMoreTransactionsScreen(selectedMonth = selectedMonth)
        }


        // Add Expense
        composable("addexpense") { // ✅ renamed to match navigation
            AddExpenseScreen(navController)
        }

        // Category detail expenses
        composable("categoryExpenses/{categoryName}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryExpensesScreen(categoryName)
        }

        // Speech to text page
        composable("speechtotext") {
            SpeechToTextScreen(navController)
        }

        // More transactions page

        }

    }


// Global navigation holder
object GlobNavigation {
    @SuppressLint("StaticFieldLeak")
    lateinit var navController: NavHostController
}
