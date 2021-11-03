package com.github.nthily.swsclient.ui.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(
    navController: NavController,
    pages: List<Screen>,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomNavigation(
        backgroundColor = Color.White
    ) {
        pages.forEach { screen ->
            BottomNavigationItem(
                selected = currentDestination?.hierarchy?.any { it -> it.route == screen.route } == true,
                icon = {
                    Icon(painterResource(id = screen.icon!!), null)
                },
                label = {
                    Text(screen.name!!)
                },
                onClick = {
                    if(currentDestination!!.route != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(currentDestination.route!!) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }
    }
}

data class Screen(
    val route: String,
    val name: String? = null,
    val icon: Int? = null,
    val showBottomBar: Boolean = true
)
