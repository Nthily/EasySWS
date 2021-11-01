package com.github.nthily.swsclient.components

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class Navigator private constructor(
    context: Context
) : DefaultLifecycleObserver {

    private val _navigateFlow = MutableSharedFlow<NavigateEventInfo>(extraBufferCapacity = 1)

    val navigateFlow = _navigateFlow.asSharedFlow()


    fun bind(activity: ComponentActivity) {
        activity.lifecycle.addObserver(this)
    }

    suspend fun navigate(destination: String) = coroutineScope {
        _navigateFlow.emit(NavigateEvent(destination))
    }

    suspend fun back() = coroutineScope {
        _navigateFlow.emit(NavigateBackEvent)
    }


    companion object {

        private const val TAG = "Navigator"

        @Volatile
        private var _instance: Navigator? = null


        fun getInstance(): Navigator? {
            return _instance
        }

        fun getInstance(context: Context): Navigator {
            return _instance ?: synchronized(this) {
                val instance = Navigator(context)
                _instance = instance
                instance
            }
        }


        sealed interface EventInfo
        object Event : EventInfo

        sealed interface NavigateEventInfo : EventInfo
        data class NavigateEvent(val destination: String) : NavigateEventInfo
        object NavigateBackEvent : NavigateEventInfo

    }

}
