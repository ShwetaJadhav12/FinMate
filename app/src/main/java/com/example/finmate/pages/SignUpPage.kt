package com.example.finmate.pages


import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.finmate.R
import com.example.finmate.components.OnboardingPrefs
import com.example.project_2_ecommerce_app.viewmodel.AuthViewModel

@Composable
fun SignupScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Error messages
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // 🌙 Detect Dark Theme
    val isDark = isSystemInDarkTheme()

    // 🎨 Dynamic Colors
    val backgroundColor = if (isDark) Color(0xFF101820) else Color.White
    val titleColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF073156)
    val textColor = if (isDark) Color.White else Color.Black
    val hintColor = if (isDark) Color(0xFFB0BEC5) else Color.Gray
    val borderColor = if (isDark) Color.White else Color(0xFF4181D0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(id = R.drawable.s),
            contentDescription = "Signup Illustration",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Create Account",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ======================= FULL NAME ==========================
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = null
            },
            label = { Text("Full Name", color = hintColor) },
            singleLine = true,
            textStyle = TextStyle(color = textColor),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                cursorColor = textColor,
            ),

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = nameError != null

        )
        if (nameError != null) {
            Text(text = nameError!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ========================= EMAIL ============================
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("Email", color = hintColor) },
            singleLine = true,
            textStyle = TextStyle(color = textColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                cursorColor = textColor,
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = emailError != null
        )
        if (emailError != null) {
            Text(text = emailError!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ======================== PASSWORD =========================
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Password", color = hintColor) },
            singleLine = true,
            textStyle = TextStyle(color = textColor),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                cursorColor = textColor,
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = passwordError != null
        )
        if (passwordError != null) {
            Text(text = passwordError!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ========================= SIGNUP BUTTON =========================
        Button(
            onClick = {
                val valid = validateInputs(
                    name = name,
                    email = email,
                    password = password,
                    onNameError = { nameError = it },
                    onEmailError = { emailError = it },
                    onPasswordError = { passwordError = it }
                )
                if (valid) {
                    authViewModel.Signup(email, name, password) { success, message ->
                        if (success) {
                            isLoading = true

                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            isLoading = false
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF1976D2) else Color(0xFF4181D0),
                contentColor = Color.White
            )
        ) {
            Text("Sign Up", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login", color = hintColor)
        }
    }
}


private fun validateInputs(
    name: String,
    email: String,
    password: String,
    onNameError: (String?) -> Unit,
    onEmailError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    // ---------------- NAME VALIDATION ----------------
    if (name.isBlank()) {
        onNameError("Name cannot be empty")
        isValid = false
    } else if (name.length < 3) {
        onNameError("Name must be at least 3 characters")
        isValid = false
    } else if (!name.matches(Regex("^[A-Za-z ]+$"))) {
        onNameError("Name must contain only letters")
        isValid = false
    } else {
        onNameError(null)
    }

    // ---------------- EMAIL VALIDATION ----------------
    if (email.isBlank()) {
        onEmailError("Email cannot be empty")
        isValid = false
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError("Invalid email format")
        isValid = false
    } else {
        onEmailError(null)
    }

    // ---------------- PASSWORD VALIDATION ----------------
    if (password.isBlank()) {
        onPasswordError("Password cannot be empty")
        isValid = false
    } else if (password.length < 6) {
        onPasswordError("Password must be at least 6 characters")
        isValid = false
    } else if (!password.matches(Regex(".*[A-Z].*"))) {
        onPasswordError("Must contain at least 1 uppercase letter")
        isValid = false
    } else if (!password.matches(Regex(".*[a-z].*"))) {
        onPasswordError("Must contain 1 lowercase letter")
        isValid = false
    } else if (!password.matches(Regex(".*[0-9].*"))) {
        onPasswordError("Must contain 1 digit")
        isValid = false
    } else if (!password.matches(Regex(".*[!@#\$%^&*(),.?\":{}|<>].*"))) {
        onPasswordError("Must contain 1 special character")
        isValid = false
    } else {
        onPasswordError(null)
    }

    return isValid
}