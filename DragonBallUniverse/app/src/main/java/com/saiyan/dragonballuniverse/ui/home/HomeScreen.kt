package com.saiyan.dragonballuniverse.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.saiyan.dragonballuniverse.R
import com.saiyan.dragonballuniverse.MainViewModel
import com.saiyan.dragonballuniverse.UiState
import com.saiyan.dragonballuniverse.manga.MangaArc
import com.saiyan.dragonballuniverse.manga.MangaViewModel
import com.saiyan.dragonballuniverse.manga.ui.MangaChapterReaderScreen
import com.saiyan.dragonballuniverse.manga.ui.MangaHomeScreen
import com.saiyan.dragonballuniverse.quiz.QuizMainScreen
import com.saiyan.dragonballuniverse.quiz.QuizViewModel
import com.saiyan.dragonballuniverse.ui.anime.AnimeSeason
import com.saiyan.dragonballuniverse.ui.anime.DEFAULT_DBZ_COVER_URL
import com.saiyan.dragonballuniverse.ui.anime.Episode
import com.saiyan.dragonballuniverse.ui.anime.Manga
import com.saiyan.dragonballuniverse.ui.anime.MangaChapter
import com.saiyan.dragonballuniverse.ui.anime.NullableAnimeSeasonSaver
import com.saiyan.dragonballuniverse.ui.components.DragonBallBottomBar
import com.saiyan.dragonballuniverse.ui.components.DragonBallTopBar
import com.saiyan.dragonballuniverse.ui.components.GenreChip
import com.saiyan.dragonballuniverse.ui.components.MainDestination
import com.saiyan.dragonballuniverse.ui.theme.DarkBackground
import com.saiyan.dragonballuniverse.ui.theme.GokuOrange
import com.saiyan.dragonballuniverse.ui.theme.VegetaBlue
import com.saiyan.dragonballuniverse.ui.utils.bounceClick
import com.saiyan.dragonballuniverse.ui.utils.resolveImageUrl
import com.saiyan.dragonballuniverse.ui.video.VideoPlayerScreen

@Composable
fun DragonBallScaffold(
    viewModel: MainViewModel,
    quizViewModel: QuizViewModel,
    mangaViewModel: MangaViewModel,
) {
    var selectedDestination by rememberSaveable { mutableStateOf(MainDestination.Anime) }

    var isSearchMode by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            DragonBallTopBar(
                isSearchMode = isSearchMode,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onToggleSearch = {
                    isSearchMode = !isSearchMode
                    if (!isSearchMode) searchQuery = ""
                }
            )
        },
        bottomBar = {
            DragonBallBottomBar(
                selected = selectedDestination,
                onSelect = { selectedDestination = it }
            )
        }
    ) { innerPadding ->
        DragonBallHomeContent(
            viewModel = viewModel,
            quizViewModel = quizViewModel,
            mangaViewModel = mangaViewModel,
            selectedDestination = selectedDestination,
            searchQuery = searchQuery,
            modifier = Modifier.padding(innerPadding),
        )
    }
}


private fun statusBadgeColor(status: String): Color =
    when (status) {
        "ongoing" -> Color(0xFF2E7D32)
        "completed" -> Color(0xFF1565C0)
        "coming" -> Color(0xFFEF6C00)
        else -> Color(0xFF616161)
    }


@Composable
private fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    minimizedMaxLines: Int = 3
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val showToggle = text.length > 140

    Text(
        text = text,
        color = Color(0xFFDDDDDD),
        lineHeight = 24.sp,
        maxLines = if (expanded) Int.MAX_VALUE else minimizedMaxLines,
        modifier = modifier
    )

    if (showToggle) {
        Text(
            text =
                if (expanded) stringResource(R.string.show_less)
                else stringResource(R.string.show_more),
            color = GokuOrange,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable { expanded = !expanded }
        )
    }
}

private fun clampPanOffset(
    offset: Offset,
    scale: Float,
    container: IntSize
): Offset {
    if (container == IntSize.Zero || scale <= 1f) return Offset.Zero

    val maxTranslationX = (container.width * (scale - 1f)) / 2f
    val maxTranslationY = (container.height * (scale - 1f)) / 2f

    return Offset(
        x = offset.x.coerceIn(-maxTranslationX, maxTranslationX),
        y = offset.y.coerceIn(-maxTranslationY, maxTranslationY)
    )
}

