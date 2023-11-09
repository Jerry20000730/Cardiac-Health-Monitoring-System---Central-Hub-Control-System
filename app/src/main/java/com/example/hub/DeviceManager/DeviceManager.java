/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.DeviceManager
 * @ClassName: DeviceManager
 * @Description: the device manager, to manage the bluetooth device, contains some useful utils
 * @Author: GRP_Team14
 * @CreateDate: 2022/1/14
 * @Version: 1.0
 */

package com.example.hub.DeviceManager;

import android.bluetooth.BluetoothClass;
import android.content.Context;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.example.hub.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A manager to manage the ble device that has been added into a list
 */
public class DeviceManager {
    // contain the device (BleDevice class) and Boolean (connectivity status)
    private HashMap<BleDevice, Boolean> DeviceCache;
    private static DeviceManager deviceManagerInstance = new DeviceManager();

    /**
     * the constructor of DeviceManager
     */
    private DeviceManager() {
        this.DeviceCache = new HashMap<>();
    }

    /**
     * Using singleton design pattern
     * Method to return single instance of device manager
     * @return single instance of device manager
     */
    public static DeviceManager getInstance(){
        return deviceManagerInstance;
    }

    /**
     * Getter function to get device cache
     * @return hashmap contain BLE device and its connectivity
     */
    public HashMap<BleDevice, Boolean> getDeviceCache() {
        return DeviceCache;
    }

    /**
     * Method to get the size of the ble device list
     * @return the size of the ble device list
     */
    public int getCount() {
        return DeviceCache.size();
    }

    /**
     * Method to get the device status providing the instance of BleDevice
     * @param bleDevice the requested BLE device
     * @return the device's connectivity status (true for connected, false for not connected)
     */
    public Boolean getDeviceStatusInfo(BleDevice bleDevice) {
        return DeviceCache.get(bleDevice);
    }

