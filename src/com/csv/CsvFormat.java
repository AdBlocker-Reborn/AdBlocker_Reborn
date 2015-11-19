package com.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import android.os.Environment;

import com.peerblock.IpRange;

public abstract class CsvFormat
{
	public String FilePath;
	private Object SyncLock = new Object();
	
	protected abstract void onCreate();
	public String ErrorMsg = "";
	private File TargetFile;
	
	public CsvFormat(String FilePath) throws Exception
	{
		this.FilePath = FilePath;
		this.TargetFile = new File(this.FilePath);

		if(!TargetFile.exists())
		{
			String parent = TargetFile.getParent();
			File dir = new File(parent);
			if(!dir.exists())
				dir.mkdirs();
			
			try
			{
				TargetFile.createNewFile();
			} catch (Exception e)
			{
				ErrorMsg = e.getMessage();
			}
			onCreate();
		}
		else if(TargetFile.length() == 0)
		{
			onCreate();
		}
	}
	
	protected String GetString(String Key, int ColumnIndex)
	{
		synchronized(SyncLock)
		{
			String[] vals = GetValue(Key);
			if(ColumnIndex < vals.length)
				return vals[ColumnIndex].replace("<enter>", "\r\n").replace("<comma>", ",");
			return "";
		}
	}
	
	protected boolean GetBoolean(String Key, int ColumnIndex)
	{
		synchronized(SyncLock)
		{
			String value = GetString(Key, ColumnIndex);
			if(value.equals("true"))
				return true;
			return false;
		}
	}
	
	protected int GetInteger(String Key, int ColumnIndex)
	{
		synchronized(SyncLock)
		{
			String value = GetString(Key, ColumnIndex);
			return Integer.parseInt(value);
		}
	}
	
	protected long GetLong(String Key, int ColumnIndex)
	{
		synchronized(SyncLock)
		{
			String value = GetString(Key, ColumnIndex);
			return Long.parseLong(value);
		}
	}
	
	
	
	@SuppressWarnings("resource")
	protected String[] GetValue(String Key)
	{
		synchronized(SyncLock)
		{
			try
			{
				RandomAccessFile File = openFile();
				String line = "";
	
				do
				{
					try
					{
						line = File.readLine();
						
						if(line != null)
						{
							StringTokenizer tokens = new StringTokenizer(line, ",");
							if(tokens.hasMoreElements())
							{
								String keyStr = tokens.nextElement().toString();
								
								if(keyStr.equals(Key))
								{
									int count = tokens.countTokens();
									String[] values = new String[count];
									
									for(int i = 0; i < count; i++)
										values[i] = tokens.nextToken();
									return values;
								}
							}
						}
					}
					catch (IOException e)
					{
						ErrorMsg = e.getMessage();
					}
				 } while (line != null);
			}
			catch(Exception ex)
			{
				ErrorMsg = ex.getMessage();
			}
			return new String[0];
		}
	}
	
	@SuppressWarnings("resource")
	protected ArrayList<String[]> GetValues(String Key, CsvCallback callback)
	{
		synchronized(SyncLock)
		{
			try
			{
				RandomAccessFile File = openFile();
				String line = "";
				ArrayList<String[]> values = new ArrayList<String[]>();
	
				do
				{
					try
					{
						line = File.readLine();
						
						if(line != null)
						{
							StringTokenizer tokens = new StringTokenizer(line, ",");
							if(tokens.hasMoreElements())
							{
								String keyStr = tokens.nextElement().toString();
								
								if(keyStr.equals(Key))
								{
									int count = tokens.countTokens();
									String[] vals = new String[count];
									
									for(int i = 0; i < count; i++)
										vals[i] = tokens.nextToken();
									
									if(callback != null)
										callback.onCallback(vals);
									else
										values.add(vals);
								}
							}
						}
					}
					catch (IOException e)
					{
						ErrorMsg = e.getMessage();
					}
				 } while (line != null);
				return values;
			}
			catch(Exception ex)
			{
				ErrorMsg = ex.getMessage();
			}
			return new ArrayList<String[]>();
		}
	}
	
	protected void WriteKey(String Key, String... Values)
	{
		synchronized(SyncLock)
		{
			try
			{
				RandomAccessFile File = openFile();
				File.seek(File.length());
				String ValStr = Key + ",";
				
				for(int i = 0; i < Values.length; i++)
				{
					ValStr += Values[i].replace("\r\n", "<enter>").replace(",", "<comma>") + (i+1<Values.length ? "," : "");
				}
				File.writeBytes(ValStr + "\r\n");
				File.close();
			}
			catch(Exception ex)
			{
				ErrorMsg = ex.getMessage();
			}
		}
	}
	
	public File getTempDir()
	{
		File tempDir = new File(Environment.getExternalStorageDirectory() + "/PeerBlock/temp");
		if(!tempDir.exists())
			tempDir.mkdirs();
		return tempDir;
	}
	
	protected void WriteValue(String Key, String... Values)
	{
		synchronized(SyncLock)
		{
			try
			{
				File temp = File.createTempFile("file", ".tmp", getTempDir());
				RandomAccessFile tempFile = new RandomAccessFile(temp, "rws");
				RandomAccessFile File = openFile();
				String line = "";
				boolean JustCopy = false;
	
				do
				{
					try
					{
						line = File.readLine();
						if(line != null)
						{
							if(JustCopy)
							{
								tempFile.writeBytes(line + "\r\n");
							}
							else
							{
								if(line.startsWith(Key + ","))
								{
									JustCopy = true;
								}
								else
								{
									tempFile.writeBytes(line + "\r\n");
								}
							}
						}
					}
					catch(Exception ex)
					{
						ErrorMsg = ex.getMessage();
					}
				} while (line != null);
				
				File.close();
				tempFile.close();
				copyFile(temp, TargetFile);
				temp.delete();
				File = openFile();
				WriteKey(Key, Values);
			}
			catch(Exception e)
			{
				ErrorMsg = e.getMessage();
			}
		}
	}
	
	private RandomAccessFile openFile() throws Exception
	{
		return new RandomAccessFile(this.FilePath, "rws");
	}
	
	public static void copyFile(File sourceFile, File destFile) throws Exception
	{
	    if(!destFile.exists())
	    {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
}