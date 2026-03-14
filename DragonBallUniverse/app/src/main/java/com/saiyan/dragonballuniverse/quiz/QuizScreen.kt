package com.saiyan.dragonballuniverse.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saiyan.dragonballuniverse.R
import com.saiyan.dragonballuniverse.quiz.audio.QuizSfx
import com.saiyan.dragonballuniverse.quiz.audio.SoundPoolSoundManager
import com.saiyan.dragonballuniverse.quiz.ui.QuizDifficultyBackground
import com.saiyan.dragonballuniverse.quiz.ui.difficultyToUiTier
import com.saiyan.dragonballuniverse.ui.theme.DarkBackground
import com.saiyan.dragonballuniverse.ui.theme.GokuOrange
import com.saiyan.dragonballuniverse.ui.theme.VegetaBlue
import kotlin.math.max
import kotlinx.coroutines.delay

@Composable
fun QuizMainScreen(
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel,
    bounceClick: (onClick: () -> Unit) -> Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(uiState) {
        val s = uiState
        if (s is QuizUiState.Error) {
            Toast
                .makeText(context, context.getString(R.string.quiz_error, s.details), Toast.LENGTH_LONG)
                .show()
        }
    }

    when (val state = uiState) {
        QuizUiState.Home -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(16.dp)
            ) {
                QuizHomeContent(
                    powerLevel = stats.powerLevel,
                    senzuBeans = stats.senzuBeans,
                    highestStreak = stats.highestStreak,
                    onStart = { viewModel.startGame() },
                    bounceClick = bounceClick
                )
            }
        }

        QuizUiState.Loading -> {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = stringResource(R.string.quiz_loading_questions), color = Color.White)
            }
        }

        QuizUiState.Playing -> {
            val tier = difficultyToUiTier(session?.currentQuestion?.difficulty ?: "")
            QuizDifficultyBackground(
                tier = tier,
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                QuizPlayingContent(
                    session = session,
                    senzuBeans = stats.senzuBeans,
                    onAnswer = { index -> viewModel.answer(index) },
                    onTimeExpired = { viewModel.onTimeExpired() },
                    bounceClick = bounceClick
                )
            }
        }

        is QuizUiState.GameOver -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(16.dp)
            ) {
                QuizResultContent(
                    title = stringResource(R.string.quiz_game_over_title),
                    subtitle = stringResource(R.string.quiz_game_over_subtitle),
                    earnedPower = state.earnedPower,
                    powerLevel = stats.powerLevel,
                    onBack = { viewModel.backToHome() },
                    bounceClick = bounceClick
                )
            }
        }

        is QuizUiState.Victory -> {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                        .padding(16.dp),
            ) {
                QuizResultContent(
                    title = stringResource(R.string.quiz_victory_title),
                    subtitle = stringResource(R.string.quiz_victory_subtitle),
                    earnedPower = state.earnedPower,
                    powerLevel = stats.powerLevel,
                    onBack = { viewModel.backToHome() },
                    bounceClick = bounceClick,
                )
            }
        }

        is QuizUiState.Error -> {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                        .padding(16.dp),
            ) {
                QuizHomeContent(
                    powerLevel = stats.powerLevel,
                    senzuBeans = stats.senzuBeans,
                    highestStreak = stats.highestStreak,
                    onStart = { viewModel.startGame() },
                    bounceClick = bounceClick,
                )
            }
        }
    }
}

