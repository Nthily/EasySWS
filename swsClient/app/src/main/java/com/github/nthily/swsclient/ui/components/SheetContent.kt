package com.github.nthily.swsclient.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.nthily.swsclient.utils.SecondaryText
import com.github.nthily.swsclient.viewModel.BluetoothViewModel
import kotlinx.coroutines.launch

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