/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.BLESubscribeThread
 * @ClassName: BLESubscribeThread
 * @Description: BLESubscribe thread for notifying bluetooth, three device with six characteristic should be obtained at the same time.
 * @Author: GRP_Team14
 * @CreateDate: 2022/1/14
 * @LastModify: 2022/3/25
 * @Version: 1.0
 */

package com.example.hub.BLESubscribeThread;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.example.hub.MainActivity;
import com.example.hub.R;
import com.example.hub.FileOperator.fileOperator;
import com.example.hub.Notification.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class extends for thread
 * for subscribing different service of the bluetooth
 * to receive data
 * by different service uuid and characteristic uuid
 */
public class BLESubscribeThread extends Thread {
    private MainActivity mainActivity;
    private BleDevice bleDevice;
    private String type;
    private SQLiteDatabase db;
    private String storagePath;
    private String uuid_service;
    private String uuid_characteristic_notify;
    private boolean isNotifySuccess;
    private static final String field = "(timestamp long, content TEXT)";
    private int lastID;
    private boolean firstTurn = true;

    /**
     * Constructor of an BLE subscribe thread
     * This is required if we want to record the data from the BLE device
     * @param mainActivity main thread
     * @param bleDevice the ble device we want to retrieve the data
     * @param type the type of data (e.g. "HR", "PPG", "ACC")
     * @param db the local database to store the data
     * @param storagePath the storage path of file storage
     * @param uuid_service uuid_service for BLE device
     * @param uuid_characteristic_notify uuid_characteristic_notify for BLE device
     */
    public BLESubscribeThread(MainActivity mainActivity, BleDevice bleDevice, String type, SQLiteDatabase db, String storagePath, String uuid_service, String uuid_characteristic_notify) {
        this.mainActivity = mainActivity;
        this.bleDevice = bleDevice;
        this.type = type;
        this.db = db;
        this.storagePath = storagePath;
        this.uuid_service = uuid_service;
        this.uuid_characteristic_notify = uuid_characteristic_notify;
        isNotifySuccess = false;
    }

    /**
     * The thread that will run to notify the BLE device
     */
    @Override
    public void run() {
        BleManager.getInstance().notify(
                bleDevice,
                uuid_service,
                uuid_characteristic_notify,
                new BleNotifyCallback() {
                    // initialize a table name
                    String table_name = "";
                    @Override
                    public void onNotifySuccess() {
                        db.execSQL("CREATE TABLE IF NOT EXISTS " + create_table_name() + field);
                        table_name = create_table_name();
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        String notice = bleDevice.getName() + " " + type + " " + mainActivity.getString(R.string.notify_fail);
                        mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, notice, Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // long time = System.currentTimeMillis();
                        // Date date = new Date(time);

                        // check the validation of heart rate and ppg signal
                        if (type.equals("HR")) {
                            data_check_hr(data);
                        }else if(type.equals("PPG")){
                            data_check_ppg(data);
                        }
                        storeInDatabase(data, table_name);
                        // store_in_file(data, date);
                    }
                });
    }


    /**
     * Method to create the table name according to the type
     * @return the name of the table
     */
    private String create_table_name() {
        if (type.equals("HR")) {
            return bleDevice.getName().replace('-','_') + "_HR";
        } else if (type.equals("PPG")) {
            return bleDevice.getName().replace('-','_') + "_PPG";
        } else if (type.equals("ACC")) {
            return bleDevice.getName().replace('-', '_') + "_ACC";
        }
        return null;
    }

    /**
     * Method to create the file name according to the type
     * @param date the date when creating the file
     * @return the name of the file
     */
    private String create_file_name(String date) {
        if (type.equals("HR")) {
            return bleDevice.getName() + date + "-HR";
        } else if (type.equals("PPG")) {
            return bleDevice.getName() + date + "-PPG";
        } else if (type.equals("ACC")) {
            return bleDevice.getName() + date + "-ACC";
        }
        return null;
    }

