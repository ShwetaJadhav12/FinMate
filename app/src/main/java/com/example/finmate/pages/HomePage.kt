package com.example.finmate.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finmate.model.DrawerItems
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier) {
    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("User") }
    val context = LocalContext.current

    val drawerItems = listOf(
        DrawerItems("Home", "home"),
        DrawerItems("Analytics", "analytics"),
        DrawerItems("Budget", "budget"),
        DrawerItems("Logout", "logout")
    )

    // Get user name from Firestore
    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("name") ?: "User"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to fetch user name", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    // Scaffold with Drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "FinMate",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item.route == "logout") {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            } else {
                                // navigate to other screens (optional)
                                Toast.makeText(context, "${item.title} clicked", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Welcome 👋") },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable { scope.launch { drawerState.open() } }
                        )
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { Toast.makeText(context, "Profile Clicked", Toast.LENGTH_SHORT).show() }
                            .background(Color(0xFF4CAF50))
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // TODO: Other cards/content
            }
        }
    }
}
