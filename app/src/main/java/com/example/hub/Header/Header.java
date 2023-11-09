/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.Header
 * @ClassName: HexUtil
 * @Description: customized header
 * @Author: GRP_Team14
 * @CreateDate: 2022/1/15
 * @Version: 1.0
 */

package com.example.hub.Header;

import com.example.hub.Constant.Constant;

import java.nio.ByteBuffer;

/**
 * This class is to deal with the customized header through the socket transmission.
 */
public class Header {
    private byte instructionCmd;
    private byte ID;
    private byte[] packageNumber;
    private byte[] packageTotalNumber;
    private byte[] packageLength;

    /**
     * Constructor of the header class, creating a header where:
     * instructionCmd = (byte)255
     * ID = (byte)255
     * package number = 0x00 0x00 0x00 0x00
     * packageTotalNumber = 0x00 0x00 0x00 0x00
     * packageLength = 0x00 0x00 0x00 0x00
     */
    public Header() {
        this.instructionCmd = (byte)0xFF;
        this.ID = (byte)0xFF;
        this.packageNumber = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        this.packageTotalNumber = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        this.packageLength = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte) 0x00};
    }

    /**
     * Constructor of the header class, extrating an existing header where:
     * instructionCmd = inputStream[0];
     * Id = inputStream[1];
     * packageNumber = inputStream[2:6];
     * packageTotalNumber = inputStream[6:10];
     * packageLength = inputStream[10:14]
     * @param inputStream an existing header
     */
    public Header(byte[] inputStream) {
        assert inputStream.length == 14;
        this.instructionCmd = inputStream[0];
        this.ID = inputStream[1];
        this.packageNumber = new byte[Constant.HEADER_PACKAGE_NUM_LENGTH];
        this.packageTotalNumber = new byte[Constant.HEADER_PACKAGE_NUM_TOTAL_LENGTH];
        this.packageLength = new byte[Constant.HEADER_PACKAGE_LENGTH_LENGTH];
        System.arraycopy(inputStream, 2, this.packageNumber, 0, Constant.HEADER_PACKAGE_NUM_LENGTH);
        System.arraycopy(inputStream, 2 + this.packageNumber.length, this.packageTotalNumber, 0, Constant.HEADER_PACKAGE_NUM_TOTAL_LENGTH);
        System.arraycopy(inputStream, 2 + this.packageNumber.length + this.packageTotalNumber.length, this.packageLength, 0, Constant.HEADER_PACKAGE_NUM_TOTAL_LENGTH);
    }

    /**
     * reset method to reset the header
     */
    public void Reset() {
        this.instructionCmd = (byte)0xFF;
        this.packageNumber = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        this.packageTotalNumber = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        this.packageLength = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
    }

    // Getter and Setter section

    /**
     * getter function to get the instruction cmd
     * @return current instruction cmd
     */
    public byte getInstructionCmd() {
        return instructionCmd;
    }

    /**
     * getter function to get the ID
     * @return current ID
     */
    public int getID() {
        return ID;
    }

    /**
     * getter function to get current package Number
     * @return current package number
     */
    public byte[] getPackageNumber() {
        return packageNumber;
    }

    /**
     * getter function to get the package total number (how many chunks for one whole file)
     * @return current package total number
     */
    public byte[] getPackageTotalNumber() {
        return packageTotalNumber;
    }

    /**
     * getter function to get the package length
     * @return current package length
     */
    public byte[] getPackageLength() {
        return packageLength;
    }

    /**
     * setter function to set cmd (1 for normal request, 2 for file transmission)
     * @param instructionCmd cmd
     */
    public void setInstructionCmd(int instructionCmd) {
        // instructionCmd = 0 <- File transmission
        // instructionCmd = 1 <- RequestForConnection or AnswerToRequestConnection
        // instructionCmd = 2 <- RequestForRecording or AnswerToRequestRecording
        String hex = Integer.toHexString(instructionCmd);
        this.instructionCmd = hexToByte(hex);
    }

    /**
     * setter function to set ID
     * @param ID ID
     */
    public void setID(int ID) {
        String hex = Integer.toHexString(ID);
        this.ID = hexToByte(hex);
    }

    /**
     * setter function to set package total number
     * @param packageTotalNumber package total number
     */
    public void setPackageTotalNumber(int packageTotalNumber) {
        byte[] FileTotalNumberbytes = ByteBuffer.allocate(4).putInt(packageTotalNumber).array();
        this.packageTotalNumber = FileTotalNumberbytes;
    }

    /**
     * setter function to set package number
     * @param packageNumber package number
     */
    public void setPackageNumber(int packageNumber) {
        byte[] fileNumberBytes = ByteBuffer.allocate(4).putInt(packageNumber).array();
        this.packageNumber = fileNumberBytes;
    }

    /**
     * setter function to set package length
     * @param packageLength package length
     */
    public void setPackageLength(int packageLength) {
        byte[] fileLengthNumberBytes = ByteBuffer.allocate(4).putInt(packageLength).array();
        this.packageLength = fileLengthNumberBytes;
    }

    /**
     * method to combine CMD, ID, package number, package total number, package length into one header
     * @return a byte array of complete header
     */
    public byte[] TransByteArrayForTransmission() {
        // file header: [CMD|packageID|packageNum|packageTotalNumber|packageLength]
        byte[] header_cmd = new byte[]{this.instructionCmd};
        byte[] header_id = new byte[]{this.ID};
        byte[] information = new byte[header_cmd.length + header_id.length + packageNumber.length + packageTotalNumber.length + packageLength.length];

        // combine all bytes together
        System.arraycopy(header_cmd, 0, information, 0, header_cmd.length);
        System.arraycopy(header_id, 0, information, header_cmd.length, header_id.length);
        System.arraycopy(packageNumber, 0, information, header_cmd.length + header_id.length, packageNumber.length);
        System.arraycopy(packageTotalNumber, 0, information, header_cmd.length + header_id.length + packageNumber.length, packageTotalNumber.length);
        System.arraycopy(packageLength, 0, information, header_cmd.length + header_id.length + packageNumber.length + packageTotalNumber.length, packageLength.length);

        return information;
    }

    /**
     * method to transform string of hexadecimal form to byte form
     * @param hexString the string of hexadecimal form
     * @return byte form
     */
    private byte hexToByte(String hexString) {
        if (hexString.length() == 1) {
            hexString = "0" + hexString;
        }
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    // convert hex string to hexidecimal number
    // e.g. ("73" to 7*16^1 and 3*16^0)
    private int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if(digit == -1) {
            throw new IllegalArgumentException("Invalid Hexadecimal Character: "+ hexChar);
        }
        return digit;
    }
}
