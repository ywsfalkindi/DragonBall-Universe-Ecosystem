package com.saiyan.dragonballuniverse.ui.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Bounce click + Haptic feedback.
 * - onPress: scale to 0.95f + haptic
 * - onRelease: scale back to 1f then trigger onClick
 *
 * Notes about recomposition:
 * - `pressed` is kept inside the `composed {}` block so it is scoped to the modifier.
 * - We avoid reading any unrelated state to prevent unnecessary recompositions.
 */
internal fun Modifier.bounceClick(
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 140, easing = LinearEasing),
        label = "bounceScale"
    )

    // Allocation note:
    // - `pointerInput` creates a coroutine scope; in large Lazy lists this can add churn.
    // - Using `composed {}` + `remember` makes the gesture modifier stable per item,
    //   so recomposition doesn't recreate the input handler.
    val gestureModifier =
        remember(onClick, haptic) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        val released = tryAwaitRelease()
                        pressed = false

                        if (released) onClick()
                    }
                )
            }
        }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(gestureModifier)
}
