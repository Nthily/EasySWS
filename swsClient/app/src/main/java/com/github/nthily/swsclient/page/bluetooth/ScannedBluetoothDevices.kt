package com.github.nthily.swsclient.page.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.nthily.swsclient.ui.components.SecondaryText

@Composable
fun ScannedBluetoothDevices(
    scannedDevices: SnapshotStateList<BluetoothDevice>,
    pairBluetoothDevice: (device: BluetoothDevice) -> Unit,
    displayMacAddress: Boolean
) {
    scannedDevices.forEach { item ->
        Spacer(Modifier.padding(vertical = 5.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            elevation = 5.dp
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pairBluetoothDevice(item) }
                    .animateContentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    GetBluetoothDeviceIcon(item)
                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                    Column {
                        item.name?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.h6
                            )
                        }
                        if(item.name == null) {
                            item.address?.let {
                                SecondaryText { Text(it) }
                            }
                        } else if(displayMacAddress) {
                            item.address?.let {
                                SecondaryText { Text(it) }
                            }
                        }
                    }
                }
                if(item.bondState != BluetoothDevice.BOND_BONDING) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0079D3),
                        progress = when(item.bondState) {
                            BluetoothDevice.BOND_NONE -> 0f
                            else -> 1f
                        }
                    )
                } else LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF0079D3))
            }
        }
    }
}
