package com.github.nthily.swsclient.page.bluetooth

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.nthily.swsclient.R
import com.github.nthily.swsclient.ui.components.SecondaryText

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PairedBluetoothDevices(
    pairedDevices: SnapshotStateList<BluetoothDevice>,
    onClickPairedDevice: (selectedDevice: BluetoothDevice) -> Unit,
    displayMacAddress: Boolean
) {

    Text(
        text = "已配对设备",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.h6

    )
    pairedDevices.forEach { item ->
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
                    .clickable { onClickPairedDevice(item) }
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
                        if(displayMacAddress) {
                            item.address?.let {
                                SecondaryText { Text(it) }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "已保存",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0079D3),
                    progress = 1f
                )
            }
        }
    }
    Spacer(Modifier.padding(vertical = 8.dp))
}


@Composable
fun GetBluetoothDeviceIcon(device: BluetoothDevice) {
    return when (device.bluetoothClass.deviceClass) {
        BluetoothClass.Device.COMPUTER_LAPTOP -> Icon(painterResource(R.drawable.laptop), null)
        BluetoothClass.Device.PHONE_SMART -> Icon(painterResource(R.drawable.smartphone), null)
        BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> Icon(
            painterResource(R.drawable.headphones),
            null
        )
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER -> Icon(
            painterResource(R.drawable.tv),
            null
        )
        BluetoothClass.Device.WEARABLE_WRIST_WATCH -> Icon(painterResource(R.drawable.watch), null)
        BluetoothClass.Device.Major.UNCATEGORIZED -> Icon(
            painterResource(R.drawable.bluetooth),
            null
        )
        else -> Icon(painterResource(R.drawable.bluetooth), null)
    }
}
