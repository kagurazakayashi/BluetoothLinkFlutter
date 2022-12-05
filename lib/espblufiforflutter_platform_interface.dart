import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'espblufiforflutter_method_channel.dart';

abstract class EspblufiforflutterPlatform extends PlatformInterface {
  /// Constructs a EspblufiforflutterPlatform.
  EspblufiforflutterPlatform() : super(token: _token);

  static final Object _token = Object();

  static EspblufiforflutterPlatform _instance = MethodChannelEspblufiforflutter();

  /// The default instance of [EspblufiforflutterPlatform] to use.
  ///
  /// Defaults to [MethodChannelEspblufiforflutter].
  static EspblufiforflutterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [EspblufiforflutterPlatform] when
  /// they register themselves.
  static set instance(EspblufiforflutterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
