package com.example.blunt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.blunt.data.AppContainer
import com.example.blunt.navigation.BluntApp
import com.example.blunt.ui.theme.BluntTheme

class MainActivity : ComponentActivity() {
    private val app by lazy { (application as BluntApplication).container }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluntTheme {
                BluntApp(app)
            }
        }
    }
}

class BluntApplication : android.app.Application() {
    val container by lazy { AppContainer(this) }
}
