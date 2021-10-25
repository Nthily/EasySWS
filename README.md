# EasySWS

Use your phone as a steering wheel simulator! Suitable for Assetto Corsa / Assetto Corsa Competizione / F1 20XX

让你的手机变成虚拟方向盘！适用于神力科莎，神力科莎争锋，F1 系列的赛车模拟游戏

## 1. 开始安装

* 下载 `release` 中的 `app` 和 `exe` 
* 在电脑上安装 [`vjoy`](https://github.com/jshafer817/vJoy) (一个虚拟游戏手柄)，过高或者过低的版本可能会影响软件运行，超链接中的版本是刚好适用于当前的软件的
* 运行下载完的 `exe`，这将会自动启动 `vjoy` 驱动 (期间会有 UAC 授权确认)，并且会开启电脑的蓝牙连接广播。如果检测不到 `vjoy 或者未授权会停止软件的运行
* 运行下载完的 `app`, 和电脑设备进行配对（此操作也可以在手机的系统蓝牙完成），配对完成后会在已配对的列表中显示你的电脑设备，点击一次设备会弹出一个窗口，在底下点击开始连接就能尝试和电脑设备进行连接。连接成功会自动跳转到操控界面

|步骤1|步骤2|步骤3|
|------|-----|-------|
|<img src="https://user-images.githubusercontent.com/31311826/138748056-659b0b1c-9c33-47fe-99fe-8b26e3b1ae24.jpg" height = 80% width = 80%>|<img src="https://user-images.githubusercontent.com/31311826/138748076-047cb8ca-378a-4b4e-9f7b-e7faff45c3c7.jpg" height = 80% width = 80%>|<img src="https://user-images.githubusercontent.com/31311826/138748081-fa850e89-767e-4f75-878a-4f64993f04e1.jpg" height = 100% width = 100%>|


## 2. 测试 && 游玩

如果你有 [`Assetto Corsa Content Manager`](https://acstuff.ru/app/) 的话，可以在设置中查看是否成功启用了 `vjoy` 以及是否能够操控

* 开启了 `vjoy` 的情况下

![image](https://user-images.githubusercontent.com/31311826/138749275-3e9554c6-e2c0-4a85-b47d-5cb903da0831.png)

注意右边的 “检测到的设备”，如果成功安装 `vJoy` 并且打开了 `vJoy` 驱动，将会显示 vJoy Device 以及 8  条都在中间的轴

* 开启了 `exe` 并且和 `app` 成功连接的情况下

![image](https://user-images.githubusercontent.com/31311826/138749501-24613388-04e7-44de-842d-984d5587f293.png)

右边的轴 2 和轴 3 将会归零，这时候在你的 `app` 中尝试滑动滑条和按按钮，测试右边的 `vJoy device` 是否产生了变化，如果是的话，那么恭喜你，你可以开始游玩游戏了！

记得设置转向模块为轴 1，油门和刹车可以在轴 2 轴 3 中任意选择 （目前和开发有关系，所以只能选择这么多）
在按钮中设置升档和降档的按钮为按钮 1 或者 按钮 2
