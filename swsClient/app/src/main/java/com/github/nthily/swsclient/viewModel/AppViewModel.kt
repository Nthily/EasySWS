package com.github.nthily.swsclient.viewModel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class AppViewModel(
    application: Application
) : AndroidViewModel(application) {
    var currentPage = mutableStateOf(0) // 当前选中的底部导航页面

}