@Composable
private fun ZoomableImage(
    modifier: Modifier = Modifier
) {
    var scale by remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val gestureModifier =
        modifier
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 3f)
                    scale = newScale

                    if (newScale == 1f) {
                        offset = Offset.Zero
                    } else {
                        offset = clampPanOffset(offset + pan, newScale, containerSize)
                    }
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }

    Image(
        imageVector = Icons.Filled.AccountBox,
        contentDescription = null,
        modifier = gestureModifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun MangaReaderScreen(
    onBack: () -> Unit
) {
    val pages = (1..5).toList()
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
            modifier = Modifier.fillMaxSize()
        ) {
            ZoomableImage(
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = "${pagerState.currentPage + 1} / ${pages.size}",
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = Color.White
            )
        }
    }
}


@Composable
private fun DragonBallHomeContent(
    viewModel: MainViewModel,
    quizViewModel: QuizViewModel,
    mangaViewModel: MangaViewModel,
    selectedDestination: MainDestination,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    var selectedSeason by rememberSaveable(stateSaver = NullableAnimeSeasonSaver) { mutableStateOf<AnimeSeason?>(null) }
    var selectedVideoUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedEpisodeId by rememberSaveable { mutableStateOf<String?>(null) }

    var selectedMangaArc by rememberSaveable { mutableStateOf<MangaArc?>(null) }
    var selectedMangaChapterNumber by rememberSaveable { mutableStateOf<Int?>(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val episodes: List<Episode> =
        when (val state = uiState) {
            is UiState.Success -> state.episodes
            else -> emptyList()
        }

    if (selectedVideoUrl != null && selectedEpisodeId != null) {
        val sampleUrlSnapshot =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

        VideoPlayerScreen(
            episodeId = selectedEpisodeId!!,
            videoUrl = selectedVideoUrl!!,
            onPlayNext = { currentId, _ ->
                val currentIndex = episodes.indexOfFirst { it.id == currentId }
                val next = episodes.getOrNull(currentIndex + 1) ?: return@VideoPlayerScreen
                selectedEpisodeId = next.id
                selectedVideoUrl = sampleUrlSnapshot
            },
            onBack = {
                selectedVideoUrl = null
                selectedEpisodeId = null
            },
            getSavedProgressMs = { id ->
                viewModel.watchProgressMs(id)
            },
            saveWatchProgress = { id, progress ->
                viewModel.saveWatchProgress(id, progress)
            }
        )
        return
    }

    if (selectedMangaArc != null && selectedMangaChapterNumber != null) {
        MangaChapterReaderScreen(
            arc = selectedMangaArc!!,
            chapterNumber = selectedMangaChapterNumber!!,
            onBack = {
                selectedMangaArc = null
                selectedMangaChapterNumber = null
            },
            viewModel = mangaViewModel,
        )
        return
    }

    val sampleUrl =
        remember {
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        }

    val filteredEpisodesList by remember(searchQuery, episodes) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                episodes
            } else {
                val q = searchQuery.trim()
                episodes.filter { it.title.contains(q, ignoreCase = true) }
            }
        }
    }

    val dbzSeason =
        remember(filteredEpisodesList) {
            AnimeSeason(
                title = "Dragon Ball Z",
                year = "1989",
                description = "",
                episodes = filteredEpisodesList,
                imageUrl = DEFAULT_DBZ_COVER_URL,
                status = "completed"
            )
        }

    val seasonsToShow by remember(dbzSeason) {
        derivedStateOf { listOf(dbzSeason) }
    }

    when (selectedDestination) {
        MainDestination.Anime -> {
            when (uiState) {
                UiState.Loading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = modifier
                            .fillMaxSize()
                            .background(DarkBackground),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(6) {
                            ShimmerPosterCard()
                        }
                    }
                }

                is UiState.Error -> {
                    val message = (uiState as UiState.Error).message
                    NetworkErrorScreen(
                        errorMessage = message,
                        onRetry = { viewModel.fetchEpisodes() }
                    )
                }

                is UiState.Success -> {
                    if (selectedSeason == null) {
                        AnimeSeasonsScreen(
                            seasons = seasonsToShow,
                            onSeasonClick = { selectedSeason = it },
                            modifier = modifier
                        )
                    } else {
                        SeasonDetailsScreen(
                            season = selectedSeason!!,
                            onBack = { selectedSeason = null },
                            onEpisodeClick = { ep ->
                                selectedEpisodeId = ep.id
                                selectedVideoUrl = sampleUrl
                            },
                            modifier = modifier,
                            isFavorite = { episodeId ->
                                // No @Composable calls here; this is consumed inside the Lazy list item scope.
                                viewModel.isFavoriteState(episodeId).value
                            },
                            onToggleFavorite = { episodeId, isFav ->
                                viewModel.toggleFavorite(episodeId, isFav)
                            }
                        )
                    }
                }
            }
        }

        MainDestination.Manga -> {
            MangaHomeScreen(
                viewModel = mangaViewModel,
                onOpenChapter = { arc, chapterNumber ->
                    selectedMangaArc = arc
                    selectedMangaChapterNumber = chapterNumber
                },
            )
        }

        MainDestination.Quiz -> {
            QuizMainScreen(
                modifier = modifier,
                viewModel = quizViewModel,
                bounceClick = { onClick -> Modifier.bounceClick(onClick) }
            )
        }
    }
}

