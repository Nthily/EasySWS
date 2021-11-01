package com.github.nthily.swsclient.components

// 发送控制器的数据到服务器端的代码
// 数据包的构成为 [数据包长度][数据包类型][子类型][数据]

// 数据包长度用于接收端解析数据包的数据
// 数据包类型表示这个数据包用于发送什么，在底下 PacketType 中，input 就代表输入 vJoy，代表发送控制 vJoy 手柄的数据包。以此类推，可以有 message 类型等
// 子类型，根据第二个参数而定
// 数据，根据之前东西的而定

object Sender {

    fun getUpShiftButtonsData(
        press: Boolean
    ): ByteArray {

        val upShiftButton: Byte = 0 // 按钮位置
        val pressValue: Byte = if (press) 1 else 0 // 按下 / 松开

        val input = byteArrayOf(PacketType.Input.buttons)

        val packetData = (pressValue.toInt() shl upShiftButton.toInt()).toByteArray()
        val packetType = byteArrayOf(PacketType.input)
        val packetSize = byteArrayOf((packetType.size + packetData.size + input.size).toByte())

        return packetSize + packetType + input + packetData // 返回数据包 [长度][包类型][按钮ID][按钮状态]
    }

    fun getDownShiftButtonsData(
        press: Boolean,
    ): ByteArray {

        val downShiftButton: Byte = 1 // 按钮位置
        val pressValue: Byte = if (press) 1 else 0 // 按下 / 松开

        val input = byteArrayOf(PacketType.Input.buttons)

        val packetData = (pressValue.toInt() shl downShiftButton.toInt()).toByteArray()
        val packetType = byteArrayOf(PacketType.input)
        val packetSize = byteArrayOf((packetType.size + packetData.size + input.size).toByte())

        return packetSize + packetType + input + packetData // 返回数据包 [长度][包类型][按钮ID][按钮状态]
    }

    fun getBrakeData(
        value: Float
    ): ByteArray {

        val input = byteArrayOf(PacketType.Input.axisY)
        val packetData = value.toByteArray()
        val packetType = byteArrayOf(PacketType.input)
        val packetSize = byteArrayOf((packetType.size + packetData.size + input.size).toByte())

        return packetSize + packetType + input + packetData // 返回数据包 [长度][包类型][轴ID][轴的值]
    }

    fun getThrottleData(
        value: Float
    ): ByteArray {

        val input = byteArrayOf(PacketType.Input.axisZ)
        val packetData = value.toByteArray()
        val packetType = byteArrayOf(PacketType.input)
        val packetSize = byteArrayOf((packetType.size + packetData.size + input.size).toByte())

        return packetSize + packetType + input + packetData // 返回数据包 [长度][包类型][轴ID][轴的值]
    }

    fun getSensorData(
        value: Float
    ): ByteArray {
        val input = byteArrayOf(PacketType.Input.axisX)
        val packetData = value.toByteArray()
        val packetType = byteArrayOf(PacketType.input)
        val packetSize = byteArrayOf((packetType.size + packetData.size + input.size).toByte())

        return packetSize + packetType + input + packetData // 返回数据包 [长度][包类型][轴ID][轴的值]
    }

}

object PacketType { // 数据包类型

    const val input: Byte = 3

    // 具体类型

    object Input { // vJoy 类型 ID
        const val buttons: Byte = 1
        const val axisX: Byte = 10 // 方向盘
        const val axisY: Byte = 11 // 刹车
        const val axisZ: Byte = 12 // 油门
    }
}

fun Int.toByteArray() = this.let {
    byteArrayOf(
        (it and 0xFF).toByte(), ((it ushr 8) and 0xFF).toByte(),
        ((it ushr 16) and 0xFF).toByte(), ((it ushr 24) and 0xFF).toByte()
    )
}

fun Float.toByteArray() = this.toBits().let {
    byteArrayOf(
        (it and 0xFF).toByte(), ((it ushr 8) and 0xFF).toByte(),
        ((it ushr 16) and 0xFF).toByte(), ((it ushr 24) and 0xFF).toByte()
    )
}
