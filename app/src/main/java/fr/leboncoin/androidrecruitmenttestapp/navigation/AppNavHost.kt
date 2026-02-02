package fr.leboncoin.androidrecruitmenttestapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.leboncoin.feature.album.ui.AlbumsScreen
import fr.leboncoin.feature.details.ui.SongDetailsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Albums,
        modifier = modifier
    ) {
        composable(Route.Albums) {
            AlbumsScreen(
                onItemSelected = { song ->
                    navController.navigate(Route.details(song.id))
                }
            )
        }
        composable(Route.Details) {
            SongDetailsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

object Route {
    const val Albums = "albums"
    const val Details = "details/{songId}"

    fun details(songId: Int) = "details/$songId"
}
