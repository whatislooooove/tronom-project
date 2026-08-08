package com.example.drummersmetronome.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class MetronomeEngine(private val scope: CoroutineScope) {
    private var _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private var _bpm = MutableStateFlow(120)
    val bpm = _bpm.asStateFlow()

    private var metronomeJob: Job? = null
    
    // Programmatic click sound generation
    private val sampleRate = 44100
    private val clickDurationMs = 20
    private val frequency = 1000.0
    private val audioData: ShortArray = generateClick()

    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(audioData.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build().apply {
            write(audioData, 0, audioData.size)
            setLoopPoints(0, audioData.size, 0)
        }

    fun toggle() {
        if (_isPlaying.value) {
            stop()
        } else {
            start()
        }
    }

    fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(20, 300)
    }

    private fun start() {
        _isPlaying.value = true
        metronomeJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                playClick()
                val delayMs = (60_000.0 / _bpm.value).toLong()
                delay(delayMs)
            }
        }
    }

    private fun stop() {
        _isPlaying.value = false
        metronomeJob?.cancel()
        metronomeJob = null
    }

    private fun playClick() {
        audioTrack.stop()
        audioTrack.reloadStaticData()
        audioTrack.play()
    }

    private fun generateClick(): ShortArray {
        val numSamples = (sampleRate * clickDurationMs / 1000.0).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            // Sine wave with fade out
            val envelope = (numSamples - i).toDouble() / numSamples
            buffer[i] = (sin(2.0 * PI * i * frequency / sampleRate) * Short.MAX_VALUE * envelope).toInt().toShort()
        }
        return buffer
    }
}
