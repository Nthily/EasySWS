using System;

namespace enablevjoy
{
    class Program
    {
        static string vJoyInstanceId;
        static Guid hdiGuid = new("{745a17a0-74d3-11d0-b6fe-00a0c90f57da}");
        static int Main(string[] args)
        {
            try
            {
                var deviceInfo = DeviceInstallation.EnumDevices(hdiGuid);
                Console.WriteLine(deviceInfo.Length);

                foreach (var device in deviceInfo)
                {
                    Console.WriteLine(device.Description);
                    if (device.Description == "vJoy Device")
                    {
                        Console.WriteLine("aaaa");
                        vJoyInstanceId = device.InstanceId;
                        break;
                    }
                }
                DeviceInstallation.EnableDevice(hdiGuid, vJoyInstanceId);
                Console.ReadLine();
                return 0;
            } 
            catch(Exception ex)
            {
                Console.WriteLine(ex);
                Console.ReadLine();
                return 114514;
            }
            
        }
    }
}
