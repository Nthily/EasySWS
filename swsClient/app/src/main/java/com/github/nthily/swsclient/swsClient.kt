package com.github.nthily.swsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.nthily.swsclient.components.SteeringSensor
import com.github.nthily.swsclient.page.bluetooth.Bluetooth
import com.github.nthily.swsclient.page.console.Console
import com.github.nthily.swsclient.ui.theme.SwsClientTheme
import com.github.nthily.swsclient.utils.Sender
import com.github.nthily.swsclient.utils.Utils
import com.github.nthily.swsclient.viewModel.AppViewModel
import com.github.nthily.swsclient.viewModel.ConsoleViewModel
import com.github.nthily.swsclient.viewModel.Screen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.lang.Exception
import android.annotation.SuppressLint
import android.view.WindowManager
import java.lang.reflect.Method


class MainActivity : ComponentActivity(){

    private val appViewModel by viewModels<AppViewModel>()
    private val consoleViewModel by viewModels<ConsoleViewModel>()

    @ExperimentalComposeUiApi
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        SteeringSensor.getInstance(applicationContext) { steering ->
            appViewModel.mBluetoothSocket.outputStream.write(Sender.getSensorData(steering))
        }.bind(this)

        lifecycle.addObserver(appViewModel)
        lifecycle.addObserver(consoleViewModel)

        setContent {

            SwsClientTheme {

                val navController = rememberNavController()
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = MaterialTheme.colors.isLight

                SideEffect {
                    systemUiController.setStatusBarColor(Color.Transparent, useDarkIcons)
                }

                NavHost(navController = navController, startDestination = Screen.Bluetooth.route) {
                    composable(Screen.Bluetooth.route) {
                        Bluetooth(appViewModel, navController)
                    }
                    composable(Screen.Console.route) {
                        Console(appViewModel, consoleViewModel, navController)
                    }
                }
            }
        }
    }
}
