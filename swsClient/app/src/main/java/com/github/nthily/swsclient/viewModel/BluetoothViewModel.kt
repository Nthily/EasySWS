package com.github.nthily.swsclient.viewModel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.nthily.swsclient.components.BluetoothCenter
import com.github.nthily.swsclient.components.DataClient
import com.github.nthily.swsclient.components.Navigator
import com.github.nthily.swsclient.utils.Utils
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException

class BluetoothViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = getApplication<Application>()

    private val uuid = "00001101-0000-1000-8000-00805F9B34FB"   // 接收端蓝牙服务 UUID
    private var isBondingAnyDevice = false // 当前是否有蓝牙设备正在配对，如果没有的话则可以配对设备

    val bthName = mutableStateOf("") // 蓝牙设备名
    val pairedDevices = mutableStateListOf<BluetoothDevice>() // 已配对的蓝牙设备列表
    val scannedDevices = mutableStateListOf<BluetoothDevice>() // 已扫描到的蓝牙设备列表

    var bthReady = mutableStateOf(false) // 蓝牙是否可用
    var bthEnabled = mutableStateOf(false) // 蓝牙是否已启用
    var showMacAddress = mutableStateOf(false) // 是否显示 mac 地址
    var bthDiscovering = mutableStateOf(false) // 是否正在搜索蓝牙设备

    var selectedPairedDevice = mutableStateOf<BluetoothDevice?>(null) // 当前被选中的已配对的蓝牙设备，用于底部弹窗

    init {
        viewModelScope.apply {

            // 监听接收端返回的数据
            launch {
                DataClient.getInstance()?.responseFlow?.collect { e ->
                    val msg = e.response.decodeToString()
                    Utils.log("服务器返回 $msg")
                    if (msg == "connected") {
                        viewModelScope.launch(Dispatchers.Main) {
                            Navigator.getInstance()?.navigate(Screen.Console.route)
                        }
                    }
                }
            }

            // 监听断连
            launch {
                DataClient.getInstance()?.disconnectFlow?.collect { event ->
                    Utils.log("已断开连接，原因：${event.cause}")
                    Navigator.getInstance()?.back()
                }
            }

            // 监听发现到的蓝牙设备
            launch {
                BluetoothCenter.getInstance()?.deviceFoundFlow?.collect { e ->
                    Log.d("AppViewModel", "发现了设备 ${e.device.name} (${e.device.address})")
                    // sort device from a -> z, not null -> null
                    // 排序，从 a -> z, 有名字到无名字的蓝牙设备

                    if (!pairedDevices.contains(e.device) && !scannedDevices.contains(e.device)) {
                        if (scannedDevices.isEmpty()) scannedDevices.add(e.device)
                        else {
                            val size = scannedDevices.size
                            for (index in 0 until scannedDevices.size) {
                                val device = scannedDevices[index]
                                if (e.device.name != null && device.name == null) continue
                                else if (e.device.name == null) {
                                    scannedDevices.add(scannedDevices.size, e.device)
                                    break
                                } else if (device.name != null && e.device.name.compareTo(
                                        device.name,
                                        true
                                    ) < 0
                                ) {
                                    scannedDevices.add(index, e.device)
                                    break
                                }
                            }
                            // If none of the devices in scannedDevices has a name, add it to the beginning
                            // 如果当前已扫描出来的蓝牙设备都没有名字，就添加到最前面
                            if (size == scannedDevices.size) scannedDevices.add(0, e.device)
                        }
                    }
                }
            }

            // 监听蓝牙开启
            launch {
                BluetoothCenter.getInstance()?.enableFlow?.collect { _ ->
                    Log.d("123", "监听到蓝牙被打开了")
                    bthEnabled.value = true
                    getBondedDevices()
                }
            }

            // 监听蓝牙关闭
            launch {
                BluetoothCenter.getInstance()?.disableFlow?.collect { _ ->
                    bthEnabled.value = false
                    Log.d("123", "监听到蓝牙被关闭了")
                }
            }

            // 监听设备发现开启
            launch {
                BluetoothCenter.getInstance()?.discoveryStartFlow?.collect {
                    Log.d("AppViewModel", "已开启设备发现")
                    bthDiscovering.value = true
                }
            }

            // 监听设备发现关闭
            launch {
                BluetoothCenter.getInstance()?.discoveryEndFlow?.collect {
                    Log.d("AppViewModel", "已停止设备发现")
                    bthDiscovering.value = false
                }
            }

            // 监听设备已绑定事件
            launch {
                BluetoothCenter.getInstance()?.deviceBoundFlow?.collect { e ->
                    pairedDevices.add(e.device)
                    scannedDevices.remove(e.device)
                    isBondingAnyDevice = false
                }
            }

            // 监听设备正在绑定事件
            launch {
                BluetoothCenter.getInstance()?.deviceBindingFlow?.collect { e ->
                    scannedDevices.remove(e.device)
                    scannedDevices.add(0, e.device)
                }
            }

            // 监听设备未绑定或绑定失败事件
            launch {
                BluetoothCenter.getInstance()?.deviceUnboundFlow?.collect { e ->
                    if (e.failed) {
                        scannedDevices.remove(e.device)
                        scannedDevices.add(e.device)
                        isBondingAnyDevice = false
                    } else {
                        pairedDevices.remove(e.device)
                    }
                }
            }

            // 初始化界面时，获取蓝牙信息
            launch {
                bthReady.value = true
                bthEnabled.value = BluetoothCenter.getInstance()?.enabled ?: false
                bthName.value = BluetoothCenter.getInstance()?.name ?: "Phone"
                getBondedDevices()
            }

        }
    }

    private fun getBondedDevices() {
        BluetoothCenter.getInstance()?.boundDevices?.forEach {
            if(!pairedDevices.contains(it)) pairedDevices.add(it)
        }
    }

    fun enableBluetooth() {
        BluetoothCenter.getInstance()?.enable()
    }

    fun disableBluetooth() {
        BluetoothCenter.getInstance()?.disable()
    }

    fun startDiscovery() {
        scannedDevices.clear()
        BluetoothCenter.getInstance()?.startDiscovery()
    }

    fun stopDiscovery() {
        BluetoothCenter.getInstance()?.stopDiscovery()
    }

    fun bindDevice(device: BluetoothDevice) {
        if(!isBondingAnyDevice) {
            BluetoothCenter.getInstance()?.bind(device)
            isBondingAnyDevice = true
        }
    }

    fun unbindDevice(device: BluetoothDevice) {
        BluetoothCenter.getInstance()?.unbind(device)
    }

    fun connectByBluetooth() {
        viewModelScope.launch {
            selectedPairedDevice.value?.let {
                try {
                    DataClient.getInstance()?.connect(it, UUID.fromString(uuid))
                } catch(ex: IOException) {
                    Toast.makeText(app.applicationContext, "接收端未开启", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            DataClient.getInstance()?.disconnect()
            Navigator.getInstance()?.back()
        }
    }

    companion object {

        private const val TAG = "BluetoothViewModel"

    }

}
