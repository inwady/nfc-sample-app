package ru.iojs.troikagen;

import java.math.BigInteger;

public class Utils
{
    /**
     * Get hex string from byte array
     * @param buf Byte buffer
     * @return Hex string
     */
    public static String getHexString(byte[] buf)
    {
        StringBuilder sb = new StringBuilder();

        for (byte b : buf)
            sb.append(String.format("%02X", b));

        return sb.toString().trim();
    }

    /**
     * Get byte array from binary string
     * @param s Binary string
     * @return Byte array
     */
    public static byte[] binaryStringToByteArray(String s)
    {
        byte[] ret = new byte[(s.length()+8-1) / 8];

        BigInteger bigint = new BigInteger(s, 2);
        byte[] bigintbytes = bigint.toByteArray();

        if (bigintbytes.length > ret.length) {
            //get rid of preceding 0
            for (int i = 0; i < ret.length; i++) {
                ret[i] = bigintbytes[i+1];
            }
        }
        else {
            ret = bigintbytes;
        }
        return ret;
    }

    public static byte[] getByteArray(String hex) {
        int length = hex.length() / 2;
        byte[] raw = new byte[length];
        for (int i = 0; i < length; i++) {
            int high = Character.digit(hex.charAt(i * 2), 16);
            int low = Character.digit(hex.charAt(i * 2 + 1), 16);
            int value = (high << 4) | low;
            if (value > 127)
                value -= 256;
            raw[i] = (byte) value;
        }
        return raw;
    }

    /**
     * Get binary string from byte array
     * @param input Byte array
     * @return Binary string
     */
    public static String getBinaryString(byte[] input)
    {
        StringBuilder sb = new StringBuilder();

        for (byte c : input)
        {
            for (int n = 128; n > 0; n >>= 1)
            {
                String res = ((c & n) == 0) ? "0" : "1";
                sb.append(res);
            }
        }

        return sb.toString();
    }
}
