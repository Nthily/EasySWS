package com.github.nthily.swsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.nthily.swsclient.components.BluetoothCenter
import com.github.nthily.swsclient.components.DataClient
import com.github.nthily.swsclient.components.Navigator
import com.github.nthily.swsclient.components.SteeringSensor
import com.github.nthily.swsclient.page.bluetooth.Bluetooth
import com.github.nthily.swsclient.page.console.Console
import com.github.nthily.swsclient.ui.theme.SwsClientTheme
import com.github.nthily.swsclient.components.Sender
import com.github.nthily.swsclient.page.network.NetWork
import com.github.nthily.swsclient.page.settings.Settings
import com.github.nthily.swsclient.ui.view.BottomBar
import com.github.nthily.swsclient.ui.view.Screen
import com.github.nthily.swsclient.viewModel.AppViewModel
import com.github.nthily.swsclient.viewModel.BluetoothViewModel
import com.github.nthily.swsclient.viewModel.ConsoleViewModel
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(){

    private val appViewModel by viewModels<AppViewModel>()
    private val bluetoothViewModel by viewModels<BluetoothViewModel>()
    private val consoleViewModel by viewModels<ConsoleViewModel>()

    @ExperimentalComposeUiApi
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化组件
        BluetoothCenter.getInstance(applicationContext).bind(this)
        SteeringSensor.getInstance(applicationContext).bind(this)
        DataClient.getInstance(applicationContext) {
            val buffer = ByteArray(1024)
            val len = read(buffer)
            buffer.sliceArray(0 until len)
        }.bind(this)
        Navigator.getInstance(applicationContext).bind(this)

        // 转发传感器数据
        lifecycleScope.launch {
            DataClient.getInstance()?.repeatWhenConnected {
                SteeringSensor.getInstance()?.steeringFlow?.collect { e ->
                    DataClient.getInstance()?.send(Sender.getSensorData(e.steering))
                }
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SwsClientTheme {
                ProvideWindowInsets {
                    App(bluetoothViewModel, consoleViewModel)
                }
            }
        }
    }

    companion object {

        private const val TAG = "MainActivity"

    }

}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun App(
    bluetoothViewModel: BluetoothViewModel,
    consoleViewModel: ConsoleViewModel
) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight

    SideEffect {
        systemUiController.setStatusBarColor(Color.Transparent, useDarkIcons)
    }

    LaunchedEffect(Unit) {
        Navigator.getInstance()?.navigateFlow?.collect { e ->
            when (e) {
                is Navigator.Companion.NavigateEvent -> {
                    navController.navigate(e.destination)
                }
                is Navigator.Companion.NavigateBackEvent -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController) },
        modifier = Modifier.fillMaxSize().systemBarsPadding()
    ) {
        NavHost(navController = navController, startDestination = Screen.Bluetooth.route) {
            composable(Screen.Bluetooth.route) {
                Bluetooth(bluetoothViewModel)
            }
            composable(Screen.Console.route) {
                Console(consoleViewModel, it) {
                    bluetoothViewModel.disconnect()
                }
            }
            composable(Screen.Network.route) {
                NetWork()
            }
            composable(Screen.Settings.route) {
                Settings()
            }
        }
    }
}
