package com.telsizokulu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.telsizokulu.app.engine.AudioEngine
import com.telsizokulu.app.ui.navigation.AppNavGraph
import com.telsizokulu.app.ui.theme.Slate950
import com.telsizokulu.app.ui.theme.TelsizOkuluTheme

class MainActivity : ComponentActivity() {

    private lateinit var audioEngine: AudioEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioEngine = AudioEngine(applicationContext)

        setContent {
            TelsizOkuluTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Slate950)
                ) {
                    AppNavGraph(audioEngine = audioEngine)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
    }
}
