/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.Server
 * @ClassName: serverThread
 * @Description: serverThread of the Hub, used to transmit data
 * @Author: GRP_Team14
 * @CreateDate: 2022/1/14
 * @Version: 1.0
 */

package com.example.hub.Server;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.hub.Constant.Constant;
import com.example.hub.Header.Header;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class serverThread {
    // socket: an endpoint for communication between two machines
    private Socket socket;
    // input stream: an input stream is an input stream of bytes.
    private InputStream is = null;
    // output stream: an output stream accepts output bytes and sends them to some sink.
    private OutputStream os = null;

    // variable to store the context about to be sent through socket
    private String SendingContext = "";
    // variable to use an arraylist to store data that cuts into different parts
    private ArrayList<String> chunks = new ArrayList<>();

    // variable to store the database
    private SQLiteDatabase db;
    // variable to store the HR tables
    private ArrayList<String> HRTables;

    // variable to store the time_interval of heart-rate data
    private int time_interval;

    public Header inputHeader;

    // constructor need independent socket
    public serverThread(Socket socket) {
        this.socket = socket;
    }

    /**
     * Method to receive information from edge
     * @return the info received from the edge
     */
    public String recv() {
        try {
            // acquire the input stream from the socket port
            is = socket.getInputStream();

            // interpret the input stream
            byte[] buffer = new byte[1024];
            int len;
            // if the input stream (in bytes) that put into the buffer is not null
            while ((len = is.read(buffer)) != -1) {
                byte[] inputHeaderStream = new byte[Constant.HEADER_LENGTH];
                System.arraycopy(buffer, 0, inputHeaderStream, 0, Constant.HEADER_LENGTH);
                inputHeader = new Header(inputHeaderStream);
                String text = new String(buffer, Constant.HEADER_LENGTH, len - Constant.HEADER_LENGTH);
                if ((int) inputHeader.getInstructionCmd() == 1) {
                    text = "Initial Binding: " + text;
                    return text;
                } else if ((int) inputHeader.getInstructionCmd() == 2) {
                    text = "[INFO] Connection List: " + text;
                    // shut down input before opening the output stream
                    return text;
                }
//                else if ((int) inputHeader.getInstructionCmd() == 2) {
//                    // shut down input before opening the output stream
//                    return "[INFO] Start recording";
//                } else {
//                    time_interval = retrieveTimeInterval(text);
//                    return "[INFO] Sending files.";
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to send info(feedback) to the edge
     * @param info the info about to send
     * @throws IOException
     */
    public void send(String info) throws IOException {
        os = socket.getOutputStream();
        try {
            if ((int) inputHeader.getInstructionCmd() == 1 && !info.isEmpty()) {
                Header RequestInitialBindingHeader = new Header();
                RequestInitialBindingHeader.setInstructionCmd(1);
                byte[] RequestInitialBinding = combineHeaderAndContents(RequestInitialBindingHeader.TransByteArrayForTransmission(), info.getBytes(StandardCharsets.UTF_8));
                os.write(RequestInitialBinding);
                os.flush();
            } else if ((int) inputHeader.getInstructionCmd() == 2 && !info.isEmpty()) {
                Header RequestConnectionHeader = new Header();
                RequestConnectionHeader.setInstructionCmd(2);
                byte[] RequestConnectionFeedback = combineHeaderAndContents(RequestConnectionHeader.TransByteArrayForTransmission(), info.getBytes(StandardCharsets.UTF_8));
                os.write(RequestConnectionFeedback);
                os.flush();
            }

//            else if ((int) inputHeader.getInstructionCmd() == 2 && !info.isEmpty()) {
//                Header RequestRecordingHeader = new Header();
//                RequestRecordingHeader.setInstructionCmd(2);
//                byte[] RequestRecordingFeedback = combineHeaderAndContents(RequestRecordingHeader.TransByteArrayForTransmission(), info.getBytes(StandardCharsets.UTF_8));
//                os.write(RequestRecordingFeedback);
//                os.flush();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        is.close();
        os.close();
        socket.close();
    }

    /**
     * Method to send files to the edge
     * @throws IOException
     */
    public void sendFile() throws IOException {
        try {
            os = socket.getOutputStream();
            // request all the tables from the database,
            // each contains one signal data of one particular HR sensor
            HRTables = getTablesFromDB();
            writeDataIntoOS();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // socket.shutdownInput();
        // socket.shutdownOutput();
        os.close();
    }

    /**
     * method to retrieve the time interval from the request
     */
    private int retrieveTimeInterval(String request) {
        if (inputHeader.getInstructionCmd() == 2) {
            return Integer.parseInt(request.substring(17));
        } else if (inputHeader.getInstructionCmd() == 0) {
            return Integer.parseInt(request.substring(7));
        }
        return 0;
    }

    /**
     * Method to write data into the output stream
     * @throws Exception exception
     */
    private void writeDataIntoOS() throws Exception {
        for (int table_index = 0; table_index < HRTables.size(); table_index++) {
            chunks.clear();
            SendingContext = "";
            SendingContext += HRTables.get(table_index) + "\n";
            SendingContext += getDataFromDB(HRTables.get(table_index), time_interval);
            Header fileHeader = new Header();
            chunks = ChunkSplit(SendingContext, 1024);
            int length = chunks.size();
            fileHeader.Reset();
            for (int j = 1; j <= length; j++) {
                fileHeader.Reset();
                fileHeader.setID(HRTables.size() - table_index);
                fileHeader.setPackageLength(chunks.get(j - 1).getBytes().length);
                fileHeader.setPackageTotalNumber(length);
                fileHeader.setPackageNumber(j);
                fileHeader.setInstructionCmd(2);
                byte[] fileChunks = combineHeaderAndContents(fileHeader.TransByteArrayForTransmission(), chunks.get(j - 1).getBytes());
                // write information to the output stream
                os.write(fileChunks);
                os.flush();
            }
        }
    }

    /**
     * method to split the sending content into fix-sized chunks
     */
    public ArrayList<String> ChunkSplit(String content, int split_size) throws Exception{
        ArrayList<String> chunks = new ArrayList<String>();
        if (content.getBytes().length > split_size) {
            byte[] buffer = new byte[split_size];
            int start = 0, end = buffer.length;
            long remaining = content.getBytes().length;

            ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
            while ((bis.read(buffer, start, end)) != -1) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bos.write(buffer, start, end);
                chunks.add(bos.toString("UTF-8"));
                remaining = remaining - end;
                if (remaining <= end) {
                    end = (int)remaining;
                }
            }
            return chunks;
        }
        chunks.add(content);
        return chunks;
    }

    /**
     * Method to combine a customized header with the byte array sending content
     * @param header a customized header
     * @param contents the byte array of sending contents
     * @return a reconstructed byte array with a header in front of each chunk
     */
    public byte[] combineHeaderAndContents(byte[] header, byte[] contents) {
        // initialize a byte array
        byte[] information = new byte[header.length + contents.length];

        // copy two byte array into one byte array
        System.arraycopy(header, 0, information, 0, header.length);
        System.arraycopy(contents, 0, information, header.length, contents.length);

        return information;
    }

    /**
     * Method to require data from database according to the table name
     * @param table_name the specific table name
     * @param time_interval the time interval of retrieved data
     * @return all the requested data in string form
     */
    public String getDataFromDB(String table_name, int time_interval) {
        // store the current timestamp
        long timestamp = System.currentTimeMillis();
        // set the start time to (time_interval) ago
        long startTime = timestamp - ((long) time_interval * 60 * 1000);
        // set the end time to current time
        long endTime = timestamp;
        // initialize the variable to store the data
        String content = "";

        // query the database
        String[] args = {Long.toString(startTime), Long.toString(endTime)};
        Cursor cursor = db.query(table_name, new String[]{"timestamp", "content"}, "timestamp>=? AND timestamp<=?", args, null, null, null);

        // set the date format
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        // reconstruct the data and put it into the sending content
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                long time = cursor.getLong(0);
                String value = cursor.getString(1);

                // transform date to string in designated format
                Date date = new Date(time);
                String str_time=formatter.format(date);
                // reconstruct the data to the following form:
                // "yyyy-MM-dd HH:mm:ss:SSS 'data'\n"
                content += str_time + "  " + value + "\n";
                cursor.moveToNext();
            }
        }
        return content;
    }

    /**
     * Method to require data from database
     * @return all the tables that are not empty
     */
    public ArrayList<String> getTablesFromDB() {
        // temp variable to store the table
        ArrayList<String> tempTable = new ArrayList<String>();
        // open the database at designated place
        db = SQLiteDatabase.openOrCreateDatabase("/storage/emulated/0/device.db", null);

        // retrieve data from database
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String sql = "select count(*) from " + name;
            Cursor cursor_table = db.rawQuery(sql, null);

            cursor_table.moveToFirst();
            long count = cursor_table.getLong(0);

            if (name.contains("CL") && count > 0) {
                tempTable.add(name);
                Log.d("database ", name);
            }
        }
        db.close();
        return tempTable;
    }
}