    /**
     * Method to check the data
     * By doing this, software can detect whether the data subscribed is abnormal
     * Or user did not wear it properly (The data from the device is zero)
     *
     * @param data
     */
    private void data_check_hr(byte[] data) {
        Integer cs = (int) (data[1] & 0xFF);
        if (cs == 0) {
            Notification.getInstance(mainActivity).sendMsgOnNotWearDevice(mainActivity.getApplicationContext());
        } else if (cs < 50 || cs > 200) {
            Notification.getInstance(mainActivity).sendMsgOnAbHR(mainActivity.getApplicationContext());
        }
    }

    /**
     * Method to judge whether the string contains illegal character
     * @param toExamine string to be examined
     * @return true the string contains illegal character, false otherwise
     */
    public boolean containsIllegals(String toExamine) {
        Pattern pattern = Pattern.compile("[-~#@*+%{}<>\\[\\]|\"\\_^]");
        Matcher matcher = pattern.matcher(toExamine);
        return matcher.find();
    }

    /**
     * Method to check that ppg data is continuous
     * @param data data received through Bluetooth notify
     */
    private void data_check_ppg(byte[] data) {
        Integer packageID = (int) (data[19] & 0xFF);
        String str = String.valueOf(data[4]) + (String.valueOf(data[3]));
        if(!containsIllegals(str)){
            Integer ppg = Integer.parseInt(str);
            if(firstTurn){
                lastID = packageID;
                firstTurn = false;
                if (ppg <= 5) {
                    Notification.getInstance(mainActivity).sendMsgOnPPGNotNormal(mainActivity.getApplicationContext());
                }
            }else{
                if (ppg <= 5) {
                    Notification.getInstance(mainActivity).sendMsgOnPPGNotNormal(mainActivity.getApplicationContext());
                }else if(!(this.lastID + 1 == packageID || this.lastID - packageID == 255)){
                    Notification.getInstance(mainActivity).sendMsgOnPPGNotContinuous(mainActivity.getApplicationContext(), bleDevice.getName());
                }
            }
        }else{
            Notification.getInstance(mainActivity).sendMsgOnPPGNotNormal(mainActivity.getApplicationContext());
        }
        this.lastID = packageID;
    }

    /**
     * Method to store the data into the database
     * @param data
     * @param table_name
     */
    private void storeInDatabase(byte[] data, String table_name) {
        // only used when storing files (deprecated)
        // String filename = "";

        String value = HexUtil.formatHexString(data, true);
        long time = System.currentTimeMillis();

        // assemble value to put into database
        ContentValues values = new ContentValues();
        values.put("timestamp", time);
        values.put("content", value);
        db.insert(table_name,null,values);

        // two data formatter (deprecated)
        // SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        // SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");

        // storage path if stored in files
        // String url = "/storage/emulated/0/";
        // Date date = new Date(time);
        // filename = bleDevice.getName() + formatter2.format(date) + "-HR";
        // String time_mil = formatter.format(date).toString();
        // FileOperator.writeData(url, filename, formatter.format(date)  + "  " + value);
    }

    /**
     * Method to store the data into the file
     * @param data data to be stored into the file
     * @param date the time of each notify time, to be written as the content in the file
     */
    private void store_in_file(byte[] data, Date date) {
        String value = HexUtil.formatHexString(data, true);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String url = storagePath;
        String filename = create_file_name(storagePath.substring(45, storagePath.length()-1));
        fileOperator.writeData(url, filename, formatter.format(date)  + "  " + value);
    }

    /**
     * Getter function to judge whether the notification is successful
     * @return the boolean value, true for the notify is success, false otherwise
     */
    public boolean isNotifySuccess() {
        return isNotifySuccess;
    }

//    public void setIsPause(boolean isPause) {
//        this.isPause = isPause;
//    }
}
