package com.github.nthily.swsclient.page.bluetooth

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.nthily.swsclient.ui.theme.SwsClientTheme
import com.github.nthily.swsclient.utils.SecondaryText
import com.github.nthily.swsclient.viewModel.AppViewModel
import com.github.nthily.swsclient.viewModel.removeBond
import kotlinx.coroutines.launch
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import com.github.nthily.swsclient.components.SteeringSensor
import com.github.nthily.swsclient.page.console.Console
import com.github.nthily.swsclient.utils.Sender
import com.github.nthily.swsclient.viewModel.ConsoleViewModel
import com.github.nthily.swsclient.viewModel.Screen

@ExperimentalMaterialApi
@Composable
fun Bluetooth(
    appViewModel: AppViewModel,
    navController: NavHostController
) {

    val bthReady by remember { appViewModel.bthReady }
    val bthEnabled by remember { appViewModel.bthEnabled }
    val macAddress by remember { appViewModel.showMacAddress }
    val selectedDevice by remember { appViewModel.selectedPairedDevice }

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            SheetContent(
                device = selectedDevice,
                sheetState = sheetState,
                connectDevice = { appViewModel.connectDevice(it, navController) }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .padding(top = 48.dp, start = 14.dp, end = 14.dp)
                .verticalScroll(rememberScrollState())
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
                            appViewModel.bthDevice?.let {
                                Text(
                                    text = it,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.h6
                                )
                            }
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
                                        appViewModel.disableBluetooth()
                                    else
                                        appViewModel.enableBluetooth()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(0xFF0079D3),
                                    checkedThumbColor = Color(0xFF0079D3)
                                )
                            )
                        }
                    }

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
                                      appViewModel.showMacAddress.value = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(0xFF0079D3),
                                    checkedThumbColor = Color(0xFF0079D3)
                                )
                            )
                        }
                    }
                    Spacer(Modifier.padding(vertical = 8.dp))
                    BluetoothDevices(appViewModel, sheetState)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetContent(
    device: BluetoothDevice?,
    sheetState: ModalBottomSheetState,
    connectDevice: (device: BluetoothDevice) -> Unit
) {
    val scope = rememberCoroutineScope()
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
                device?.let {
                    connectDevice(it)
                    scope.launch {
                        sheetState.hide()
                    }
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
                  device?.let {
                      it.removeBond()
                      scope.launch {
                          sheetState.hide()
                      }
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
