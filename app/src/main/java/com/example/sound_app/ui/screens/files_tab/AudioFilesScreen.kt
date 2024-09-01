package com.example.sound_app.ui.screens.files_tab

import android.media.MediaPlayer
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun AudioFilesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val recordedFilesDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recorded_audio")
    val downloadedFilesDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "downloaded_audio")

    val recordedFiles = recordedFilesDirectory.listFiles()?.toList() ?: emptyList()
    val downloadedFiles = downloadedFilesDirectory.listFiles()?.toList() ?: emptyList()

    // MediaPlayer state
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var currentPlayingFile by remember { mutableStateOf<File?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // Recordings Section
            Text("Recordings", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(recordedFiles) { file ->
            val isCurrentItem = file == currentPlayingFile

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = file.name,
                    modifier = Modifier
                        .clickable {
                            if (isCurrentItem) {
                                // If the current item is clicked, toggle playback
                                if (isPlaying) {
                                    mediaPlayer?.pause()
                                    isPlaying = false
                                    isPaused = true
                                } else if (isPaused) {
                                    mediaPlayer?.start()
                                    isPlaying = true
                                    isPaused = false
                                }
                            } else {
                                // Stop current playback if a new file is clicked
                                mediaPlayer?.stop()
                                mediaPlayer?.reset()

                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(file.absolutePath)
                                    prepare()
                                    start()
                                    isPlaying = true
                                    isPaused = false
                                    currentPlayingFile = file
                                    setOnCompletionListener {
                                        isPlaying = false
                                        isPaused = false
                                    }
                                }
                            }
                        }
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isCurrentItem) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.pause()
                                    isPlaying = false
                                    isPaused = true
                                } else if (isPaused) {
                                    mediaPlayer?.start()
                                    isPlaying = true
                                    isPaused = false
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
                                isPlaying = true
                                isPaused = false
                            },
                            enabled = !isPlaying && currentPlayingFile != null
                        ) {
                            Text("Restart", style = TextStyle(fontSize = 14.sp))
                        }
                    }
                }
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = file.name,
                    modifier = Modifier
                        .clickable {
                            if (isCurrentItem) {
                                if (isPlaying) {
                                    mediaPlayer?.pause()
                                    isPlaying = false
                                    isPaused = true
                                } else if (isPaused) {
                                    mediaPlayer?.start()
                                    isPlaying = true
                                    isPaused = false
                                }
                            } else {
                                mediaPlayer?.stop()
                                mediaPlayer?.reset()

                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(file.absolutePath)
                                    prepare()
                                    start()
                                    isPlaying = true
                                    isPaused = false
                                    currentPlayingFile = file
                                    setOnCompletionListener {
                                        isPlaying = false
                                        isPaused = false
                                    }
                                }
                            }
                        }
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (isCurrentItem) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.pause()
                                    isPlaying = false
                                    isPaused = true
                                } else if (isPaused) {
                                    mediaPlayer?.start()
                                    isPlaying = true
                                    isPaused = false
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
                                isPlaying = true
                                isPaused = false
                            },
                            enabled = !isPlaying && currentPlayingFile != null
                        ) {
                            Text("Restart", style = TextStyle(fontSize = 14.sp))
                        }
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
