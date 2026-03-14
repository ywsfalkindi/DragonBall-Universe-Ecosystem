package com.saiyan.dragonballuniverse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saiyan.dragonballuniverse.R
import com.saiyan.dragonballuniverse.ui.theme.GokuOrange
import com.saiyan.dragonballuniverse.ui.theme.VegetaBlue

/**
 * NOTE:
 * This file is intentionally "common UI only".
 * Keep it free of HomeScreen-specific state and business logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DragonBallTopBar(
    title: String = stringResource(R.string.topbar_title_default),
    isSearchMode: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
) {
    TopAppBar(
        title = {
            if (isSearchMode) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.search_placeholder_episode)) }
                )
            } else {
                Text(text = title)
            }
        },
        actions = {
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search_cd),
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VegetaBlue,
            titleContentColor = Color.White
        )
    )
}

enum class MainDestination {
    Anime,
    Manga,
    Quiz
}

@Composable
fun DragonBallBottomBar(
    selected: MainDestination,
    onSelect: (MainDestination) -> Unit
) {
    NavigationBar(containerColor = VegetaBlue) {
        NavigationBarItem(
            selected = selected == MainDestination.Anime,
            onClick = { onSelect(MainDestination.Anime) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.nav_anime)
                )
            },
            label = { Text(stringResource(R.string.nav_anime)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GokuOrange,
                selectedTextColor = GokuOrange,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selected == MainDestination.Manga,
            onClick = { onSelect(MainDestination.Manga) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = stringResource(R.string.nav_manga)
                )
            },
            label = { Text(stringResource(R.string.nav_manga)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GokuOrange,
                selectedTextColor = GokuOrange,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = selected == MainDestination.Quiz,
            onClick = { onSelect(MainDestination.Quiz) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = stringResource(R.string.nav_quiz)
                )
            },
            label = { Text(stringResource(R.string.nav_quiz)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GokuOrange,
                selectedTextColor = GokuOrange,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun GenreChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF2A2A2A))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFE0E0E0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
