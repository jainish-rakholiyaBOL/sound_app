package com.example.sound_app.ui.screens.search_tab

import com.example.sound_app.network.FileInfo
import com.example.sound_app.network.SearchResult
import com.example.sound_app.network.WikimediaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepository @Inject constructor(private val apiService: WikimediaApiService) {
    suspend fun searchAudioFiles(keyword: String): List<SearchResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchAudioFiles(srsearch = "$keyword filetype:audio")
            if (response.isSuccessful) {
                response.body()?.query?.search ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFileInfo(title: String): FileInfo? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFileInfo(titles = title)
            if (response.isSuccessful) {
                response.body()?.query?.pages?.values?.firstOrNull()?.imageInfo?.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
