/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.FileOperator
 * @ClassName: fileOperator
 * @Description: fileOperator uses singleton design pattern as the util of file
 * @Author: GRP_Team14
 * @CreateDate: 2022/1/14
 * @Version: 1.0
 */

package com.example.hub.FileOperator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.hub.DeviceManager.DeviceManager;
import com.example.hub.MainActivity;
import com.jcraft.jsch.IO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class is to deal with all the operation concerning the file
 * (including read files, storing and writing files, finding files, etc.)
 */
public class fileOperator {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    /**
     * Method to verify whether the external storage permission is granted in current machine
     *
     * @param activity the activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //check whether the permission is allowed for writing
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // if no permission, the dialog of permission grant for write will pop.
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Highest method to write data, giving the url in string form,
     * name of the file in string form,
     * and the content of the file in string form
     *
     * @param url     url of file
     * @param name    the name of the file
     * @param content the content of the file
     */
    public static void writeData(String url, String name, String content) {
        String filePath = url;
        String fileName = name + ".txt";
        writeTxtToFile(content, filePath, fileName);
    }

    /**
     * Method to write data into txt file
     *
     * @param strContent the content in String form that are about to be written into the text file
     * @param filePath   the path of the file
     * @param fileName   the name of the file
     */
    private static void writeTxtToFile(String strContent, String filePath, String fileName) {
        //Generate directory before creating files
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        String strContentTemp = strContent + "\r\n";

        try {
            File file = new File(strFilePath);
            // create a new file if file does not exist
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            // random access the file, write into the file at the end.
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContentTemp.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    /**
     * Method to create the file by designated filepath and filename given as the input
     *
     * @param filePath the path of the file
     * @param fileName the name of the file
     * @return the file that is created according to the input file path and input file name
     */
    private static File makeFilePath(String filePath, String fileName) {
        File file = null;
        // write it in the root directory
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Method to create the file in the root directory
     *
     * @param filePath the file path
     */
    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            // if current file directory does not exist, we create one
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    /**
     * Method to set up the directory (for convenience of data retrieving, maybe deprecated in the future)
     *
     * @param filePath the path of the file
     * @param date     the date of creating the directory
     * @return the processed path of the directory to store the recorded file
     */
    public static String setUpOrganizedDataDirectory(String filePath, Date date) {
        try {
            File hubFolder = new File(filePath + "HubData");
            if (!hubFolder.exists()) {
                hubFolder.mkdir();
            }

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatter_for_sessionDirectory = new SimpleDateFormat("yyyy_MM_dd_HH.mm");

            String finalPath = filePath + "Hub_Data" + File.separator + formatter_for_sessionDirectory.format(date);
            File sessionFolder = new File(finalPath);
            if (!sessionFolder.exists()) {
                sessionFolder.mkdir();
            }
            return finalPath + File.separator;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String setUpOrganizedDataDirectory(String filePath, String date) {
        try {
            File hubFolder = new File(filePath + "HubData");
            if (!hubFolder.exists()) {
                hubFolder.mkdir();
            }

            String finalPath = filePath + "Hub_Data" + File.separator + date;
            File sessionFolder = new File(finalPath);
            if (!sessionFolder.exists()) {
                sessionFolder.mkdir();
            }
            return finalPath + File.separator;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to judge whether file exist and the content of the file is non-empty
     * @param filePath the path of the file
     * @return true if the file exist and non-empty, false otherwise
     * @throws IOException io exception
     */
    public static boolean isFileExistAndNonEmpty(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Method to delete file at given path
     * @param filePath the path of file
     * @return true if the file is deleted, false otherwise
     */
    public static boolean deleteFile(String filePath){
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println(filePath + " does not exist");
            return false;
        } else {
            if (file.isFile()) {
                return deleteSingleFile(filePath);
            }
            return false;
        }
    }

    private static boolean deleteSingleFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            file.delete();
            System.out.println("Delete single file: " + filePath + " successfully！");
            return true;
        } else {
            System.out.println("Delete single file" + filePath + " not successfully！");
            return false;
        }
    }

    /**
     * Method to read from file
     *
     * @param filePath the path of the file
     * @return the content of the file
     * @throws IOException IOException
     */
    public static String readFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        StringBuffer buffer = new StringBuffer();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        while (raf.getFilePointer() < raf.length()) {
            buffer.append(raf.readLine() + "\n");
        }
        String contents = buffer.toString();
        return contents;
    }

    public static ArrayList<String> getTableName(SQLiteDatabase db) {
        ArrayList<String> tables = new ArrayList<>();
        String[] valid_tables;

        valid_tables = DeviceManager.getInstance().getDeviceNameList();
        for(int i = 0; i<valid_tables.length;i++){
            if(valid_tables[i].contains("831")){
                tables.add(valid_tables[i].replace("-","_") + "_HR");
                tables.add(valid_tables[i].replace("-","_") + "_PPG");
            }else if(valid_tables[i].contains("880")){
                tables.add(valid_tables[i].replace("-","_") + "_HR");
                tables.add(valid_tables[i].replace("-","_") + "_PPG");
            }else if(valid_tables[i].contains("800")){
                tables.add(valid_tables[i].replace("-","_") + "_HR");
                tables.add(valid_tables[i].replace("-","_") + "_ACC");
            }
        }

//        Cursor cursor = null;
//        try {
//            cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
//            while (cursor.moveToNext()) {
//                tables.add(cursor.getString(0));
//            }
//            for (String table : tables) {
//                for (String model : DeviceManager.getInstance().getConnectedDeviceNameList()) {
//                    if (table.contains(model.replace('-', '_'))) {
//                        valid_tables.add(table);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (cursor != null) cursor.close();
//        }
        return tables;
    }


    public static String getPath(SQLiteDatabase db, String start_time){
        long time = Long.parseLong(start_time);
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH.mm");
        String path = fileOperator.setUpOrganizedDataDirectory("/storage/emulated/0/", formatter.format(date));
        return path;
    }

    /**
     * Method to store the data into the file
     */
    public static void storeInFile(String path, String table_name, String time_stamp, String[] content) {
        long time = Long.parseLong(time_stamp);
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss");
        StringBuffer str = new StringBuffer();
        for (String s : content) {
            str.append(s + " ");
        }
        str.deleteCharAt(str.length() - 1);
        String string = str.toString();
        fileOperator.writeData(path, table_name, formatter.format(date) + " " + string);
    }
}
