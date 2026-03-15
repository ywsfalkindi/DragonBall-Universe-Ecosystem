package com.saiyan.dragonballuniverse

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import coil.Coil
import coil.ImageLoader
import com.google.firebase.messaging.FirebaseMessaging
import com.saiyan.dragonballuniverse.BuildConfig
import com.saiyan.dragonballuniverse.manga.MangaViewModel
import com.saiyan.dragonballuniverse.network.UnsafeOkHttp
import com.saiyan.dragonballuniverse.quiz.QuizViewModel
import com.saiyan.dragonballuniverse.ui.home.DragonBallScaffold
import com.saiyan.dragonballuniverse.ui.theme.DragonBallUniverseTheme
import com.saiyan.dragonballuniverse.ui.theme.GokuOrange
import com.saiyan.dragonballuniverse.ui.theme.VegetaBlue

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val quizViewModel: QuizViewModel by viewModels()
    private val mangaViewModel: MangaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("APP_DEBUG", "Checkpoint 1: MainActivity onCreate started")
        enableEdgeToEdge()
        // DEBUG-ONLY: Make Coil trust self-signed/untrusted TLS for Manga page images.
        //
        // NOTE: Do NOT install this as a global Coil ImageLoader. Doing so may impact other
        // parts of the app and can trigger unexpected platform behavior on some devices.
        //
        // Manga screens should use a dedicated Coil ImageLoader / OkHttp client instead.
        //
        // DEBUG-ONLY: Optional helper to make Coil trust self-signed/untrusted TLS for Manga page images.
        // Keeping this disabled by default; enable ONLY when needed for local/dev servers.
        //
        // if (BuildConfig.DEBUG) {
        //     val imageLoader =
        //         ImageLoader.Builder(applicationContext)
        //             .okHttpClient(UnsafeOkHttp.create())
        //             .build()
        //     Coil.setImageLoader(imageLoader)
        // }

        Log.d("APP_DEBUG", "Checkpoint 2: About to call setContent")
        setContent {
            Log.d("APP_DEBUG", "Checkpoint 3: Inside setContent")
            // Android 13+ requires runtime notification permission.
            val notificationPermissionLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { /* no-op */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted =
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_GRANTED

                    if (!granted) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Subscribe to manga updates topic (so dashboard pushes reach this device).
                FirebaseMessaging.getInstance().subscribeToTopic("new_chapters")
            }

            DragonBallUniverseTheme {
                Log.d("APP_DEBUG", "Checkpoint 4: Calling DragonBallScaffold")
                DragonBallScaffold(
                    viewModel = mainViewModel,
                    quizViewModel = quizViewModel,
                    mangaViewModel = mangaViewModel,
                )
            }
        }
    }
}
