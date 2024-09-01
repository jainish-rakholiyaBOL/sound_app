package com.example.sound_app.network

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("query") val query: SearchQuery
)

data class SearchQuery(
    @SerializedName("search") val search: List<SearchResult>
)

data class SearchResult(
    @SerializedName("title") val title: String,
    @SerializedName("snippet") val snippet: String
)

data class FileInfoResponse(
    @SerializedName("query") val query: FileInfoQuery
)

data class FileInfoQuery(
    @SerializedName("pages") val pages: Map<String, FileInfoPage>
)

data class FileInfoPage(
    @SerializedName("imageinfo") val imageInfo: List<FileInfo>
)

data class FileInfo(
    @SerializedName("url") val url: String,
    @SerializedName("mime") val mimeType: String
)