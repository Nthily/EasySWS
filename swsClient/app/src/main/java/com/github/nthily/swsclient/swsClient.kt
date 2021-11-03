package com.github.nthily.swsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.github.nthily.swsclient.ui.components.BottomBar
import com.github.nthily.swsclient.ui.components.Screen
import com.github.nthily.swsclient.ui.components.SheetContent
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val systemUiController = rememberSystemUiController()
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val useDarkIcons = MaterialTheme.colors.isLight

    val appPages = listOf(AppScreen.bluetooth, AppScreen.network, AppScreen.settings)

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

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            SheetContent(
                sheetState = sheetState,
                bluetoothViewModel = bluetoothViewModel
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if(currentDestination?.route != AppScreen.console.route) {
                    BottomBar(
                        navController= navController,
                        pages = appPages,
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            NavHost(navController = navController, startDestination = AppScreen.bluetooth.route) {
                composable(AppScreen.bluetooth.route) {
                    Bluetooth(bluetoothViewModel, sheetState)
                }
                composable(AppScreen.console.route) {
                    Console(consoleViewModel, it) { bluetoothViewModel.disconnect() }
                }
                composable(AppScreen.network.route) {
                    NetWork()
                }
                composable(AppScreen.settings.route) {
                    Settings()
                }
            }
        }
    }
}

object AppScreen {
    val bluetooth = Screen("bluetooth", name = "蓝牙", icon = R.drawable.bluetooth)
    val console =  Screen("console", showBottomBar = false)
    val network = Screen("network", name = "网络", icon = R.drawable.wifi)
    val settings =  Screen("settings", name = "设置", icon = R.drawable.settings)
}
