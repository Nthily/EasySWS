package com.github.nthily.swsclient.utils

import android.bluetooth.BluetoothDevice

fun BluetoothDevice.removeBond() {
    this.javaClass.getMethod("removeBond").invoke(this)
}
