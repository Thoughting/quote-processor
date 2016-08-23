package com.baidu.stock.process.util;

import java.nio.ByteBuffer;

/**
 * 二进制操作类
 * @author v_longxi
 *
 */
public class ReadUtil
{

	/**
	 * 把short 转化 byte[]
	 * @param n
	 * @return
	 */
	public static byte[] shortToByte(short n)
	{
		byte[] shortByte = new byte[2];

		shortByte[0] = (byte) n;
		shortByte[1] = (byte) (n >> 8);
		return shortByte;
	}

	/**
	 * 把int 转化 byte[]
	 * @param n
	 * @return
	 */
	public static byte[] intToByte(int n)
	{
		byte[] intByte = new byte[4];

		intByte[0] = (byte) n;

		intByte[1] = (byte) (n >> 8);

		intByte[2] = (byte) (n >> 16);

		intByte[3] = (byte) (n >> 24);

		return intByte;
	}

	/**
	 * 把float 转化 byte[]
	 * @param n
	 * @return
	 */
	public static byte[] floatToByte(float n)
	{
		long l = Float.floatToIntBits(n);

		byte[] floatByte = new byte[4];

		for (int i = 0; i < 4; i++)
		{
			floatByte[i] = (byte) (l & 0xff);

			l = l >>> 8;

		}

		return floatByte;
	}

	
	
	/**
	 * 把double 转化 byte[]
	 * @param db
	 * @return
	 */
	public static byte[] doubleToBytes(double db)
	{
		long l = Double.doubleToRawLongBits(db);

		byte[] b = new byte[8];

		for (int i = 0; i < 8; i++)
		{
			b[i] = (byte) (l & 0xff);
			l = l >>> 8;
		}
		return b;
	}

	/**
	 * 把byte[] 转化 double
	 * @param db
	 * @return
	 */
	public static double byteToDouble(byte[] b) throws NumberFormatException
	{
		if (b == null || b.length < 8)
		{
			throw new NumberFormatException();
		}
		
		long lval = 0;
		for (int i = 0; i < 8; i++)
		{
			lval = lval << 8;
			lval += (b[(7 - i)]);
		}
		return Double.longBitsToDouble(lval);
	}

	/**
	 * 从byte[] 数组中 在指定位置读取short
	 * @param src
	 * @param offset
	 * @return
	 */
	public static int readShort(byte[] src, int offset)
	{
		int b1 = src[offset] & 0x00FF;
		int b2 = src[offset + 1] & 0x00FF;
		int ret = (b1 | (b2 << 8));
		return ret;
	}

	/**
	 * 从byte[] 数组中 在指定位置读取int
	 * @param src
	 * @param offset
	 * @return
	 */
	public static int readInt(byte[] src, int offset)
	{

		int b1 = src[offset] & 0x000000FF;
		int b2 = src[offset + 1] & 0x000000FF;
		int b3 = src[offset + 2] & 0x000000FF;
		int b4 = src[offset + 3] & 0x000000FF;
		int ret = (b1 | (b2 << 8) | (b3 << 16) | (b4 << 24));
		return ret;
	}

	/**
	 * 从byte[] 数组中 在指定位置读取float
	 * @param src
	 * @param offset
	 * @return
	 */
	public static float readFloat(byte[] src, int offset)
	{

		int b1 = (src[offset + 3] & 0x00000080) >> 7;
		int b2 = ((src[offset + 3] & 0x0000007F) << 1) | ((src[offset + 2] & 0x00000080) >> 7);
		int b3 = (((((src[offset + 2] & 0x0000007F)) | 0x00000080) << 16)
				| ((src[offset + 1] & 0x000000FF) << 8) | ((src[offset] & 0x000000FF)));
		// int b4 = src[offset + 3] & 0x000000FF;
		int b4 = 23 - b2 + 127;
		float ret = (float) b3;
		if (b4 > 0)
		{
			for (int i = 0; i < b4; i++)
			{
				ret /= 2;
			}
		}
		else
		{
			for (int i = 0; i > b4; i--)
			{
				ret *= 2;
			}
		}
		
		if (b1 != 0)
		{
			ret = -ret;
		}

		if (ret < 0.00001)
		{
			ret = 0.00f;
		}
		
		return ret;
	}

	/**
	 * 把目标byte数组 拷贝到源byte[] 指定的位置后
	 * @param src
	 * @param offset
	 * @return
	 */
	public static void copyInBytes(byte[] sourceBytes, byte[] copyBytes, int off)
	{
		int copyLength = copyBytes.length;

		for (int i = 0; i < copyLength; i++)
		{
			sourceBytes[i + off] = copyBytes[i];
		}

	}
	/**
	 * 把目标byte拷贝到源byte[] 指定的位置后
	 * @param src
	 * @param offset
	 * @return
	 */
	public static void copyInBytes(byte[] sourceBytes, byte copyByte, int off)
	{
		sourceBytes[off] = copyByte;
	}

	/**
	 * 把short 放入byte[]
	 * @param src
	 * @param offset
	 * @return
	 */
	public static void putShort(byte b[], short s, int index)
	{
		b[index] = (byte) (s >> 8);
		b[index + 1] = (byte) (s >> 0);
	}

	/**
	 * 把short 放入byte[]。高低字节反过来适应C++
	 * @param src
	 * @param offset
	 * @return
	 */
	public static void putReverseBytesShort(byte b[], short s, int index)
	{
		b[index] = (byte) (s >> 0);
		b[index + 1] = (byte) (s >> 8);
	}

	/**
	 * 从byte[] 数组中 读取一个short
	 * @param b
	 * @param index 索引位置
	 * @return
	 */
	public static short getShort(byte[] b, int index)
	{
		return (short) (((b[index] << 8) | b[index + 1] & 0xff));
	}

