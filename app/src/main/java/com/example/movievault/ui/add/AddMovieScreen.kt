package com.example.movievault.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.movievault.data.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovieScreen(
    onBack: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var title by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("WATCHLIST") }
    var ratingText by remember { mutableStateOf("") }
    var review by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun saveMovie() {
        errorMessage = null
        val user = auth.currentUser
        if (user == null) {
            errorMessage = "Utilisateur non connecté."
            return
        }
        if (title.isBlank()) {
            errorMessage = "Le titre est obligatoire."
            return
        }

        val rating = ratingText.trim().toIntOrNull()
        if (rating != null && (rating < 1 || rating > 10)) {
            errorMessage = "La note doit être entre 1 et 10."
            return
        }

        val now = System.currentTimeMillis()
        val movie = Movie(
            title = title.trim(),
            status = status,
            rating = rating,
            review = review.trim().ifBlank { null },
            createdAt = now,
            updatedAt = now
        )

        loading = true
        db.collection("users")
            .document(user.uid)
            .collection("movies")
            .add(movie)
            .addOnSuccessListener {
                loading = false
                onBack()
            }
            .addOnFailureListener { e ->
                loading = false
                errorMessage = e.localizedMessage ?: "Erreur lors de l'ajout"
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Movie") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))


            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = status == "WATCHLIST",
                    onClick = { status = "WATCHLIST" },
                    label = { Text("Watchlist") }
                )
                FilterChip(
                    selected = status == "WATCHED",
                    onClick = { status = "WATCHED" },
                    label = { Text("Watched") }
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = ratingText,
                onValueChange = { ratingText = it },
                label = { Text("Note (1-10) optionnel") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = review,
                onValueChange = { review = it },
                label = { Text("Commentaire (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { saveMovie() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Text("Save")
                }
            }
        }
    }
}
