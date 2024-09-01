package com.example.sound_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sound_app.ui.screens.files_tab.AudioFilesScreen
import com.example.sound_app.ui.screens.files_tab.AudioMergeScreen
import com.example.sound_app.ui.screens.merge_tab.PlayMergeScreen
import com.example.sound_app.ui.screens.recording_tab.AudioRecordScreen
import com.example.sound_app.ui.screens.search_tab.AudioSearchScreen
import com.example.sound_app.ui.theme.Sound_appTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

enum class Tab {
    SOUND, RECORD, FILE_VIEW, FILE_MERGE, PLAY
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var selectedTab by remember { mutableStateOf(Tab.SOUND) }

            Sound_appTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigationBar(selectedTab) { tab ->
                            selectedTab = tab
                        }
                    }
                ) { innerPadding ->
                    EnsureDirectoriesCreated(context = context)
                    RequestPermissions(context = context)

                    when (selectedTab) {
                        Tab.SOUND -> AudioSearchScreen(modifier = Modifier.padding(innerPadding))
                        Tab.RECORD -> AudioRecordScreen(modifier = Modifier.padding(innerPadding))
                        Tab.FILE_VIEW -> AudioFilesScreen(modifier = Modifier.padding(innerPadding))
                        Tab.FILE_MERGE -> AudioMergeScreen(modifier = Modifier.padding(innerPadding))
                        Tab.PLAY -> PlayMergeScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Tab, onTabSelected: (Tab) -> Unit) {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            label = { Text("Search") },
            selected = selectedTab == Tab.SOUND,
            onClick = { onTabSelected(Tab.SOUND) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.RecordVoiceOver, contentDescription = "Record") },
            label = { Text("Record") },
            selected = selectedTab == Tab.RECORD,
            onClick = { onTabSelected(Tab.RECORD) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.AudioFile, contentDescription = "Files") },
            label = { Text("Files") },
            selected = selectedTab == Tab.FILE_VIEW,
            onClick = { onTabSelected(Tab.FILE_VIEW) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Merge, contentDescription = "Files merging") },
            label = { Text("Merge") },
            selected = selectedTab == Tab.FILE_MERGE,
            onClick = { onTabSelected(Tab.FILE_MERGE) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.PlayCircleOutline, contentDescription = "Play merged Files") },
            label = { Text("Play") },
            selected = selectedTab == Tab.PLAY,
            onClick = { onTabSelected(Tab.PLAY) }
        )
    }
}

@Composable
fun RequestPermissions(context: Context) {
    val permissionsGranted = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    if (!permissionsGranted) {
        ActivityCompat.requestPermissions(
            context as ComponentActivity,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            1000
        )
    }
}

@Composable
fun EnsureDirectoriesCreated(context: Context) {
    val recordedFilesDirectory =
        File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recorded_audio")
    val downloadedFilesDirectory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "downloaded_audio"
    )

    if (!recordedFilesDirectory.exists()) recordedFilesDirectory.mkdirs()
    if (!downloadedFilesDirectory.exists()) downloadedFilesDirectory.mkdirs()
}
