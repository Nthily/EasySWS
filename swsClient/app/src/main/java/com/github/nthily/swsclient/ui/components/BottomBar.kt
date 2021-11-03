package com.github.nthily.swsclient.ui.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.nthily.swsclient.R

@Composable
fun BottomBar(
    navController: NavController
) {

    val items = listOf(Screen.Bluetooth, Screen.Network, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomNavigation(
        backgroundColor = Color.White
    ) {
        items.forEach { screen ->
            BottomNavigationItem(
                selected = currentDestination?.hierarchy?.any { it -> it.route == screen.route } == true,
                icon = {
                    when (screen) {
                        is Screen.Bluetooth -> Icon(
                            painter = painterResource(id = R.drawable.bluetooth),
                            contentDescription = null
                        )
                        is Screen.Network -> Icon(
                            painter = painterResource(id = R.drawable.wifi),
                            contentDescription = null
                        )
                        is Screen.Settings -> Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null
                        )
                    }
                },
                label = {
                    when (screen) {
                        is Screen.Bluetooth -> Text("蓝牙")
                        is Screen.Network -> Text("网络")
                        is Screen.Settings -> Text("设置")
                    }
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

sealed class Screen(val route: String) {
    object Bluetooth: Screen("bluetooth")
    object Console : Screen("console")
    object Network: Screen("network")
    object Settings: Screen("settings")
}