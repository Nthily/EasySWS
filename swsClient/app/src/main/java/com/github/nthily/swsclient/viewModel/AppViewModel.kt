package com.github.nthily.swsclient.viewModel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context.BLUETOOTH_SERVICE
import android.content.IntentFilter
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.github.nthily.swsclient.utils.BluetoothReceiver
import com.github.nthily.swsclient.utils.Utils
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(
    application: Application
) : AndroidViewModel(application), DefaultLifecycleObserver {

    private val app = getApplication<Application>()

    private val bthManager = app.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    private val bthAdapter: BluetoothAdapter? = bthManager.adapter // 蓝牙适配器，用于检测手机是否有蓝牙等

    private val uuid = "00001101-0000-1000-8000-00805F9B34FB"
    private var isBondingAnyDevice = false // 当前是否有蓝牙设备正在配对，如果没有的话则可以配对设备

    val bthDevice = bthAdapter?.name // 蓝牙设备名
    val pairedDevices = mutableStateListOf<BluetoothDevice>() // 已配对的蓝牙设备列表
    val scannedDevices = mutableStateListOf<BluetoothDevice>() // 已扫描到的蓝牙设备列表

    lateinit var mBluetoothSocket: BluetoothSocket
    var bthReady = mutableStateOf(false) // 蓝牙是否可用
    var bthEnabled = mutableStateOf(false) // 蓝牙是否已启用
    var showMacAddress = mutableStateOf(false) // 是否显示 mac 地址
    var bthDiscovering = mutableStateOf(false) // 是否正在搜索蓝牙设备
    var bthDeviceConnectState = mutableStateOf(false)

    var selectedPairedDevice = mutableStateOf<BluetoothDevice?>(null) // 当前被选中的已配对的蓝牙设备，用于底部弹窗
    var serverEnabled = mutableStateOf(false) // 服务器是否已经开启

    private val bluetoothReceiver = BluetoothReceiver(
        onDeviceBondStateChanged = { device, state, prevState ->
            when(state) {
                BluetoothDevice.BOND_NONE -> {
                    if(prevState == BluetoothDevice.BOND_BONDING) {
                       // recompose 重组
                        scannedDevices.remove(device)
                        scannedDevices.add(device)
                        isBondingAnyDevice = false
                    } else {
                        pairedDevices.remove(device)
                    }
                }
                BluetoothDevice.BOND_BONDED -> {
                    pairedDevices.add(device)
                    scannedDevices.remove(device)
                    isBondingAnyDevice = false
                }
                BluetoothDevice.BOND_BONDING -> {
                    // recompose
                    scannedDevices.remove(device)
                    scannedDevices.add(0, device)
                }
            }
        },
        onDiscoveryStarted = { bthDiscovering.value = true },
        onDiscoveryFinished = { bthDiscovering.value = false },
        onFoundDevice = {

            // sort device from a -> z, not null -> null
            // 排序，从 a -> z, 有名字到无名字的蓝牙设备

            if(!pairedDevices.contains(it) && !scannedDevices.contains(it)) {
                if(scannedDevices.isEmpty()) scannedDevices.add(it)
                else {
                    val size = scannedDevices.size
                    for(index in 0 until scannedDevices.size) {
                        val device = scannedDevices[index]
                        if(it.name != null && device.name == null) continue
                        else if(it.name == null) {
                            scannedDevices.add(scannedDevices.size, it)
                            break
                        }
                        else if(device.name != null && it.name.compareTo(device.name, true) < 0) {
                            scannedDevices.add(index, it)
                            break
                        }
                    }
                    // If none of the devices in scannedDevices has a name, add it to the beginning
                    // 如果当前已扫描出来的蓝牙设备都没有名字，就添加到最前面
                    if(size == scannedDevices.size) scannedDevices.add(0, it)
                }
            }
        },
        onBluetoothConnected = {
            getBondedDevices()
            bthEnabled.value = true
        },
        onBluetoothDisconnected = { bthEnabled.value = false },
        onAnyBluetoothDeviceConnectionStateChanged = { bthDeviceConnectState.value = it }
    )

    // MainActivity
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        app.registerReceiver(
            bluetoothReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )

        if(bthAdapter != null) {
            bthReady.value = true
            bthEnabled.value = bthAdapter.isEnabled
            getBondedDevices()
        }
    }

    // MainActivity
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        app.unregisterReceiver(bluetoothReceiver)
    }

    private fun getBondedDevices() {
        bthAdapter!!.bondedDevices.forEach {
            if(!pairedDevices.contains(it)) pairedDevices.add(it)
        }
    }

    fun enableBluetooth() {
        bthAdapter?.enable()
    }

    fun disableBluetooth() {
        bthAdapter?.disable()
    }

    fun startDeviceScan() {
        scannedDevices.clear()
        bthAdapter?.startDiscovery()
    }

    fun stopDeviceScan() {
        bthAdapter?.cancelDiscovery()
    }

    fun bondDevice(device: BluetoothDevice) {
        if(!isBondingAnyDevice) {
            device.createBond()
            isBondingAnyDevice = true
        }
    }

    fun connectDevice(device: BluetoothDevice, navController: NavHostController) {
        val mBuffer = ByteArray(1024)
        if(bthDiscovering.value) stopDeviceScan()
        mBluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))

        viewModelScope.launch(Dispatchers.IO) {

            try {
                mBluetoothSocket.connect()

                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        while (true) {
                            val msgBytes = mBluetoothSocket.inputStream
                            val msg = mBuffer.decodeToString(endIndex = msgBytes.read(mBuffer))
                            if(msg == "connected") {
                                viewModelScope.launch { navController.navigate("console") }
                                serverEnabled.value = true
                            }
                        }
                    } catch (e: Exception) {
                        serverEnabled.value = false
                        viewModelScope.launch { navController.popBackStack() }
                    }
                }

            } catch (e: Exception) {
                viewModelScope.launch { Toast.makeText(app.applicationContext, "服务端未开启", Toast.LENGTH_LONG).show() }
            }
        }
    }
}

fun BluetoothDevice.removeBond() {
    this.javaClass.getMethod("removeBond").invoke(this)
}

sealed class Screen(val route: String) {
    object Bluetooth: Screen("bluetooth")
    object Console : Screen("console")
}
