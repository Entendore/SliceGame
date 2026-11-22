package com.example.slicinggame

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream

class SoundManager(context: Context) {

    private val soundPool: SoundPool
    private var sliceSoundId = 0
    private var winSoundId = 0
    private var loaded = false
    private val ctx = context.applicationContext

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = true
        }

        sliceSoundId = soundPool.load(createSliceWav(), 1)
        winSoundId = soundPool.load(createWinWav(), 1)
    }

    fun playSlice() { if (loaded && Settings.soundEnabled) soundPool.play(sliceSoundId, 1f, 1f, 1, 0, 1.2f) }
    fun playWin()   { if (loaded && Settings.soundEnabled) soundPool.play(winSoundId, 1f, 1f, 1, 0, 1f) }

    fun release() { soundPool.release() }

    private fun createSliceWav(): String {
        val file = File(ctx.cacheDir, "slice.wav")
        if (file.exists()) return file.absolutePath
        val sampleRate = 22050
        val durationMs = 180
        val numSamples = sampleRate * durationMs / 1000
        val data = ByteArray(numSamples * 2)
        val rng = java.util.Random(42)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val env = (1f - t) * (1f - t)
            val noise = (rng.nextFloat() * 2f - 1f)
            val sweep = kotlin.math.sin(2 * Math.PI * (800 - 400 * t) * i / sampleRate)
            val sample = ((noise * 0.6 + sweep * 0.4) * env * 0.6 * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            val s = sample.toShort()
            data[i * 2] = (s.toInt() and 0xFF).toByte()
            data[i * 2 + 1] = (s.toInt() shr 8).toByte()
        }
        writeWav(file, data, sampleRate)
        return file.absolutePath
    }

    private fun createWinWav(): String {
        val file = File(ctx.cacheDir, "win.wav")
        if (file.exists()) return file.absolutePath
        val sampleRate = 22050
        val durationMs = 600
        val numSamples = sampleRate * durationMs / 1000
        val data = ByteArray(numSamples * 2)
        val notes = floatArrayOf(523f, 659f, 784f, 1047f)
        val noteLen = numSamples / notes.size
        for (i in 0 until numSamples) {
            val noteIdx = (i / noteLen).coerceAtMost(notes.size - 1)
            val freq = notes[noteIdx]
            val localT = (i % noteLen).toFloat() / noteLen
            val env = (1f - localT) * 0.8f
            val sample = kotlin.math.sin(2 * Math.PI * freq * i / sampleRate) * env
            val s = (sample * Short.MAX_VALUE * 0.7f).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            data[i * 2] = (s.toInt() and 0xFF).toByte()
            data[i * 2 + 1] = (s.toInt() shr 8).toByte()
        }
        writeWav(file, data, sampleRate)
        return file.absolutePath
    }

    private fun writeWav(file: File, pcm: ByteArray, sampleRate: Int) {
        FileOutputStream(file).use { out ->
            val dataSize = pcm.size
            val totalSize = 36 + dataSize
            out.write("RIFF".toByteArray())
            out.write(intToBytes(totalSize))
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(intToBytes(16))
            out.write(shortToBytes(1))
            out.write(shortToBytes(1))
            out.write(intToBytes(sampleRate))
            out.write(intToBytes(sampleRate * 2))
            out.write(shortToBytes(2))
            out.write(shortToBytes(16))
            out.write("data".toByteArray())
            out.write(intToBytes(dataSize))
            out.write(pcm)
        }
    }

    private fun intToBytes(v: Int) = byteArrayOf(
        (v and 0xFF).toByte(), (v shr 8 and 0xFF).toByte(),
        (v shr 16 and 0xFF).toByte(), (v shr 24 and 0xFF).toByte()
    )
    private fun shortToBytes(v: Int) = byteArrayOf(
        (v and 0xFF).toByte(), (v shr 8 and 0xFF).toByte()
    )
}
