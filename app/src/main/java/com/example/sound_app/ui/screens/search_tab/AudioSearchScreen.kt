package com.example.sound_app.ui.screens.search_tab

import android.media.MediaPlayer
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSearchScreen(audioViewModel: AudioViewModel = hiltViewModel(), modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by audioViewModel.searchResults.collectAsState()
    val selectedFileInfo by audioViewModel.selectedFileInfo.collectAsState()
    val isLoading by audioViewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var currentPlayingIndex by remember { mutableStateOf(-1) }
    var hasStarted by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search audio files") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { audioViewModel.searchAudioFiles(searchQuery) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Search", style = TextStyle(fontSize = 14.sp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn {
                items(searchResults) { result ->
                    val itemIndex = searchResults.indexOf(result)
                    val isCurrentItem = itemIndex == currentPlayingIndex

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = result.title,
                            modifier = Modifier.clickable {
                                if (currentPlayingIndex != itemIndex) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.reset()
                                    isPlaying = false
                                    isPaused = false
                                    hasStarted = false
                                    currentPlayingIndex = itemIndex
                                    audioViewModel.fetchFileInfo(result.title)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isCurrentItem && selectedFileInfo != null && selectedFileInfo?.url != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp) // Give the Box a fixed height
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxSize() // Fill the parent Box
                                        .align(Alignment.Center)
                                ) {
                                    item {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    isDownloading = true
                                                    downloadProgress = 0
                                                    downloadFile(selectedFileInfo!!.url) { progress ->
                                                        downloadProgress = progress
                                                    }
                                                    isDownloading = false
                                                }
                                            },
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text("Download", style = TextStyle(fontSize = 14.sp))
                                        }
                                    }
                                    item {
                                        Button(
                                            onClick = {
                                                mediaPlayer?.release()
                                                mediaPlayer = MediaPlayer().apply {
                                                    setDataSource(selectedFileInfo!!.url)
                                                    prepare()
                                                    start()
                                                    isPlaying = true
                                                    hasStarted = true
                                                    isPaused = false
                                                    setOnCompletionListener {
                                                        isPlaying = false
                                                    }
                                                }
                                            },
                                            enabled = !hasStarted,
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text("Start", style = TextStyle(fontSize = 14.sp))
                                        }
                                    }
                                    item {
                                        Button(
                                            onClick = {
                                                if (isPaused) {
                                                    mediaPlayer?.start()
                                                    isPaused = false
                                                    isPlaying = true
                                                } else {
                                                    mediaPlayer?.pause()
                                                    isPaused = true
                                                    isPlaying = false
                                                }
                                            },
                                            enabled = isPlaying || isPaused,
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text(if (isPaused) "Resume" else "Pause", style = TextStyle(fontSize = 14.sp))
                                        }
                                    }
                                    item {
                                        Button(
                                            onClick = {
                                                mediaPlayer?.seekTo(0)
                                                mediaPlayer?.start()
                                                isPlaying = true
                                                isPaused = false
                                            },
                                            enabled = !isPlaying && hasStarted,
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text("Restart", style = TextStyle(fontSize = 14.sp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isDownloading) {
        AlertDialog(
            onDismissRequest = { /* Handle dialog dismiss */ },
            title = { Text("Downloading") },
            text = {
                Column {
                    Text("Download progress: $downloadProgress%")
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = downloadProgress / 100f)
                }
            },
            confirmButton = {
                Button(
                    onClick = { /* Handle cancel download */ }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun downloadFile(fileUrl: String, onProgress: (Int) -> Unit): File {
    val fileName = fileUrl.substringAfterLast('/')
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/downloaded_audio", fileName)
    val urlConnection = URL(fileUrl).openConnection()
    urlConnection.connect()
    val totalSize = urlConnection.contentLength
    var downloadedSize = 0
    URL(fileUrl).openStream().use { input ->
        FileOutputStream(file).use { output ->
            val data = ByteArray(1024)
            var count = input.read(data)
            while (count != -1) {
                downloadedSize += count
                output.write(data, 0, count)
                val progress = (downloadedSize * 100) / totalSize
                onProgress(progress)
                count = input.read(data)
            }
        }
    }
    return file
}
