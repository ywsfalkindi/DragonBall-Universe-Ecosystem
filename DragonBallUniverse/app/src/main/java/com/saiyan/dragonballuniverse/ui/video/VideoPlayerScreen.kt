package com.saiyan.dragonballuniverse.ui.video

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoPlayerScreen(
    episodeId: String,
    videoUrl: String,
    onPlayNext: (String, String) -> Unit,
    onBack: () -> Unit,
    getSavedProgressMs: suspend (String) -> Long?,
    saveWatchProgress: (String, Long) -> Unit
) {
    if (videoUrl.isBlank()) {
        onBack()
        return
    }

    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(activity) {
        val job: Job? =
            if (activity == null) {
                null
            } else {
                kotlinx.coroutines.CoroutineScope(Dispatchers.Main.immediate).launch {
                    delay(200)
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                }
            }

        onDispose {
            job?.cancel()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Smart Resume: load progress once per episode (no DB access inside UI)
    val savedProgressMs by produceState<Long?>(initialValue = null, key1 = episodeId) {
        value = getSavedProgressMs(episodeId)
    }

    val player = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    // Seek once we have saved progress
    LaunchedEffect(savedProgressMs, player) {
        val ms = savedProgressMs
        if (ms != null && ms > 0L) {
            player.seekTo(ms)
        }
    }

    // Auto play next when ended
    DisposableEffect(player, onPlayNext, videoUrl, episodeId) {
        val listener =
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onPlayNext(episodeId, videoUrl)
                    }
                }
            }

        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    // Save watch progress on player state changes (pause/buffer/idle/end)
    // and also safely on dispose (leaving screen / switching episode).
    val lastSavedPositionMs = remember(episodeId) { mutableLongStateOf(-1L) }
    DisposableEffect(player, episodeId, saveWatchProgress) {
        fun saveIfChanged() {
            val pos = player.currentPosition
            if (pos != lastSavedPositionMs.longValue) {
                lastSavedPositionMs.longValue = pos
                saveWatchProgress(episodeId, pos)
            }
        }

        val listener =
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    // Save when playback transitions to paused/stopped.
                    if (!isPlaying) saveIfChanged()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    // Save on state transitions that often occur during user interactions
                    // (pausing, leaving app, playback ended, etc.)
                    if (playbackState != Player.STATE_READY) saveIfChanged()
                }
            }

        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            saveIfChanged()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "رجوع",
                tint = Color.White
            )
        }
    }
}