    /**
     * Method to get the device status providing the name of the BLE device
     * @param bleDeviceName the name of the requested BLE device
     * @return the device's connectivity status (true for connected, false for not connected)
     */
    public Boolean getDeviceStatus(String bleDeviceName) {
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getKey().getName().contains(bleDeviceName))
                return entry.getValue();
        }
        return null;
    }

    /**
     * Method to get the device status and return the status by string
     * @param bleDeviceName the name of the requested BLE device
     * @return
     */
    public String getDeviceStatusInfo(String bleDeviceName) {
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getKey().getName().contains(bleDeviceName))
                if (entry.getValue()) {
                    return "Connected";
                } else {
                    return "Disconnected";
                }
        }
        return null;
    }

    public BleDevice getDeviceByItsName(String bleDeviceName) {
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getKey().getName().contains(bleDeviceName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isDeviceContained(String bleDeviceName) {
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getKey().getName().contains(bleDeviceName)) {
                return true;
            }
        }
        return false;
    }

    public void setDeviceStatusAll(boolean isConnected) {
        if (!DeviceCache.isEmpty()) {
            for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
                entry.setValue(isConnected);
            }
        }
    }

    /**
     * Method to add device into the list
     * @param bleDevice ble device of BleDevice class
     */
    public void addDevice(BleDevice bleDevice) {
        DeviceCache.put(bleDevice, false);
    }

    /**
     * Method to add device into the list and
     * @param bleDevice
     * @param status
     */
    public void addDevice(BleDevice bleDevice, boolean status) {
        DeviceCache.put(bleDevice, status);
    }

    public String[] getConnectedDeviceNameList() {
        String[] deviceNameList = new String[getConnectedDeviceCount()];
        int i = 0;
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getValue()) {
                deviceNameList[i] = entry.getKey().getName();
                i++;
            }
        }
        return deviceNameList;
    }

    public String[] getDeviceNameList() {
        String[] deviceNameList = new String[getCount()];
        int i = 0;
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            deviceNameList[i] = entry.getKey().getName();
            i++;
        }
        return deviceNameList;
    }

    /**
     * Method to get the number of the connected device
     * @return the number of device count
     */
    public int getConnectedDeviceCount() {
        int count = 0;
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getValue()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Method to judge whether the devices are all connected
     * @return true <- all of the device is connected
     *         false <- some of the device is not connected
     */
    public boolean isAllConnected() {
        boolean isAllConnected = true;
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (!entry.getValue()) {
                isAllConnected = false;
            }
        }
        return isAllConnected;
    }

    /**
     * Method to judge whether all the device are all disconnected
     * @return true <- all of the device is disconnected
     *         false <- some of the device is still connected
     */
    public boolean isAllDisconnected() {
        boolean isAllDisconnected = true;
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getValue()) {
                isAllDisconnected = false;
            }
        }
        return isAllDisconnected;
    }

    /**
     * Method to remove the device from the list
     * @param bleDevice ble device of BleDevice class
     */
    public void removeDevice(BleDevice bleDevice) {
        DeviceCache.remove(bleDevice);
    }

    /**
     * Method to clear scan device and connected device
     */
    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

    /**
     * Method to clear the connected device in the list
     */
    public void clearConnectedDevice() {
        Iterator<Map.Entry<BleDevice, Boolean>> it = DeviceCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BleDevice, Boolean> entry = it.next();
            if (BleManager.getInstance().isConnected(entry.getKey())) {
                it.remove();
            }
        }
    }

    /**
     * Method to clear the scanned device in the list
     */
    public void clearScanDevice() {
        Iterator<Map.Entry<BleDevice, Boolean>> it = DeviceCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BleDevice, Boolean> entry = it.next();
            if (!BleManager.getInstance().isConnected(entry.getKey())) {
                it.remove();
            }
        }
    }

    public boolean containDevice(BleDevice bleDevice) {
        boolean isContain = false;
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getKey().getName().equals(bleDevice.getName())) {
                isContain = true;
            }
        }
        return isContain;
    }

    /**
     * Method to judge whether the device is contained by passing the name of the device
     * @param deviceName the name of BLE device
     * @return true <- the device (name) is contained in the hashmap
     *         false <- the device (name) is not contained in the hashmap
     */
    public boolean containDeviceName(String deviceName) {
        boolean isContain = false;
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getKey().getName().equals(deviceName)) {
                isContain = true;
            }
        }
        return isContain;
    }

    public ArrayList<BleDevice> getAllDisconnectBleDevice() {
        ArrayList<BleDevice> disconnectDeviceList = new ArrayList<BleDevice>();
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (!entry.getValue()) {
                disconnectDeviceList.add(entry.getKey());
            }
        }
        return disconnectDeviceList;
    }

    public ArrayList<BleDevice> getAllConnectBleDevice() {
        ArrayList<BleDevice> ConnectDeviceList = new ArrayList<BleDevice>();
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            if (entry.getValue()) {
                ConnectDeviceList.add(entry.getKey());
            }
        }
        return ConnectDeviceList;
    }

    public ArrayList<BleDevice> getAllBleDevice() {
        ArrayList<BleDevice> DeviceList = new ArrayList<BleDevice>();
        for (Map.Entry<BleDevice, Boolean> entry : DeviceCache.entrySet()) {
            DeviceList.add(entry.getKey());
        }
        return DeviceList;
    }

    public ArrayList<BleDevice> sortDevice() {
        ArrayList<BleDevice> ConnectDeviceList = getAllConnectBleDevice();
        for (int i=0; i<ConnectDeviceList.size()-1; i++) {
            for (int j=i+1; j<ConnectDeviceList.size(); j++) {
                if (Integer.parseInt(ConnectDeviceList.get(i).getName().substring(2, 5)) < Integer.parseInt(ConnectDeviceList.get(i).getName().substring(2, 5))) {
                    Collections.swap(ConnectDeviceList, i, j);
                }
            }
        }
        return ConnectDeviceList;
    }
}
