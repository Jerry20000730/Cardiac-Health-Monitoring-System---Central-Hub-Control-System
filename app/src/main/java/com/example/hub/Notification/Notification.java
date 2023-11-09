package com.example.hub.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.clj.fastble.data.BleDevice;
import com.example.hub.R;

/**
 * This class is to deal with the notification on the hub.
 */
public class Notification {
    private static Notification notification;
    private final String CHANNEL_ID = "CHANNEL_ID";
    private final int notificationId = 1;

    /**
     * Singleton design pattern is used
     * Method to get the only one instance of the notification class
     * @param context The app context
     * @return the only instance of the notification class
     */
    public static Notification getInstance(Context context) {
        if (notification == null) {
            notification = new Notification();
        }
        return notification;
    }

    /**
     * This method is to create a channel to send notification to user.
     * @param context The app context
     */
    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CHANNELNAME";
            String description = "CHANNELD";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * This method is to send user notification if the user does not wear the device properly.
     * @param context The app context
     */
    public void sendMsgOnNotWearDevice(Context context) {
        createNotificationChannel(context);
        String message = "The device has not worn properly!";
        long[] vib= {100,400,100,400};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Warning!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(vib)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
    }


    /**
     * This method is to send user notification if the user does not connect to the device .
     * @param context The app context
     */
    public void sendMsgOnActivedisconnect(Context context, String deviceName) {
        createNotificationChannel(context);
        String message = deviceName + " Disconnect!";
        long[] vib= {100,400,100,400};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Active Disconnect Success!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(vib)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }


    /**
     * This method is to send user notification if the system does not capture data.
     * @param context The app context
     * @author GRP_team14
     */
    public void sendMsgOnNotCaptureData(Context context) {
        createNotificationChannel(context);
        String message = "The data is not captured";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Warning!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(2, builder.build());
    }

    /**
     * This method is to send user notification if the report is generated.
     * @param context The app context
     * @author GRP_team14
     */
    public void sendMsgOnReportGenerated(Context context) {
        createNotificationChannel(context);
        String message = "A report is generated";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Report is Ready")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(3, builder.build());
    }

    /**
     * This method is to send user notification if the heart rate is too high.
     * @param context The app context
     */
    public void sendMsgOnAbHR(Context context) {
        createNotificationChannel(context);
        String message = "Abnormal heart rate!";
        long[] vib= {100,400,100,400};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Alert!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(vib)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(4, builder.build());
    }

    /**
     * This method is to send user notification if the sensor battery is below 20.
     * @param context The app context
     * @param sensorName the name of the connected sensor
     * @author GRP_team14
     */
    public void sendMsgOnLowBatteryOfSensors(Context context, String sensorName) {
        createNotificationChannel(context);
        String message = "Please charge sensor: " + sensorName + ", low battery";
        long[] vib= {100,400,100,400};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Alert!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(vib)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(5, builder.build());
    }

    /**
     * This method is to send user notification if the sensor battery is below 5.
     * @param context The app context
     * @param sensorName the name of the connected sensor
     * @author GRP_team14
     */
    public void sendMsgOnExtremeLowBatteryOfSensors(Context context, String sensorName) {
        createNotificationChannel(context);
        String message = "Please charge sensor: " + sensorName + ", otherwise sensor will stop functioning soon.";
        long[] vib= {100,400,100,400};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Alert!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(vib)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(6, builder.build());
    }

    /**
     * This method is to send message if PPG is not normal
     * @param context the app context
     * @author GRP_team14
     */
    public void sendMsgOnPPGNotNormal(Context context) {
        createNotificationChannel(context);
        String message = "PPG is abnormal";
        long[] vib= {100,400,100,400};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Alert!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(vib)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(7, builder.build());
    }

    /**
     * This method is to send message if PPG is not continuous
     * @param context the app context
     * @param sensorName the name of the sensor in String
     * @author GRP_team14
     */
    public void sendMsgOnPPGNotContinuous(Context context, String sensorName) {
        createNotificationChannel(context);
        String message = "PPG Data of " + sensorName + " is not continuous";
        long[] vib= {100,400,100,400};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Alert!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(vib)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(8, builder.build());
    }

    /**
     * This method is to send message if disconnect accidentally
     * @param context the app context
     * @param deviceName the name of the sensor in String
     * @author GRP_team14
     */
    public void sendMsgOnDisactivedisconnect(Context context, String deviceName) {
        createNotificationChannel(context);
        String message = deviceName + " Disconnect accidentally!";
        long[] vib= {100,400,100,400};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Warning")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(vib)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(9, builder.build());
    }
}
