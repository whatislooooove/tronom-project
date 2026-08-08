package com.example.drummersmetronome.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class MetronomeEngine(private val scope: CoroutineScope) {
    private val sampleRate = 44100
    private var _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private var _bpm = MutableStateFlow(120)
    val bpm = _bpm.asStateFlow()

    private var _numerator = MutableStateFlow(4)
    val numerator = _numerator.asStateFlow()

    private var _denominator = MutableStateFlow(4)
    val denominator = _denominator.asStateFlow()

    // Increased buffer capacity to ensure no events are lost
    private val _beatEvent = MutableSharedFlow<Int>(extraBufferCapacity = 64)
    val beatEvent = _beatEvent.asSharedFlow()

    private var playbackJob: Job? = null

    // Prepare audio track for streaming as Media/Music
    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT))
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    private val clickNormal = generateClick(1000.0, 0.02)
    private val clickAccent = generateClick(1500.0, 0.03)

    fun toggle() {
        if (_isPlaying.value) stop() else start()
    }

    fun setBpm(value: Int) { _bpm.value = value.coerceIn(20, 300) }
    fun setSignature(num: Int, den: Int) {
        _numerator.value = num.coerceIn(1, 16)
        _denominator.value = den.coerceIn(1, 16)
    }

    private fun start() {
        _isPlaying.value = true
        audioTrack.play()
        playbackJob = scope.launch(Dispatchers.Default) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            
            var currentBeat = 0
            
            // Small initial delay to ensure UI collectors are ready
            kotlinx.coroutines.delay(50)
            
            while (isActive) {
                val bpmVal = _bpm.value
                val numVal = _numerator.value
                val denVal = _denominator.value

                // Sample-accurate calculation
                val samplesPerBeat = ((60.0 / bpmVal) * (4.0 / denVal) * sampleRate).toInt()
                val click = if (currentBeat == 0) clickAccent else clickNormal
                val buffer = ShortArray(samplesPerBeat)
                
                for (i in buffer.indices) {
                    if (i < click.size) buffer[i] = click[i] else buffer[i] = 0
                }

                // First emit the event so UI flashes immediately
                _beatEvent.emit(currentBeat)
                
                // Then write to buffer (this blocks until space is available)
                audioTrack.write(buffer, 0, buffer.size)
                
                currentBeat = (currentBeat + 1) % numVal
            }
        }
    }

    private fun stop() {
        _isPlaying.value = false
        playbackJob?.cancel()
        audioTrack.pause()
        audioTrack.flush()
    }

    private fun generateClick(freq: Double, durationSec: Double): ShortArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val envelope = (numSamples - i).toDouble() / numSamples
            buffer[i] = (sin(2.0 * PI * i * freq / sampleRate) * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
        }
        return buffer
    }
}
