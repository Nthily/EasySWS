using System;
using System.IO;
using System.Text;
using InTheHand.Net.Bluetooth;
using InTheHand.Net.Sockets;

namespace swsServer
{
    class Program
    {
        static void Main(string[] args)
        {
            var UUID = new Guid("00001101-0000-1000-8000-00805F9B34FB");
            var bluetoothClient = new BluetoothClient();
            var bluetoothRadio = BluetoothRadio.Default;
            var bluetoothListener = new BluetoothListener(UUID);

            Joystick.Init();

            if(bluetoothRadio != null)
            {
                getDeviceInfo(bluetoothRadio, bluetoothClient);
                bluetoothListener.Start();
                while (true)
                {
                    bluetoothClient = bluetoothListener.AcceptBluetoothClient();
                    Console.WriteLine($"{bluetoothClient.RemoteMachineName} 已经连接");
                    var mStream = bluetoothClient.GetStream();
                    mStream.Write(Encoding.UTF8.GetBytes("connected"));

                    while (bluetoothClient.Client.Connected)
                    {
                        try
                        {
                            // 由于蓝牙传输类似于 tcp 协议，面向流，所以当数据量过大时，会造成无法返回正确的包长度
                            // 手动通过包格式获取包的长度，写入新的 byte 数组

                            byte[] packetSizeBuffer = new byte[1];
                            mStream.Read(packetSizeBuffer, 0, 1);
                            int packetSize = (int)packetSizeBuffer[0];
                            byte[] packet = new byte[packetSize];
                            int packetIndex = 0;
                            while ((packetIndex += mStream.Read(packet, packetIndex, packetSize - packetIndex)) < packetSize);


                            if (packetSize == 0)
                            {
                                throw new IOException();
                            }
                            else
                            {
                                Joystick.DetectData(packet);
                            }
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine($"设备 {bluetoothClient.RemoteMachineName} 已断开连接\n {ex.ToString()}");
                            break;
                        }
                    }
                }

            } 
            else
            {
                Console.Write("按下任意键继续");
                Console.ReadKey();
            }
        }

        static public void getDeviceInfo(BluetoothRadio bluetoothRadio, BluetoothClient bluetoothClient)
        {
            Console.WriteLine($"本机蓝牙名称： {bluetoothRadio.Name}\n" +
                $"Mac 地址：{bluetoothRadio.LocalAddress}\n");

            Console.WriteLine("\n");

            foreach (BluetoothDeviceInfo device in bluetoothClient.PairedDevices)
            {
                Console.WriteLine($"已配对的设备 {device.DeviceName}");
            }
        }

    }
}
