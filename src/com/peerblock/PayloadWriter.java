package com.peerblock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PayloadWriter
{
	public ByteArrayOutputStream payload;
    public PayloadWriter()
    {
        this.payload = new ByteArrayOutputStream();
    }
    
    public void WriteByte(byte value)
    {
        payload.write(value);
    }
    
    public void WriteBytes(byte[] value)
    {
        this.payload.write(value, 0, value.length);
    }
    
    public void WriteShort(short value)
    {
        WriteBytes(BitConverter.getBytes(value));
    }
    
    public void WriteInteger(int value)
    {
        WriteBytes(BitConverter.getBytes(value));
    }
    
    public void WriteLong(long value)
    {
        WriteBytes(BitConverter.getBytes(value));
    }
    
    public void WriteDouble(double value)
    {
        WriteBytes(BitConverter.getBytes(value));
    }
    
    public void WriteFloat(long value)
    {
        WriteBytes(BitConverter.getBytes(value));
    }
    
    public void WriteString(String value)
    {
        for(int i = 0; i < value.length(); i++)
            WriteBytes( BitConverter.getBytes(value.charAt(i)));
        WriteByte((byte)0);
        WriteByte((byte)0);
    }
    
    public byte[] ToByteArray()
    {
        return payload.toByteArray();
    }
    
    public void Dispose()
    {
        try {
            payload.close();
            payload = null;
        } catch (IOException ex) {

        }
    }
}
