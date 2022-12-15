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

- 发送: Flutter -> `scan_bt_devices` -> `Map<String,String>` -> `methodChannel` -> Native
  - `Map<String,String>` 可选参数：
    - `timeout` (Long): 蓝牙搜索超时时间（毫秒，默认10秒）
    - `interval` (Long): 返回数据间隔时间（毫秒，默认1秒）
    - `real` (Boolean): 是否实时返回扫描到的设备，默认false
- 接收: 
  - Native -> `methodChannel` -> `Map(k=scan_bt_devices,v=值)` -> Flutter
    - `v=` 可能的值：
      - `main_bt_disable_msg`: 蓝牙不可用
      - `start_scan_ble_error`: 无法启动扫描，可能是没有权限
      - `start_scan_ble`: 开始扫描蓝牙设备
  - Native -> `eventChannel` -> `Map(k=scan_bt_devices,t=状态,v=值,r=Bool)` -> Flutter
    - `t=` 可能的值：
      - `list`: v=蓝牙设备及其信息列表，每秒刷新(JSON)
      - `real`: v=实时扫描到的当前蓝牙设备及其信息(JSON)
      - `stat`: v=扫描状态发生了变化。此时 `v=` 可能的值：
        - `thread_interrupted`: 扫描线程结束
        - `stop_scan_ble`: 扫描中止
    - `r=` 布尔值：是否正在运行， false 为最终结果。
  - 返回结果示例：
    - `{r: true, c: 8981, t: list, v: [{"name":null,"address":"E9:E8:1C:53:82:00","rssi":"-87","type":"0","bondState":"10","uuids":"null","class":"0","describeContents":"0","hashCode":"-1765052277"},{"name":null,"address":"12:DE:7B:50:D1:00","rssi":"-95","type":"0","bondState":"10","uuids":"null","class":"0","describeContents":"0","hashCode":"1146831679"}] }`
    - `{t: stat, v: thread_interrupted, k: scan_bt_devices}`

### 停止扫描蓝牙设备列表
- 发送: Flutter -> `stop_scan_ble` -> `methodChannel` -> Native
- 接收: Native -> `methodChannel` -> `Map(k=stop_scan_ble,v=stop_scan_ble)` -> Flutter

## LICENSE

- [kagurazakayashi/EspBlufiForFlutter LICENSE](LICENSE)

EspBlufi is based on the BLUFI protocol, which connect with IOT devices for BLE data communication, realizes device config network, and custom data transmission and reception. EspBlufiForAndroid/EspBlufiForiOS is developed and maintained by Espressif Corp.

- [EspressifApp/EspBlufiForAndroid LICENSE](https://github.com/EspressifApp/EspBlufiForAndroid/blob/master/LICENSE)
- [EspressifApp/EspBlufiForiOS LICENSE](https://github.com/EspressifApp/EspBlufiForiOS/blob/master/LICENSE.txt)
