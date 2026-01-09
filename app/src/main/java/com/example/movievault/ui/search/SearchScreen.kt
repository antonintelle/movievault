package com.example.movievault.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movievault.data.api.OmdbApi
import com.example.movievault.data.api.OmdbMovieShort
import com.example.movievault.data.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    val omdb = remember { OmdbApi(apiKey = "8cfc5e13") }


    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<OmdbMovieShort>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun search() {
        error = null
        if (query.isBlank()) {
            error = "Tape un titre."
            return
        }

        loading = true
        scope.launch {
            try {
                val movies = omdb.searchMovies(query.trim())
                if (movies.isEmpty()) {
                    results = emptyList()
                    error = "Aucun résultat"
                } else {
                    results = movies
                }
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Erreur réseau"
            } finally {
                loading = false
            }
        }
    }


    fun addToFirestore(item: OmdbMovieShort) {
        val user = auth.currentUser ?: run {
            error = "Utilisateur non connecté."
            return
        }
        val now = System.currentTimeMillis()
        val movie = Movie(
            title = item.title,
            status = "WATCHLIST",
            posterUrl = item.poster,
            imdbId = item.imdbId,
            createdAt = now,
            updatedAt = now
        )

        db.collection("users")
            .document(user.uid)
            .collection("movies")
            .add(movie)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search (OMDb)") },
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
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Rechercher un film") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { search() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Searching...")
                } else {
                    Text("Search")
                }
            }

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(item.year ?: "")
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { addToFirestore(item) }) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
