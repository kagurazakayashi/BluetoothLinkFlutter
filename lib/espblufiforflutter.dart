
import 'espblufiforflutter_platform_interface.dart';

class Espblufiforflutter {
  Future<String?> getPlatformVersion() {
    return EspblufiforflutterPlatform.instance.getPlatformVersion();
  }
}
