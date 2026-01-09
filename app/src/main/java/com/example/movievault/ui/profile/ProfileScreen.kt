package com.example.movievault.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movievault.util.gravatarUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email.orEmpty()
    val avatarUrl = gravatarUrl(email)

    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var savingName by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AsyncImage(
                model = avatarUrl,
                contentDescription = "Profil picture",
                modifier = Modifier.size(96.dp).clip(CircleShape)
            )

            Spacer(Modifier.height(12.dp))

            Text(text = email, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(24.dp))

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    FirebaseCrashlytics.getInstance().log("Test crash button clicked")
                    throw RuntimeException("Test Crash MovieVault")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(" Test Crash ")
            }


            OutlinedButton(
                onClick = {
                    msg = null
                    val u = FirebaseAuth.getInstance().currentUser
                    if (u == null) {
                        msg = "User not logged in."
                        return@OutlinedButton
                    }
                    if (displayName.trim().isBlank()) {
                        msg = "Empty name."
                        return@OutlinedButton
                    }

                    savingName = true

                    val updates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName.trim())
                        .build()

                    u.updateProfile(updates)
                        .addOnSuccessListener {
                            db.collection("users")
                                .document(u.uid)
                                .set(
                                    mapOf(
                                        "displayName" to displayName.trim(),
                                        "email" to (u.email ?: ""),
                                        "updatedAt" to System.currentTimeMillis(),
                                        "createdAt" to System.currentTimeMillis()
                                    ),
                                    com.google.firebase.firestore.SetOptions.merge()
                                )

                                .addOnSuccessListener {
                                    savingName = false
                                    msg = "✅ Name updated."
                                }
                                .addOnFailureListener { e ->
                                    savingName = false
                                    msg = e.localizedMessage ?: "Firestore Error"
                                }
                        }
                        .addOnFailureListener { e ->
                            savingName = false
                            msg = e.localizedMessage ?: "Update profile Error"
                        }
                },
                enabled = !savingName,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (savingName) "..." else "Register name")
            }

            Spacer(Modifier.height(12.dp))


            OutlinedButton(
                onClick = { showPasswordDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change password")
            }

            if (msg != null) {
                Spacer(Modifier.height(12.dp))
                Text(msg!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                newPassword = ""
                confirmPassword = ""
            },
            title = { Text("New password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New password") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !loading,
                    onClick = {
                        msg = null

                        if (newPassword.length < 6) {
                            msg = "Password too short (min 6 characters)."
                            return@TextButton
                        }
                        if (newPassword != confirmPassword) {
                            msg = "Passwords don't match."
                            return@TextButton
                        }

                        val u = FirebaseAuth.getInstance().currentUser
                        if (u == null) {
                            msg = "User not logged in."
                            return@TextButton
                        }

                        loading = true
                        u.updatePassword(newPassword)
                            .addOnSuccessListener {
                                loading = false
                                msg = "✅ Password updated."
                                showPasswordDialog = false
                                newPassword = ""
                                confirmPassword = ""
                            }
                            .addOnFailureListener { e ->
                                loading = false
                                msg = e.localizedMessage ?: "Password change Error"
                            }
                    }
                ) { Text(if (loading) "..." else "Save") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    newPassword = ""
                    confirmPassword = ""
                }) { Text("Cancel") }
            }
        )
    }
}
