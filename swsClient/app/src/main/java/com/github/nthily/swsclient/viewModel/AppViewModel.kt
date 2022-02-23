package com.github.nthily.swsclient.viewModel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class AppViewModel(
    application: Application
) : AndroidViewModel(application) {
    var text = mutableStateOf("")
}
