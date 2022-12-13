import 'package:flutter/services.dart';

import 'espblufiforflutter_platform_interface.dart';

abstract class EspblufiforflutterDelegate {
  espblufiforflutterVersionDelegate(Map? info);
}

class Espblufiforflutter {
  // Future<String?> getPlatformVersion() {
  //   return EspblufiforflutterPlatform.instance.getPlatformVersion();
  // }
  EspblufiforflutterDelegate? delegateVersion;
  // 向原生髮送通知，原生可以回覆該通知
  final MethodChannel _methodChannel = const MethodChannel('flutter_to_native');
  // 接收原生隨時傳送的通知
  final EventChannel _eventChannel = const EventChannel('native_to_flutter');

  // 構造方法
  Espblufiforflutter() {
    // 在構造方法裡面設定監聽原生隨時傳送的通知，收到原生髮來的通知時，呼叫 eventChannelData 函式
    _eventChannel.receiveBroadcastStream().listen(eventChannelData);
  }

  // 【非同步】收到原生隨時發來的通知，event 是原生傳回來的資料
  eventChannelData(event) {
    print("== native ==");
    print(event);
    print("== /native ==");
    // 可以是 Map 也可以是 String 等其他
    Map arguments = event;
    if (delegateVersion == null) {
      return;
    }
    switch (arguments["k"]) {
      case "getPlatformVersion":
        delegateVersion!.espblufiforflutterVersionDelegate(arguments);
        break;
      default:
        break;
    }
  }

  // 向原生髮送通知，讓原生執行某項功能
  Future<Map?> getPlatformVersion() async {
    // 可以是 Map 也可以是 String 等其他
    // 【同步】await 是等待它執行結束，直接拿到原生的返回值
    // final Map? info = await _methodChannel.invokeMethod('getPlatformVersion');
    final Map? info = await _methodChannel.invokeMethod('scan_bt_devices', {
      "timeout": "10000",
    });
    return info;
  }
}
