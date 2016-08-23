package com.baidu.stock.process.util;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtil
{

	public static void putShort(byte b[], short s, int index)
	{
		b[index] = (byte) (s >> 8);
		b[index + 1] = (byte) (s >> 0);
	}

	public static void putReverseBytesShort(byte b[], short s, int index)
	{
		b[index] = (byte) (s >> 0);
		b[index + 1] = (byte) (s >> 8);
	}

	public static short getShort(byte[] b, int index)
	{
		return (short) (((b[index] << 8) | b[index + 1] & 0xff));
	}

	public static short getReverseBytesShort(byte[] b, int index)
	{
		return (short) (((b[index + 1] << 8) | b[index] & 0xff));
	}

	// ///////////////////////////////////////////////////////
	public static void putInt(byte[] bb, int x, int index)
	{
		bb[index + 0] = (byte) (x >> 24);
		bb[index + 1] = (byte) (x >> 16);
		bb[index + 2] = (byte) (x >> 8);
		bb[index + 3] = (byte) (x >> 0);
	}

	public static void putReverseBytesInt(byte[] bb, int x, int index)
	{
		bb[index + 3] = (byte) (x >> 24);
		bb[index + 2] = (byte) (x >> 16);
		bb[index + 1] = (byte) (x >> 8);
		bb[index + 0] = (byte) (x >> 0);
	}

	public static int getInt(byte[] bb, int index)
	{
		return (int) ((((bb[index + 0] & 0xff) << 24) | ((bb[index + 1] & 0xff) << 16) | ((bb[index + 2] & 0xff) << 8) | ((bb[index + 3] & 0xff) << 0)));
	}

	public static int getReverseBytesInt(byte[] bb, int index)
	{
		return (int) ((((bb[index + 3] & 0xff) << 24) | ((bb[index + 2] & 0xff) << 16) | ((bb[index + 1] & 0xff) << 8) | ((bb[index + 0] & 0xff) << 0)));
	}

	// /////////////////////////////////////////////////////////
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

	public static long getLong(byte[] bb, int index)
	{
		return ((((long) bb[index + 0] & 0xff) << 56) | (((long) bb[index + 1] & 0xff) << 48) | (((long) bb[index + 2] & 0xff) << 40) | (((long) bb[index + 3] & 0xff) << 32) | (((long) bb[index + 4] & 0xff) << 24) | (((long) bb[index + 5] & 0xff) << 16) | (((long) bb[index + 6] & 0xff) << 8) | (((long) bb[index + 7] & 0xff) << 0));
	}

	public static long getReverseBytesLong(byte[] bb, int index)
	{
		return ((((long) bb[index + 7] & 0xff) << 56) | (((long) bb[index + 6] & 0xff) << 48) | (((long) bb[index + 5] & 0xff) << 40) | (((long) bb[index + 4] & 0xff) << 32) | (((long) bb[index + 3] & 0xff) << 24) | (((long) bb[index + 2] & 0xff) << 16) | (((long) bb[index + 1] & 0xff) << 8) | (((long) bb[index + 0] & 0xff) << 0));
	}

	public static float getFloat(byte[] bb, int index)
	{

		byte[] bytes = new byte[4];

		System.arraycopy(bb, index, bytes, 0, bytes.length);

		ByteBuffer bbf = ByteBuffer.wrap(bytes);

		float f = bbf.getFloat();

		return f;
	}

	public static float getReverseBytesFloat(byte[] bb, int index)
	{
		ByteBuffer bb1 = ByteBuffer.allocate(4);
		bb1.put(bb, index, 4);
		bb1.flip();
		
		bb1.order(ByteOrder.LITTLE_ENDIAN);
		float f = bb1.getFloat();
		bb1.clear();
		return f;
		
		/*int b1 = (bb[index + 3] & 0x00000080) >> 7;

		int b2 = ((bb[index + 3] & 0x0000007F) << 1) | ((bb[index + 2] & 0x00000080) >> 7);

		int b3 = (((((bb[index + 2] & 0x0000007F)) | 0x00000080) << 16)

		| ((bb[index + 1] & 0x000000FF) << 8) | ((bb[index] & 0x000000FF)));

		int b4 = 23 - b2 + 127;

		float f = (float) b3;

		if (b4 > 0)
		{
			for (int i = 0; i < b4; i++)
			{
				f /= 2;
			}
		}
		else
		{
			for (int i = 0; i > b4; i--)
			{
				f *= 2;
			}
		}
		
		if (b1 != 0)
		{
			f = -f;
		}
		
		
		
		return f;*/
	}

	public static void putFloat(byte[] bb, float x, int index)
	{

		long l = Float.floatToIntBits(x);

		for (int i = index + 3; i >= index; i--)
		{
			bb[i] = (byte) (l & 0xff);

			l = l >>> 8;
		}

	}

	public static void putReverseBytesFloat(byte[] bb, float x, int index)
	{
		long l = Float.floatToIntBits(x);

		for (int i = index; i < 4 + index; i++)
		{
			bb[i] = (byte) (l & 0xff);

			l = l >>> 8;
		}

	}

	public static byte[] intToByte(int x)
	{
		byte[] bb = new byte[4];

		putInt(bb, x, 0);

		return bb;
	}
	
	
	public static void putReverseBytesDouble(byte[] bb, double x, int index)
	{  
        // byte[] b = new byte[8];  
        long l = Double.doubleToLongBits(x);  
        for (int i = 0; i < 8; i++) 
        {  
            bb[index + i] = new Long(l).byteValue();  
            l = l >> 8;  
        }  
    }  
	
	public static double getReverseBytesDouble(byte[] b, int index) 
	{  
	        long l;  
	        l = b[0+index];  
	        l &= 0xff;  
	        l |= ((long) b[1+index] << 8);  
	        l &= 0xffff;  
	        l |= ((long) b[2+index] << 16);  
	        l &= 0xffffff;  
	        l |= ((long) b[3+index] << 24);  
	        l &= 0xffffffffl;  
	        l |= ((long) b[4+index] << 32);  
	        l &= 0xffffffffffl;  
	        l |= ((long) b[5+index] << 40);  
	        l &= 0xffffffffffffl;  
	        l |= ((long) b[6+index] << 48);  
	        l &= 0xffffffffffffffl;  
	        l |= ((long) b[7+index] << 56);  
	        return Double.longBitsToDouble(l);  
	  } 

	public static char getChar(byte[] b, int index) 
	{  
        int s = 0;  
        
        if (b[index + 1] > 0) 
        {
            s += b[index + 1];  
        }
        else 
        {
            s += 256 + b[index + 0];  
        }
        
        s *= 256;  
        
        if (b[index + 0] > 0) 
        {
            s += b[index + 1]; 
        }
        else 
        {
            s += 256 + b[index + 0];  
        }
        
        char ch = (char) s;  
        
        return ch;  
    } 
	
	public static void putChar(byte[] bb, char ch, int index) 
	{  
        int temp = (int) ch;  
        // byte[] b = new byte[2];  
        for (int i = 0; i < 2; i ++ ) 
        {  
            bb[index + i] = new Integer(temp & 0xff).byteValue(); 
            temp = temp >> 8; 
        }  
    }  


}
