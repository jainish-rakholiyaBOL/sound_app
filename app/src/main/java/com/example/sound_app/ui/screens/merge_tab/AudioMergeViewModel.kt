package com.example.sound_app.ui.screens.merge_tab

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.mobileffmpeg.FFmpeg
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class AudioFilesMergeViewModel @Inject constructor(@ApplicationContext val context: Context) :
    ViewModel() {

    private val recordedFilesDirectory: File
    private val downloadedFilesDirectory: File
    private val mergedFilesDirectory: File

    init {
        recordedFilesDirectory =
            File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recorded_audio")
        downloadedFilesDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "downloaded_audio"
        )
        mergedFilesDirectory = File(recordedFilesDirectory.parentFile, "merged_audio")

        // Ensure the directories exist
        if (!recordedFilesDirectory.exists()) {
            recordedFilesDirectory.mkdirs()
        }
        if (!downloadedFilesDirectory.exists()) {
            downloadedFilesDirectory.mkdirs()
        }
        if (!mergedFilesDirectory.exists()) {
            mergedFilesDirectory.mkdirs()
        }
    }

    fun getRecordedFiles(): List<File> {
        return recordedFilesDirectory.listFiles()?.toList() ?: emptyList()
    }

    fun getDownloadedFiles(): List<File> {
        return downloadedFilesDirectory.listFiles()?.toList() ?: emptyList()
    }

    fun getMergedFiles(): List<File> {
        return mergedFilesDirectory.listFiles()?.toList() ?: emptyList()
    }

    fun mergeFiles(recordedFile: File?, downloadedFile: File?) {
        if (recordedFile == null || downloadedFile == null) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Create a merged output file
                val outputFile =
                    File(mergedFilesDirectory, "merged_${recordedFile.nameWithoutExtension}_${downloadedFile.nameWithoutExtension}.mp3")
                val command = arrayOf(
                    "-y",
                    "-i", recordedFile.absolutePath,
                    "-i", downloadedFile.absolutePath,
                    "-filter_complex", "[0:0][1:0]amix=inputs=2:duration=longest",
                    "-c:a", "libmp3lame",
                    outputFile.absolutePath
                )
                val result = withContext(Dispatchers.IO) {
                    FFmpeg.execute(command)
                }
                withContext(Dispatchers.Main) {
                    if (result == 0) {
                        // Success
                        Toast.makeText(context, "Audio files merged successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Failure
                        Toast.makeText(context, "Failed to merge audio files", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}