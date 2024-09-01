package com.example.sound_app.ui.screens.recording_tab

import android.content.Context
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun AudioRecordScreen(modifier: Modifier = Modifier) {
    var isRecording by remember { mutableStateOf(false) }
    var audioFilePath by remember { mutableStateOf<String?>(null) }
    var recordingTime by remember { mutableStateOf(0) }
    var visualizerData by remember { mutableStateOf<FloatArray?>(null) }
    val context = LocalContext.current

    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    val audioVisualizer = remember { AudioVisualizer(context) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            // Use Scoped Storage directory
            val directory =
                File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recorded_audio")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val audioFile = File(
                directory,
                "audio_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp3"
            )
            audioFilePath = audioFile.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFile.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                    start()
                    // Start visualizer
                    audioVisualizer.start()
                    // Update recording time and visualizer data
                    while (isRecording) {
                        delay(1000L)
                        recordingTime += 1
                        visualizerData = audioVisualizer.getData()
                    }
                    // Stop visualizer
                    audioVisualizer.stop()
                } catch (e: Exception) {
                    Log.e("AudioRecordScreen", "Recording failed", e)
                }
            }
        } else {
            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                    scanFile(context, File(audioFilePath!!))
                } catch (e: Exception) {
                    Log.e("AudioRecordScreen", "Stopping recording failed", e)
                }
            }
            mediaRecorder = null
        }
    }

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(16.dp)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle with recording time
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), shape = CircleShape)
                    .padding(8.dp)
            ) {
                Text(
                    text = formatRecordingTime(recordingTime),
                    fontSize = 28.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Recording Now" text
            if (isRecording) {
                Text(
                    text = "Recording Now",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Improved Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.DarkGray.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            ) {
                visualizerData?.let { data ->
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawImprovedVisualizer(data)
                    }
                }
            }
        }

        // Start and Stop Buttons at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { isRecording = !isRecording },
                enabled = !isRecording,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Blue, CircleShape)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Start Recording",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(
                onClick = { isRecording = !isRecording },
                enabled = isRecording,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Red, CircleShape)
            ) {
                Icon(
                    Icons.Filled.Stop,
                    contentDescription = "Stop Recording",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

fun formatRecordingTime(seconds: Int): String {
    val minutes = (seconds / 60).toString().padStart(2, '0')
    val secs = (seconds % 60).toString().padStart(2, '0')
    return "$minutes:$secs"
}

fun DrawScope.drawImprovedVisualizer(data: FloatArray) {
    val width = size.width
    val height = size.height
    val barWidth = width / data.size
    val maxBarHeight = height * 0.8f

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = Color(0xFF00E5FF)  // Bright cyan color
            strokeWidth = 2.dp.toPx() // Thinner line
            strokeCap = StrokeCap.Round
        }

        // Ensure the visualizer is contained within the Canvas
        val halfHeight = height / 2
        val maxWidth = width - barWidth

        data.forEachIndexed { index, value ->
            val barHeight = (value * maxBarHeight).coerceIn(0f, maxBarHeight)
            val x = (index * barWidth).coerceIn(0f, maxWidth)
            val y = halfHeight - barHeight / 2

            // Draw a line connecting to the next data point
            if (index < data.size - 1) {
                val nextX = ((index + 1) * barWidth).coerceIn(0f, maxWidth)
                val nextBarHeight = ((data[index + 1] * maxBarHeight).coerceIn(0f, maxBarHeight))
                val nextY = halfHeight - nextBarHeight / 2

                canvas.drawLine(
                    p1 = Offset(x, y),
                    p2 = Offset(nextX, nextY),
                    paint = paint
                )
            }
        }
    }
}

fun scanFile(context: Context, file: File) {
    MediaScannerConnection.scanFile(
        context,
        arrayOf(file.absolutePath),
        null
    ) { path, uri ->
        Log.i("AudioRecordScreen", "Scanned $path:")
        Log.i("AudioRecordScreen", "-> uri=$uri")
    }
}
