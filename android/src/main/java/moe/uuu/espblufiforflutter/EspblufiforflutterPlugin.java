package moe.uuu.espblufiforflutter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * EspblufiforflutterPlugin
 */

public class EspblufiforflutterPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {

    // 接收來自 Flutter 的通知，可以回覆該通知
    private MethodChannel methodChannel;
    // 可以隨時傳送的通知
    private EventChannel eventChannel;
    private EventChannel.EventSink eventChannelSink = null;

    private Map<String, ScanResult> mDeviceMap;
    private volatile long mScanStartTime;
    private ScanCallback mScanCallback;
    private Future<Boolean> mUpdateFuture;
    private ExecutorService mThreadPool;

    private long scanTimeout = 4000L;
    private Map<String, String> returnVal;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            // TODO:11111
            if (msg.what == 1) {

            }

            returnVal.put("v", msg.getData().getString("key"));
            eventChannelSink.success(returnVal);
        }
    };

    public EspblufiforflutterPlugin() {
        mDeviceMap = new HashMap<>();
        returnVal = new HashMap<>();
            mScanCallback = new ScanCallback();
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    @Override
    // 註冊這些通知的名稱
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        methodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_to_native");
        methodChannel.setMethodCallHandler(this);
        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "native_to_flutter");
        eventChannel.setStreamHandler(this);
    }

    @Override
    // 接收 Flutter 的通知，call.method 是通知名稱
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        String method = call.method;
        // call.arguments 是通知的參數
        if (method.equals("getPlatformVersion")) {
            returnVal.clear();
            returnVal.put("k", "getPlatformVersion");
            returnVal.put("f", "result");
            returnVal.put("v", android.os.Build.VERSION.RELEASE);
            result.success(returnVal);
            pushPlatformVersion();
        } else if (method.equals("scan_bt_devices")) {
            returnVal.clear();
            returnVal.put("k", "scan_bt_devices");
            returnVal.put("f", "start");
            returnVal.put("v", "");
            result.success(returnVal);
            scan();
        } else {
            result.notImplemented();
        }
    }

    // 在其他地方可以直接返回資料的 eventChannel 通知
    private void pushPlatformVersion() {
        returnVal.clear();
        returnVal.put("k", "getPlatformVersion");
        returnVal.put("f", "event");
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
        Log.i("BT", "onListen");
        eventChannelSink = events;
    }

    @Override
    // eventChannelSink
    public void onCancel(Object arguments) {
        eventChannelSink = null;
    }

    // 蓝牙实现部分

    private void onIntervalScanUpdate(boolean over) {
        Log.i("BT", "onIntervalScanUpdate");
        List<ScanResult> devices = new ArrayList<>(mDeviceMap.values());
        Collections.sort(devices, (dev1, dev2) -> {
//            Log.i("BT", "devices");
            Integer rssi1 = null;
                rssi1 = dev1.getRssi();
            Integer rssi2 = null;
                rssi2 = dev2.getRssi();
            return rssi2.compareTo(rssi1);
        });

        Log.i("BT", "devices:" + devices.size());
        Log.i("BT", devices.toString());

        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("key", "555");
        message.setData(bundle);
        handler.sendMessage(message);
        returnVal.put("k", "scan_bt_devices");
        returnVal.put("t", "dev");
        returnVal.put("v", devices.toString());
//        runOnUiThread(() -> {
//            mBleList.clear();
//            mBleList.addAll(devices);
//            mBleAdapter.notifyDataSetChanged();
//
//            if (over) {
//                mBinding.refreshLayout.setRefreshing(false);
//            }
//        });
    }

    private void returnError(String error) {
        returnVal.put("k", "scan_bt_devices");
        returnVal.put("t", "stat");
        returnVal.put("v", error);
        eventChannelSink.success(returnVal);
    }

    private String scan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner = adapter.getBluetoothLeScanner();
        } else {
            Log.i("[BT]", "os_var_low"); // 系統版本過低
            return "os_var_low";
        }
        if (!adapter.isEnabled() || scanner == null) {
            Log.i("[BT]", "main_bt_disable_msg"); // 藍芽不可用
//            mBinding.refreshLayout.setRefreshing(false);
            return "main_bt_disable_msg";
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // 檢查定位服務是否開啟
//            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//            boolean locationEnable = locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager);
////            if (!locationEnable) {
////                Toast.makeText(this, R.string.main_location_disable_msg, Toast.LENGTH_SHORT).show();
////                mBinding.refreshLayout.setRefreshing(false);
////                return;
////            }
//        }

        mDeviceMap.clear();
//        mBleAdapter.notifyDataSetChanged();
//        mBlufiFilter = (String) BlufiApp.getInstance().settingsGet(SettingsConstants.PREF_SETTINGS_KEY_BLE_PREFIX,
//                BlufiConstants.BLUFI_PREFIX);
        mScanStartTime = SystemClock.elapsedRealtime();
        Log.i("[BT]", "start_scan_ble"); // 開始搜尋藍芽裝置
        scanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mScanCallback);
        mUpdateFuture = mThreadPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                long scanCost = SystemClock.elapsedRealtime() - mScanStartTime;
                if (scanCost > scanTimeout) {
                    break;
                }

                onIntervalScanUpdate(false);
            }

            BluetoothLeScanner inScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            if (inScanner != null) {
                inScanner.stopScan(mScanCallback);
            }
            onIntervalScanUpdate(true);
            Log.i("[BT]", "thread_interrupted"); // 掃描執行緒中斷
            returnError("thread_interrupted");
            return true;
        });
        return "start_scan_ble"; // 開始掃描
    }

    private void stopScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = null;
            scanner = adapter.getBluetoothLeScanner();
        if (scanner != null) {
                scanner.stopScan(mScanCallback);
        }
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
        }
        Log.i("[BT]", "stop_scan_ble");
        returnError("stop_scan_ble");
    }

    public class ScanCallback extends android.bluetooth.le.ScanCallback {
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                onLeScan(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onLeScan(result);
        }

        private void onLeScan(ScanResult scanResult) {
            String name = scanResult.getDevice().getName();
//            if (!TextUtils.isEmpty(mBlufiFilter)) {
//                if (name == null || !name.startsWith(mBlufiFilter)) {
//                    return;
//                }
//            }
            mDeviceMap.put(scanResult.getDevice().getAddress(), scanResult);
        }
    }
}
