package com.telsizokulu.app.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

/**
 * Telsiz ses efektlerini saf kodla üretir (PCM 16-bit mono) ve AudioTrack ile çalar.
 * Ses dosyası (wav/mp3) gerektirmez — squelch, statik parazit, roger beep, tonlar.
 */
object RadioSfx {

    private const val SR = 22050

    // ── Üreticiler ────────────────────────────────────────────────

    private fun tone(
        freq: Double, ms: Int, vol: Double = 0.55,
        attackMs: Int = 4, releaseMs: Int = 18
    ): ShortArray {
        val n = SR * ms / 1000
        val out = ShortArray(n)
        val att = (SR * attackMs / 1000).coerceAtLeast(1)
        val rel = (SR * releaseMs / 1000).coerceAtLeast(1)
        for (i in 0 until n) {
            var env = 1.0
            if (i < att) env = i.toDouble() / att
            if (i > n - rel) env = ((n - i).toDouble() / rel).coerceIn(0.0, 1.0)
            val s = sin(2 * PI * freq * i / SR) * vol * env
            out[i] = (s * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun noise(ms: Int, vol: Double = 0.4, fadeOut: Boolean = true): ShortArray {
        val n = SR * ms / 1000
        val out = ShortArray(n)
        val rnd = Random()
        val att = (SR * 3 / 1000).coerceAtLeast(1)
        val rel = (n * 0.6).toInt().coerceAtLeast(1)
        var last = 0.0
        for (i in 0 until n) {
            var env = 1.0
            if (i < att) env = i.toDouble() / att
            if (fadeOut && i > n - rel) env = ((n - i).toDouble() / rel).coerceIn(0.0, 1.0)
            // hafif alçak geçiren → "tıs" yerine "şşş" (telsiz cızırtısı)
            val white = rnd.nextDouble() * 2 - 1
            last = last * 0.45 + white * 0.55
            out[i] = (last * vol * env * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun concat(vararg arrs: ShortArray): ShortArray {
        val out = ShortArray(arrs.sumOf { it.size })
        var p = 0
        for (a in arrs) { a.copyInto(out, p); p += a.size }
        return out
    }

    // ── Efekt reçeteleri ──────────────────────────────────────────

    private val squelchOpen by lazy { noise(55, 0.5, fadeOut = false) }
    private val squelchClose by lazy { concat(noise(45, 0.45), tone(180.0, 25, 0.2)) }
    private val rogerBeep by lazy { tone(1500.0, 95, 0.5) }
    private val dogru by lazy { concat(tone(880.0, 80, 0.5), tone(1318.0, 130, 0.55)) }
    private val yanlis by lazy { concat(tone(392.0, 120, 0.5), tone(294.0, 200, 0.5)) }
    private val bolum by lazy {
        concat(
            tone(523.0, 90, 0.5), tone(659.0, 90, 0.5),
            tone(784.0, 90, 0.5), tone(1047.0, 220, 0.6)
        )
    }

    // ── Çalma ─────────────────────────────────────────────────────

    private fun play(samples: ShortArray, volume: Float = 1f) {
        Thread {
            try {
                val bytes = samples.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SR)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bytes.coerceAtLeast(512))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.setVolume(volume)
                track.play()
                Thread.sleep(samples.size * 1000L / SR + 80)
                track.stop()
                track.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    // ── Sürekli statik bed (TTS sırasında) ───────────────────────

    private var bedTrack: AudioTrack? = null

    fun statikBaslat(vol: Float = 0.12f) {
        if (bedTrack != null) return
        try {
            val samples = noise(1000, 0.5, fadeOut = false)
            val t = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SR)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes((samples.size * 2).coerceAtLeast(512))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            t.write(samples, 0, samples.size)
            t.setLoopPoints(0, samples.size, -1)
            t.setVolume(vol)
            t.play()
            bedTrack = t
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun statikDurdur() {
        try {
            bedTrack?.stop()
            bedTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bedTrack = null
    }

    fun squelchOpen(vol: Float = 0.6f) = play(squelchOpen, vol)
    fun squelchClose(vol: Float = 0.5f) = play(squelchClose, vol)
    fun rogerBeep(vol: Float = 0.45f) = play(rogerBeep, vol)
    fun dogru(vol: Float = 0.7f) = play(dogru, vol)
    fun yanlis(vol: Float = 0.7f) = play(yanlis, vol)
    fun bolum(vol: Float = 0.7f) = play(bolum, vol)
}
