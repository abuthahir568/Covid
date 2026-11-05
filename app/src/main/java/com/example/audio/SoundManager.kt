package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(private val context: Context) {
  var isSoundEnabled: Boolean = true
  var isVibrationEnabled: Boolean = true

  private val scope = CoroutineScope(Dispatchers.Default)

  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  fun playChomp() {
    if (!isSoundEnabled) return
    scope.launch {
      // 8-bit chomp: rapid pitch blip
      generateTone(startFreq = 400.0, endFreq = 950.0, durationMs = 70, volume = 0.5f)
    }
    vibrate(30)
  }

  fun playYum() {
    if (!isSoundEnabled) return
    scope.launch {
      // Ascending musical chime
      generateTone(startFreq = 523.25, endFreq = 659.25, durationMs = 90, volume = 0.6f)
      generateTone(startFreq = 659.25, endFreq = 783.99, durationMs = 90, volume = 0.6f)
      generateTone(startFreq = 783.99, endFreq = 1046.50, durationMs = 120, volume = 0.7f)
    }
    vibrate(60)
  }

  fun playDash() {
    if (!isSoundEnabled) return
    scope.launch {
      // Whoosh sound: downward slide with noise/overtones
      generateTone(startFreq = 880.0, endFreq = 320.0, durationMs = 140, volume = 0.5f)
    }
    vibrate(45)
  }

  fun playHurt() {
    if (!isSoundEnabled) return
    scope.launch {
      // Descending buzz
      generateTone(startFreq = 280.0, endFreq = 110.0, durationMs = 220, volume = 0.7f, squareWave = true)
    }
    vibrate(180)
  }

  fun playGameOver() {
    if (!isSoundEnabled) return
    scope.launch {
      generateTone(startFreq = 440.0, endFreq = 392.0, durationMs = 150, volume = 0.6f)
      generateTone(startFreq = 392.0, endFreq = 349.0, durationMs = 150, volume = 0.6f)
      generateTone(startFreq = 349.0, endFreq = 261.0, durationMs = 300, volume = 0.7f, squareWave = true)
    }
    vibrate(250)
  }

  fun playClick() {
    if (!isSoundEnabled) return
    scope.launch {
      generateTone(startFreq = 600.0, endFreq = 750.0, durationMs = 35, volume = 0.4f)
    }
    vibrate(15)
  }

  fun playCarHonk() {
    if (!isSoundEnabled) return
    scope.launch {
      // Dual-tone classic car horn: BEEP-BEEP!
      generateTone(startFreq = 440.0, endFreq = 440.0, durationMs = 80, volume = 0.6f)
      generateTone(startFreq = 554.37, endFreq = 554.37, durationMs = 120, volume = 0.6f)
    }
    vibrate(50)
  }

  fun playCrunch() {
    if (!isSoundEnabled) return
    scope.launch {
      // Deep satisfying bite/crunch
      generateTone(startFreq = 350.0, endFreq = 180.0, durationMs = 85, volume = 0.7f, squareWave = true)
    }
    vibrate(40)
  }

  fun playBusVroom() {
    if (!isSoundEnabled) return
    scope.launch {
      generateTone(startFreq = 120.0, endFreq = 220.0, durationMs = 140, volume = 0.5f, squareWave = true)
    }
    vibrate(60)
  }

  private fun vibrate(ms: Long) {
    if (!isVibrationEnabled || vibrator == null) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(ms)
      }
    } catch (_: Exception) {}
  }

  private fun generateTone(
    startFreq: Double,
    endFreq: Double,
    durationMs: Int,
    volume: Float = 0.5f,
    squareWave: Boolean = false
  ) {
    val sampleRate = 22050
    val numSamples = (sampleRate * durationMs) / 1000
    val buffer = ShortArray(numSamples)

    var phase = 0.0
    for (i in 0 until numSamples) {
      val progress = i.toDouble() / numSamples
      val freq = startFreq + (endFreq - startFreq) * progress
      val phaseIncrement = 2.0 * PI * freq / sampleRate
      phase += phaseIncrement

      val envelope = if (i > numSamples - (numSamples / 5)) {
        // fade out
        (numSamples - i).toDouble() / (numSamples / 5)
      } else {
        1.0
      }

      val sampleValue = if (squareWave) {
        if (sin(phase) >= 0) 1.0 else -1.0
      } else {
        sin(phase)
      }

      val sample = (sampleValue * Short.MAX_VALUE * volume * envelope).toInt()
      buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }

    try {
      val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
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
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      audioTrack.write(buffer, 0, buffer.size)
      audioTrack.play()
      // Release track when done
      Thread.sleep(durationMs.toLong() + 30)
      audioTrack.stop()
      audioTrack.release()
    } catch (_: Exception) {}
  }
}
