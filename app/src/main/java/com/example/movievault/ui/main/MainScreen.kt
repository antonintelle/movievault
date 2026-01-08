package com.example.movievault.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddMovie: () -> Unit,
    onAbout: () -> Unit,
    onProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MovieVault") },
                actions = {
                    TextButton(onClick = onAbout) { Text("About") }
                    TextButton(onClick = onProfile) { Text("Profile") }
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
        ) {
            Text("Main screen (movie list later)")
        }
    }
}

