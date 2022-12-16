package moe.uuu.espblufiforflutter;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * Flutter 外掛主類
 *
 * @author 神楽坂雅詩
 * @version 1.0.0
 */
public class EspblufiforflutterPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {

    // 接收來自 Flutter 的通知，可以回覆該通知
    private MethodChannel methodChannel;
    private EventChannel.EventSink eventChannelSink = null;
    private BluScan bluScan = new BluScan();

    public EspblufiforflutterPlugin() {
    }

    @Override
    // 註冊這些通知的名稱
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        methodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_to_native");
        methodChannel.setMethodCallHandler(this);
        // 可以隨時傳送的通知
        EventChannel eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "native_to_flutter");
        eventChannel.setStreamHandler(this);
    }

    @Override
    // 接收 Flutter 的通知，call.method 是通知名稱
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        String method = call.method;
        Map<String, String> arg = new HashMap<>();
        if (call.arguments != null) {
            arg = (Map<String, String>) call.arguments;
        }
        Map<String, Object> returnVal = new HashMap<>();
        if (method.equals("getPlatformVersion")) {
            returnVal.put("k", "getPlatformVersion");
            returnVal.put("t", "result");
            returnVal.put("v", android.os.Build.VERSION.RELEASE);
            result.success(returnVal);
            pushPlatformVersion();
        } else if (method.equals("scan_bt_devices")) {
            bluScan.config(arg);
            returnVal.put("k", "scan_bt_devices");
            returnVal.put("v", bluScan.scan());
            result.success(returnVal);
        } else if (method.equals("stop_scan_ble")) {
            bluScan.config(arg);
            returnVal.put("k", "stop_scan_ble");
            returnVal.put("v", bluScan.stopScan());
            result.success(returnVal);
        } else {
            result.notImplemented();
        }
    }

    // 在其他地方可以直接返回資料的 eventChannel 通知
    private void pushPlatformVersion() {
        Map<String, Object> returnVal = new HashMap<>();
        returnVal.clear();
        returnVal.put("k", "getPlatformVersion");
        returnVal.put("t", "event");
        returnVal.put("v", android.os.Build.VERSION.RELEASE);
        // 將資料返回給 Flutter ，可以是 Map 也可以是 String 等其他。
        // 注意用 EventChannel.EventSink 不是 EventChannel 型別。
        eventChannelSink.success(returnVal);
    }

    @Override
    // eventChannelSink
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
    }

    @Override
    // eventChannelSink
    public void onListen(Object arguments, EventChannel.EventSink events) {
        eventChannelSink = events;
        bluScan.eventChannelSink = events;
    }

    @Override
    // eventChannelSink
    public void onCancel(Object arguments) {
        eventChannelSink = null;
    }

    // 蓝牙实现部分


}