@Composable
private fun MangaDetailsScreen(
    manga: Manga,
    modifier: Modifier = Modifier,
    onChapterClick: (MangaChapter) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF2A2A2A))
                )

                Text(
                    text = manga.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Text(
                    text = manga.description,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 6.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color = Color(0xFF2A2A2A),
                    thickness = 1.dp
                )
            }
        }

        items(manga.chapters) { chapter ->
            ChapterRowItem(
                chapter = chapter,
                onClick = { onChapterClick(chapter) }
            )
        }
    }
}

@Composable
private fun ChapterRowItem(
    chapter: MangaChapter,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isRead = chapter.isRead

    val chapterNumberColor = if (isRead) Color.Gray else GokuOrange
    val chapterTitleColor = if (isRead) Color.Gray else Color.White
    val chapterDateColor = Color.Gray

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        val clickableModifier =
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(clickableModifier)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${stringResource(R.string.chapter_prefix)} ${chapter.number}",
                    color = chapterNumberColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = chapter.title,
                    color = chapterTitleColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = chapter.date,
                    color = chapterDateColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = stringResource(R.string.download_cd),
                tint = Color.Gray
            )
        }

        HorizontalDivider(
            color = Color(0xFF1E1E1E),
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun AnimeSeasonsScreen(
    seasons: List<AnimeSeason>,
    onSeasonClick: (AnimeSeason) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(seasons) { season ->
            PosterCard(
                season = season,
                onClick = { onSeasonClick(season) }
            )
        }
    }
}

@Composable
private fun PosterCard(
    season: AnimeSeason,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val imageUrl = season.imageUrl.resolveImageUrl()
    val statusLabel = season.status?.trim().orEmpty()

    Card(
        modifier = modifier
            .size(width = 160.dp, height = 240.dp)
            .clip(shape)
            .bounceClick { onClick() },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl.resolveImageUrl())
                        .setHeader("User-Agent", "Mozilla/5.0")
                        .crossfade(true)
                        .build(),
                    contentDescription =
                        "${stringResource(R.string.cover_cd_prefix)} ${season.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color(0xFF2A2A2A)),
                    error = ColorPainter(Color(0xFF2A2A2A))
                )

                if (statusLabel.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusBadgeColor(statusLabel))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
                    .background(Color(0xFF1E1E1E))
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = season.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ShimmerPosterCard(
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    var size by remember { mutableStateOf(IntSize.Zero) }

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "shimmerProgress"
    )

    val startX = (-size.width).toFloat() + (size.width * 2f * progress)
    val shimmerBrush =
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF2A2A2A),
                Color(0xFF3A3A3A),
                Color(0xFF2A2A2A)
            ),
            start = Offset(startX, 0f),
            end = Offset(startX + size.width, size.height.toFloat())
        )

    Box(
        modifier = modifier
            .size(width = 160.dp, height = 240.dp)
            .clip(shape)
            .background(shimmerBrush)
            .onSizeChanged { size = it }
    )
}

