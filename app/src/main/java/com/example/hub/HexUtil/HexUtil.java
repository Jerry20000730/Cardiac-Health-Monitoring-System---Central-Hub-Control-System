/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.HexUtil
 * @ClassName: HexUtil
 * @Description: hexadecimal util
 * @Author: GRP_Team14
 * @CreateDate: 2022/1/14
 * @Version: 1.0
 */

package com.example.hub.HexUtil;

/**
 * This class is used for dealing with byte.
 */
public class HexUtil {

    /**
     * combine the int array into a byte array
     * @param bytes the bytes array
     * @return the bytes array
     */
    public static byte[] compose(final int... bytes) {
        final byte[] dest = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            dest[i] = (byte) (bytes[i] & 0xFF);
        }
        return dest;
    }

    /**
     * Method Overloading
     * combine a series of byte into a byte array
     * @param bytes the bytes array
     * @return the bytes array
     */
    public static byte[] compose(final byte... bytes) {
        final byte[] dest = new byte[bytes.length];
        System.arraycopy(bytes, 0, dest, 0, bytes.length);
        return dest;
    }

    /**
     * Method to compose two byte (or byte array) into one byte array
     * @param dest the pre-existing byte array
     * @param source the byte that are going to be appended
     * @return a byte array composed of source and dest
     */
    public static byte[] append(final byte[] dest, final byte source) {
        final byte[] temp = new byte[]{source};
        return append(dest, temp);
    }

    /**
     * Method to compose two byte (or byte array) into one byte array
     *
     * @param source the pre-existing byte
     * @param dest   the byte array that are going to be appended
     * @return a byte array composed of source and dest
     */
    public static byte[] append(final byte source, final byte[] dest) {
        final byte[] temp = new byte[]{source};
        return append(temp, dest);
    }

    /**
     * Method to compose two byte (or byte array) into one byte array
     *
     * @param source the pre-existing byte array
     * @param dest   the byte array that are going to be appended
     * @return a byte array composed of source and dest
     */
    public static byte[] append(final byte[] source, final byte[] dest) {
        final byte[] result = new byte[source.length + dest.length];
        System.arraycopy(source, 0, result, 0, source.length);
        final int offset = result.length - dest.length;
        System.arraycopy(dest, 0, result, offset, dest.length);
        return result;
    }

    /**
     * Method to compose several byte array into one byte array
     *
     * @param bytes the list of byte arrays
     * @return one byte array composed of several byte arrays
     */
    public static byte[] append(final byte[]... bytes) {
        byte[] dest = new byte[0];
        for (byte[] outs : bytes) {
            dest = append(dest, outs);
        }
        return dest;
    }
}
