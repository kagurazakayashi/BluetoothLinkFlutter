class BluetoothDeviceInformation {
  final String name;
  final String address;
  int rssi;
  final int type;
  final int bond;
  final String uuid;
  final String deviceClass;
  final int describeContents;
  final int hash;

  BluetoothDeviceInformation(Map<String, dynamic> map)
      : name = map["name"] ?? "",
        address = map["address"] ?? "",
        rssi = map["rssi"] != "" ? int.parse(map["rssi"]) : -1,
        type = map["type"] != "" ? int.parse(map["type"]) : -1,
        bond = map["bondState"] != "" ? int.parse(map["bondState"]) : -1,
        uuid = map["uuids"] ?? "",
        deviceClass = map["class"] ?? "",
        describeContents = map["describeContents"] != "" ? int.parse(map["describeContents"]) : -1,
        hash = map["hashCode"] != "" ? int.parse(map["hashCode"]) : -1;

  @override
  String toString() {
    return 'BluetoothDeviceInformation{name: $name, address: $address, rssi: $rssi, type: $type, bond: $bond, uuid: $uuid, deviceClass: $deviceClass, describeContents: $describeContents, hashCode: $hash}';
  }
}
