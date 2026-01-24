package com.rhinepereira.saints.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rhinepereira.saints.category.CategoryScreen
import com.rhinepereira.saints.category.CategoryViewModel
import com.rhinepereira.saints.content.ContentScreen
import com.rhinepereira.saints.content.ContentViewModel
import com.rhinepereira.saints.content.DailyFeastScreen
import com.rhinepereira.saints.content.DailyFeastViewModel
import com.rhinepereira.saints.content.DailyReadingsScreen
import com.rhinepereira.saints.content.DailyReadingsViewModel
import com.rhinepereira.saints.data.repository.ContentRepository
import com.rhinepereira.saints.home.HomeScreen
import com.rhinepereira.saints.home.HomeViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rhinepereira.saints.ccc.CCCScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: ContentRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return HomeViewModel(repository) as T
                    }
                }
            )
            HomeScreen(
                viewModel = viewModel,
                onCategoryClick = { categoryId ->
                    when (categoryId) {
                        "daily-feast" -> navController.navigate("daily-feast-content")
                        "daily-readings" -> navController.navigate("daily-readings-content")
                        "ccc" -> navController.navigate("ccc-content")
                        else -> navController.navigate(Screen.Category.createRoute(categoryId))
                    }
                }
            )
        }

        composable("ccc-content") {
            CCCScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("daily-feast-content") {
            val viewModel: DailyFeastViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return DailyFeastViewModel(repository) as T
                    }
                }
            )
            DailyFeastScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "daily-readings-content",
            deepLinks = listOf(
                navDeepLink { uriPattern = "saints://daily-readings" }
            )
        ) {
            val viewModel: DailyReadingsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return DailyReadingsViewModel(repository) as T
                    }
                }
            )
            DailyReadingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Category.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "saints://category/{categoryId}" }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
            val viewModel: CategoryViewModel = viewModel(
                key = categoryId,
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return CategoryViewModel(repository, categoryId) as T
                    }
                }
            )
            CategoryScreen(
                viewModel = viewModel,
                onItemClick = { contentId ->
                    navController.navigate(Screen.Content.createRoute(categoryId, contentId))
                }
            )
        }

        composable(
            route = Screen.Content.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("contentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
            val contentId = backStackEntry.arguments?.getString("contentId") ?: return@composable
            val viewModel: ContentViewModel = viewModel(
                key = contentId,
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ContentViewModel(repository, categoryId, contentId) as T
                    }
                }
            )
            ContentScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
