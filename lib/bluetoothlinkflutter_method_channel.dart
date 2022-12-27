import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'bluetoothlinkflutter_platform_interface.dart';

/// An implementation of [bluetoothlinkflutterPlatform] that uses method channels.
class MethodChannelbluetoothlinkflutter extends bluetoothlinkflutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('bluetoothlinkflutter');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
