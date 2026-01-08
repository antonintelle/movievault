package com.example.movievault.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmdbSearchResponse(
    @SerialName("Search") val search: List<OmdbMovieShort> = emptyList(),
    @SerialName("totalResults") val totalResults: String? = null,
    @SerialName("Response") val response: String = "False",
    @SerialName("Error") val error: String? = null
)

@Serializable
data class OmdbMovieShort(
    @SerialName("Title") val title: String = "",
    @SerialName("Year") val year: String? = null,
    @SerialName("imdbID") val imdbId: String = "",
    @SerialName("Type") val type: String? = null,
    @SerialName("Poster") val poster: String? = null
)
