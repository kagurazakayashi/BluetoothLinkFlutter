import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'bluetoothlinkflutter_method_channel.dart';

abstract class bluetoothlinkflutterPlatform extends PlatformInterface {
  /// Constructs a bluetoothlinkflutterPlatform.
  bluetoothlinkflutterPlatform() : super(token: _token);

  static final Object _token = Object();

  static bluetoothlinkflutterPlatform _instance = MethodChannelbluetoothlinkflutter();

  /// The default instance of [bluetoothlinkflutterPlatform] to use.
  ///
  /// Defaults to [MethodChannelbluetoothlinkflutter].
  static bluetoothlinkflutterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [bluetoothlinkflutterPlatform] when
  /// they register themselves.
  static set instance(bluetoothlinkflutterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
