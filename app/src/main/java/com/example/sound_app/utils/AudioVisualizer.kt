package com.example.sound_app.ui.screens.recording_tab

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlin.math.absoluteValue
import kotlin.math.log10

class AudioVisualizer(context: Context) {

    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    @SuppressLint("MissingPermission")
    private val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)

    init {
        audioRecord.startRecording()
    }

    fun getData(): FloatArray {
        val buffer = ShortArray(bufferSize)
        audioRecord.read(buffer, 0, bufferSize)
        return calculateAmplitude(buffer)
    }

    private fun calculateAmplitude(buffer: ShortArray): FloatArray {
        val amplitudes = FloatArray(buffer.size)
        buffer.forEachIndexed { index, value ->
            amplitudes[index] = log10(value.toDouble().absoluteValue + 1).toFloat()
        }
        return amplitudes
    }

    fun start() {
        audioRecord.startRecording()
    }

    fun stop() {
        audioRecord.stop()
    }
}
