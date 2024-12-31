package com.example.recipemate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recipemate.ui.theme.RecipeMateTheme
import java.util.regex.Pattern

class SignupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipeMateTheme {
                SignupScreen(
                    onSignupSuccess = {
                        // Handle signup success (e.g., navigate to the main activity or show a message)
                        Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()
                        finish() // Close SignupActivity
                    },
                    onGoogleSignup = {
                        // Handle Google Signup Logic
                        Toast.makeText(this, "Google Signup Clicked!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onGoogleSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Load the background image and logo
    val background = painterResource(id = R.drawable.background) // Replace with your background image resource ID
    val logo = painterResource(id = R.drawable.logo) // Replace with your logo resource ID

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = background,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay to improve text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = logo,
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp) // Adjust size as needed
                    .padding(bottom = 32.dp), // Space between logo and fields
                contentScale = ContentScale.Fit
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null // Clear error when user modifies input
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null // Clear error when user modifies input
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                isError = passwordError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password"
                        )
                    }
                }
            )
            passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null // Clear error when user modifies input
                },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                isError = confirmPasswordError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
            )
            confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(modifier = Modifier.height(24.dp))

            // Signup Button
            Button(
                onClick = {
                    // Validate inputs
                    val isEmailValid = isValidEmail(email)
                    val isPasswordValid = password.length >= 6
                    val isPasswordMatch = password == confirmPassword

                    if (!isEmailValid) {
                        emailError = "Please enter a valid email"
                    }
                    if (!isPasswordValid) {
                        passwordError = "Password must be at least 6 characters long"
                    }
                    if (!isPasswordMatch) {
                        confirmPasswordError = "Passwords do not match"
                    }

                    if (isEmailValid && isPasswordValid && isPasswordMatch) {
                        // If all validations pass, invoke onSignupSuccess
                        onSignupSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Signup Button
            Button(
                onClick = onGoogleSignup,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Text("Sign Up with Google")
            }
        }
    }
}

// Utility function to validate email
fun isValidEmail(email: String): Boolean {
    val emailPattern = Pattern.compile(
        "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    )
    return emailPattern.matcher(email).matches()
}
