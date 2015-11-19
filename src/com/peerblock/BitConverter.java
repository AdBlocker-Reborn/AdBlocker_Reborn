package com.peerblock;

public class BitConverter
{
	public static byte[] getBytes(boolean x)
    {
        return new byte[]{
            (byte) (x ? 1:0)
            };
    }

    public static byte[] getBytes(char c)
    {
        return new byte[]  { 
            (byte)(c & 0xff), 
            (byte)(c >> 8 & 0xff) };
    }

    public static byte[] getBytes(double x)
    {
        return getBytes(
                Double.doubleToRawLongBits(x));
    }

    public static byte[] getBytes(short x)
    {
        return new byte[] { 
            (byte)(x >>> 8),
            (byte)x
            };
    }

    public static byte[] getBytes(int x)
    {
        return new byte[] {
            (byte)(x ),
            (byte)(x >>> 8),
            (byte)(x >>> 16),
            (byte)(x >>> 24)
            };
    }

    public static byte[] getBytes(long x)
    {
        return new byte[] {
            (byte)(x >>> 56), 
            (byte)(x >>> 48),
            (byte)(x >>> 40), 
            (byte)(x >>> 32), 
            (byte)(x >>> 24),
            (byte)(x >>> 16),
            (byte)(x >>> 8),
            (byte)x
            };
    }

    public static byte[] getBytes(float x)
    {
        return getBytes(Float.floatToRawIntBits(x));
    }

    public static byte[] getBytes(String x) 
    {
        return x.getBytes();
    }

    public static long doubleToInt64Bits(double x)
    {
        return Double.doubleToRawLongBits(x);
    }

    public static double int64BitsToDouble(long x)
    {
        return (double)x;
    }

    public boolean toBoolean(byte[] bytes, int index) throws Exception
    {
        return bytes[index] != 0;
    }

    public char toChar(byte[] bytes, int index) throws Exception
    {
        return (char)((0xff & bytes[index]) << 8 | (0xff & bytes[index + 1]));
    }

    public double toDouble(byte[] bytes, int index) throws Exception
    {
        return Double.longBitsToDouble(toInt64(bytes, index));
    }

    public static short toInt16(byte[] bytes, int index) throws Exception
    {
        return (short)((0xff & bytes[index]) | (0xff & bytes[index + 1]) << 8);
    }

    public static int toInt32(byte[] bytes, int index) throws Exception
    {
        return (int)((int)(0xff & bytes[index])  |
                     (int)(0xff & bytes[index + 1]) << 8  |
                     (int)(0xff & bytes[index + 2]) << 16  |
                     (int)(0xff & bytes[index + 3]) << 24);
    }

    public static long toInt64(byte[] bytes, int index) throws Exception
    {
            return (long)(
            (long)(0xff & bytes[index]) << 56  |
            (long)(0xff & bytes[index + 1]) << 48  |
            (long)(0xff & bytes[index + 2]) << 40  |
            (long)(0xff & bytes[index + 3]) << 32  |
            (long)(0xff & bytes[index + 4]) << 24  |
            (long)(0xff & bytes[index + 5]) << 16  |
            (long)(0xff & bytes[index + 6]) << 8   |
            (long)(0xff & bytes[index + 7])
            );
    }

    public static float toSingle(byte[] bytes, int index) throws Exception
    {
        return Float.intBitsToFloat(
        toInt32(bytes, index));
    }

    public static String toString(byte[] bytes) throws Exception
    {
        String ret = "";
        for(int i = 0; i < bytes.length; i++)
            ret += bytes[i] + "-";
        return ret;
    }
}
