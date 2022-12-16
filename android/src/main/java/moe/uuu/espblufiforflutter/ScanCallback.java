package moe.uuu.espblufiforflutter;

import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 藍芽掃描回撥
 *
 * @author 神楽坂雅詩
 * @version 1.0.0
 */
public class ScanCallback extends android.bluetooth.le.ScanCallback {
    public Handler handler;
    public final Map<String, ScanResult> mDeviceMap;
    public boolean realCallback = false;

    /**
     * 建構函式
     */
    public ScanCallback(Handler handler) {
        mDeviceMap = new HashMap<>();
        this.handler = handler;
    }

    /**
     * 無法啟動掃描時的回撥
     *
     * @param errorCode 掃描失敗的錯誤碼 (one of SCAN_FAILED_*)
     */
    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
    }

    /**
     * 批次結果下發回撥
     *
     * @param results 之前掃描的掃描結果列表
     */
    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult result : results) {
            onLeScan(result);
        }
    }

    /**
     * 發現藍芽裝置廣播時的回撥
     *
     * @param callbackType 確定如何觸發此回撥。 可能是其中之一：
     *                     - {ScanSettings#CALLBACK_TYPE_ALL_MATCHES}
     *                     - {ScanSettings#CALLBACK_TYPE_FIRST_MATCH}
     *                     - {ScanSettings#CALLBACK_TYPE_MATCH_LOST}
     * @param result       藍芽掃描結果
     */
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        onLeScan(result);
    }

    /**
     * 向 Flutter 彙報掃描結果
     *
     * @param scanResult 藍芽掃描結果
     */
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
        if (realCallback) {
            Message message = new Message();
            message.what = 3;
            Bundle bundle = new Bundle();
            bundle.putString("v", devicesJSON(addr));
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    /**
     * 將已掃描到的藍芽裝置資訊轉換為 JSON 字串
     *
     * @return JSON 字串
     */
    public String devicesJSON(String onlyAddr) {
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
}
