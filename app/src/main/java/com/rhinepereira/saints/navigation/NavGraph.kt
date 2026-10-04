package com.rhinepereira.saints.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navDeepLink
import com.rhinepereira.saints.AppLanguage
import com.rhinepereira.saints.R
import com.rhinepereira.saints.app
import com.rhinepereira.saints.ccc.CCCScreen
import com.rhinepereira.saints.ccc.CCCViewModel
import com.rhinepereira.saints.content.ContentScreen
import com.rhinepereira.saints.content.ContentViewModel
import com.rhinepereira.saints.library.CategoryScreen
import com.rhinepereira.saints.library.CategoryViewModel
import com.rhinepereira.saints.library.LibraryScreen
import com.rhinepereira.saints.library.LibraryViewModel
import com.rhinepereira.saints.nameday.NameDayScreen
import com.rhinepereira.saints.today.TodayScreen
import com.rhinepereira.saints.today.TodayViewModel
import java.util.Locale

private enum class Tab(val route: String, val label: Int, val icon: ImageVector) {
    TODAY("today", R.string.tab_today, Icons.Outlined.WbTwilight),
    LIBRARY("library", R.string.tab_library, Icons.Outlined.LocalLibrary),
    CATECHISM("catechism", R.string.tab_catechism, Icons.Outlined.AutoStories)
}

@Composable
fun NavGraph(navController: NavHostController, language: AppLanguage, locale: Locale) {
    val app = LocalContext.current.app
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val showBar = Tab.entries.any { tab -> destination?.route == tab.route || destination?.route?.startsWith("category/") == true }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { it.route == tab.route } == true ||
                                (tab == Tab.LIBRARY && destination?.route?.startsWith("category/") == true),
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { outer ->
        Box(Modifier.fillMaxSize().padding(bottom = outer.calculateBottomPadding())) {
            NavHost(
                navController = navController,
                startDestination = Tab.TODAY.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                composable(
                    Tab.TODAY.route,
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "saints://today" },
                        navDeepLink { uriPattern = "saints://daily-readings" }
                    )
                ) {
                    val vm: TodayViewModel = viewModel(factory = viewModelFactory {
                        initializer { TodayViewModel(app.calendar, app.readings, app.nameDays) }
                    })
                    TodayScreen(
                        viewModel = vm,
                        locale = locale,
                        onToggleLanguage = {
                            app.language.set(if (language == AppLanguage.ENGLISH) AppLanguage.MARATHI else AppLanguage.ENGLISH)
                        },
                        onOpenNameDays = { navController.navigate("namedays") }
                    )
                }

                composable(Tab.LIBRARY.route) {
                    val vm: LibraryViewModel = viewModel(factory = viewModelFactory {
                        initializer { LibraryViewModel(app.content) }
                    })
                    LibraryScreen(vm, onOpen = { navController.navigate("category/$it") })
                }

                composable(
                    "category/{categoryId}",
                    deepLinks = listOf(navDeepLink { uriPattern = "saints://category/{categoryId}" })
                ) { entry ->
                    val id = entry.arguments?.getString("categoryId").orEmpty()
                    val vm: CategoryViewModel = viewModel(key = id, factory = viewModelFactory {
                        initializer { CategoryViewModel(app.content, id) }
                    })
                    CategoryScreen(vm, onBack = { navController.popBackStack() }, onOpen = { navController.navigate("content/$id/$it") })
                }

                composable("content/{categoryId}/{contentId}") { entry ->
                    val categoryId = entry.arguments?.getString("categoryId").orEmpty()
                    val contentId = entry.arguments?.getString("contentId").orEmpty()
                    val vm: ContentViewModel = viewModel(key = contentId, factory = viewModelFactory {
                        initializer { ContentViewModel(app.content, categoryId, contentId) }
                    })
                    ContentScreen(vm, language, onBack = { navController.popBackStack() })
                }

                composable(Tab.CATECHISM.route) {
                    val vm: CCCViewModel = viewModel(factory = viewModelFactory {
                        initializer { CCCViewModel(app.assets) }
                    })
                    CCCScreen(vm)
                }

                composable("namedays") {
                    NameDayScreen(app.nameDays, app.calendar.saints, locale, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
