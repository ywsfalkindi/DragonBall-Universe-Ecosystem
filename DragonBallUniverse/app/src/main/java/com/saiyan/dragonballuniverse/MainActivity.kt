package com.saiyan.dragonballuniverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.saiyan.dragonballuniverse.manga.MangaViewModel
import com.saiyan.dragonballuniverse.quiz.QuizViewModel
import com.saiyan.dragonballuniverse.ui.home.DragonBallScaffold
import coil.Coil
import coil.ImageLoader
import com.saiyan.dragonballuniverse.BuildConfig
import com.saiyan.dragonballuniverse.network.UnsafeOkHttp
import com.saiyan.dragonballuniverse.ui.theme.DragonBallUniverseTheme
import com.saiyan.dragonballuniverse.ui.theme.GokuOrange
import com.saiyan.dragonballuniverse.ui.theme.VegetaBlue

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val quizViewModel: QuizViewModel by viewModels()
    private val mangaViewModel: MangaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // DEBUG-ONLY: Make Coil trust self-signed/untrusted TLS for Manga page images.
        //
        // NOTE: Do NOT install this as a global Coil ImageLoader. Doing so may impact other
        // parts of the app and can trigger unexpected platform behavior on some devices.
        //
        // Manga screens should use a dedicated Coil ImageLoader / OkHttp client instead.
        //
        // Keeping this block intentionally disabled for now:
        // if (BuildConfig.DEBUG) { ... }
        if (false && BuildConfig.DEBUG) {
            val imageLoader =
                ImageLoader.Builder(applicationContext)
                    .okHttpClient(UnsafeOkHttp.create())
                    .build()
            Coil.setImageLoader(imageLoader)
        }

        setContent {
            DragonBallUniverseTheme {
                DragonBallScaffold(
                    viewModel = mainViewModel,
                    quizViewModel = quizViewModel,
                    mangaViewModel = mangaViewModel,
                )
            }
        }
    }
}
