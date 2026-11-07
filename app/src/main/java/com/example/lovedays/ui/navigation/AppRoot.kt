package com.example.lovedays.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.lovedays.R
import com.example.lovedays.ui.home.HomeScreen
import com.example.lovedays.ui.milestones.MilestonesScreen
import com.example.lovedays.ui.milestones.SpecialDayEditScreen
import com.example.lovedays.ui.onboarding.OnboardingScreen
import com.example.lovedays.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object MilestonesRoute
@Serializable object SettingsRoute
@Serializable data class SpecialDayEditRoute(val id: Long = 0)

private data class TopLevel(
    val route: Any,
    val label: Int,
    val selected: ImageVector,
    val unselected: ImageVector,
)

private val topLevels = listOf(
    TopLevel(HomeRoute, R.string.nav_home, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    TopLevel(MilestonesRoute, R.string.nav_milestones, Icons.Filled.Event, Icons.Outlined.Event),
    TopLevel(SettingsRoute, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
)

/** Lần đầu: onboarding. Sau khi hoàn tất (hoặc xoá dữ liệu) tự chuyển đổi theo DataStore. */
@Composable
fun AppRoot(onboardingCompleted: Boolean) {
    if (!onboardingCompleted) {
        OnboardingScreen()
    } else {
        MainScaffold()
    }
}

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val showBottomBar = topLevels.any { top -> destination?.hasRoute(top.route::class) == true }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevels.forEach { top ->
                        val selected = destination?.hasRoute(top.route::class) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(top.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (selected) top.selected else top.unselected, contentDescription = null) },
                            label = { Text(stringResource(top.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier,
        ) {
            composable<HomeRoute> { HomeScreen(contentPadding = padding) }
            composable<MilestonesRoute> {
                MilestonesScreen(
                    contentPadding = padding,
                    onAdd = { navController.navigate(SpecialDayEditRoute()) },
                    onEdit = { id -> navController.navigate(SpecialDayEditRoute(id)) },
                )
            }
            composable<SettingsRoute> { SettingsScreen(contentPadding = padding) }
            composable<SpecialDayEditRoute> { entry ->
                val route = entry.toRoute<SpecialDayEditRoute>()
                SpecialDayEditScreen(isNew = route.id == 0L, onDone = { navController.popBackStack() })
            }
        }
    }
}
