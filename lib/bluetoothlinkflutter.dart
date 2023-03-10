import 'dart:convert';

import 'package:bluetoothlinkflutter/data_structure/bluetooth_device_information.dart';
import 'package:flutter/services.dart';

import 'bluetoothlinkflutter_platform_interface.dart';

abstract class bluetoothlinkflutterDelegateVersion {
  bluetoothlinkflutterDelegateOSVersion(Map? info);
}

abstract class bluetoothlinkflutterDelegateScan {
  bluetoothlinkflutterScanBtDevicesDelegate(List<BluetoothDeviceInformation> infos);
  bluetoothlinkflutterScanBtStatusDelegate(String info);
}

class bluetoothlinkflutter {
  // Future<String?> getPlatformVersion() {
  //   return bluetoothlinkflutterPlatform.instance.getPlatformVersion();
  // }
  bluetoothlinkflutterDelegateVersion? delegateVersion;
  bluetoothlinkflutterDelegateScan? delegateScan;
  // 向原生髮送通知，原生可以回覆該通知
  final MethodChannel _methodChannel = const MethodChannel('flutter_to_native');
  // 接收原生隨時傳送的通知
  final EventChannel _eventChannel = const EventChannel('native_to_flutter');

  // 構造方法
  bluetoothlinkflutter() {
    // 在構造方法裡面設定監聽原生隨時傳送的通知，收到原生髮來的通知時，呼叫 eventChannelData 函式
    _eventChannel.receiveBroadcastStream().listen(eventChannelData);
  }

  // 【非同步】收到原生隨時發來的通知，event 是原生傳回來的資料
  eventChannelData(event) {
    // print("== native ==");
    // print(event);
    // print("== /native ==");
    // 可以是 Map 也可以是 String 等其他
    Map arguments = event;
    if (delegateVersion == null) {
      return;
    }
    switch (arguments["k"]) {
      case "getPlatformVersion":
        delegateVersion?.bluetoothlinkflutterDelegateOSVersion(arguments);
        break;
      case "scan_bt_devices":
        {
          switch (arguments["t"]) {
            case "list":
              {
                List<BluetoothDeviceInformation> infos = [];
                List<dynamic> btInfo = jsonDecode(arguments["v"]);
                for (Map<String, dynamic> bti in btInfo) {
                  // print(bti); // {name: null, address: 4C:9B:13:D7:7C:00, rssi: -72, type: 0, bondState: 10, uuids: null, class: 0, describeContents: 0, hashCode: 1797016041}
                  BluetoothDeviceInformation nowble = BluetoothDeviceInformation(bti);
                  infos.add(nowble);
                }
                delegateScan?.bluetoothlinkflutterScanBtDevicesDelegate(infos);
                break;
              }
            case "stat":
              {
                delegateScan?.bluetoothlinkflutterScanBtStatusDelegate(arguments["v"]);
                break;
              }
            default:
          }
          break;
        }
      default:
        break;
    }
  }

  // 向原生髮送通知，讓原生執行某項功能
  Future<Map?> getPlatformVersion() async {
    // 可以是 Map 也可以是 String 等其他
    // 【同步】await 是等待它執行結束，直接拿到原生的返回值
    final Map? info = await _methodChannel.invokeMethod('getPlatformVersion');
  }

  String boolToString(bool value) {
    return value ? "true" : "false";
  }

  Future<Map?> scanBtDevices({int timeout = 10000, int interval = 1000, bool real = false}) async {
    /*
    - `timeout` (Long): 藍芽搜尋超時時間（毫秒，預設10秒）
    - `interval` (Long): 返回資料間隔時間（毫秒，預設1秒）
    - `real` (Boolean): 是否實時返回掃描到的裝置，預設false
    */
    final Map? info = await _methodChannel.invokeMethod('scan_bt_devices', {
      "timeout": timeout.toString(),
      "interval": interval.toString(),
      "real": boolToString(real),
    });
    return info;
  }

  Future<Map?> stopScanBtDevices() async {}
}
