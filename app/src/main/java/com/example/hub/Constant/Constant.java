/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.Constant
 * @ClassName: SFTPConstants
 * @Description: the constant needed for Hub
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/10
 * @Version: 1.0
 */

package com.example.hub.Constant;

/**
 * All the constant that used in this project will be stored here.
 */
public class Constant {
    // public static final UUID BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");

    // heart rate subscribe section
    public static final String HEART_RATE_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb";
    public static final String HEART_RATE_CHARACTERISTIC_UUID = "00002a37-0000-1000-8000-00805f9b34fb";

    // ppg subscribe section
    public static final String PPG_SERVICE_UUID = "aae28f00-71b5-42a1-8c3c-f9cf6ac969d0";
    public static final String PPG_CHARACTERISTIC_UUID = "aae21542-71b5-42a1-8c3c-f9cf6ac969d0";

    // aac subscribe section
    public static final String ACC_SERVICE_UUID = "aae28f00-71b5-42a1-8c3c-f9cf6ac969d0";
    public static final String ACC_CHARACTERISTIC_UUID = "aae28f01-71b5-42a1-8c3c-f9cf6ac969d0";

    // CL880 heart rate switch section
    public static final String HR_SWITCH_SERVICE_UUID = "aae28f00-71b5-42a1-8c3c-f9cf6ac969d0";
    public static final String HR_SWITCH_CHARACTERISTIC_UUID = "aae28f02-71b5-42a1-8c3c-f9cf6ac969d0";

    // three sensors battery service section
    public static final String BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String BATTERY_CHARACTERISTIC_UUID = "00002a19-0000-1000-8000-00805f9b34fb";

    // header-related section
    public static final int HEADER_LENGTH = 14;
    public static final int RECONNECT_COUNT_MAX = 10;
    public static final int HEADER_PACKAGE_NUM_TOTAL_LENGTH = 4;
    public static final int HEADER_PACKAGE_NUM_LENGTH = 4;
    public static final int HEADER_PACKAGE_LENGTH_LENGTH = 4;
}
