package com.example.sound_app.network

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface WikimediaApiService {
    @GET("w/api.php")
    suspend fun searchAudioFiles(
        @Query("action") action: String = "query",
        @Query("list") list: String = "search",
        @Query("srsearch") srsearch: String,
        @Query("srnamespace") srnamespace: String = "6", // File namespace
        @Query("srwhat") srwhat: String = "text",
        @Query("srlimit") srlimit: Int = 50,
        @Query("format") format: String = "json"
    ): Response<SearchResponse>

    @GET("w/api.php")
    suspend fun getFileInfo(
        @Query("action") action: String = "query",
        @Query("prop") prop: String = "imageinfo",
        @Query("iiprop") iiprop: String = "url|mime",
        @Query("titles") titles: String,
        @Query("format") format: String = "json"
    ): Response<FileInfoResponse>
}
