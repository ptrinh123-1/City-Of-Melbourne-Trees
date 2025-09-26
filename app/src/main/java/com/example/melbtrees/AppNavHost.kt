package com.example.melbtrees

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.melbtrees.ui.screens.TreeDetailScreen
import com.example.melbtrees.ui.screens.TreeListScreen
import com.example.melbtrees.ui.screens.MainScreen
import com.example.melbtrees.ui.viewmodel.TreeViewModel
import android.util.Log

@Composable
fun AppNavHost(viewModel: TreeViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mainScreen") {
        composable("mainScreen") {
            MainScreen(
                viewModel = viewModel,
                onTreeClick = { treeId ->
                    Log.d("AppNavHost", "Tree clicked with ID: $treeId")
                    navController.navigate("treeDetail/$treeId")
                }
            )
        }
        composable(
            route = "treeDetail/{treeId}",
            arguments = listOf(navArgument("treeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val treeId = backStackEntry.arguments?.getString("treeId")
            TreeDetailScreen(
                viewModel = viewModel,
                treeId = treeId,
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}