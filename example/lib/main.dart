import 'package:espblufiforflutter/data_structure/bluetooth_device_information.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:espblufiforflutter/espblufiforflutter.dart';
import 'package:responsive_sizer/responsive_sizer.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> implements EspblufiforflutterDelegateVersion, EspblufiforflutterDelegateScan {
  String _platformVersion = '';
  final _espblufiforflutterPlugin = Espblufiforflutter();
  List<BluetoothDeviceInformation> _scanBleInfo = [];

  @override
  void initState() {
    super.initState();
    _espblufiforflutterPlugin.delegateVersion = this;
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    /*
    藍芽搜尋
    - `timeout` (Long): 藍芽搜尋超時時間（毫秒，預設10秒）
    - `interval` (Long): 返回資料間隔時間（毫秒，預設1秒）
    - `real` (Boolean): 是否實時返回掃描到的裝置，預設false
    */
    Map? platformVersion = await _espblufiforflutterPlugin.scanBtDevices(timeout: 1000, interval: 1000, real: false);

    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    // try {
    //   platformVersion = await _espblufiforflutterPlugin.getPlatformVersion();
    // } on PlatformException {
    //   platformVersion = 'Failed to get platform version.';
    // }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion += "\n$platformVersion";
    });
  }

  @override
  Widget build(BuildContext context) {
    return ResponsiveSizer(builder: (context, orientation, deviceType) {
      return MaterialApp(
        home: Scaffold(
            appBar: AppBar(
              title: const Text('Plugin example app'),
            ),
            body: _scanBleInfo.isNotEmpty
                ? ListView.builder(
                    padding: const EdgeInsets.only(
                      left: 13,
                      right: 13,
                    ),
                    itemCount: _scanBleInfo.length * 2 - 1,
                    itemBuilder: (context, i) {
                      if (i.isOdd) {
                        return const Divider();
                      }
                      final index = i ~/ 2;
                      BluetoothDeviceInformation bleInfo = _scanBleInfo[index];
                      return InkWell(
                        // onTap: () => Navigator.of(context).push(
                        //   MaterialPageRoute(
                        //     builder: (context) => DeviceHandle2Page(r: result),
                        //   ),
                        // ),
                        child: Row(
                          mainAxisSize: MainAxisSize.max,
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(bleInfo.rssi.toString()),
                            SizedBox(
                              width: 50.w,
                              child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(bleInfo.name.isNotEmpty ? bleInfo.name : bleInfo.address),
                                  if (bleInfo.name.isNotEmpty)
                                    Text(
                                      bleInfo.address,
                                      style: const TextStyle(
                                        fontSize: 13,
                                        color: Colors.grey,
                                      ),
                                    ),
                                ],
                              ),
                            ),
                            SizedBox(width: 5.w)
                          ],
                        ),
                      );
                    },
                  )
                : Container()),
      );
    });
  }

  @override
  espblufiforflutterDelegateOSVersion(Map? info) {
    if (info == null) {
      return;
    }
    setState(() {
      _platformVersion += "\n$info";
    });
  }

  @override
  espblufiforflutterScanBtDevicesDelegate(List<BluetoothDeviceInformation> infos) {
    // 去除重複的內容
    List<BluetoothDeviceInformation> temp = [];
    for (BluetoothDeviceInformation info in infos) {
      bool isExist = false;
      for (BluetoothDeviceInformation temp in temp) {
        if (info.address == temp.address) {
          isExist = true;
          break;
        }
      }
      if (!isExist) {
        // 新增新的訊號
        temp.add(info);
      } else {
        // 更新訊號質量
        for (BluetoothDeviceInformation temp in temp) {
          if (info.address == temp.address) {
            temp.rssi = info.rssi;
            break;
          }
        }
      }
      // 按訊號質量排序
      temp.sort((a, b) => b.rssi.compareTo(a.rssi));
      // 更新列表
      setState(() {
        _scanBleInfo = temp;
      });
    }
  }

  @override
  espblufiforflutterScanBtStatusDelegate(String info) {
    print("SCAN END");
  }
}
