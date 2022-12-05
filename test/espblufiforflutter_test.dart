import 'package:flutter_test/flutter_test.dart';
import 'package:espblufiforflutter/espblufiforflutter.dart';
import 'package:espblufiforflutter/espblufiforflutter_platform_interface.dart';
import 'package:espblufiforflutter/espblufiforflutter_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockEspblufiforflutterPlatform
    with MockPlatformInterfaceMixin
    implements EspblufiforflutterPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final EspblufiforflutterPlatform initialPlatform = EspblufiforflutterPlatform.instance;

  test('$MethodChannelEspblufiforflutter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelEspblufiforflutter>());
  });

  test('getPlatformVersion', () async {
    Espblufiforflutter espblufiforflutterPlugin = Espblufiforflutter();
    MockEspblufiforflutterPlatform fakePlatform = MockEspblufiforflutterPlatform();
    EspblufiforflutterPlatform.instance = fakePlatform;

    expect(await espblufiforflutterPlugin.getPlatformVersion(), '42');
  });
}
