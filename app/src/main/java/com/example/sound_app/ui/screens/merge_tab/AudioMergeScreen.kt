@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sound_app.ui.screens.files_tab

import android.media.MediaPlayer
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sound_app.ui.screens.merge_tab.AudioFilesMergeViewModel
import java.io.File
import com.example.sound_app.ui.screens.merge_tab.PlaybackControls

@Composable
fun AudioMergeScreen(audioFilesMergeViewModel: AudioFilesMergeViewModel = hiltViewModel(), modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val recordedFilesDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recorded_audio")
    val downloadedFilesDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "downloaded_audio")

    val recordedFiles = recordedFilesDirectory.listFiles()?.toList() ?: emptyList()
    val downloadedFiles = downloadedFilesDirectory.listFiles()?.toList() ?: emptyList()

    // State for selected files
    var selectedRecordedFile by remember { mutableStateOf<File?>(null) }
    var selectedDownloadedFile by remember { mutableStateOf<File?>(null) }

    // MediaPlayer state
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var currentPlayingFile by remember { mutableStateOf<File?>(null) }

    // App bar with merge icon
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Files") },
                actions = {
                    IconButton(
                        onClick = { audioFilesMergeViewModel.mergeFiles(selectedRecordedFile, selectedDownloadedFile) },
                        enabled = selectedRecordedFile != null && selectedDownloadedFile != null
                    ) {
                        Icon(Icons.Filled.Merge, contentDescription = "Merge Files")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(text = "Choose 1 file from both recording and downloads each to merge", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recordings Section
            item {
                Text("Recordings", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(recordedFiles) { file ->
                val isCurrentItem = file == currentPlayingFile

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            selectedRecordedFile = file
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRecordedFile == file,
                        onClick = { selectedRecordedFile = file }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(file.name)
                }

                if (isCurrentItem) {
                    PlaybackControls(
                        isPlaying = isPlaying,
                        isPaused = isPaused,
                        mediaPlayer = mediaPlayer,
                        currentPlayingFile = currentPlayingFile,
                        file = file,
                        setIsPlaying = { isPlaying = it },
                        setIsPaused = { isPaused = it },
                        setCurrentPlayingFile = { currentPlayingFile = it }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                // Downloads Section
                Text("Downloads", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(downloadedFiles) { file ->
                val isCurrentItem = file == currentPlayingFile

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            selectedDownloadedFile = file
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedDownloadedFile == file,
                        onClick = { selectedDownloadedFile = file }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(file.name)
                }

                if (isCurrentItem) {
                    PlaybackControls(
                        isPlaying = isPlaying,
                        isPaused = isPaused,
                        mediaPlayer = mediaPlayer,
                        currentPlayingFile = currentPlayingFile,
                        file = file,
                        setIsPlaying = { isPlaying = it },
                        setIsPaused = { isPaused = it },
                        setCurrentPlayingFile = { currentPlayingFile = it }
                    )
                }
            }
        }
    }

    // Clean up media player on disposal
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
}
