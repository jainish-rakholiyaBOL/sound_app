@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sound_app.ui.screens.merge_tab

import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File

@Composable
fun PlayMergeScreen(
    modifier: Modifier = Modifier,
    viewModel: AudioFilesMergeViewModel = hiltViewModel()
) {
    val mergedFiles = viewModel.getMergedFiles()

    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var currentPlayingFile by remember { mutableStateOf<File?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merged Audio Files") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn {
                items(mergedFiles) { file ->
                    val isCurrentItem = file == currentPlayingFile

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                // Click to play file logic
                                if (mediaPlayer == null) {
                                    mediaPlayer = MediaPlayer().apply {
                                        setDataSource(file.absolutePath)
                                        prepare()
                                        start()
                                        isPlaying = true
                                        currentPlayingFile = file
                                    }
                                } else {
                                    mediaPlayer?.release()
                                    mediaPlayer = MediaPlayer().apply {
                                        setDataSource(file.absolutePath)
                                        prepare()
                                        start()
                                        isPlaying = true
                                        isPaused = false
                                        currentPlayingFile = file
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
    }

    // Clean up media player on disposal
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
}

@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    isPaused: Boolean,
    mediaPlayer: MediaPlayer?,
    currentPlayingFile: File?,
    file: File,
    setIsPlaying: (Boolean) -> Unit,
    setIsPaused: (Boolean) -> Unit,
    setCurrentPlayingFile: (File?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = {
                if (isPlaying) {
                    mediaPlayer?.pause()
                    setIsPlaying(false)
                    setIsPaused(true)
                } else if (isPaused) {
                    mediaPlayer?.start()
                    setIsPlaying(true)
                    setIsPaused(false)
                }
            },
            enabled = isPlaying || isPaused
        ) {
            Text(if (isPaused) "Resume" else "Pause", style = TextStyle(fontSize = 14.sp))
        }
        Button(
            onClick = {
                mediaPlayer?.seekTo(0)
                mediaPlayer?.start()
                setIsPlaying(true)
                setIsPaused(false)
            },
            enabled = !isPlaying && currentPlayingFile != null
        ) {
            Text("Restart", style = TextStyle(fontSize = 14.sp))
        }
    }
}
