package moe.uuu.espblufiforflutter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.flutter.plugin.common.EventChannel;

public class BluScan {
    public EventChannel.EventSink eventChannelSink = null;
    private volatile long mScanStartTime;
    private final ScanCallback mScanCallback;
    private Future<Boolean> mUpdateFuture;
    private final ExecutorService mThreadPool;

    private long scanTimeout = 10000L;
    private long scanInterval = 1000L;

    // 接收子執行緒發來的資訊
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Map<String, Object> returnVal = new HashMap<>();
            android.os.Bundle bundle = msg.getData();
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

    /**
     * 建構函式
     */
    public BluScan() {
        mScanCallback = new ScanCallback(handler);
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    /**
     * 配置本功能
     *
     * @param arg 從 Flutter 傳過來的引數
     */
    public void config(Map<String, String> arg) {
        if (arg == null) {
            return;
        }
        if (arg.containsKey("timeout")) {
            scanTimeout = Long.parseLong(Objects.requireNonNull(arg.get("timeout")));
        }
        if (arg.containsKey("interval")) {
            scanInterval = Long.parseLong(Objects.requireNonNull(arg.get("interval")));
        }
        if (arg.containsKey("real")) {
            mScanCallback.realCallback = Boolean.parseBoolean(Objects.requireNonNull(arg.get("real")));
        }
    }

    /**
     * 定時彙報藍芽掃描結果
     *
     * @param over     是否已結束
     * @param scanCost 已經過毫秒數
     */
    private void onIntervalScanUpdate(boolean over, long scanCost) {
        Message message = new Message();
        message.what = 1;
        Bundle bundle = new Bundle();
        bundle.putString("v", mScanCallback.devicesJSON(null));
        bundle.putBoolean("end", over);
        long eta = over ? 0 : scanTimeout - scanCost;
        bundle.putLong("time", eta);
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
     * 向 Flutter 報告狀態變更資訊
     *
     * @param error 資訊內容
     */
    private void returnError(String error) {
        Message message = new Message();
        message.what = 2;
        Bundle bundle = new Bundle();
        bundle.putString("v", error);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    /**
     * 開始掃描藍芽裝置
     *
     * @return 報告掃描任務是否成功開始執行，以及原因
     */
    public String scan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
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

        mScanCallback.mDeviceMap.clear();
//        mBleAdapter.notifyDataSetChanged();
//        mBlufiFilter = (String) BlufiApp.getInstance().settingsGet(SettingsConstants.PREF_SETTINGS_KEY_BLE_PREFIX,
//                BlufiConstants.BLUFI_PREFIX);
        mScanStartTime = SystemClock.elapsedRealtime();
        try {
            scanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mScanCallback);
            mUpdateFuture = mThreadPool.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(scanInterval);
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
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("BT", "startScan: " + e.getMessage());
            return "start_scan_ble_error";
        }
        return "start_scan_ble"; // 開始掃描
    }

    /**
     * 停止藍芽掃描
     *
     * @return 報告掃描任務是否成功停止執行，以及原因
     */
    public String stopScan() {
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
}
