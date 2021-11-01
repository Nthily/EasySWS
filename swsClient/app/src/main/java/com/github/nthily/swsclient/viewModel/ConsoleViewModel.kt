package com.github.nthily.swsclient.viewModel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.nthily.swsclient.components.DataClient
import com.github.nthily.swsclient.components.Sender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConsoleViewModel(
    application: Application,
) : AndroidViewModel(application) {

    var brakeValue = mutableStateOf(0f)
    var throttleValue = mutableStateOf(0f)

    fun sendUpShiftValue() {
        viewModelScope.launch {
            DataClient.getInstance()?.send(Sender.getUpShiftButtonsData(true))
            delay(150)
            DataClient.getInstance()?.send(Sender.getUpShiftButtonsData(false))
        }
    }

    fun sendDownShiftValue() {
        viewModelScope.launch {
            DataClient.getInstance()?.send(Sender.getDownShiftButtonsData(true))
            delay(150)
            DataClient.getInstance()?.send(Sender.getDownShiftButtonsData(false))
        }
    }

    fun sendThrottleValue(value: Float) {
        viewModelScope.launch {
            DataClient.getInstance()?.send(Sender.getThrottleData(value))
        }
    }

    fun sendBrakeValue(value: Float) {
        viewModelScope.launch {
            DataClient.getInstance()?.send(Sender.getBrakeData(value))
        }
    }

}
