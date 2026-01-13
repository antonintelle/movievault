package com.example.movievault.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase


@Composable
fun LoginScreen(
    onGoRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val analytics = remember { Firebase.analytics }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun login() {
        errorMessage = null

        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Fill mail and password."
            return
        }

        loading = true
        auth.signInWithEmailAndPassword(email.trim(), password)
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                loading = false
                if (task.isSuccessful) {

                    analytics.logEvent("login_success", null)

                    onLoginSuccess()
                } else {

                    analytics.logEvent("login_failed", null)

                    errorMessage = task.exception?.localizedMessage ?: "Connexion Error"
                }
            }

    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Login", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { login() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Connexion...")
                } else {
                    Text("Log in")
                }
            }

            TextButton(onClick = onGoRegister) {
                Text("Create account")
            }
        }
    }
}
