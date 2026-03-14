package com.saiyan.dragonballuniverse.ui.utils

import com.saiyan.dragonballuniverse.ui.anime.DEFAULT_DBZ_COVER_URL

/**
 * Resolves an image URL:
 * - trims input
 * - falls back to [fallback] when blank/null
 * - upgrades "http://" to "https://"
 */
fun String?.resolveImageUrl(
    fallback: String = DEFAULT_DBZ_COVER_URL
): String {
    val trimmed = this?.trim().orEmpty()
    val chosen = if (trimmed.isBlank()) fallback else trimmed
    return if (chosen.startsWith("http://")) {
        "https://${chosen.removePrefix("http://")}"
    } else {
        chosen
    }
}