	/**
	 * 从byte[] 数组中 读取一个short 高低字节反过来适应C++
	 * @param b
	 * @param index 索引位置
	 * @return
	 */
	public static short getReverseBytesShort(byte[] b, int index)
	{
		return (short) (((b[index + 1] << 8) | b[index] & 0xff));
	}

	/**
	 * 把int 放入byte[]
	 * @param src
	 * @param offset
	 * @return
	 */
	public static void putInt(byte[] bb, int x, int index)
	{
		bb[index + 0] = (byte) (x >> 24);
		bb[index + 1] = (byte) (x >> 16);
		bb[index + 2] = (byte) (x >> 8);
		bb[index + 3] = (byte) (x >> 0);
	}

	/**
	 * 把int 放入byte[]。高低字节反过来适应C++
	 * @param src
	 * @param offset
	 * @return
	 */
	public static void putReverseBytesInt(byte[] bb, int x, int index)
	{
		bb[index + 3] = (byte) (x >> 24);
		bb[index + 2] = (byte) (x >> 16);
		bb[index + 1] = (byte) (x >> 8);
		bb[index + 0] = (byte) (x >> 0);
	}

	/**
	 * 从byte[] 数组中 读取一个int
	 * @param b
	 * @param index 索引位置
	 * @return
	 */
	
	public static int getInt(byte[] bb, int index)
	{
		return (int) ((((bb[index + 0] & 0xff) << 24) | ((bb[index + 1] & 0xff) << 16)
				| ((bb[index + 2] & 0xff) << 8) | ((bb[index + 3] & 0xff) << 0)));
	}

	/**
	 * 从byte[] 数组中 读取一个short 高低字节反过来适应C++
	 * @param b
	 * @param index 索引位置
	 * @return
	 */
	public static int getReverseBytesInt(byte[] bb, int index)
	{
		return (int) ((((bb[index + 3] & 0xff) << 24) | ((bb[index + 2] & 0xff) << 16)
				| ((bb[index + 1] & 0xff) << 8) | ((bb[index + 0] & 0xff) << 0)));
	}

	/**
	 * 把int 放入byte[]。
	 * @param src
	 * @param offset
	 * @return
	 */
	public static void putLong(byte[] bb, long x, int index)
	{
		bb[index + 0] = (byte) (x >> 56);
		bb[index + 1] = (byte) (x >> 48);
		bb[index + 2] = (byte) (x >> 40);
		bb[index + 3] = (byte) (x >> 32);
		bb[index + 4] = (byte) (x >> 24);
		bb[index + 5] = (byte) (x >> 16);
		bb[index + 6] = (byte) (x >> 8);
		bb[index + 7] = (byte) (x >> 0);
	}

	/**
	 * 把long 放入byte[]。高低字节反过来适应C++
	 * @param src
	 * @param offset
	 * @return
	 */
	public static void putReverseBytesLong(byte[] bb, long x, int index)
	{
		bb[index + 7] = (byte) (x >> 56);
		bb[index + 6] = (byte) (x >> 48);
		bb[index + 5] = (byte) (x >> 40);
		bb[index + 4] = (byte) (x >> 32);
		bb[index + 3] = (byte) (x >> 24);
		bb[index + 2] = (byte) (x >> 16);
		bb[index + 1] = (byte) (x >> 8);
		bb[index + 0] = (byte) (x >> 0);
	}

	/**
	 * 从byte[] 数组中 读取一个long
	 * @param b
	 * @param index 索引位置
	 * @return
	 */
	public static long getLong(byte[] bb, int index)
	{
		return ((((long) bb[index + 0] & 0xff) << 56) | (((long) bb[index + 1] & 0xff) << 48)
				| (((long) bb[index + 2] & 0xff) << 40) | (((long) bb[index + 3] & 0xff) << 32)
				| (((long) bb[index + 4] & 0xff) << 24) | (((long) bb[index + 5] & 0xff) << 16)
				| (((long) bb[index + 6] & 0xff) << 8) | (((long) bb[index + 7] & 0xff) << 0));
	}

	/**
	 * 从byte[] 数组中 读取一个long 高低字节反过来适应C++
	 * @param b
	 * @param index 索引位置
	 * @return
	 */
	public static long getReverseBytesLong(byte[] bb, int index)
	{
		return ((((long) bb[index + 7] & 0xff) << 56) | (((long) bb[index + 6] & 0xff) << 48)
				| (((long) bb[index + 5] & 0xff) << 40) | (((long) bb[index + 4] & 0xff) << 32)
				| (((long) bb[index + 3] & 0xff) << 24) | (((long) bb[index + 2] & 0xff) << 16)
				| (((long) bb[index + 1] & 0xff) << 8) | (((long) bb[index + 0] & 0xff) << 0));
	}

	/**
	 * 把float 放入byte[]。
	 * @param src
	 * @param offset
	 * @return
	 */
	public static void putFloat(byte[] bb, float x, int index)
	{

		long l = Float.floatToIntBits(x);

		for (int i = index + 3; i >= index; i--)
		{
			bb[i] = (byte) (l & 0xff);

			l = l >>> 8;

		}

	}
	/**
	 * 从byte[] 数组中 读取一个float
	 * @param b
	 * @param index 索引位置
	 * @return
	 */
	public static float getFloat(byte[] bb, int index)
	{

		byte[] bytes = new byte[4];

		System.arraycopy(bb, index, bytes, 0, bytes.length);

		ByteBuffer bbf = ByteBuffer.wrap(bytes);

		float f = bbf.getFloat();

		return f;
	}

	
}
