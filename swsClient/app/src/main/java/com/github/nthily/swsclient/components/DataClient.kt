package com.github.nthily.swsclient.components

import android.app.admin.ConnectEvent
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DataClient private constructor(
    context: Context,
    val responseHandler: InputStream.() -> ByteArray
) : DefaultLifecycleObserver {

    @Volatile
    var connected = false
        private set

    var peer: Peer? = null
        private set

    private val _connectFlow = MutableSharedFlow<ConnectEvent>(extraBufferCapacity = 1)
    private val _disconnectFlow = MutableSharedFlow<DisconnectEvent>(extraBufferCapacity = 1)
    private val _responseFlow = MutableSharedFlow<ResponseEvent>(extraBufferCapacity = 1)

    val connectFlow = _connectFlow.asSharedFlow()
    val disconnectFlow = _disconnectFlow.asSharedFlow()
    val responseFlow = _responseFlow.onSubscription { Log.d(TAG, "wtfman") }

    private var _inputStream: InputStream? = null
    private var _outputStream: OutputStream? = null
    private val _inputReadingScope = CoroutineScope(Job() + Dispatchers.IO)
    private val _connectionMutex = Mutex()


    fun bind(activity: ComponentActivity) {
        activity.lifecycle.addObserver(this)
    }

    suspend fun connect(device: BluetoothDevice, uuid: UUID) {
        _connect {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()
            peer = BluetoothPeer(device)
            _inputStream = socket.inputStream
            _outputStream = socket.outputStream
        }
    }

    suspend fun connect(endpoint: InetSocketAddress) {
        _connect {
            val socket = Socket()
            socket.connect(endpoint)
            peer = NetworkPeer(endpoint)
            _inputStream = socket.inputStream
            _outputStream = socket.outputStream
        }
    }

    suspend fun disconnect() {
        _disconnect()
    }

    suspend fun send(data: ByteArray) = withContext(Dispatchers.IO) {
        try {
            _outputStream?.write(data)
        } catch(ex: Exception) {
            _disconnect(ex)
        }
    }

    suspend fun repeatWhenConnected(block: suspend CoroutineScope.() -> Unit) = coroutineScope {
        while (isActive) {
            Log.d(TAG, "Waiting for connect")
            if (!connected) {
                suspendCoroutine<Unit> { cont ->
                    launch {
                        connectFlow.collectLatest {
                            cont.resume(Unit)
                            cancel()
                        }
                    }
                }
            }
            val job = launch(block = block)
            Log.d(TAG, "Waiting for disconnect")
            if (connected) {
                suspendCoroutine<Unit> { cont ->
                    launch {
                        disconnectFlow.collectLatest {
                            cont.resume(Unit)
                            cancel()
                        }
                    }
                }
            }
            job.cancel()
        }
    }

    private suspend inline fun _connect(crossinline block: () -> Unit) = withContext(Dispatchers.IO) {
        _connectionMutex.withLock {
            if (connected) return@withContext
            Log.d(TAG, "Trying to connect to peer")
            block()
            connected = true
            _connectFlow.emit(ConnectEvent(peer!!))
            Log.d(TAG, "Connected to peer")
            _inputReadingScope.launch {
                try {
                    while (isActive) {
                        Log.d(TAG, "Processing data from peer")
                        val response = _inputStream!!.responseHandler()
                        _responseFlow.emit(ResponseEvent(peer!!, response))
                    }
                } catch (ex: Exception) {
                    _disconnect(ex)
                }
            }
        }
    }

    private suspend fun _disconnect(cause: Exception? = null)
            = withContext(Dispatchers.IO) {
        _connectionMutex.withLock {
            if (!connected) return@withContext

            try { _inputReadingScope.coroutineContext.cancelChildren() } catch (_: Exception) {}
            try { _inputStream!!.close() } catch (_: Exception) {}
            try { _outputStream!!.close() } catch (_: Exception) {}

            connected = false
            val lastPeer = peer!!
            peer = null
            if (cause != null)
                Log.e(TAG, "Disconnected accidentally", cause)
            _disconnectFlow.emit(DisconnectEvent(lastPeer, cause))
        }
    }


    // DefaultLifecycleObserver

    override fun onResume(owner: LifecycleOwner) {

    }

    override fun onPause(owner: LifecycleOwner) {

    }


    companion object {

        private const val TAG = "DataClient"

        @Volatile
        private var _instance: DataClient? = null


        fun getInstance(): DataClient? {
            return _instance
        }

        fun getInstance(
            context: Context,
            responseHandler: InputStream.() -> ByteArray
        ): DataClient {
            return _instance ?: synchronized(this) {
                val instance = DataClient(context, responseHandler)
                _instance = instance
                instance
            }
        }


        sealed class Peer
        data class BluetoothPeer(val device: BluetoothDevice) : Peer()
        data class NetworkPeer(val endpoint: InetSocketAddress) : Peer()


        sealed interface EventInfo
        object Event : EventInfo

        data class ConnectEvent(val peer: Peer) : EventInfo
        data class DisconnectEvent(val peer: Peer, val cause: Exception? = null) : EventInfo
        data class ResponseEvent(val peer: Peer, val response: ByteArray) : EventInfo

    }

}
