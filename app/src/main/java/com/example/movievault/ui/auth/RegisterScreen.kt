package com.example.movievault.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }

    fun register() {
        errorMessage = null

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Remplis tous les champs."
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Les mots de passe ne correspondent pas."
            return
        }
        if (password.length < 6) {
            errorMessage = "Mot de passe trop court (min 6 caractères)."
            return
        }

        loading = true
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                loading = false
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        // 1. Update displayName (Auth)
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim())
                            .build()

                        user.updateProfile(profileUpdates)

                        // 2. Create Firestore user document
                        val db = FirebaseFirestore.getInstance()
                        val uid = user.uid

                        val data = hashMapOf(
                            "uid" to uid,
                            "email" to (user.email ?: ""),
                            "displayName" to name.trim(),
                            "photoUrl" to "",
                            "createdAt" to System.currentTimeMillis(),
                            "updatedAt" to System.currentTimeMillis()
                        )

                        db.collection("users")
                            .document(uid)
                            .set(data)
                            .addOnSuccessListener {
                                onRegisterSuccess()
                            }
                            .addOnFailureListener { e ->
                                errorMessage = e.localizedMessage ?: "Erreur Firestore"
                            }
                    }
                } else {
                    errorMessage = task.exception?.localizedMessage ?: "Erreur d'inscription"
                }

            }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Register", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))


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
                label = { Text("Mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmer le mot de passe") },
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
                onClick = { register() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Création...")
                } else {
                    Text("Créer un compte")
                }
            }

            TextButton(onClick = onBackToLogin) {
                Text("Back to Login")
            }
        }
    }
}
