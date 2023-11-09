/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.sensorCheck
 * @ClassName: sensorCheck
 * @Description: batteryCheck, broadcast and for a fixed period of time
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/3
 * @Version: 1.0
 */

package com.example.hub.sensorCheck;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.appcompat.app.AlertDialog;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.example.hub.Constant.Constant;
import com.example.hub.DeviceManager.DeviceManager;
import com.example.hub.MainActivity;
import com.example.hub.Notification.Notification;

import java.util.Map;

/**
 * Background service that run on fixed period of time
 * to get the battery level of three sensors.
 */
public class sensorCheck extends Service {
    private static final String ACTION = "BATTERY_CHECK";
    private static final long TIME_INTERVAL = 60000 * 60;
    private PendingIntent pendingIntent;
    private AlarmManager manager;

    private BroadcastReceiver batteryCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: indicate for bluetooth service
            for (Map.Entry<BleDevice, Boolean> entry : DeviceManager.getInstance().getDeviceCache().entrySet()) {
                if (entry.getValue()) {
                    readi(entry.getKey(), Constant.BATTERY_SERVICE_UUID, Constant.BATTERY_CHARACTERISTIC_UUID);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter(ACTION);
        registerReceiver(batteryCheckReceiver, intentFilter);

        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION), 0);

        // Android 6.0+ need this method to invoke alarm service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        }
        // Android 4.4+ need this method to invoke alarm service
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        }
        // Android 4.4- need old way to invoke alarm service
        else {
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), TIME_INTERVAL, pendingIntent);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryCheckReceiver);
        manager.cancel(pendingIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // read individual sensors
    public void readi(BleDevice bleDevice, String uuid_service, String uuid_characteristic_read) {
        BleManager.getInstance().read(
                bleDevice,
                uuid_service,
                uuid_characteristic_read,
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        System.out.println("Battery Level of " + bleDevice.getName() + " is received.");
                        Integer battery_level = (int) (data[0] & 0xFF);
                        System.out.println("Battery Level of " + bleDevice.getName() + " is: " + battery_level + ".");
                        if (battery_level == 5)
                            Notification.getInstance(sensorCheck.this).sendMsgOnLowBatteryOfSensors(sensorCheck.this, bleDevice.getName());
                        else if (battery_level == 20)
                            Notification.getInstance(sensorCheck.this).sendMsgOnExtremeLowBatteryOfSensors(sensorCheck.this, bleDevice.getName());
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        System.out.println("Battery Level of " + bleDevice.getName() + " is not received.");
                    }
                });
    }


}
