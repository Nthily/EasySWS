package com.github.nthily.swsclient.components

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class Permission {

    fun request(activity: ComponentActivity, block: () -> Unit) {

        val requestPermission = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it.values.forEach { permission ->
                    if (!permission) activity.finish()
                }
            } else {
                if (it[Manifest.permission.ACCESS_FINE_LOCATION] == false) activity.finish()
            }
            block()
        }

        requestPermission.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        )
    }
}
