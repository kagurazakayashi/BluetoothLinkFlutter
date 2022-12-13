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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private EventChannel.EventSink eventChannelSink = null;

    private final Map<String, ScanResult> mDeviceMap;
    private volatile long mScanStartTime;
    private final ScanCallback mScanCallback;
    private Future<Boolean> mUpdateFuture;
    private final ExecutorService mThreadPool;

    private final long scanTimeout = 10000L;
    private final Map<String, Object> returnVal;

    // 接收子執行緒發來的資訊
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            android.os.Bundle bundle = msg.getData();
            returnVal.clear();
            returnVal.put("k", "scan_bt_devices");
            if (msg.what == 1) {
                // 搜尋到的各個藍芽裝置 (JSON)
                returnVal.put("t", "list");
                returnVal.put("r", !bundle.getBoolean("end"));
                returnVal.put("c", bundle.getLong("time"));
            } else if (msg.what == 2) {
                // 狀態變化 (String)
                returnVal.put("t", "stat");
            } else if (msg.what == 3) {
                // 搜尋到一個藍芽裝置 (JSON)
                returnVal.put("t", "scan");
            }
            returnVal.put("v", bundle.getString("v"));
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
        // 可以隨時傳送的通知
        EventChannel eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "native_to_flutter");
        eventChannel.setStreamHandler(this);
    }

    @Override
    // 接收 Flutter 的通知，call.method 是通知名稱
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        String method = call.method;
        if (method.equals("getPlatformVersion")) {
            returnVal.clear();
            returnVal.put("k", "getPlatformVersion");
            returnVal.put("t", "result");
            returnVal.put("v", android.os.Build.VERSION.RELEASE);
            result.success(returnVal);
            pushPlatformVersion();
        } else if (method.equals("scan_bt_devices")) {
            returnVal.clear();
            returnVal.put("k", "scan_bt_devices");
            returnVal.put("v", scan());
            result.success(returnVal);
        } else if (method.equals("stop_scan_ble")) {
            returnVal.clear();
            returnVal.put("k", "stop_scan_ble");
            returnVal.put("v", stopScan());
            result.success(returnVal);
        } else {
            result.notImplemented();
        }
    }

    // 在其他地方可以直接返回資料的 eventChannel 通知
    private void pushPlatformVersion() {
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
    }

    @Override
    // eventChannelSink
    public void onCancel(Object arguments) {
        eventChannelSink = null;
    }

    // 蓝牙实现部分

    // 扫描蓝牙设备时执行，会在扫描过程中执行多次
    private void onIntervalScanUpdate(boolean over, long scanCost) {
        Message message = new Message();
        message.what = 1;
        Bundle bundle = new Bundle();
        bundle.putString("v", devicesJSON(null));
        bundle.putBoolean("end", over);
        bundle.putLong("time", scanTimeout - scanCost);
        message.setData(bundle);
        handler.sendMessage(message);
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

    /**
     * 將已掃描到的藍芽裝置資訊轉換為 JSON 字串
     * @return JSON 字串
     */
    private String devicesJSON(String onlyAddr) {
//        Collections.sort(devices, (dev1, dev2) -> {
//            Integer rssi1 = null;
//            rssi1 = dev1.getRssi();
//            Integer rssi2 = null;
//            rssi2 = dev2.getRssi();
//            return rssi2.compareTo(rssi1);
//        });
        List<Map<String, String>> btinfos = new LinkedList<>();
        List<ScanResult> devices = new ArrayList<>(mDeviceMap.values());
        for (ScanResult device : devices) {
            Map<String, String> btinfo = new LinkedHashMap();
            android.bluetooth.BluetoothDevice deviceInfo = device.getDevice();
            String addr = deviceInfo.getAddress();
            if (onlyAddr != null && !onlyAddr.equals(addr)) {
                continue;
            }
            btinfo.put("name", deviceInfo.getName());
            btinfo.put("address", addr);
            btinfo.put("rssi", String.valueOf(device.getRssi()));
            btinfo.put("type", String.valueOf(deviceInfo.getType()));
            btinfo.put("bondState", String.valueOf(deviceInfo.getBondState()));
            btinfo.put("uuids", Arrays.toString(deviceInfo.getUuids()));
            btinfo.put("class", String.valueOf(deviceInfo.getBluetoothClass()));
//            btinfo.put("manufacturer", String.valueOf(deviceInfo.getManufacturerSpecificData()));
//            btinfo.put("serviceData", String.valueOf(deviceInfo.getServiceData()));
//            btinfo.put("serviceUuids", String.valueOf(deviceInfo.getServiceUuids()));
//            btinfo.put("txPowerLevel", String.valueOf(deviceInfo.getTxPowerLevel()));
            btinfo.put("describeContents", String.valueOf(deviceInfo.describeContents()));
            btinfo.put("hashCode", String.valueOf(deviceInfo.hashCode()));
//            btinfo.put("toString", String.valueOf(deviceInfo.toString()));
            btinfos.add(btinfo);
        }
        JSONArray jsonArray = new JSONArray(btinfos);
        return jsonArray.toString();
    }

    private void returnError(String error) {
        Message message = new Message();
        message.what = 2;
        Bundle bundle = new Bundle();
        bundle.putString("v", error);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private String scan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = null;
        scanner = adapter.getBluetoothLeScanner();
        if (!adapter.isEnabled() || scanner == null) {
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

                onIntervalScanUpdate(false, scanCost);
            }

            BluetoothLeScanner inScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            if (inScanner != null) {
                inScanner.stopScan(mScanCallback);
            }
            onIntervalScanUpdate(true, 0);
            returnError("thread_interrupted");
            return true;
        });
        return "start_scan_ble"; // 開始掃描
    }

    // 停止藍芽掃描
    private String stopScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner != null) {
            scanner.stopScan(mScanCallback);
        }
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
        }
        return "stop_scan_ble"; // 停止掃描
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
//            String name = scanResult.getDevice().getName();
//            if (!TextUtils.isEmpty(mBlufiFilter)) {
//                if (name == null || !name.startsWith(mBlufiFilter)) {
//                    return;
//                }
//            }
            String addr = scanResult.getDevice().getAddress();
            // 去重，所以用 Map
            mDeviceMap.put(addr, scanResult);
            Message message = new Message();
            message.what = 3;
            Bundle bundle = new Bundle();
            bundle.putString("v", devicesJSON(addr));
            message.setData(bundle);
//            handler.sendMessage(message);
        }
    }
}
