/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub
 * @ClassName: MainActivity
 * @Description: Main activity of the Hub
 * @Author: GRP_Team14
 * @CreateDate: 2022/1/14
 * @Version: 1.0
 */

package com.example.hub;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.example.hub.BLESubscribeThread.BLESubscribeThread;
import com.example.hub.DeviceManager.DeviceManager;
import com.example.hub.HexUtil.HexUtil;
import com.example.hub.SCP.SFTPFileTransfer;
import com.example.hub.Server.serverThread;
import com.example.hub.WriteSuccessObserver.WriteSuccessObserver;
import com.example.hub.WriteSuccessObserver.isWriteSuccessObservable;
import com.example.hub.Constant.Constant;
import com.example.hub.FileOperator.fileOperator;
import com.example.hub.Notification.Notification;
import com.example.hub.isInitialBindingSuccess.BindingSuccessObserver;
import com.example.hub.isInitialBindingSuccess.isInitialBindingSuccessObservable;
import com.example.hub.isNotifiableObserver.NotifiableObserver;
import com.example.hub.sensorCheck.sensorCheck;
import com.stephentuso.welcome.WelcomeHelper;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    // permission code
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    // array to store the read and write permission of storage
    private static final String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final String SENSORLISTFILE = "/storage/emulated/0/sensors.txt";

    private WelcomeHelper welcomeScreen;

    // the name list of the sensor, used to store the name when reading from a local file "sensors.txt"
    // the sensors are pre-binded
    private static String[] sensorNames;
    // the progress dialog, used when scanning the bluetooth, to give a visualization
    private ProgressDialog scanningProgressDialog;
    // the progress dialog, used when connecting the bluetooth, to give a visualization
    private ProgressDialog connectingProgressDialog;
    // the SQLite database to store the data
    private SQLiteDatabase db;
    // the notification instance, only one instance exist in Notification class
    private Notification notification;
    // the observable of writing success, used to check whether the writing command to designated device
    // is successful
    private isWriteSuccessObservable isWriteSuccess;
    // private isNotifiableObservable isNotifiable;
    private isInitialBindingSuccessObservable isInitialBindingSuccess;
    // device manager, used to store and manage all the BLE devices
    private DeviceManager deviceManager;

    // view components
    @SuppressLint("StaticFieldLeak")
    // text view to display the log
    private static TextView DMdisplay;
    // three buttons
    private Button start_scan_btn;
    private Button start_recording_btn;
    private Button stop_recording_btn;
    private Button upload_btn;
    private Button acquire_sensor_battery_level;
    private Button disconnect_all_sensors;
    // three textview to display the device name
    private TextView device_name_text_a;
    private TextView device_name_text_b;
    private TextView device_name_text_c;
    // three textview to display the device connectivity status
    private TextView device_status_a;
    private TextView device_status_b;
    private TextView device_status_c;
    // three textview to display the device battery level
    private TextView device_battery_level_a;
    private TextView device_battery_level_b;
    private TextView device_battery_level_c;

    // six subscribe thread for three different sensors
    private BLESubscribeThread bleSubscribeThread_hr_CL831;
    private BLESubscribeThread bleSubscribeThread_ppg_CL831;
    private BLESubscribeThread bleSubscribeThread_hr_CL880;
    private BLESubscribeThread bleSubscribeThread_ppg_CL880;
    private BLESubscribeThread bleSubscribeThread_hr_CL800;
    private BLESubscribeThread bleSubscribeThread_acc_CL800;

    // textview of battery level of hub
    private TextView batteryLevel;
    private TextView progressTextView;
    // progressBar for indicating the progress of validating & uploading
    private ProgressBar uploadingProgressBar;

    private BroadcastReceiver batteryLevelReceiver;
    private IntentFilter batteryLevelFilter;

    // private uploadTask uploadtask;
    private processTask processtask;
    private socketTask sockettask;

    // temporary solution
    private int connection_count = 0;
    private int record_count = 0;

    private int fileCount = 0;
    private int total_file_count = 0;
    private String start_time;
    private String stored_path;

    /**
     * onCreate is the first method that will be loaded every time the activity is activated
     *
     * @param savedInstanceState savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load local file into sensor list file
        checkPermission();

        // area of UI
        start_scan_btn = findViewById(R.id.start_scan);
        start_recording_btn = findViewById(R.id.start_recording);
        stop_recording_btn = findViewById(R.id.stop_recording);
        upload_btn = findViewById(R.id.uploadButton);
        acquire_sensor_battery_level = findViewById(R.id.sensorBatteryButton);
        disconnect_all_sensors = findViewById(R.id.sensorActiveDisconnectButton);

        // device name text view
        device_name_text_a = findViewById(R.id.device_name1);
        device_name_text_b = findViewById(R.id.device_name2);
        device_name_text_c = findViewById(R.id.device_name3);
        // device connectivity text view
        device_status_a = findViewById(R.id.device_status1);
        device_status_b = findViewById(R.id.device_status2);
        device_status_c = findViewById(R.id.device_status3);
        // device battery text view
        device_battery_level_a = findViewById(R.id.device_battery1);
        device_battery_level_b = findViewById(R.id.device_battery2);
        device_battery_level_c = findViewById(R.id.device_battery3);

        // display area
        DMdisplay = findViewById(R.id.DMdisplay);
        // battery level text
        batteryLevel = findViewById(R.id.BatteryTextView);
        // upload progress status text view
        progressTextView = findViewById(R.id.progressTextView);
        // upload progress bar
        uploadingProgressBar = findViewById(R.id.uploadProgressBar);

        // uploadtask = new uploadTask();
        processtask = new processTask();
        sockettask = new socketTask();

        // judge whether the binding is successful
        isInitialBindingSuccess = isInitialBindingSuccessObservable.getInstance();
        BindingSuccessObserver bindingSuccessObserver = new BindingSuccessObserver(this);
        isInitialBindingSuccess.addObserver(bindingSuccessObserver);

        // set sensor list
        try {
            while (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == -1);
            if (fileOperator.isFileExistAndNonEmpty(SENSORLISTFILE)) {
                isInitialBindingSuccess.setIsInitialBindingSuccess(true);
            } else {
                welcomeScreen = new WelcomeHelper(this, InitialBindingWelcomeActivity.class);
                welcomeScreen.forceShow();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // bind Developer mode switch strategy
        DMSwitchStrategy();
        // initialize the data
        try {
            initData();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // initialize the view
        initView();

        // bind button behaviour
        startScanBtnClick();
        startRecordingBtnClick();
        stopRecordingBtnClick();
        uploadFileBtnClick();
        acquireButtonClicked();
        activeDisconnectSensorButtonClicked();

        // monitor battery state
        monitorBatteryState();

        turnOnOffHeartModule(0);

          // deprecate: Thread for socket communication
//        new Thread(() -> {
//            try {
//                // port for socket communication is set on 7801
//                int port = 7801;
//                while (true) {
//                    ServerSocket serverSocket = new ServerSocket(port);
//                    runOnUiThread(() -> addTextInDMDisplay("[INFO] Server is listening on port: " + port));
//                    Socket socket = serverSocket.accept();
//                    serverThread server = new serverThread(socket);
//                    String returnINFO = server.recv();
//                    assert returnINFO != null;
//                    if ((int) server.inputHeader.getInstructionCmd() == 1) {
//                        runOnUiThread(() -> addTextInDMDisplay(returnINFO));
//                        sensorNames = returnINFO.substring(24).split(",");
//                        checkPermission();
//                        String info = trans_hashmap_to_string();
//                        System.out.println(info);
//                        server.send(info);
//                    } else if ((int) server.inputHeader.getInstructionCmd() == 2) {
//                        // add something here maybe
//                        runOnUiThread(() -> addTextInDMDisplay(returnINFO));
//                        NotifySuccessObserver observer = new NotifySuccessObserver(server);
//                        isNotifySuccess.addObserver(observer);
//                        checkConnectivity();
//
//                        // maybe need modification
//                        long time = System.currentTimeMillis();
//                        Date date = new Date(time);
//                        String path = fileOperator.setUpOrganizedDataDirectory("/storage/emulated/0/", date);
//                        BLENotify(path);
//                    } else {
//                        runOnUiThread(() -> addTextInDMDisplay(returnINFO));
//                        server.sendFile();
//                    }
//                    serverSocket.close();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSensorBatteryCheck();
        if (!deviceManager.isAllDisconnected()) {
            BleManager.getInstance().disconnectAllDevice();
            BleManager.getInstance().destroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (welcomeScreen != null) {
            welcomeScreen.onSaveInstanceState(outState);
        }
    }

    /**
     * Method to bind the sensor name textview with sensor names that loaded from local file
     */
    private void bindSensorNames() {
        // bind sensors names into view
        for (String deviceName : sensorNames) {
            if (deviceName.contains("CL831")) {
                device_name_text_a.setText(deviceName);
            } else if (deviceName.contains("CL880")) {
                device_name_text_b.setText(deviceName);
            } else if (deviceName.contains("CL800")) {
                device_name_text_c.setText(deviceName);
            }
        }
    }

    /**
     * For outside to use
     */
    public void enableBindingSensorNames() {
        bindSensorNames();
    }

    public void enableSocketAsyncTask() {
        // execute socket task
        sockettask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void enableSettingSensorList() throws IOException, InterruptedException {
        setSensorList(SENSORLISTFILE);
    }

    /**
     * Method to read sensors name from local file
     */
    private void setSensorList(String path) throws IOException, InterruptedException {
        Thread.sleep(500);
        String sensor_content = fileOperator.readFromFile(path);
        sensor_content = sensor_content.replace("\n", "");
        sensorNames = sensor_content.split(",");
    }

    /**
     * Method to bind sensor battery status after bluetooth read
     *
     * @param deviceName   the name of the device
     * @param batteryLevel the level of the battery
     */
    private void bindSensorBatteryStatus(String deviceName, int batteryLevel) {
        String batteryStatusPercentage = String.valueOf(batteryLevel) + "%";
        if (deviceName.contains("CL831")) {
            device_battery_level_a.setText(batteryStatusPercentage);
        } else if (deviceName.contains("CL880")) {
            device_battery_level_b.setText(batteryStatusPercentage);
        } else if (deviceName.contains("CL800")) {
            device_battery_level_c.setText(batteryStatusPercentage);
        }
    }

    /**
     * Method to bind sensor connectivity status after disconnect (whether actively or passively)
     * @param bleDevice the bledevice
     */
    private void updateSensorStatusWhenDisconnect(BleDevice bleDevice) {
        if (bleDevice.getName().contains("CL831")) {
            device_status_a.setText("Disconnected");
            device_battery_level_a.setText("Unknown");
        } else if (bleDevice.getName().contains("CL880")) {
            turnOnOffHeartModule(0);
            device_status_b.setText("Disconnected");
            device_battery_level_b.setText("Unknown");
        } else if (bleDevice.getName().contains("CL800")) {
            device_status_c.setText("Disconnected");
            device_battery_level_c.setText("Unknown");
        }
    }

    /**
     * Method to initialize the data, such as progress dialog, notification etc.
     */
    private void initData() throws InterruptedException {
        Thread.sleep(500);
        deviceManager = DeviceManager.getInstance();
        notification = Notification.getInstance(this);
        scanningProgressDialog = new ProgressDialog(this);
        connectingProgressDialog = new ProgressDialog(this);
        isWriteSuccess = new isWriteSuccessObservable();
        // isNotifiable = new isNotifiableObservable();
        db = openOrCreateDatabase("/storage/emulated/0/device.db", Context.MODE_PRIVATE, null);

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
    }

    /**
     * Method to initialize the view, e.g. which button to be enabled
     * while which should not be enabled
     */
    private void initView() {
        start_scan_btn.setEnabled(true);
        start_recording_btn.setEnabled(false);
        stop_recording_btn.setEnabled(false);
        upload_btn.setEnabled(true);
    }

    /**
     * Method to set the scan rule for hub to scan for BLE
     */
    private void setScanRule() {
        // sensor name list should be full
        // assert sensorNames != null;
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setDeviceName(true, sensorNames)         // only scan for designated device names send out by the edge
                .setScanTimeOut(10000)                          // scan upmost time, by default 10 seconds
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    /**
     * Get the device list
     *
     * @return the string list containing device name and id
     */
    public static String[] getSensorNames() {
        return sensorNames;
    }

    /**
     * Method of start scan button behaviour
     */
    private void startScanBtnClick() {
        start_scan_btn.setOnClickListener(v -> {
            if (checkBluetoothPermission()) {
                setScanRule();
                startScan();
            }
        });
    }

    /**
     * Method of start_recording button behaviour
     */
    private void startRecordingBtnClick() {
        start_recording_btn.setOnClickListener(v -> {
            start_recording_btn.setEnabled(false);
            stop_recording_btn.setEnabled(true);
            Toast.makeText(MainActivity.this, getString(R.string.recording_start), Toast.LENGTH_LONG).show();

//            WriteSuccessObserver observer = new WriteSuccessObserver();
//            isWriteSuccess.setWriteSuccess(false);
//            isWriteSuccess.addObserver(observer);
            if (record_count == 0) {
                long timestamp = System.currentTimeMillis();
                Date date = new Date(timestamp);
                start_time = Long.toString(timestamp);

                String path = fileOperator.setUpOrganizedDataDirectory("/storage/emulated/0/", date);
                // NotifiableObserver notifiableObserver = new NotifiableObserver(this, path);
                // isNotifiable.setIsNotifiable(false);
                // isNotifiable.addObserver(notifiableObserver);
                stored_path = path;
                new Thread(() -> {
                    try {
                        sleep(500);
                        // isNotifiable.setIsNotifiable(true);
                        runOnUiThread(() -> BLENotify(stored_path));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                new Thread(() -> {
                    try {
                        sleep(500);
                        // isNotifiable.setIsNotifiable(true);
                        runOnUiThread(() -> BLENotify(stored_path));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            record_count++;
        });
    }

    /**
     * Method of stop_recording button behaviour
     */
    private void stopRecordingBtnClick() {
        stop_recording_btn.setOnClickListener(v -> {
            start_scan_btn.setEnabled(true);
            start_recording_btn.setEnabled(true);
            stop_recording_btn.setEnabled(false);
            upload_btn.setEnabled(true);

            Toast.makeText(MainActivity.this, getString(R.string.recording_stop), Toast.LENGTH_LONG).show();

            ArrayList<BleDevice> connectDeviceList = deviceManager.getAllConnectBleDevice();
            for (int i = 0; i < connectDeviceList.size(); i++) {
                if (connectDeviceList.get(i).getName().contains("CL831") || connectDeviceList.get(i).getName().contains("CL880")) {
                    BleManager.getInstance().stopNotify(connectDeviceList.get(i), Constant.HEART_RATE_SERVICE_UUID, Constant.HEART_RATE_CHARACTERISTIC_UUID);
                    BleManager.getInstance().stopNotify(connectDeviceList.get(i), Constant.PPG_SERVICE_UUID, Constant.PPG_CHARACTERISTIC_UUID);
                } else if (connectDeviceList.get(i).getName().contains("CL800")) {
                    BleManager.getInstance().stopNotify(connectDeviceList.get(i), Constant.HEART_RATE_SERVICE_UUID, Constant.HEART_RATE_CHARACTERISTIC_UUID);
                    BleManager.getInstance().stopNotify(connectDeviceList.get(i), Constant.ACC_SERVICE_UUID, Constant.ACC_CHARACTERISTIC_UUID);
                }
            }
        });
    }

    /**
     * Method of upload file button behaviour
     */
    private void uploadFileBtnClick() {
        upload_btn.setOnClickListener(v -> {
            if (checkInternetPermissionForTransmittingHealthData()) {
                processtask = new processTask();
                processtask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    /**
     * Method of acquire battery level of three sensors
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void acquireButtonClicked() {
        acquire_sensor_battery_level.setOnClickListener(v -> {
            if (deviceManager.getConnectedDeviceCount() == 0) {
                String notice = getString(R.string.no_connection_battery_level);
                Toast.makeText(MainActivity.this, notice, Toast.LENGTH_LONG).show();
            } else {
                acquireBattery();
                String notice = "Battery level of " + String.join(", ", deviceManager.getConnectedDeviceNameList()) + " is updated";
                Toast.makeText(MainActivity.this, notice, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Button method to disconnect all sensors
     */
    private void activeDisconnectSensorButtonClicked() {
        disconnect_all_sensors.setOnClickListener(v -> {
            if (deviceManager.getConnectedDeviceCount() == 0) {
                Toast.makeText(MainActivity.this, "No device to disconnect!", Toast.LENGTH_LONG).show();
            } else {
                try {
                    turnOnOffHeartModule(0);
                    sleep(100);
                    stopSensorBatteryCheck();
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                BleManager.getInstance().disconnectAllDevice();
                BleManager.getInstance().destroy();
            }
        });
    }

    /**
     * Method to start scanning the ble devices
     * Bluetooth Module: FastBle is used
     */
    private void startScan() {
        connection_count = 0;
        start_scan_btn.setEnabled(false);
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                scanningProgressDialog = ProgressDialog.show(MainActivity.this, "", "Scanning");
                deviceManager.clearScanDevice();
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                // do nothing
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                scanningProgressDialog.dismiss();
                String ConnectedResult = "";
                int scan_count = 0;
                runOnUiThread(() -> addTextInDMDisplay("[INFO] Scan Finished"));
                if (scanResultList.size() > 0) {
                    for (int i = 0; i < scanResultList.size() - 1; i++) {
                        if (!deviceManager.containDevice(scanResultList.get(i))) {
                            scan_count++;
                            deviceManager.addDevice(scanResultList.get(i));
                            ConnectedResult += scanResultList.get(i).getName() + ", ";
                        }
                    }
                    if (!deviceManager.containDevice(scanResultList.get(scanResultList.size() - 1))) {
                        scan_count++;
                        deviceManager.addDevice(scanResultList.get(scanResultList.size() - 1));
                        ConnectedResult += scanResultList.get(scanResultList.size() - 1).getName();
                    }

                    if (scan_count == 0) {
                        noScanResultAlert().show();
                        runOnUiThread(() -> addTextInDMDisplay("[INFO] No available device is scanned and found"));
                        return;
                    }

                    String scannedNumber = "Current scanned device number: " + scan_count + "\n";
                    ConnectedResult = scannedNumber + "Current scanned device: " + ConnectedResult;

                    // pop scan alert on hub
                    scanAlert(ConnectedResult, scanResultList).show();
                    String finalConnectedResult = ConnectedResult;
                    runOnUiThread(() -> addTextInDMDisplay("[INFO]: " + finalConnectedResult));
                } else {
                    // pop no scan result alert on hub
                    noScanResultAlert().show();
                    runOnUiThread(() -> addTextInDMDisplay("[INFO] No available device is scanned and found"));
                }
            }
        });
    }

    /**
     * Method of FastBLE to connect to ble devices
     *
     * @param bleDevice the ble device that are about to connect
     */
    private void connect(BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                if (connection_count < 1) {
                    connectingProgressDialog = ProgressDialog.show(MainActivity.this, "", "Connecting");
                    connection_count++;
                }
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                connectingProgressDialog.dismiss();
                String notice = bleDevice.getName() + " " + getString(R.string.connect_fail);
                Toast.makeText(MainActivity.this, notice, Toast.LENGTH_LONG).show();
                addTextInDMDisplay("[INFO] " + getString(R.string.connect_fail) + ": " + bleDevice.getName());
                deviceManager.addDevice(bleDevice, false);
                start_scan_btn.setEnabled(true);
                start_recording_btn.setEnabled(true);
                // reconnect(bleDevice);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                deviceManager.addDevice(bleDevice, true);
                addTextInDMDisplay("[INFO] Connect successfully to " + bleDevice.getName());

                // temporary binding
                // TODO: implement adapter
                if (bleDevice.getName().contains("CL831")) {
                    device_status_a.setText(deviceManager.getDeviceStatusInfo(bleDevice.getName()));
                } else if (bleDevice.getName().contains("CL880")) {
                    device_status_b.setText(deviceManager.getDeviceStatusInfo(bleDevice.getName()));
                    turnOnOffHeartModule(0);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    turnOnOffHeartModule(1);
                } else if (bleDevice.getName().contains("CL800")) {
                    device_status_c.setText(deviceManager.getDeviceStatusInfo(bleDevice.getName()));
                }

                try {
                    sleep(100);
                    acquireBattery(bleDevice);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (deviceManager.isAllConnected()) {
                    connectingProgressDialog.dismiss();
                    start_scan_btn.setEnabled(false);
                    start_recording_btn.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Connection is successful", Toast.LENGTH_SHORT).show();
                    startSensorBatteryCheck();
                }
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                connectingProgressDialog.dismiss();
                if (isActiveDisConnected) {
                    Toast.makeText(MainActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                    runOnUiThread(() -> addTextInDMDisplay("[INFO] Device " + device.getName() + " disconnect actively"));
                    deviceManager.addDevice(device, false);
                    notification.sendMsgOnActivedisconnect(getApplicationContext(), bleDevice.getName());
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.passive_disconnected), Toast.LENGTH_LONG).show();
                    runOnUiThread(() -> addTextInDMDisplay("[INFO] Device " + device.getName() + " disconnect passively"));
                    deviceManager.addDevice(device, false);
                    notification.sendMsgOnDisactivedisconnect(getApplicationContext(), bleDevice.getName());
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    passiveDisconnectAlert().show();
                }

                start_scan_btn.setEnabled(true);
                start_recording_btn.setEnabled(false);
                updateSensorStatusWhenDisconnect(device);

                if (!checkBluetoothStatus()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Caution")
                            .setMessage("Bluetooth is switched off, all device has been disconnected")
                            .setNegativeButton("continue",
                                    (dialog, which) -> finish());
                    return;
                }
            }
        });
    }

    /**
     * Method to start background service to check sensor every one minute
     */
    public void startSensorBatteryCheck() {
        if (!deviceManager.isAllDisconnected()) {
            Intent intent = new Intent(MainActivity.this, sensorCheck.class);
            startService(intent);
        }
    }

    /**
     * Method to stop background service to check sensor every one minute
     */
    public void stopSensorBatteryCheck() {
        Intent intent = new Intent(MainActivity.this, sensorCheck.class);
        stopService(intent);
    }

    /**
     * Method to check the connectivity of device
     */
    private void reconnect(BleDevice bleDevice) {
        connect(bleDevice);
    }

    /**
     * Method to send out notify to bluetooth device and request data from BLE devices
     *
     * @param filePath the filepath that file is written
     */
    private void BLENotify(String filePath) {
        ArrayList<BleDevice> deviceArrayList = deviceManager.sortDevice();
        for (int i=0; i<deviceArrayList.size(); i++) {
            if (deviceArrayList.get(i).getName().contains("CL880")) {
                bleSubscribeThread_hr_CL880 = new BLESubscribeThread(MainActivity.this, deviceArrayList.get(i), "HR", db, filePath, Constant.HEART_RATE_SERVICE_UUID, Constant.HEART_RATE_CHARACTERISTIC_UUID);
                bleSubscribeThread_hr_CL880.start();
                // thread should not overlap
                runOnUiThread(() -> {
                    try {
                        // sleep some time to avoid conflict in threads
                        sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                bleSubscribeThread_ppg_CL880 = new BLESubscribeThread(MainActivity.this, deviceArrayList.get(i), "PPG", db, filePath, Constant.PPG_SERVICE_UUID, Constant.PPG_CHARACTERISTIC_UUID);
                bleSubscribeThread_ppg_CL880.start();
                // thread should not overlap
                runOnUiThread(() -> {
                    try {
                        // sleep some time to avoid conflict in threads
                        sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } else if (deviceArrayList.get(i).getName().contains("CL831")) {
                bleSubscribeThread_hr_CL831 = new BLESubscribeThread(MainActivity.this, deviceArrayList.get(i), "HR", db, filePath, Constant.HEART_RATE_SERVICE_UUID, Constant.HEART_RATE_CHARACTERISTIC_UUID);
                bleSubscribeThread_hr_CL831.start();
                // thread should not overlap
                runOnUiThread(() -> {
                    try {
                        // sleep some time to avoid conflict in threads
                        sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                bleSubscribeThread_ppg_CL831 = new BLESubscribeThread(MainActivity.this, deviceArrayList.get(i), "PPG", db, filePath, Constant.PPG_SERVICE_UUID, Constant.PPG_CHARACTERISTIC_UUID);
                bleSubscribeThread_ppg_CL831.start();
                // thread should not overlap
                runOnUiThread(() -> {
                    try {
                        // sleep some time to avoid conflict in threads
                        sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } else if (deviceArrayList.get(i).getName().contains("CL800")) {
                bleSubscribeThread_hr_CL800 = new BLESubscribeThread(MainActivity.this, deviceArrayList.get(i), "HR", db, filePath, Constant.HEART_RATE_SERVICE_UUID, Constant.HEART_RATE_CHARACTERISTIC_UUID);
                bleSubscribeThread_hr_CL800.start();
                // thread should not overlap
                runOnUiThread(() -> {
                    try {
                        // sleep some time to avoid conflict in threads
                        sleep(7000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                bleSubscribeThread_acc_CL800 = new BLESubscribeThread(MainActivity.this, deviceArrayList.get(i), "ACC", db, filePath, Constant.ACC_SERVICE_UUID, Constant.ACC_CHARACTERISTIC_UUID);
                bleSubscribeThread_acc_CL800.start();
                // thread should not overlap
                runOnUiThread(() -> {
                    try {
                        // sleep some time to avoid conflict in threads
                        sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    public void enableBLENotify(String path) {
        BLENotify(path);
    }

    /**
     * Method to monitor the battery level of hub itself
     */
    private void monitorBatteryState() {
        batteryLevelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int rawlevel = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                int status = intent.getIntExtra("status", -1);
                int health = intent.getIntExtra("health", -1);
                int level = -1;
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                if (BatteryManager.BATTERY_HEALTH_OVERHEAT == health) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Caution")
                            .setMessage("The battery is overheating, please check the phone")
                            .setNegativeButton("continue",
                                    (dialog, which) -> finish());
                } else {
                    switch (status) {
                        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                            if (level <= 5)
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Caution")
                                        .setMessage("Battery low, hub will stop functioning soon.")
                                        .setNegativeButton("continue",
                                                (dialog, which) -> finish());
                            else if (level <= 20)
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Caution")
                                        .setMessage("Battery low, please recharge hub")
                                        .setNegativeButton("continue",
                                                (dialog, which) -> finish());
                            break;
                        default:
                            break;
                    }
                }
                String info = "Battery Level: " + level;
                batteryLevel.setText(info);
            }
        };
        batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }

    /**
     * Method to check permission before scanning for bluetooth and set up connection between them
     */
    private void checkPermission() {
        // other permission: fine location & write & read external storage
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        List<String> permissionDeniedList = new ArrayList<>();

        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }

        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    /**
     * Method to check permission on bluetooth before scanning the device.
     *
     * @return true if bluetooth permission is given by user, false otherwise
     */
    private boolean checkBluetoothPermission() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // if bluetooth is not open, user should open the bluetooth manually.
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Method to check whether the Bluetooth is on
     */
    private boolean checkBluetoothStatus() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Method to check internet permission before transmitting data to Edge side
     *
     * @return true if internet permission is given by user, false otherwise
     */
    private boolean checkInternetPermissionForTransmittingHealthData() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_wifi), Toast.LENGTH_LONG).show();
            return false;
        }
        return wifiManager.isWifiEnabled();
    }

    /**
     * Method to check internet permission before pairing with Edge
     * @return true if internet permission is given by user, false otherwise
     */
    private boolean checkInternetPermissionForSocket() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_wifi_for_socket), Toast.LENGTH_LONG).show();
            return false;
        }
        return wifiManager.isWifiEnabled();
    }

    /**
     * Method to ask for user's permission to open GPS and write to external storage
     *
     * @param permission permission
     */
    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    (dialog, which) -> finish())
                            .setPositiveButton(R.string.setting,
                                    (dialog, which) -> {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                    })
                            .setCancelable(false)
                            .show();
                }
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
                break;
        }
    }

    /**
     * Method to check whether user's gps is open (some android version requires user's gps to use bluetooth)
     *
     * @return a boolean value of whether the GPS is on or not.
     */
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    /**
     * Method to turn on and turn off the heart module (specifically for CL880, the wrist band)
     * to control the on and off the heart functionality on the wrist band
     *
     * @param heart 1 <- open the heart module, 0 <- close the heart module
     */
    public void turnOnOffHeartModule(long heart) {
        int[] command = new int[2];
        command[0] = 0x09;
        command[1] = (byte) heart;
        sendCommand((byte) 6, command);
    }

    /**
     * Method to compose the information and write via bluetooth through certain characteristic uuid
     * information format is designated by the hardware manufacturer
     * information format: [head],[length],[cmd],[values: optional], [checksum]
     *
     * @param cmd    third element in the information
     * @param values real command of turning on or off the heart module
     */
    private void sendCommand(final byte cmd, final int... values) {
        // sending information: [head],[length],[cmd],[checksum]
        final int unique = 4;
        final BleDevice bleDevice;
        final byte[] result;
        if (values != null) {
            final int len = values.length + unique;
            final byte[] header = HexUtil.compose(0xFF, len, cmd);
            final byte[] bytes = HexUtil.compose(values);
            result = HexUtil.append(header, bytes);
        } else {
            result = HexUtil.compose(0xFF, unique, cmd);
        }
        // checkSum method is used to verify the completeness of the information
        final byte check = checkSum(result);
        final byte[] command = HexUtil.append(result, check);

        bleDevice = deviceManager.getDeviceByItsName("CL880");
        if (bleDevice != null) {
            writeToCharacteristic(bleDevice, command);
        }
    }

    /**
     * Method to write information to designated bledevice
     * by using the FastBLE pre-encapsulated method
     *
     * @param bledevice the bledevice to write to
     * @param command   the command
     */
    private void writeToCharacteristic(BleDevice bledevice, byte[] command) {
        BleManager.getInstance().write(
                bledevice,
                Constant.HR_SWITCH_SERVICE_UUID,
                Constant.HR_SWITCH_CHARACTERISTIC_UUID,
                command,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        if (justWrite[justWrite.length - 2] == 0x00) {
                            isWriteSuccess.setWriteSuccess(false);
                        }
                        addTextInDMDisplay("[INFO] Data is written successfully.");
                        System.out.println("[INFO] Data is written successfully.");
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        addTextInDMDisplay("[INFO] Data is written not successfully.");
                        System.out.println("[INFO] Data is not written successfully.");
                        System.out.println("[EXCEPTION]: " + exception.getDescription());
                    }
                });
    }

    /**
     * Method to produce checksum byte.
     * The process of checksum is as follows:
     * 1) Add the bytes together and drop the carries.
     * 2) Xor 0x3A
     * This 2's compliment is our checksum byte, which becomes last byte of the series.
     *
     * @param msgs the messages about to send
     * @return the checkSum byte
     */
    private byte checkSum(byte[] msgs) {
        int sum = 0;
        for (byte msg : msgs) sum += msg;

        sum = -sum;
        sum = sum ^ 0x3A;
        return (byte) sum;
    }

    /**
     * Add content on developer mode display
     *
     * @param content the content to show on the display
     */
    public static void addTextInDMDisplay(String content) {
        DMdisplay.append(content);
        DMdisplay.append("\n");
        System.out.println(content + "\n");
    }

    /**
     * Strategy to deal with how developer mode is on or not
     * IF developer mode is on, display info
     * OTHERWISE shows nothing
     */
    private void DMSwitchStrategy() {
        final TextView DMdisplay = findViewById(R.id.DMdisplay);
        @SuppressLint("UseSwitchCompatOrMaterialCode") final Switch DMSwitch = findViewById(R.id.DMswitch);
        DMSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                DMdisplay.setVisibility(TextView.VISIBLE);
            } else {
                DMdisplay.setVisibility(TextView.INVISIBLE);
            }
        });
    }

    /**
     * Methods to show alert dialog containing the device that has been scanned, show the content on the display
     *
     * @param content        the content to show on the display
     * @param scanResultList a list of bledevices
     * @return an alert dialog
     */
    private AlertDialog scanAlert(String content, List<BleDevice> scanResultList) {
        AlertDialog scanAlert = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Caution")
                .setMessage(content)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    connection_count = 0;
                    for (int j = 0; j < scanResultList.size(); j++) {
                        connect(scanResultList.get(j));
                    }
                }).setNegativeButton("Cancel", (dialogInterface, i) -> {
                    start_scan_btn.setEnabled(true);
                    dialogInterface.dismiss();
                }).create();

        return scanAlert;
    }

    /**
     * Method to show alert dialog if no device was scanned.
     * Two reason might lead to no_scan_result:
     * 1. Bluetooth was blocked because of unknown reason
     * 2. Sensors are not switched on
     *
     * @return an alert dialog
     */
    private AlertDialog noScanResultAlert() {
        String content = "No available sensors are found\n" +
                "try: \n" +
                "1. switch on the bluetooth button again\n" +
                "2. check whether sensors are out of battery";
        AlertDialog noScanResultAlert = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Caution")
                .setMessage(content)
                .setCancelable(false)
                .setNegativeButton("Ok", ((dialog, which) -> {
                    start_scan_btn.setEnabled(true);
                    dialog.dismiss();
                })).create();
        return noScanResultAlert;
    }

    /**
     * Method to show alert dialog if device are passively disconnected
     *
     * @return an alert dialog
     */
    private AlertDialog passiveDisconnectAlert() {
        String alertContent = "Oops, some device lost connection with the hub \n Reasons might be following: \n     - too far away from Hub \n     - sensors are out of battery \n Please recheck the sensors and press \'reconnect\'";
        AlertDialog passiveDisconnectAlert = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Warning")
                .setMessage(alertContent)
                .setPositiveButton("Reconnect", (dialogInterface, i) -> {
                    ArrayList<BleDevice> disconnectedDeviceList = deviceManager.getAllDisconnectBleDevice();
                    for (int j = 0; j < disconnectedDeviceList.size(); j++) {
                        reconnect(disconnectedDeviceList.get(j));
                    }
                }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).create();
        return passiveDisconnectAlert;
    }

    /**
     * Method to transform hashmap to string (for future transmission to edge)
     *
     * @return
     */
    private String transHashmapToString() {
        String info = "";
        for (Map.Entry<BleDevice, Boolean> entry : deviceManager.getDeviceCache().entrySet()) {
            info += entry.getKey().getName() + ",";
            if (entry.getValue()) {
                info += "1,";
            } else {
                info += "0,";
            }
        }
        info = addMissingDevices(info);
        return info;
    }

    /**
     * add missing device (for future transmission to edge)
     *
     * @param info
     */
    private String addMissingDevices(String info) {
        for (String name : sensorNames) {
            if (!deviceManager.containDeviceName(name)) {
                info += name + "," + "0,";
            }
        }
        return info;
    }

    /**
     * Check data's validity before packaged to Edge from Hub.
     *
     * @param table_name SQLite table name
     * @return an arraylist of effective timestamp
     */
    private void dataValidation(String table_name, Cursor cursor) {
        ArrayList<String[]> validData = new ArrayList<String[]>();
        // store the current timestamp
        long timestamp = System.currentTimeMillis();
        // set the end time to current time
        long end_time = timestamp;
        // set the start time to input date
        // query the database
        String[] args = {this.start_time, Long.toString(end_time)};
        String type;
        if (table_name.contains("HR")) {
            type = "HR";
        } else if (table_name.contains("PPG")) {
            type = "PPG";
        } else if (table_name.contains("ACC")) {
            type = "ACC";
        } else {
            type = "";
        }
        String path = fileOperator.getPath(db, start_time);
        // scan the data and get valid time bucket according to different data type
        int value;

        cursor = db.query(table_name, new String[]{"timestamp", "content"}, "timestamp>=? AND timestamp<=?", args, null, null, null);
        if (cursor.getCount() != 0) {
            String[] content;
            String time_stamp;
            switch (type) {
                case "HR":
                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast()) {
                            time_stamp = cursor.getString(0);
                            content = cursor.getString(1).split(" ");
                            value = Integer.parseInt(content[1], 16);
                            // heart rate extreme threshold
                            if (50 <= value && value <= 200) {
                                fileOperator.storeInFile(path, table_name, time_stamp, content);
                                cursor.moveToNext();
                            } else {
                                content[1] = "NaN";
                                fileOperator.storeInFile(path, table_name, time_stamp, content);
                                cursor.moveToNext();
                            }
                        }
                    }
                    break;
                case "PPG":
                    if (cursor.moveToFirst()) {
                        content = cursor.getString(1).split(" ");
                        int lastID = Integer.parseInt(content[19], 16);
                        int packageID;
                        while (!cursor.isAfterLast()) {
                            time_stamp = cursor.getString(0);
                            content = cursor.getString(1).split(" ");
                            packageID = Integer.parseInt(content[19], 16);
                            value = Integer.parseInt(content[3] + content[2], 16);
                            if (cursor.isFirst() && value >= 5) {
                                lastID = packageID;
                                fileOperator.storeInFile(path, table_name, time_stamp, content);
                                cursor.moveToNext();
                            } else if (!(cursor.isFirst()) && (lastID + 1 == packageID || lastID - packageID == 255) && value >= 5) {
                                lastID = packageID;
                                fileOperator.storeInFile(path, table_name, time_stamp, content);
                                cursor.moveToNext();
                            } else {
                                lastID = packageID;
                                content[3] = "NaN";
                                content[2] = "NaN";
                                fileOperator.storeInFile(path, table_name, time_stamp, content);
                                cursor.moveToNext();
                            }

                        }
                    }
                    break;
                case "ACC":
                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast()) {
                            time_stamp = cursor.getString(0);
                            content = cursor.getString(1).split(" ");
                            fileOperator.storeInFile(path, table_name, time_stamp, content);
                            cursor.moveToNext();
                        }
                    }
                default:
                    System.out.println("[ERROR] type error!");
                    break;
            }
        } else System.out.println("[ALERT] Empty Time Bucket!");
    }

    /**
     * background class to upload the file to the file server.
     * deprecated, please refer to processTask
     */
    public class uploadTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            fileCount = 0;
            total_file_count = 0;
            record_count = 0;
            stored_path = "";
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                File file = new File("/storage/emulated/0/Hub_Data/2022_02_22_12.52/");
                File[] fs = file.listFiles();
                total_file_count = fs.length;
                publishProgress(fileCount);
                for (File f : fs) {
                    SFTPFileTransfer scp = new SFTPFileTransfer(f.getAbsolutePath(), "/home/grp_remote_repository/", start_time);
                    scp.upload();
                    fileCount++;
                    publishProgress(fileCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progresses) {
            super.onProgressUpdate(progresses);
            uploadingProgressBar.setProgress((int) ((float) progresses[0] / (float) total_file_count * 100));
            String info = "uploading..." + (int) ((float) progresses[0] / (float) total_file_count * 100) + "%";
            progressTextView.setText(info);
        }

        @Override
        protected void onPostExecute(String result) {
            progressTextView.setText("finished");
        }

        @Override
        protected void onCancelled() {
            progressTextView.setText("Cancelled");
            uploadingProgressBar.setProgress(0);
        }
    }

    /**
     * Background class for listening on a specific port
     * and transmit data
     */
    public class socketTask extends AsyncTask<String, Integer, Void> {

        private int port;
        private ServerSocket serverSocket;

        @Override
        protected void onPreExecute() {
            // port for socket communication is set on 7801
            port = 7801;
        }

        @Override
        protected Void doInBackground(String... strings) {
            runOnUiThread(() -> addTextInDMDisplay("[INFO] Server is listening on port: " + port));
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            while (true) {
                try {
                    System.out.println("[INFO] Server is listening on port: " + port);
                    socket = serverSocket.accept();
                    serverThread server = new serverThread(socket);
                    String returnINFO = server.recv();
                    assert returnINFO != null;
                    if ((int) server.inputHeader.getInstructionCmd() == 2) {
                        runOnUiThread(() -> addTextInDMDisplay(returnINFO));
                        sensorNames = returnINFO.substring(24).split(",");
                        String info = transHashmapToString();
                        info = info.substring(0, info.length()-1);
                        System.out.println(info);
                        server.send(info);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

    }

    /**
     * Background class to process and upload file to the file server.
     */
    public class processTask extends AsyncTask<String, Integer, Void> {
        // the progress dialog for postprocessing and upload the data to the server.
        private ProgressDialog process_progress_dialog;
        private ArrayList<String> tables = fileOperator.getTableName(db);
        private Cursor cursor = null;
        File[] fs;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tables = fileOperator.getTableName(db);
            process_progress_dialog = new ProgressDialog(MainActivity.this);
            process_progress_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            process_progress_dialog.setCancelable(true);
            process_progress_dialog.setMessage("Validating the data...");
            process_progress_dialog.setMax(tables.size()*2);
            process_progress_dialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            int i = 1;
            try{
                for (String table : tables) {
                    dataValidation(table,cursor);
                    publishProgress(i);
                    i++;
                }
                runOnUiThread(() -> process_progress_dialog.setMessage("Uploading Files"));
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if(cursor!=null) cursor.close();
            }

            try {
                String filePath = fileOperator.getPath(db, start_time);
                File file = new File(filePath);
                fs = file.listFiles();
                for (File f : fs) {
                    SFTPFileTransfer scp = new SFTPFileTransfer(f.getAbsolutePath(), "/home/grp_remote_repository/", start_time);
                    scp.upload();
                    publishProgress(i);
                    i++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            process_progress_dialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
//            start_time = "";
            if (start_time == null || start_time.equals("")) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "No data to upload", Toast.LENGTH_LONG).show());
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Uploading Successfully", Toast.LENGTH_LONG).show());
            }
            process_progress_dialog.dismiss();
        }
    }

    /**
     * Method to acquire battery of all connected sensors
     * Method is bound to "Acquire Sensor Battery" button
     */
    private void acquireBattery() {
        for (Map.Entry<BleDevice, Boolean> entry : DeviceManager.getInstance().getDeviceCache().entrySet()) {
            if (entry.getValue()) {
                readAndShow(entry.getKey(), Constant.BATTERY_SERVICE_UUID, Constant.BATTERY_CHARACTERISTIC_UUID);
            }
        }
    }

    /**
     * Method to acquire a specific sensor that is connected to the Hub
     * @param bleDevice
     */
    private void acquireBattery(BleDevice bleDevice) {
        readAndShow(bleDevice, Constant.BATTERY_SERVICE_UUID, Constant.BATTERY_CHARACTERISTIC_UUID);
    }

    /**
     * Method to read and show the warning
     * @param bleDevice the bleDevice that we are going to check the battery level
     * @param uuid_service uuid for battery service
     * @param uuid_characteristic_read uuid for reading battery service
     */
    private void readAndShow(BleDevice bleDevice, String uuid_service, String uuid_characteristic_read) {
        BleManager.getInstance().read(
                bleDevice,
                uuid_service,
                uuid_characteristic_read,
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        System.out.println("Battery Level of " + bleDevice.getName() + " is received.");
                        Integer battery_level = (int) (data[0] & 0xFF);
                        bindSensorBatteryStatus(bleDevice.getName(), battery_level);

                        System.out.println("Battery Level of " + bleDevice.getName() + "is: " + battery_level + ".");
                        if (battery_level <= 5)
                            Notification.getInstance(MainActivity.this).sendMsgOnLowBatteryOfSensors(MainActivity.this, bleDevice.getName());
                        else if (battery_level <= 20)
                            Notification.getInstance(MainActivity.this).sendMsgOnExtremeLowBatteryOfSensors(MainActivity.this, bleDevice.getName());
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        System.out.println("Battery Level of " + bleDevice.getName() + " is not received.");
                    }
                });
    }
}