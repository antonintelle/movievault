package com.example.movievault.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movievault.data.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddMovie: () -> Unit,
    onAbout: () -> Unit,
    onProfile: () -> Unit,
    onSearch: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingMovie by remember { mutableStateOf<Movie?>(null) }
    var editStatus by remember { mutableStateOf("WATCHLIST") }
    var editRatingText by remember { mutableStateOf("") }
    var editReview by remember { mutableStateOf("") }


    DisposableEffect(Unit) {
        val user = auth.currentUser
        if (user == null) {
            loading = false
            errorMessage = "Utilisateur non connecté."
            onDispose { }
        } else {
            val reg: ListenerRegistration = db.collection("users")
                .document(user.uid)
                .collection("movies")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        loading = false
                        errorMessage = error.localizedMessage ?: "Erreur Firestore"
                        return@addSnapshotListener
                    }

                    val docs = snapshot?.documents.orEmpty()
                    movies = docs.map { doc ->
                        val m = doc.toObject(Movie::class.java) ?: Movie()
                        m.copy(id = doc.id)
                    }
                    loading = false
                }

            onDispose { reg.remove() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MovieVault") },
                actions = {
                    TextButton(onClick = onAbout) { Text("About") }
                    TextButton(onClick = onProfile) { Text("Profile") }
                    TextButton(onClick = onSearch) { Text("Search") }
                }

            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMovie) { Text("+") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (loading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Chargement...")
                return@Column
            }

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                return@Column
            }

            if (movies.isEmpty()) {
                Text("Aucun film pour le moment.")
                Spacer(Modifier.height(8.dp))
                Text("Appuie sur + pour en ajouter.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(movies) { movie ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(movie.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("Status: ${movie.status}")

                                if (movie.rating != null) {
                                    Text("Note: ${movie.rating}/10")
                                }
                                if (!movie.review.isNullOrBlank()) {
                                    Text("Commentaire: ${movie.review}")
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            editingMovie = movie
                                            editStatus = movie.status
                                            editRatingText = movie.rating?.toString() ?: ""
                                            editReview = movie.review ?: ""
                                        }
                                    ) { Text("Edit") }

                                    TextButton(
                                        onClick = {
                                            val user = auth.currentUser ?: return@TextButton
                                            db.collection("users")
                                                .document(user.uid)
                                                .collection("movies")
                                                .document(movie.id)
                                                .delete()
                                        }
                                    ) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (editingMovie != null) {
            AlertDialog(
                onDismissRequest = { editingMovie = null },
                title = { Text("Edit Movie") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = editStatus == "WATCHLIST",
                                onClick = { editStatus = "WATCHLIST" },
                                label = { Text("Watchlist") }
                            )
                            FilterChip(
                                selected = editStatus == "WATCHED",
                                onClick = { editStatus = "WATCHED" },
                                label = { Text("Watched") }
                            )
                        }

                        OutlinedTextField(
                            value = editRatingText,
                            onValueChange = { editRatingText = it },
                            label = { Text("Note (1-10) optionnel") },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editReview,
                            onValueChange = { editReview = it },
                            label = { Text("Commentaire (optionnel)") },
                            minLines = 2
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val movie = editingMovie ?: return@TextButton
                            val user = auth.currentUser ?: return@TextButton

                            val rating = editRatingText.trim().toIntOrNull()
                            if (rating != null && (rating < 1 || rating > 10)) {
                                return@TextButton
                            }

                            val updates = hashMapOf<String, Any>(
                                "status" to editStatus,
                                "updatedAt" to System.currentTimeMillis()
                            )

                            if (rating != null) updates["rating"] = rating else updates["rating"] = com.google.firebase.firestore.FieldValue.delete()
                            if (editReview.trim().isNotBlank()) updates["review"] = editReview.trim() else updates["review"] = com.google.firebase.firestore.FieldValue.delete()

                            db.collection("users")
                                .document(user.uid)
                                .collection("movies")
                                .document(movie.id)
                                .update(updates)
                                .addOnSuccessListener { editingMovie = null }
                        }
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { editingMovie = null }) { Text("Cancel") }
                }
            )
        }
    }
}
