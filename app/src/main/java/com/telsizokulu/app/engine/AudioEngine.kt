package com.telsizokulu.app.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * AudioEngine — Web'deki SesYoneticisi + WebAudioSynth'in Native Android karşılığı.
 *
 * Telsiz sürükleme sesi (squelch open) → TTS konuşma → Squelch close → Roger beep
 * akışını Android SoundPool + TextToSpeech API ile yönetir.
 */
class AudioEngine(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var tts: TextToSpeech? = null

    private var squelchOpenId: Int = 0
    private var squelchCloseId: Int = 0
    private var rogerBeepId: Int = 0
    private var dogruSesId: Int = 0
    private var yanliseSesId: Int = 0
    private var bolumSesId: Int = 0

    private var ttsHazir = false
    private var sesAcik = true

    init {
        initSoundPool()
        initTTS()
    }

    private fun initSoundPool() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()

        // Ses dosyalarını assets'ten yükle
        // Not: Bu dosyalar assets/sounds/ klasörüne yerleştirilmeli
        try {
            val afd1 = context.assets.openFd("sounds/squelch_open.wav")
            squelchOpenId = soundPool!!.load(afd1, 1)

            val afd2 = context.assets.openFd("sounds/squelch_close.wav")
            squelchCloseId = soundPool!!.load(afd2, 1)

            val afd3 = context.assets.openFd("sounds/roger_beep.wav")
            rogerBeepId = soundPool!!.load(afd3, 1)

            val afd4 = context.assets.openFd("sounds/dogru.wav")
            dogruSesId = soundPool!!.load(afd4, 1)

            val afd5 = context.assets.openFd("sounds/yanlis.wav")
            yanliseSesId = soundPool!!.load(afd5, 1)

            val afd6 = context.assets.openFd("sounds/bolum_tamamlandi.wav")
            bolumSesId = soundPool!!.load(afd6, 1)
        } catch (e: Exception) {
            // Ses dosyaları henüz yoksa sessizce devam et
            e.printStackTrace()
        }
    }

    private fun initTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsHazir = true
                // İngilizce erkek sesi tercih et (NATO kelimeler için)
                tts?.language = Locale.US
                tts?.setSpeechRate(0.82f)
                tts?.setPitch(0.88f)
            }
        }
    }

    /**
     * NATO sesi çal: Squelch open → TTS → Squelch close → Roger beep
     * Web'deki SesYoneticisi.oynat() ile birebir aynı akış.
     */
    fun oynatNATO(kelime: String) {
        if (!sesAcik) return
        // Squelch open (PTT bas → cızırtı)
        RadioSfx.squelchOpen()

        // ~150ms bekle sonra TTS'i başlat (altta sürekli statik bed)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            RadioSfx.statikBaslat()
            konus(kelime, onBitti = {
                // TTS bitince: statik dur + squelch close + roger beep
                RadioSfx.statikDurdur()
                RadioSfx.squelchClose()
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    RadioSfx.rogerBeep()
                }, 90)
            })
        }, 160)
    }

    /**
     * Efekt seslerini çal — Web'deki SesYoneticisi.oynatEfekt() karşılığı
     */
    fun oynatEfekt(tip: String) {
        if (!sesAcik) return
        when (tip) {
            "dogru"   -> RadioSfx.dogru()
            "yanlis"  -> RadioSfx.yanlis()
            "bolum", "rozet" -> RadioSfx.bolum()
        }
    }

    /**
     * Metni telsiz efektiyle seslendir (Türkçe veya İngilizce TTS)
     */
    fun seslendir(text: String, lang: String = "en-US") {
        if (!sesAcik || !ttsHazir) return
        val locale = if (lang.startsWith("tr", ignoreCase = true)) Locale("tr", "TR") else Locale.US

        // Squelch open (PTT cızırtısı)
        RadioSfx.squelchOpen()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            RadioSfx.statikBaslat()
            konus(text, locale, onBitti = {
                RadioSfx.statikDurdur()
                RadioSfx.squelchClose()
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    RadioSfx.rogerBeep()
                }, 90)
            })
        }, 160)
    }

    /**
     * Metni seslendir (Android TTS)
     */
    private fun konus(text: String, lang: Locale = Locale.US, onBitti: (() -> Unit)? = null) {
        if (!ttsHazir) return
        tts?.language = lang
        val utteranceId = "telsiz_${System.currentTimeMillis()}"

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onBitti?.invoke()
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onBitti?.invoke()
                }
            }
        })

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun sesAcKapa(): Boolean {
        sesAcik = !sesAcik
        if (!sesAcik) {
            tts?.stop()
            RadioSfx.statikDurdur()
        }
        return sesAcik
    }

    fun sesDurumu(): Boolean = sesAcik

    fun release() {
        RadioSfx.statikDurdur()
        tts?.stop()
        tts?.shutdown()
        soundPool?.release()
    }
}
