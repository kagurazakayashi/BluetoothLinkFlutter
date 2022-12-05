import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'espblufiforflutter_platform_interface.dart';

/// An implementation of [EspblufiforflutterPlatform] that uses method channels.
class MethodChannelEspblufiforflutter extends EspblufiforflutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('espblufiforflutter');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
