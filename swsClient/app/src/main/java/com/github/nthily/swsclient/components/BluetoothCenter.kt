package com.github.nthily.swsclient.components

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.github.nthily.swsclient.utils.removeBond
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BluetoothCenter private constructor(
    context: Context
) : BroadcastReceiver(), DefaultLifecycleObserver {

    private val _enableFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private val _disableFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private val _discoveryStartFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private val _discoveryEndFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private val _deviceFoundFlow = MutableSharedFlow<DeviceFoundEvent>(extraBufferCapacity = 1)
    private val _deviceBoundFlow = MutableSharedFlow<DeviceBoundEvent>(extraBufferCapacity = 1)
    private val _deviceBindingFlow = MutableSharedFlow<DeviceBindingEvent>(extraBufferCapacity = 1)
    private val _deviceUnboundFlow = MutableSharedFlow<DeviceUnboundEvent>(extraBufferCapacity = 1)
    private val _deviceConnectFlow = MutableSharedFlow<DeviceConnectEvent>(extraBufferCapacity = 1)
    private val _deviceDisconnectFlow = MutableSharedFlow<DeviceDisconnectEvent>(extraBufferCapacity = 1)
    private val _deviceRenameFlow = MutableSharedFlow<DeviceRenameEvent>(extraBufferCapacity = 1)

    val enableFlow = _enableFlow.asSharedFlow()
    val disableFlow = _disableFlow.asSharedFlow()
    val discoveryStartFlow = _discoveryStartFlow.asSharedFlow()
    val discoveryEndFlow = _discoveryEndFlow.asSharedFlow()
    val deviceFoundFlow = _deviceFoundFlow.asSharedFlow()
    val deviceBoundFlow = _deviceBoundFlow.asSharedFlow()
    val deviceBindingFlow = _deviceBindingFlow.asSharedFlow()
    val deviceUnboundFlow = _deviceUnboundFlow.asSharedFlow()
    val deviceConnectFlow = _deviceConnectFlow.asSharedFlow()
    val deviceDisconnectFlow = _deviceDisconnectFlow.asSharedFlow()
    val deviceRenameFlow = _deviceRenameFlow.asSharedFlow()

    private val _appContext = context.applicationContext
    private val _bthManager = _appContext.getSystemService(
        BLUETOOTH_SERVICE) as BluetoothManager
    private val _bthAdapter = _bthManager.adapter
    private val _eventScope = CoroutineScope(Job() + Dispatchers.Default)

    val name: String get() = _bthAdapter.name
    val address get() = _bthAdapter.address
    val boundDevices: MutableSet<BluetoothDevice> get() = _bthAdapter.bondedDevices
    val enabled get() = _bthAdapter.isEnabled
    val discovering get() = _bthAdapter.isDiscovering


    init {
        if (_bthAdapter == null)
            throw IllegalStateException("Bluetooth adapter is not found")
    }


    fun bind(activity: ComponentActivity) {
        activity.lifecycle.addObserver(this)
    }

    fun enable() {
        if (enabled) return

        _bthAdapter.enable()
    }

    fun disable() {
        if (!enabled) return

        _bthAdapter.disable()
    }

    fun startDiscovery() {
        if (discovering) return

        _bthAdapter.startDiscovery()
    }

    fun stopDiscovery() {
        if (!discovering) return

        _bthAdapter.cancelDiscovery()
    }

    fun getDevice(address: String) {
        _bthAdapter.getRemoteDevice(address)
    }

    fun bind(device: BluetoothDevice) {
        device.createBond()
    }

    fun unbind(device: BluetoothDevice) {
        device.removeBond()
    }


    // DefaultLifecycleObserver

    override fun onCreate(owner: LifecycleOwner) {
        _appContext.registerReceiver(
            this,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_NAME_CHANGED)
            }
        )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        _appContext.unregisterReceiver(this)
    }


    // BroadcastReceiver

    override fun onReceive(context: Context?, intent: Intent?) {
        _eventScope.launch {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)

                    when (state) {
                        BluetoothAdapter.STATE_ON -> {
                            _enableFlow.emit(Event)
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            _disableFlow.emit(Event)
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _discoveryStartFlow.emit(Event)
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _discoveryEndFlow.emit(Event)
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE)!!

                    _deviceFoundFlow.emit(DeviceFoundEvent(device))
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE)!!
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val prevState =
                        intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                    when (state) {
                        BluetoothDevice.BOND_BONDED -> {
                            _deviceBoundFlow.emit(DeviceBoundEvent(device))
                        }
                        BluetoothDevice.BOND_NONE -> {
                            _deviceUnboundFlow.emit(
                                DeviceUnboundEvent(
                                    device,
                                    prevState == BluetoothDevice.BOND_BONDING
                                )
                            )
                        }
                        BluetoothDevice.BOND_BONDING -> {
                            _deviceBindingFlow.emit(
                                DeviceBindingEvent(
                                    device
                                )
                            )
                        }
                    }
                }

                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE)!!

                    _deviceConnectFlow.emit(DeviceConnectEvent(device))
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE)!!

                    _deviceDisconnectFlow.emit(DeviceDisconnectEvent(device))
                }

                BluetoothDevice.ACTION_NAME_CHANGED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE)!!
                    val name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME)!!

                    _deviceRenameFlow.emit(DeviceRenameEvent(device, name))
                }
            }
        }
    }


    companion object {

        private const val TAG = "BluetoothCenter"

        @Volatile
        private var _instance: BluetoothCenter? = null


        fun getInstance(): BluetoothCenter? {
            return _instance
        }

        fun getInstance(context: Context): BluetoothCenter {
            return _instance ?: synchronized(this) {
                val instance = BluetoothCenter(context)
                _instance = instance
                instance
            }
        }


        sealed interface EventInfo
        object Event : EventInfo

        data class DeviceFoundEvent(val device: BluetoothDevice) : EventInfo
        data class DeviceBoundEvent(val device: BluetoothDevice) : EventInfo
        data class DeviceBindingEvent(val device: BluetoothDevice) : EventInfo
        data class DeviceUnboundEvent(val device: BluetoothDevice, val failed: Boolean) : EventInfo
        data class DeviceConnectEvent(val device: BluetoothDevice) : EventInfo
        data class DeviceDisconnectEvent(val device: BluetoothDevice) : EventInfo
        data class DeviceRenameEvent(val device: BluetoothDevice, val name: String) : EventInfo

    }
}
