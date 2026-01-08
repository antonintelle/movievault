package com.example.movievault.data.model

data class Movie(
    val id: String = "",
    val title: String = "",
    val status: String = "WATCHLIST", // WATCHLIST ou WATCHED
    val rating: Int? = null,
    val review: String? = null,
    val posterUrl: String? = null,
    val imdbId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
