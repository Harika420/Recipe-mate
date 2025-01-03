package com.example.recipemate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recipemate.ui.theme.RecipeMateTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeMateTheme {
                SplashScreenWithConsent()
            }
        }
    }

    @Composable
    fun SplashScreenWithConsent() {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences("RecipeMatePrefs", Context.MODE_PRIVATE)
        var showConsentDialog by remember { mutableStateOf(false) }
        var isSplashComplete by remember { mutableStateOf(false) }

        // Display splash screen first
        SplashScreen(
            onSplashComplete = {
                isSplashComplete = true
                if (isConsentGiven(sharedPreferences)) {
                    // If consent already given, navigate to login
                    navigateToLogin(context)
                } else {
                    // Show GDPR consent dialog if consent is not given
                    showConsentDialog = true
                }
            }
        )

        // Show GDPR consent dialog if required after splash
        if (showConsentDialog) {
            GDPRConsentDialog(
                onAccept = {
                    sharedPreferences.edit().putBoolean("GDPRConsentGiven", true).apply()
                    navigateToLogin(context)
                },
                onDecline = {
                    (context as ComponentActivity).finish() // Close app if consent is declined
                }
            )
        }
    }

    private fun isConsentGiven(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean("GDPRConsentGiven", false)
    }

    private fun navigateToLogin(context: Context) {
        context.startActivity(Intent(context, LoginActivity::class.java))
        (context as ComponentActivity).finish()
    }

    @Composable
    fun SplashScreen(onSplashComplete: () -> Unit) {
        var isSplashVisible by remember { mutableStateOf(true) }

        // Keep splash screen visible for 3 seconds
        LaunchedEffect(Unit) {
            delay(3000) // 3-second delay for splash screen
            isSplashVisible = false
            onSplashComplete()
        }

        if (isSplashVisible) {
            SplashContent()
        }
    }

    @Composable
    fun SplashContent() {
        val background: Painter = painterResource(id = R.drawable.background)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = background,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(300.dp)
                    .padding(bottom = 20.dp),
                contentScale = ContentScale.Fit
            )
        }
    }

    @Composable
    fun GDPRConsentDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss by tapping outside */ },
            title = { Text("GDPR Consent") },
            text = {
                Text(
                    "We value your privacy. By using RecipeMate, you agree to our data collection and usage policies in compliance with GDPR regulations."
                )
            },
            confirmButton = {
                Button(onClick = onAccept) {
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(onClick = onDecline, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)) {
                    Text("Decline")
                }
            }
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun SplashScreenPreview() {
        RecipeMateTheme {
            SplashContent()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GDPRConsentDialogPreview() {
        RecipeMateTheme {
            GDPRConsentDialog(onAccept = {}, onDecline = {})
        }
    }
}
