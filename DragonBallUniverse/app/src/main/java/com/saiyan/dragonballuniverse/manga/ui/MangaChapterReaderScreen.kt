package com.saiyan.dragonballuniverse.manga.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.request.ImageRequest
import com.saiyan.dragonballuniverse.manga.offline.MangaCoil
import com.saiyan.dragonballuniverse.manga.MangaArc
import com.saiyan.dragonballuniverse.manga.MangaReaderUiState
import com.saiyan.dragonballuniverse.manga.MangaViewModel
import com.saiyan.dragonballuniverse.manga.offline.MangaFileStore
import com.saiyan.dragonballuniverse.manga.offline.MangaImageLoader

@Composable
fun MangaChapterReaderScreen(
    arc: MangaArc,
    chapterNumber: Int,
    onBack: () -> Unit,
    viewModel: MangaViewModel,
) {
    val state by viewModel.readerUiState.collectAsState()

    LaunchedEffect(arc, chapterNumber) {
        viewModel.openChapter(arc, chapterNumber)
    }

    when (val s = state) {
        is MangaReaderUiState.Idle,
        is MangaReaderUiState.Loading,
        -> {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is MangaReaderUiState.Error -> {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "خطأ: ${s.message}",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        is MangaReaderUiState.Success -> {
            val chapter = s.chapter
            val pages = chapter.pages
            val initial = s.initialPageIndex

            val context = LocalContext.current

            val imageLoader =
                remember(context.applicationContext) {
                    MangaImageLoader(
                        fileStore = MangaFileStore(context.applicationContext),
                    )
                }

            var isVerticalMode by remember(arc, chapterNumber) { mutableStateOf(false) }

            // PagerState must be created in composition. Key by pages.size so the pager is recreated
            // if a new pages list arrives (e.g., after async probing/refresh).
            val pagerState =
                rememberPagerState(
                    initialPage = initial,
                    pageCount = { pages.size },
                )

            val listState = rememberLazyListState(initialFirstVisibleItemIndex = initial)

            LaunchedEffect(pages.size) {
                // If pageCount changes, keep current page valid.
                val maxIndex = (pages.size - 1).coerceAtLeast(0)
                val target = pagerState.currentPage.coerceIn(0, maxIndex)
                if (target != pagerState.currentPage) {
                    pagerState.scrollToPage(target)
                }
            }

            // Save progress from the active reader mode only.
            LaunchedEffect(isVerticalMode, pagerState.currentPage) {
                if (!isVerticalMode) {
                    val isLastPage = pagerState.currentPage == pages.lastIndex
                    viewModel.saveProgress(
                        arc = arc,
                        chapterNumber = chapterNumber,
                        lastReadPageIndex = pagerState.currentPage,
                        isCompleted = isLastPage,
                    )
                }
            }

            LaunchedEffect(isVerticalMode, listState) {
                if (isVerticalMode) {
                    snapshotFlow { listState.firstVisibleItemIndex }
                        .collect { firstVisible ->
                            val isLastPage = firstVisible == pages.lastIndex
                            viewModel.saveProgress(
                                arc = arc,
                                chapterNumber = chapterNumber,
                                lastReadPageIndex = firstVisible,
                                isCompleted = isLastPage,
                            )
                        }
                }
            }

            val coilImageLoader: ImageLoader = remember(context.applicationContext) {
                MangaCoil.imageLoader(context.applicationContext)
            }

            // Prefetch: whenever a page becomes current, enqueue the NEXT 2 pages.
            // This is fire-and-forget and runs in Coil's background dispatcher.
            LaunchedEffect(pagerState, pages) {
                snapshotFlow { pagerState.currentPage }
                    .collect { current ->
                        val nextIndices = listOf(current + 1, current + 2).filter { it in pages.indices }
                        nextIndices.forEach { idx ->
                            val prefetchModel =
                                imageLoader.resolvePageModel(
                                    arc = arc,
                                    chapterNumber = chapterNumber,
                                    pageIndex = idx,
                                    remoteUrl = pages[idx].imageUrl,
                                )

                            val req =
                                ImageRequest.Builder(context)
                                    .data(prefetchModel)
                                    // We don't need UI-side crossfade for prefetch.
                                    .crossfade(false)
                                    .build()

                            coilImageLoader.enqueue(req)
                        }
                    }
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black),
            ) {
                if (isVerticalMode) {
                    VerticalMangaReader(
                        arc = arc,
                        chapterNumber = chapterNumber,
                        pages = pages,
                        imageLoader = imageLoader,
                        listState = listState,
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        reverseLayout = true, // RTL reading
                        modifier = Modifier.fillMaxSize(),
                    ) { pageIndex ->
                        val model =
                            imageLoader.resolvePageModel(
                                arc = arc,
                                chapterNumber = chapterNumber,
                                pageIndex = pageIndex,
                                remoteUrl = pages[pageIndex].imageUrl,
                            )

                        ZoomablePageImage(
                            model = model,
                        )
                    }
                }

                val currentIndex =
                    if (isVerticalMode) {
                        listState.firstVisibleItemIndex.coerceIn(0, pages.lastIndex.coerceAtLeast(0))
                    } else {
                        pagerState.currentPage.coerceIn(0, pages.lastIndex.coerceAtLeast(0))
                    }

                val currentPageLabel = (currentIndex + 1).toString().padStart(3, '0')
                val totalLabel = pages.size.toString().padStart(3, '0')

                Text(
                    text = "$currentPageLabel / $totalLabel",
                    color = Color.White.copy(alpha = 0.75f),
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = MangaConstants.PAGE_INDICATOR_BOTTOM_PADDING),
                )

                IconButton(
                    onClick = onBack,
                    modifier =
                        Modifier
                            .padding(MangaConstants.BACK_BUTTON_PADDING)
                            .align(Alignment.TopStart),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White,
                    )
                }

                IconButton(
                    onClick = { isVerticalMode = !isVerticalMode },
                    modifier =
                        Modifier
                            .padding(MangaConstants.BACK_BUTTON_PADDING)
                            .align(Alignment.TopEnd),
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = if (isVerticalMode) "الوضع الأفقي" else "الوضع العمودي",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

private object MangaConstants {
    const val MIN_SCALE = 1f
    const val MAX_SCALE = 3f
    const val HORIZONTAL_SWIPE_THRESHOLD_PX = 0.5f

    val PAGE_INDICATOR_BOTTOM_PADDING = 16.dp
    val BACK_BUTTON_PADDING = 12.dp
}

@Composable
private fun VerticalMangaReader(
    arc: MangaArc,
    chapterNumber: Int,
    pages: List<com.saiyan.dragonballuniverse.manga.MangaPage>,
    imageLoader: MangaImageLoader,
    listState: androidx.compose.foundation.lazy.LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = pages,
            key = { idx, _ -> idx },
        ) { pageIndex, page ->
            val model =
                imageLoader.resolvePageModel(
                    arc = arc,
                    chapterNumber = chapterNumber,
                    pageIndex = pageIndex,
                    remoteUrl = page.imageUrl,
                )

            ZoomablePageImage(
                model = model,
            )
        }
    }
}

@Composable
private fun ZoomablePageImage(
    model: Any,
) {
    val context = LocalContext.current

    var scale by remember { mutableFloatStateOf(MangaConstants.MIN_SCALE) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val isZoomed by remember { derivedStateOf { scale > MangaConstants.MIN_SCALE } }

    fun clampPanOffset(
        inOffset: Offset,
        inScale: Float,
        container: IntSize,
    ): Offset {
        if (container == IntSize.Zero || inScale <= MangaConstants.MIN_SCALE) return Offset.Zero

        val maxTranslationX = (container.width * (inScale - 1f)) / 2f
        val maxTranslationY = (container.height * (inScale - 1f)) / 2f

        return Offset(
            x = inOffset.x.coerceIn(-maxTranslationX, maxTranslationX),
            y = inOffset.y.coerceIn(-maxTranslationY, maxTranslationY),
        )
    }

    // Pass-through gesture strategy (Tachiyomi-style):
    // - Pinch (2 fingers): zoom immediately (consume)
    // - Drag (1 finger) while scale > 1f: pan (consume)
    // - Drag (1 finger) while scale == 1f: DO NOT consume horizontal drags so HorizontalPager can swipe
    //   (we only consume when we're actually zooming/panning)
    val gestureModifier =
        Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Track per-gesture deltas and decide if we should "own" the gesture.
                    var gestureOwns = false

                    while (true) {
                        // Read changes without consuming by default.
                        val event = awaitPointerEvent(pass = PointerEventPass.Main)
                        val changes = event.changes

                        if (changes.isEmpty()) break

                        val pressedCount = changes.count { it.pressed }

                        // If all pointers are up, gesture ended.
                        if (pressedCount == 0) break

                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        val centroidMovedHorizontally =
                            abs(pan.x) > abs(pan.y) && abs(pan.x) > MangaConstants.HORIZONTAL_SWIPE_THRESHOLD_PX

                        val isPinch = pressedCount > 1 && zoom != 1f

                        // Golden rule + horizontal priority:
                        // - At base scale, single pointer, horizontal movement => let pager have it (never consume)
                        // - If pinch OR already zoomed => we own it and consume
                        if (!gestureOwns) {
                            gestureOwns =
                                isPinch || isZoomed || (pressedCount > 1) || (scale == MangaConstants.MIN_SCALE && !centroidMovedHorizontally && zoom != 1f)
                        }

                        if (!gestureOwns && scale == MangaConstants.MIN_SCALE && pressedCount == 1 && centroidMovedHorizontally) {
                            // Pass-through: do not consume, do not update state.
                            continue
                        }

                        // If we got here, we are handling zoom/pan => consume.
                        if (gestureOwns) {
                            // Deprecated consumePositionChange() replacement:
                            changes.forEach { it.consume() }
                        }

                        // Apply zoom immediately (pinch).
                        val newScale =
                            (scale * zoom).coerceIn(
                                MangaConstants.MIN_SCALE,
                                MangaConstants.MAX_SCALE,
                            )
                        scale = newScale

                        // Apply pan only when zoomed.
                        offset =
                            if (newScale <= MangaConstants.MIN_SCALE) {
                                Offset.Zero
                            } else {
                                clampPanOffset(offset + pan, newScale, containerSize)
                            }
                    }
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }

    val mangaImageLoader = remember(context.applicationContext) { MangaCoil.imageLoader(context.applicationContext) }

    AsyncImage(
        model =
            ImageRequest.Builder(context)
                .data(model)
                .crossfade(true)
                .build(),
        imageLoader = mangaImageLoader,
        contentDescription = null,
        modifier = gestureModifier,
    )
}
