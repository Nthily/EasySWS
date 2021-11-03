package com.github.nthily.swsclient.page.bluetooth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.nthily.swsclient.utils.SecondaryText
import com.github.nthily.swsclient.viewModel.BluetoothViewModel
import com.google.accompanist.insets.systemBarsPadding
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun Bluetooth(
    bluetoothViewModel: BluetoothViewModel,
    sheetState: ModalBottomSheetState
) {
    val bthName by remember { bluetoothViewModel.bthName }
    val bthReady by remember { bluetoothViewModel.bthReady }
    val bthEnabled by remember { bluetoothViewModel.bthEnabled }
    val macAddress by remember { bluetoothViewModel.showMacAddress }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
    ) {
        if(bthReady) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "设备名称",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = bthName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.h6
                        )
                    }
                }
                Spacer(Modifier.padding(vertical = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "开启蓝牙",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Switch(
                            checked = bthEnabled,
                            onCheckedChange = {
                                if(bthEnabled)
                                    bluetoothViewModel.disableBluetooth()
                                else
                                    bluetoothViewModel.enableBluetooth()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Color(0xFF0079D3),
                                checkedThumbColor = Color(0xFF0079D3)
                            )
                        )
                    }
                }
                if(bthEnabled) {
                    Spacer(Modifier.padding(vertical = 8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "显示 MAC 地址",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.h6
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Switch(
                                checked = macAddress,
                                onCheckedChange = {
                                    bluetoothViewModel.showMacAddress.value = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(0xFF0079D3),
                                    checkedThumbColor = Color(0xFF0079D3)
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(vertical = 8.dp))
                BluetoothDevices(bluetoothViewModel, sheetState)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetContent(
    sheetState: ModalBottomSheetState,
    bluetoothViewModel: BluetoothViewModel
) {
    val scope = rememberCoroutineScope()
    val device by remember { bluetoothViewModel.selectedPairedDevice }

    Column(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "设备 ${device?.name} 已配对",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        SecondaryText(str = "你可能想要")
        Spacer(Modifier.padding(vertical = 15.dp))
        Button(
            onClick = {
                bluetoothViewModel.connectByBluetooth()
                scope.launch {
                    sheetState.hide()
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF44D670)),
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Text("连接设备", color = Color.White)
        }
        Spacer(Modifier.padding(vertical = 15.dp))
        Button(
            onClick = {
                device?.let { bluetoothViewModel.unbindDevice(it) }
                scope.launch {
                    sheetState.hide()
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF8488A5)),
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
        ) {
            Text("取消配对")
        }
    }

    BackHandler(
        enabled = (sheetState.currentValue == ModalBottomSheetValue.HalfExpanded
                || sheetState.currentValue == ModalBottomSheetValue.Expanded),
    ) {
        scope.launch {
            sheetState.hide()
        }
    }

}
