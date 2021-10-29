package com.github.nthily.swsclient.page.bluetooth

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.github.nthily.swsclient.viewModel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BluetoothDevices(
    appViewModel: AppViewModel,
    sheetState: ModalBottomSheetState
) {
    val context = LocalContext.current
    val pairedDevices = remember { appViewModel.pairedDevices }
    val scannedDevices  = remember { appViewModel.scannedDevices }
    val bthEnabled = remember { appViewModel.bthEnabled }
    val bthDiscovering = remember { appViewModel.bthDiscovering }
    val scope = rememberCoroutineScope()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) appViewModel.startDeviceScan()
        else Toast.makeText(context, "开启权限失败", Toast.LENGTH_LONG).show()
    }

    if(bthEnabled.value) {
        Column {
            if(pairedDevices.isNotEmpty()) {
                PairedBluetoothDevices(
                    pairedDevices = pairedDevices,
                    onClickPairedDevice = {
                        appViewModel.selectedPairedDevice.value = it
                        scope.launch {
                            sheetState.show()
                        }
                    },
                    appViewModel.showMacAddress.value
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "可用设备 ${scannedDevices.size}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.h6
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = {
                            if(!bthDiscovering.value) {
                                if(ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    appViewModel.startDeviceScan()
                                } else {
                                    requestPermissionLauncher.launch(
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                }
                            } else appViewModel.stopDeviceScan()
                        }
                    ) {
                        if(bthDiscovering.value) Icon(Icons.Filled.Close, null)
                        else Icon(Icons.Filled.Refresh, null)
                    }
                }
            }

            if(bthDiscovering.value)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp)),
                    color = Color(0xFF0079D3)
                )

            ScannedBluetoothDevices(
                scannedDevices = scannedDevices,
                { appViewModel.bondDevice(it) },
                appViewModel.showMacAddress.value
            )
        }
    }
}
