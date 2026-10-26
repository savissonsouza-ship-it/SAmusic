package com.samusic.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samusic.app.data.repo.MusicRepository
import com.samusic.app.playback.PlaybackController
import com.samusic.app.ui.HomeViewModel
import com.samusic.app.ui.LibraryViewModel
import com.samusic.app.ui.SearchViewModel
import com.samusic.app.ui.SAmusicVMFactory
import com.samusic.app.ui.components.MiniPlayer
import com.samusic.app.ui.home.HomeScreen
import com.samusic.app.ui.library.LibraryScreen
import com.samusic.app.ui.player.PlayerScreen
import com.samusic.app.ui.search.SearchScreen

private enum class Dest(val label: String, val icon: ImageVector) {
    HOME("Início", Icons.Filled.Home),
    SEARCH("Buscar", Icons.Filled.Search),
    LIBRARY("Biblioteca", Icons.Filled.LibraryMusic),
}

@Composable
fun SAmusicRoot(repository: MusicRepository, playback: PlaybackController) {
    val factory = remember { SAmusicVMFactory(repository, playback) }
    val homeVm: HomeViewModel = viewModel(factory = factory)
    val searchVm: SearchViewModel = viewModel(factory = factory)
    val libraryVm: LibraryViewModel = viewModel(factory = factory)

    var dest by rememberSaveable { mutableStateOf(Dest.HOME) }
    var playerExpanded by remember { mutableStateOf(false) }
    val playerState by playback.state.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    if (playerState.current != null && !playerExpanded) {
                        MiniPlayer(playerState, playback, onExpand = { playerExpanded = true })
                    }
                    NavigationBar {
                        Dest.entries.forEach { d ->
                            NavigationBarItem(
                                selected = dest == d,
                                onClick = { dest = d },
                                icon = { Icon(d.icon, contentDescription = d.label) },
                                label = { Text(d.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            when (dest) {
                Dest.HOME -> HomeScreen(homeVm, padding)
                Dest.SEARCH -> SearchScreen(searchVm, padding)
                Dest.LIBRARY -> LibraryScreen(libraryVm, padding)
            }
        }

        AnimatedVisibility(
            visible = playerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                PlayerScreen(controller = playback, onCollapse = { playerExpanded = false })
            }
        }
    }
}