@Composable
private fun QuizHomeContent(
    powerLevel: Long,
    senzuBeans: Int,
    highestStreak: Int,
    onStart: () -> Unit,
    bounceClick: (onClick: () -> Unit) -> Modifier
) {
    val rankName = getRankName(powerLevel)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.quiz_section_title),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.quiz_power_level_label),
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp
                )
                Text(
                    text = powerLevel.toString(),
                    color = GokuOrange,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = stringResource(R.string.quiz_rank_label, rankName),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color = Color.White.copy(alpha = 0.08f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.quiz_senzu_beans_label),
                            color = Color(0xFFBDBDBD),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$senzuBeans / 3",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.quiz_highest_streak_label),
                            color = Color(0xFFBDBDBD),
                            fontSize = 12.sp
                        )
                        Text(
                            text = highestStreak.toString(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        val startEnabled = senzuBeans > 0 // 0 Senzu: Disable button.
        val startLabel =
            if (startEnabled) stringResource(R.string.quiz_start_challenge)
            else stringResource(R.string.quiz_no_senzu_beans)

        Button(
            onClick = onStart,
            enabled = startEnabled,
            modifier = bounceClick { onStart() }.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = VegetaBlue,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF2A2A2A),
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = startLabel,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        Text(
            text = stringResource(R.string.quiz_rules_text),
            color = Color(0xFFBDBDBD),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun QuizPlayingContent(
    session: QuizSessionState?,
    senzuBeans: Int,
    onAnswer: (Int) -> Unit,
    onTimeExpired: () -> Unit,
    bounceClick: (onClick: () -> Unit) -> Modifier
) {
    val q = session?.currentQuestion
    if (session == null || q == null) {
        // Safety fallback (should not happen)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.quiz_loading_generic), color = Color.White)
        }
        return
    }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val soundManager = remember {
        SoundPoolSoundManager(
            context = context,
            correctRes = R.raw.quiz_correct,
            wrongRes = R.raw.quiz_wrong
        )
    }
    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    val totalMs = 15_000
    var remainingMs by remember(q.id) { mutableIntStateOf(totalMs) }
    var progress by remember(q.id) { mutableFloatStateOf(1f) }

    // Local UI lock to prevent double taps.
    var localAnswered by remember(q.id) { mutableStateOf(false) }

    val isUrgent = remainingMs <= 5_000
    val infiniteTransition = rememberInfiniteTransition(label = "quiz_timer_blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450),
            repeatMode = RepeatMode.Reverse
        ),
        label = "quiz_timer_blink_alpha"
    )
    val urgencyAlpha = if (isUrgent) blinkAlpha else 1f
    val timerColor = if (isUrgent) Color(0xFFFF3B30) else GokuOrange

    LaunchedEffect(q.id, localAnswered) {
        if (localAnswered) return@LaunchedEffect

        val tickMs = 250
        remainingMs = totalMs
        progress = 1f

        while (remainingMs > 0 && !localAnswered) {
            delay(tickMs.toLong())
            remainingMs = max(0, remainingMs - tickMs)
            progress = remainingMs.toFloat() / totalMs.toFloat()
        }

        if (!localAnswered) {
            localAnswered = true
            soundManager.play(QuizSfx.Wrong)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onTimeExpired()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.quiz_question_counter,
                    session.currentIndex + 1,
                    session.questions.size
                ),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.quiz_senzu_counter, senzuBeans),
                color = GokuOrange,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .alpha(urgencyAlpha),
            color = timerColor,
            trackColor = Color.White.copy(alpha = 0.12f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = q.id,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInHorizontally { it / 3 } + fadeIn(animationSpec = tween(220)),
                    initialContentExit = slideOutHorizontally { -it / 3 } + fadeOut(animationSpec = tween(160)),
                    sizeTransform = null
                )
            },
            label = "quiz_question_transition"
        ) { _ ->
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = q.text,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            lineHeight = 26.sp
                        )

                        Text(
                            text = stringResource(
                                R.string.quiz_difficulty_label,
                                difficultyLabel(q.difficulty)
                            ),
                            color = Color(0xFFBDBDBD),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                q.options.forEachIndexed { index, option ->
                    AnswerButton(
                        text = option,
                        enabled = !localAnswered,
                        modifier = Modifier.padding(bottom = 10.dp),
                        bounceClick = bounceClick
                    ) {
                        if (localAnswered) return@AnswerButton

                        localAnswered = true
                        val isCorrect = index == q.correctAnswerIndex
                        soundManager.play(if (isCorrect) QuizSfx.Correct else QuizSfx.Wrong)
                        haptic.performHapticFeedback(
                            if (isCorrect) HapticFeedbackType.TextHandleMove else HapticFeedbackType.LongPress
                        )
                        onAnswer(index)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.quiz_rule_short),
            color = Color(0xFFBDBDBD),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun AnswerButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    bounceClick: (onClick: () -> Unit) -> Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    val glowTransition = rememberInfiniteTransition(label = "answer_glow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "answer_glow_alpha"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            VegetaBlue.copy(alpha = 0.95f),
            GokuOrange.copy(alpha = 0.95f),
            Color.White.copy(alpha = 0.55f),
        )
    )

    val glowColor =
        if (enabled) {
            // Subtle, DBZ-ish aura vibe.
            VegetaBlue.copy(alpha = glowAlpha)
        } else {
            Color.Transparent
        }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (enabled) bounceClick { onClick() } else Modifier)
            .drawBehind {
                // Soft outer glow "aura".
                if (enabled) {
                    val strokeWidth = 10.dp.toPx()
                    val half = strokeWidth / 2f
                    drawRoundRect(
                        color = glowColor,
                        topLeft = Offset(-half, -half),
                        size = size.copy(
                            width = size.width + strokeWidth,
                            height = size.height + strokeWidth
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            x = 18.dp.toPx(),
                            y = 18.dp.toPx()
                        ),
                        style = Stroke(width = strokeWidth)
                    )
                }

                // Crisp gradient border.
                val borderStroke = 2.dp.toPx()
                drawRoundRect(
                    brush = borderBrush,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        x = 14.dp.toPx(),
                        y = 14.dp.toPx()
                    ),
                    style = Stroke(width = borderStroke)
                )
            }
            .background(Color(0xFF161616))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun QuizResultContent(
    title: String,
    subtitle: String,
    earnedPower: Long,
    powerLevel: Long,
    onBack: () -> Unit,
    bounceClick: (onClick: () -> Unit) -> Modifier
) {
    // Phase 1: "Power Level" count-up animation (quick, punchy).
    var animatedPower by remember(powerLevel) { mutableStateOf(0L) }
    LaunchedEffect(powerLevel) {
        val start = 0L
        val end = powerLevel
        val durationMs = 650
        val steps = 26
        animatedPower = start
        repeat(steps) { i ->
            val t = (i + 1).toFloat() / steps.toFloat()
            // Ease-out-ish curve without extra deps.
            val eased = 1f - (1f - t) * (1f - t)
            animatedPower = (start + ((end - start) * eased)).toLong()
            delay((durationMs / steps).toLong())
        }
        animatedPower = end
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFBDBDBD),
                    modifier = Modifier.padding(top = 8.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(top = 14.dp),
                    color = Color.White.copy(alpha = 0.08f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.quiz_earned_power_label),
                            color = Color(0xFFBDBDBD),
                            fontSize = 12.sp
                        )
                        Text(
                            text = earnedPower.toString(),
                            color = GokuOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.quiz_current_power_level_label),
                            color = Color(0xFFBDBDBD),
                            fontSize = 12.sp
                        )
                        Text(
                            text = animatedPower.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onBack,
            modifier = bounceClick { onBack() }.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VegetaBlue, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = stringResource(R.string.quiz_back),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
fun getRankName(powerLevel: Long): String =
    when {
        powerLevel < 1_000L -> stringResource(R.string.rank_earthling)
        powerLevel < 10_000L -> stringResource(R.string.rank_fighter)
        powerLevel < 100_000L -> stringResource(R.string.rank_elite_fighter)
        powerLevel < 1_000_000L -> stringResource(R.string.rank_super_saiyan_1)
        powerLevel < 10_000_000L -> stringResource(R.string.rank_super_saiyan_2)
        powerLevel < 100_000_000L -> stringResource(R.string.rank_super_saiyan_3)
        else -> stringResource(R.string.rank_ultra_instinct)
    }

@Composable
private fun difficultyLabel(diff: String): String =
    when (diff) {
        DIFF_EASY -> stringResource(R.string.difficulty_easy)
        DIFF_MEDIUM -> stringResource(R.string.difficulty_medium)
        DIFF_HARD -> stringResource(R.string.difficulty_hard)
        DIFF_INSANE -> stringResource(R.string.difficulty_insane)
        else -> diff
    }
