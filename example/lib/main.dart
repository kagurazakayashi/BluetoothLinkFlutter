import 'package:bluetoothlinkflutter/data_structure/bluetooth_device_information.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:bluetoothlinkflutter/bluetoothlinkflutter.dart';
import 'package:responsive_sizer/responsive_sizer.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> implements bluetoothlinkflutterDelegateVersion, bluetoothlinkflutterDelegateScan {
  String _title = "蓝牙设备列表";
  final _bluetoothlinkflutterPlugin = bluetoothlinkflutter();
  int _scansec = -1;
  List<BluetoothDeviceInformation> _scanBleInfo = [];

  @override
  void initState() {
    super.initState();
    _bluetoothlinkflutterPlugin.delegateVersion = this;
    _bluetoothlinkflutterPlugin.delegateScan = this;
    initPlatformState();
  }

  Future<void> _startScanBtDevices(int sec) async {
    Map? scanBtDevices = await _bluetoothlinkflutterPlugin.scanBtDevices(timeout: sec * 10, interval: 1000, real: false);
    if (scanBtDevices != null && scanBtDevices["v"] == "start_scan_ble" && scanBtDevices["k"] == "scan_bt_devices") {
      const period = Duration(seconds: 1);
      Timer.periodic(period, (timer) {
        // 到時回撥
        _scansec--;
        if (_scansec <= 0) {
          timer.cancel();
        }
      });
      setState(() {
        _scansec = sec;
      });
      _scanTitle();
    }
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    /*
    藍芽搜尋
    - `timeout` (Long): 藍芽搜尋超時時間（毫秒，預設10秒）
    - `interval` (Long): 返回資料間隔時間（毫秒，預設1秒）
    - `real` (Boolean): 是否實時返回掃描到的裝置，預設false
    */
    _startScanBtDevices(10);

    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    // try {
    //   platformVersion = await _bluetoothlinkflutterPlugin.getPlatformVersion();
    // } on PlatformException {
    //   platformVersion = 'Failed to get platform version.';
    // }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
  }

  void _scanTitle() {
    setState(() {
      if (_scansec > 0) {
        _title = "正在搜索蓝牙设备...";
      } else {
        _title = "蓝牙设备列表";
      }
    });
  }

  Icon signalIcon(int rssi) {
    if (rssi >= -50) {
      return const Icon(Icons.signal_wifi_4_bar, color: Colors.green);
    } else if (rssi >= -70) {
      return const Icon(Icons.network_wifi_3_bar, color: Colors.blue);
    } else if (rssi >= -80) {
      return const Icon(Icons.network_wifi_2_bar, color: Colors.orange);
    } else if (rssi >= -90) {
      return const Icon(Icons.network_wifi_1_bar, color: Colors.red);
    } else {
      return const Icon(Icons.signal_wifi_statusbar_null, color: Colors.grey);
    }
  }

  @override
  Widget build(BuildContext context) {
    return ResponsiveSizer(builder: (context, orientation, deviceType) {
      return MaterialApp(
        home: Scaffold(
            appBar: AppBar(
              title: Text(_title),
              actions: [
                if (_scansec <= 0)
                  IconButton(
                    icon: const Icon(Icons.forward_10),
                    onPressed: () {
                      _startScanBtDevices(10);
                    },
                  ),
                if (_scansec <= 0)
                  IconButton(
                    icon: const Icon(Icons.forward_30),
                    onPressed: () {
                      _startScanBtDevices(30);
                    },
                  ),
                if (_scansec > 0)
                  Padding(
                    padding: const EdgeInsets.all(20),
                    child: Text(_scansec.toString()),
                  ),
                if (_scansec > 0)
                  IconButton(
                    icon: const Icon(Icons.stop),
                    onPressed: () {
                      _bluetoothlinkflutterPlugin.stopScanBtDevices();
                    },
                  ),
              ],
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
                            signalIcon(bleInfo.rssi),
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
  bluetoothlinkflutterDelegateOSVersion(Map? info) {
    if (info == null) {
      return;
    }
    setState(() {
      _title = "\n$info";
    });
  }

  @override
  bluetoothlinkflutterScanBtDevicesDelegate(List<BluetoothDeviceInformation> infos) {
    print("SCAN START");
    print(infos);
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
  bluetoothlinkflutterScanBtStatusDelegate(String info) {
    setState(() {
      _scansec = 0;
    });
    _scanTitle();
  }
}
