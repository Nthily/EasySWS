package com.github.nthily.swsclient.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.nthily.swsclient.R

@Composable
fun UpShiftButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .width(120.dp)
            .height(400.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFF0079D3)
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun DownShiftButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .width(120.dp)
            .height(400.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFF0079D3)
    ) {
        Icon(painterResource(id = R.drawable.remove), contentDescription = null, tint = Color.White)
    }
}
