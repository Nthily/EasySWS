package com.github.nthily.swsclient.page.console.solution

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavBackStackEntry
import com.github.nthily.swsclient.AppScreen
import com.github.nthily.swsclient.components.SteeringSensor
import com.github.nthily.swsclient.ui.components.ComposeVerticalSlider
import com.github.nthily.swsclient.ui.components.rememberComposeVerticalSliderState
import com.github.nthily.swsclient.utils.Utils.findActivity
import com.github.nthily.swsclient.viewModel.ConsoleViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConsoleTwo(
    consoleViewModel: ConsoleViewModel,
    navBackStackEntry: NavBackStackEntry,
    quit: () -> Unit
) {

    val throttleState = rememberComposeVerticalSliderState()
    val throttleValue by remember { consoleViewModel.throttleValue }

    val brakeState = rememberComposeVerticalSliderState()
    val brakeValue by remember { consoleViewModel.brakeValue }

    val context = LocalContext.current

    DisposableEffect(true) {
        // 当进入这个 Composable
        SteeringSensor.getInstance()?.start()

        onDispose { // 当离开这个 Composable
            SteeringSensor.getInstance()?.stop() // 取消传感器监听器
        }
    }

    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose { }
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }

    ConstraintLayout(Modifier.fillMaxSize()) {

        val (throttle, brake) = createRefs()

        Box(
            modifier = Modifier.constrainAs(brake) {
                end.linkTo(throttle.start, margin = 48.dp)
                centerVerticallyTo(parent)
            }
        ) {
            ComposeVerticalSlider( // 刹车
                state = brakeState,
                progressValue = (brakeValue * 100).toInt(),
                onProgressChanged =  {
                    consoleViewModel.brakeValue.value = it / 100f
                    consoleViewModel.sendBrakeValue(brakeValue)
                }
            ) {
                consoleViewModel.brakeValue.value = 0f
                brakeState.update(0)
                consoleViewModel.sendBrakeValue(brakeValue)
            }
        }

        Box(
            modifier = Modifier.constrainAs(throttle) {
                end.linkTo(parent.end, margin = 16.dp)
                centerVerticallyTo(parent)
            }
        ) {
            ComposeVerticalSlider( // 油门
                state = throttleState,
                progressValue = (throttleValue * 100).toInt(),
                onProgressChanged =  {
                    consoleViewModel.throttleValue.value = it / 100f
                    consoleViewModel.sendThrottleValue(throttleValue)
                }
            ) {
                consoleViewModel.throttleValue.value = 0f
                throttleState.update(0)
                consoleViewModel.sendThrottleValue(throttleValue)
            }
        }
    }

    BackHandler(
        enabled = navBackStackEntry.destination.route == AppScreen.console.route
    ) {
        quit()
    }


}
