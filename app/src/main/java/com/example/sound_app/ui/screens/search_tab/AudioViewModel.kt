package com.example.sound_app.ui.screens.search_tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound_app.network.FileInfo
import com.example.sound_app.network.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults

    private val _selectedFileInfo = MutableStateFlow<FileInfo?>(null)
    val selectedFileInfo: StateFlow<FileInfo?> = _selectedFileInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun searchAudioFiles(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val results = audioRepository.searchAudioFiles(query)
            _searchResults.value = results
            _isLoading.value = false
        }
    }

    fun fetchFileInfo(title: String) {
        viewModelScope.launch {
            val fileInfo = audioRepository.getFileInfo(title)
            _selectedFileInfo.value = fileInfo
        }
    }
}