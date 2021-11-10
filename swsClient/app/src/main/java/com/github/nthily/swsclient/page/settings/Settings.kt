package com.github.nthily.swsclient.page.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nthily.swsclient.R
import com.github.nthily.swsclient.ui.components.SecondaryText

@Composable
fun Settings(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.padding(vertical = 10.dp))
      //  Bluetooth()
    }
}

@Composable
fun Bluetooth() {
    Column(
        modifier = Modifier
            .padding(horizontal = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(id = R.drawable.bluetooth), null)
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            Text(
                text = "蓝牙模块",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.padding(vertical = 6.dp))
        Divider(thickness = 1.dp)
        Spacer(modifier = Modifier.padding(vertical = 6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(30.dp)
                .clickable(
                    onClick = { },
                    indication = null,
                    interactionSource = MutableInteractionSource()
                )
        ) {
            SecondaryText {
                Text(
                    text = "编辑设备名称",
                    style = MaterialTheme.typography.subtitle1
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(painterResource(id = R.drawable.arrow_right), null)
            }
        }
    }
}
