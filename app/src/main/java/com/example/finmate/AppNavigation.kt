package com.example.finmate

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finmate.pages.AddExpenseScreen
import com.example.finmate.pages.AuthScreen
import com.example.finmate.pages.HomeScreen
import com.example.finmate.pages.LoginScreen
import com.example.finmate.pages.ProfilePage
import com.example.finmate.pages.SignupScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(

    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    GlobNavigation.navController = navController
    val isLoggedIn = Firebase.auth.currentUser != null
    val firstPage = if (isLoggedIn) "home" else "auth"
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = firstPage
    ) {
        composable("auth") {
            AuthScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignupScreen(navController)
        }
        composable("home") {
            HomeScreen(
                navController

            )
        }
        composable("profilepage") {
            ProfilePage(navController)
        }
        composable("addExpense") {
            AddExpenseScreen(navController)
        }
    }


    }
object GlobNavigation {
    @SuppressLint("StaticFieldLeak")
    lateinit var navController: NavHostController
}
