package com.example.finmate

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finmate.GlobNavigation.navController
import com.example.finmate.pages.CategoryGridScreen
import com.example.finmate.ui.theme.FinMateTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FinMateTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding)) }
            }
        }
    }
}

