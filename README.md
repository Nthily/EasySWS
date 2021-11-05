# EasySWS

Use your phone as a steering wheel simulator! Suitable for Assetto Corsa / Assetto Corsa Competizione / F1 20XX

让你的手机变成虚拟方向盘！适用于神力科莎，神力科莎争锋，F1 系列等赛车模拟游戏


[演示视频](https://www.bilibili.com/video/BV1aP4y1b7cj?spm_id_from=333.999.0.0)

## 1. 开始安装 🚀

* 下载 `release` 中的 `app` 和 `exe` 
* 在电脑上安装 [`vJoy`](https://github.com/jshafer817/vJoy) (一个虚拟游戏手柄)，过高或者过低的版本可能会影响软件运行，超链接中的版本是刚好适用于当前软件的
* 运行下载完的 `exe`，这将会自动启动 `vjoy` 驱动 (期间会有 UAC 授权确认)，并且会开启电脑的蓝牙连接广播。如果检测不到 `vjoy` 或者未授权会停止软件的运行
* 运行下载完的 `app`, 和电脑设备进行配对（此操作也可以在手机的系统蓝牙完成），配对完成后会在已配对的列表中显示你的电脑设备，点击一次设备会弹出一个窗口，在底下点击开始连接就能尝试和电脑设备进行连接。连接成功会自动跳转到操控界面

|步骤1|步骤2|步骤3
|------|-----|-------|
|![14C88336C45DC2594ABD77142389C759](https://user-images.githubusercontent.com/31311826/140234451-75de5135-8881-45db-abdb-fda36e66646e.jpg)|![0843BBC1F633B14DE43FDD9AF8106FFC](https://user-images.githubusercontent.com/31311826/140234504-a176e13d-1c37-42a9-95bd-a90536eaf89c.jpg)|![E8CA0E389ADF0761E9B3D5E188386CA8](https://user-images.githubusercontent.com/31311826/140234478-63c44405-7cde-4e50-a777-d290d949ec88.jpg)|


## 2. 测试 && 游玩 💨

如果你有 [`Assetto Corsa Content Manager`](https://acstuff.ru/app/) 的话，可以在设置中查看是否成功启用了 `vJoy` 以及是否能够操控

* 开启了 `vJoy` 的情况下

![image](https://user-images.githubusercontent.com/31311826/138749275-3e9554c6-e2c0-4a85-b47d-5cb903da0831.png)

注意右边的 “检测到的设备”，如果成功安装 `vJoy` 并且打开了 `vJoy` 驱动，将会显示 `vJoy Device` 以及 `8` 条都在中间的轴

* 开启了 `exe` 并且和 `app` 成功连接的情况下

![image](https://user-images.githubusercontent.com/31311826/138749501-24613388-04e7-44de-842d-984d5587f293.png)

右边的轴 2 和轴 3 将会归零，这时候在你的 `app` 中尝试滑动滑条和按按钮，测试右边的 `vJoy device` 是否产生了变化，如果是的话，那么恭喜你，你可以开始游玩游戏了！

记得设置转向模块为轴 1，油门和刹车可以在轴 2 轴 3 中任意选择 （目前和开发有关系，所以只能选择这么多）

在按钮中设置升档和降档的按钮为按钮 1 或者 按钮 2


## 3. 开发 💦

* APP 端
 
  * `Android Studio` (需支持 Jetpack Compose)
  * `Compose` 版本 `1.0.4`
  * `Kotlin` 版本 `1.5.31`

软件中使用的传输数据包格式为：[数据包长度][数据包类型][子类型][数据]

* 数据包长度是 1 字节的 `Byte` 类型

* 数据包类型是长度为 1 字节的 `Byte` 类型

* 子类型是长度为 1 字节的 `Byte` 类型

* 数据是长度为 4 字节的 `Float` 或 `Int` 类型

* 数据包长度 = 数据包类型 + 子类型 + 数据

数据包中的数据包长度是为了避免在高速传输数据中，因为 `inputStream` 可能无法完整的读取包的长度，导致读取数据失败（因为蓝牙传输可能是和 TCP 协议类似，面向流的）。所以设计了一个长度来描述这个数据包的长度，之后再循环读取真正的包的数据

* PC 端
* 
  * `Visual Studio`
  * `InTheHand.Net.Bluetooth` 库，版本 `4.0.21`

## 4. 未来支持 👀

* APP 端自定义控件，大小，位置等
* PC 端的 UI 设计
* 多语言 （PC / APP）
* APP 端的 UI 设计
* 支持更多 vJoy 轴 / 按钮，尝试实现一些力反馈
* 根据不同手机支持的硬件设备提高体验，如在一些手机上可以调用线性马达
* 待续...

## 5. 所用的第三方库 🔑

[compose-vertical-slider](https://github.com/aakarshrestha/compose-vertical-slider)

## 6. 开源协议 📄

```
Copyright (C) 2021 Nthily.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```
