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

