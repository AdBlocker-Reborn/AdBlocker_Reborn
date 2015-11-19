package com.peerblock;

import java.io.IOException;

public class PayloadReader
{
	protected byte[] data;
    protected int offset;
    public PayloadReader(byte[] data)
    {
        this.data = data;
        this.offset = 0;
    }
    
    public int getOffset()
    {
    	return offset;
    }
    public void skip(int size)
    {
    	offset += size;
    }
    
    public int ReadByte() throws IOException
    {
        int ret = data[offset];
        offset++;
        return ret;
    }
    
    public byte[] ReadBytes(int length)
    {
        byte[] ret = new byte[length];
        System.arraycopy(data, offset, ret, 0, length);
        offset += length;
        return ret;
    }
    
    public short ReadShort() throws Exception
    {
        short ret = BitConverter.toInt16(data, offset);
        offset += 2;
        return ret;
    }
    
    public int ReadInteger() throws Exception
    {
        int ret = BitConverter.toInt32(data, offset);
        offset += 4;
        return ret;
    }
    
    public long ReadLong() throws Exception
    {
        long ret = BitConverter.toInt64(data, offset);
        offset += 8;
        return ret;
    }
    
    public String ReadString() throws Exception
    {
        String ret = "";
        
        for(int i = offset; i < data.length; i += 2)
        {
            short tmp = BitConverter.toInt16(data, i);
            
            if(tmp == 0)
                break;
            
            ret += (char)tmp;
        }
        offset += (ret.length() * 2) + 2;
        return ret;
    }
    
    public void Dispose()
    {
        data = null;
    }
}
