package com.github.nthily.swsclient.page.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Settings() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 35.dp, start = 6.dp, end = 6.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.h2,
            fontWeight = FontWeight.Bold
        )
    }
}
