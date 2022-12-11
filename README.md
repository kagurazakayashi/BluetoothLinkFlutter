# EspBlufiForFlutter

Flutter APP 与乐鑫 ESP8266/ESP32 蓝牙设备通信插件。

**没有做完，请勿使用。**

## 引用库

本插件基于以下项目构建：

- [EspressifApp/EspBlufiForAndroid](https://github.com/EspressifApp/EspBlufiForAndroid) v1.6.3
- [EspressifApp/EspBlufiForiOS](https://github.com/EspressifApp/EspBlufiForiOS) v1.3.1

## Flutter 与 Native 的交互

使用的通知通道名称：
- MethodChannel: `flutter_to_native`
- EventChannel:  `native_to_flutter`

### 获取系统版本号
会同时收到来自 methodChannel 和 eventChannel 两个通道的回复，用于测试通道工作是否正常。

- 发送: Flutter -> `getPlatformVersion` -> `methodChannel` -> Native
- 接收: Native -> `methodChannel`+`eventChannel` -> `Map(k=getPlatformVersion)` -> Flutter
  - Map: `{"k":"getPlatformVersion","t":"result/event","v":"版本号"}`

### 扫描蓝牙设备列表

- 发送: Flutter -> `scan_bt_devices` -> `methodChannel` -> Native
- 接收: 
  - Native -> `methodChannel` -> `Map(k=scan_bt_devices,t=start,v=值)` -> Flutter
    - `v=` 可能的值：
      - `main_bt_disable_msg`: 蓝牙不可用
      - `start_scan_ble`: 开始扫描蓝牙设备
  - Native -> `eventChannel` -> `Map(k=scan_bt_devices,t=状态,v=值)` -> Flutter
    - `t=` 可能的值：
      - `list`: v=蓝牙设备及其信息列表(JSON)
      - `stat`: v=扫描状态发生了变化。此时 `v=` 可能的值：
        - `thread_interrupted`: 扫描线程结束
        - `stop_scan_ble`: 扫描中止

## LICENSE

- [kagurazakayashi/EspBlufiForFlutter LICENSE](LICENSE)

EspBlufi is based on the BLUFI protocol, which connect with IOT devices for BLE data communication, realizes device config network, and custom data transmission and reception. EspBlufiForAndroid/EspBlufiForiOS is developed and maintained by Espressif Corp.

- [EspressifApp/EspBlufiForAndroid LICENSE](https://github.com/EspressifApp/EspBlufiForAndroid/blob/master/LICENSE)
- [EspressifApp/EspBlufiForiOS LICENSE](https://github.com/EspressifApp/EspBlufiForiOS/blob/master/LICENSE.txt)
