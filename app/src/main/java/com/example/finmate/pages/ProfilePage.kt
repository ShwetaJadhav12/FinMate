package com.example.finmate.pages

import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finmate.GlobNavigation
import com.example.finmate.R
import com.example.finmate.components.EditProfileDialog
import com.example.finmate.components.fetchUserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    navController: NavController,
    selectedIndex: Int = 3,
    onTabSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(3) } // Default to Profile page

    var showeditpage by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("Loading...") }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Dynamic colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val buttonColor = if (isDarkTheme) Color(0xFF12648D) else Color(0xFF2196F3)

    LaunchedEffect(Unit) {
        fetchUserData(
            onSuccess = {
                userName = it.name
                userEmail = it.email
            },
            onFailure = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = onPrimaryColor
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = primaryColor) {
                val items = listOf("Home", "Analytics", "Category", "Settings")
                val icons = listOf(
                    Icons.Default.Home,
                    R.drawable.baseline_auto_graph_24,
                    R.drawable.baseline_category_24,
                    Icons.Default.Settings
                )

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            onTabSelected(index)
                        },
                        icon = {
                            if (icons[index] is ImageVector) {
                                Icon(
                                    imageVector = icons[index] as ImageVector,
                                    contentDescription = item,
                                    tint = if (selectedIndex == index) secondaryColor else onPrimaryColor
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = icons[index] as Int),
                                    contentDescription = item,
                                    tint = if (selectedIndex == index) secondaryColor else onPrimaryColor
                                )
                            }
                        },
                        label = { Text(item, fontSize = 12.sp) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = secondaryColor,
                            selectedIconColor = secondaryColor,
                            selectedTextColor = secondaryColor,
                            unselectedIconColor = onPrimaryColor,
                            unselectedTextColor = onPrimaryColor
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture with gradient
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                    fontSize = 40.sp,
                    color = onPrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
            Text(text = userEmail, fontSize = 16.sp, color = onSurfaceColor.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(32.dp))

            if (showeditpage) {
                EditProfileDialog(
                    onDismiss = { showeditpage = false },
                    onUserUpdated = {
                        userName = it.name
                        userEmail = it.email
                    }
                )
            }

            Button(
                onClick = { showeditpage = true },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text("Edit Profile", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    Toast.makeText(context, "Get Your Summary card", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text("Summary Card", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Logout", color = Color.White)
            }
        }
    }
}
