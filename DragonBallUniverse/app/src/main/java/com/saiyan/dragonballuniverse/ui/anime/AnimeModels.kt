package com.saiyan.dragonballuniverse.ui.anime

import androidx.compose.runtime.saveable.Saver as ComposeSaver

const val DEFAULT_DBZ_COVER_URL: String =
    "https://j.top4top.io/p_3722xahg41.jpg"

data class Episode(
    val number: Int,
    val title: String,
    val duration: String,
    val imageUrl: String = DEFAULT_DBZ_COVER_URL,
    val progress: Float = 0f,
    val id: String = number.toString(),
)

data class AnimeSeason(
    val title: String,
    val year: String,
    val description: String,
    val episodes: List<Episode>,
    val imageUrl: String = DEFAULT_DBZ_COVER_URL,
    val status: String? = null
)

data class MangaChapter(
    val number: String,
    val title: String,
    val date: String,
    val isRead: Boolean = false
)

data class Manga(
    val title: String,
    val description: String,
    val chapters: List<MangaChapter>
)

val EpisodeSaver: ComposeSaver<Episode, List<Any?>> =
    ComposeSaver(
        save = { episode ->
            listOf(
                episode.number,
                episode.title,
                episode.duration,
                episode.imageUrl,
                episode.progress,
                episode.id
            )
        },
        restore = { saved ->
            val number = saved.getOrElse(0) { null } as? Int ?: return@ComposeSaver null
            val title = saved.getOrElse(1) { null } as? String ?: return@ComposeSaver null
            val duration = saved.getOrElse(2) { null } as? String ?: return@ComposeSaver null
            val imageUrl = saved.getOrElse(3) { "" } as? String ?: ""
            val progress = saved.getOrElse(4) { 0f } as? Float ?: 0f
            val id = saved.getOrElse(5) { number.toString() } as? String ?: number.toString()
            Episode(
                number = number,
                title = title,
                duration = duration,
                imageUrl = imageUrl.ifBlank { DEFAULT_DBZ_COVER_URL },
                progress = progress.coerceIn(0f, 1f),
                id = id
            )
        }
    )

val AnimeSeasonSaver: ComposeSaver<AnimeSeason, List<Any?>> =
    ComposeSaver(
        save = { season ->
            val episodesSaved = ArrayList<List<Any?>>(season.episodes.size)
            season.episodes.forEach { ep ->
                episodesSaved.add(with(EpisodeSaver) { save(ep) } ?: return@ComposeSaver null)
            }
            listOf(
                season.title,
                season.year,
                season.description,
                episodesSaved,
                season.imageUrl,
                season.status
            )
        },
        restore = { saved ->
            val title = saved.getOrElse(0) { null } as? String ?: return@ComposeSaver null
            val year = saved.getOrElse(1) { null } as? String ?: return@ComposeSaver null
            val description = saved.getOrElse(2) { null } as? String ?: return@ComposeSaver null
            val episodesPayload = saved.getOrElse(3) { null } as? List<*> ?: return@ComposeSaver null
            val imageUrl = saved.getOrElse(4) { "" } as? String ?: ""
            val status = saved.getOrElse(5) { null } as? String

            val episodes = episodesPayload.mapNotNull { payload ->
                val listPayload = payload as? List<Any?> ?: return@mapNotNull null
                with(EpisodeSaver) { restore(listPayload) }
            }

            AnimeSeason(
                title = title,
                year = year,
                description = description,
                episodes = episodes,
                imageUrl = imageUrl.ifBlank { DEFAULT_DBZ_COVER_URL },
                status = status
            )
        }
    )

val NullableAnimeSeasonSaver: ComposeSaver<AnimeSeason?, Any> =
    ComposeSaver(
        save = { season ->
            season?.let { with(AnimeSeasonSaver) { save(it) } } ?: 0
        },
        restore = { saved ->
            when (saved) {
                0 -> null
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    with(AnimeSeasonSaver) { restore(saved as List<Any?>) }
                }

                else -> null
            }
        }
    )