@Composable
private fun SeasonDetailsScreen(
    season: AnimeSeason,
    onBack: () -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: (String) -> Boolean,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    val bannerHeight = 280.dp
    val posterWidth = 150.dp
    val posterHeight = 225.dp // 2:3
    val posterShape = RoundedCornerShape(12.dp)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
    ) {
        item {
            // HEADER: Banner + Overlay 60% + (Blur-like via gradient) + Poster يمين + Meta يسار
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight)
            ) {
                val imageUrl = season.imageUrl.resolveImageUrl()

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl.resolveImageUrl())
                        .setHeader("User-Agent", "Mozilla/5.0")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Banner ${season.title}",
                    modifier = Modifier
                        .matchParentSize()
                        .blur(25.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color(0xFF2A2A2A)),
                    error = ColorPainter(Color(0xFF2A2A2A))
                )

                // Glassmorphism overlay: تدرّج أسود فوق البلور لإبراز النصوص والبوستر
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black.copy(alpha = 0.60f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // زر الرجوع (RTL: أعلى اليسار مناسب كـ back)
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = Color.White
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Meta (يسار البوستر)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = season.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            maxLines = 2
                        )

                        val statusText =
                            season.status?.trim().takeUnless { it.isNullOrBlank() }
                                ?: stringResource(R.string.status_unknown)
                        val rating = "8.7"
                        val episodesCount = season.episodes.size

                        Text(
                            text =
                                "${season.year}  •  $statusText  •  $rating  •  $episodesCount ${stringResource(R.string.episode_count_suffix)}",
                            color = Color(0xFFBDBDBD),
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GenreChip(text = stringResource(R.string.genre_action))
                            GenreChip(text = stringResource(R.string.genre_adventure))
                            GenreChip(text = stringResource(R.string.genre_shonen))
                        }

                        Card(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = GokuOrange)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: watch now */ }
                                    .padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Text(
                                    text = stringResource(R.string.watch_now),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Poster Card (يمين)
                    Card(
                        modifier = Modifier
                            .size(width = posterWidth, height = posterHeight),
                        shape = posterShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(season.imageUrl.resolveImageUrl())
                                .setHeader("User-Agent", "Mozilla/5.0")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Poster ${season.title}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(Color(0xFF2A2A2A)),
                            error = ColorPainter(Color(0xFF2A2A2A))
                        )
                    }
                }
            }
        }

        item {
            // SYNOPSIS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(R.string.synopsis_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                ExpandableText(
                    text = season.description.ifBlank { stringResource(R.string.synopsis_empty) },
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        item {
            // EPISODES TITLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.episodes_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${season.episodes.size}",
                    color = Color(0xFFBDBDBD)
                )
            }
        }

        items(season.episodes) { episode ->
            EpisodeRowCard(
                episode = episode,
                onClick = { onEpisodeClick(episode) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                isFavorite = isFavorite(episode.id),
                onToggleFavorite = { checked ->
                    onToggleFavorite(episode.id, checked)
                }
            )
        }
    }
}


@Composable
private fun NetworkErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = "https://e.top4top.io/p_3722fwcuz1.jpg",
                contentDescription = "Network error",
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Text(
                text = errorMessage,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )

            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.retry),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EpisodeRowCard(
    episode: Episode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    onToggleFavorite: (Boolean) -> Unit
) {
    val favoriteTint = if (isFavorite) Color(0xFFE53935) else Color(0xFF9E9E9E)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Texts (يسار)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${stringResource(R.string.episode_prefix)} ${episode.number}",
                        color = Color(0xFFBDBDBD),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = episode.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1
                    )
                    Text(
                        text = episode.duration,
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    IconToggleButton(
                        checked = isFavorite,
                        onCheckedChange = { checked ->
                            onToggleFavorite(checked)
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorite_cd),
                            tint = favoriteTint
                        )
                    }
                }

                // Thumbnail (يمين) + Play overlay
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 72.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    val imageUrl = episode.imageUrl.resolveImageUrl()

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl.resolveImageUrl())
                            .setHeader("User-Agent", "Mozilla/5.0")
                            .crossfade(true)
                            .build(),
                        contentDescription =
                            "${stringResource(R.string.episode_image_cd_prefix)} ${episode.number}",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color(0xFF2A2A2A)),
                        error = ColorPainter(Color(0xFF2A2A2A))
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )

                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.play_cd),
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(34.dp)
                    )
                }
            }

            // Progress bar (أسفل الكارت)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.06f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = episode.progress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(GokuOrange)
                )
            }
        }
    }
}
