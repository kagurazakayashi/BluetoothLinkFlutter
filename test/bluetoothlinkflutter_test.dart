import 'package:flutter_test/flutter_test.dart';
import 'package:bluetoothlinkflutter/bluetoothlinkflutter.dart';
import 'package:bluetoothlinkflutter/bluetoothlinkflutter_platform_interface.dart';
import 'package:bluetoothlinkflutter/bluetoothlinkflutter_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockbluetoothlinkflutterPlatform with MockPlatformInterfaceMixin implements bluetoothlinkflutterPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final bluetoothlinkflutterPlatform initialPlatform = bluetoothlinkflutterPlatform.instance;

  test('$MethodChannelbluetoothlinkflutter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelbluetoothlinkflutter>());
  });

  test('getPlatformVersion', () async {
    bluetoothlinkflutter bluetoothlinkflutterPlugin = bluetoothlinkflutter();
    MockbluetoothlinkflutterPlatform fakePlatform = MockbluetoothlinkflutterPlatform();
    bluetoothlinkflutterPlatform.instance = fakePlatform;

    expect(await bluetoothlinkflutterPlugin.getPlatformVersion(), '42');
  });
}
