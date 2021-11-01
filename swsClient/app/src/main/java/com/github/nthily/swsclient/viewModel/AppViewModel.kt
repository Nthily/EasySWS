package com.github.nthily.swsclient.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class AppViewModel(
    application: Application
) : AndroidViewModel(application) {

}

sealed class Screen(val route: String) {
    object Bluetooth: Screen("bluetooth")
    object Console : Screen("console")
}
