package com.example.movievault.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject

data class OmdbMovieShort(
    val title: String,
    val year: String?,
    val imdbId: String,
    val poster: String?
)

class OmdbApi(
    private val apiKey: String
) {
    private val client = HttpClient(OkHttp)

    suspend fun searchMovies(query: String): List<OmdbMovieShort> {
        val url =
            "https://www.omdbapi.com/?apikey=$apiKey&s=${query.replace(" ", "+")}"

        val response = client.get(url).bodyAsText()
        val json = JSONObject(response)

        if (json.optString("Response") != "True") {
            return emptyList()
        }

        val array = json.getJSONArray("Search")
        val results = mutableListOf<OmdbMovieShort>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            results.add(
                OmdbMovieShort(
                    title = item.optString("Title"),
                    year = item.optString("Year"),
                    imdbId = item.optString("imdbID"),
                    poster = item.optString("Poster")
                )
            )
        }
        return results
    }
}
