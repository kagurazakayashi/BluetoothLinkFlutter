import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:espblufiforflutter/espblufiforflutter_method_channel.dart';

void main() {
  MethodChannelEspblufiforflutter platform = MethodChannelEspblufiforflutter();
  const MethodChannel channel = MethodChannel('espblufiforflutter');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
