package com.peerblock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.StringTokenizer;

import android.os.Environment;

public class HostNameBlocks
{
	public File TargetFile = new File(Environment.getExternalStorageDirectory() + "/PeerBlock/HostNames.txt");
	public HostNameBlocks()
	{
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
				
			}
		}
	}
	
	public boolean ContainsHost(String HostName)
	{
		synchronized(TargetFile)
		{
			HostName = HostName.toLowerCase();
			InputStream instream = null;
	    	BufferedReader buffreader = null;
			try
			{
				instream = new FileInputStream(TargetFile);
				InputStreamReader inputreader = new InputStreamReader(instream);
				buffreader = new BufferedReader(inputreader);
				String line = null;

				do
				{
					line = buffreader.readLine();
					
					if(line != null)
					{
						if(HostName.contains(line.toLowerCase()))
						{
							try { instream.close(); } catch (IOException e) { }
							try { buffreader.close(); } catch (IOException e) { }
							return true;
						}
					}
				 } while (line != null);
			}
			catch (Exception e1)
			{
	
			}
			
			if(instream != null)
			{
				try { instream.close(); } catch (IOException e) { }
				try { buffreader.close(); } catch (IOException e) { }
			}
			return false;
		}
	}
	
	public void WriteHost(String HostName)
	{
		synchronized(TargetFile)
		{
			try
			{
				RandomAccessFile file = new RandomAccessFile(this.TargetFile, "rws");
				file.seek(file.length());
				file.writeBytes(HostName + "\r\n");
				file.close();
			}
			catch (Exception e)
			{

			}
		}
	}
	
	public long getHostNameCount()
	{
		long count = 0;
		InputStream instream = null;
    	BufferedReader buffreader = null;
		try
		{
			instream = new FileInputStream(TargetFile);
			InputStreamReader inputreader = new InputStreamReader(instream);
			buffreader = new BufferedReader(inputreader);
			String line = null;

			do
			{
				line = buffreader.readLine();
				
				if(line != null)
					count++;
			 } while (line != null);
		}
		catch (Exception e1)
		{
			
		}
		
		if(instream != null)
		{
			try { instream.close(); } catch (IOException e) { }
			try { buffreader.close(); } catch (IOException e) { }
		}
		return count;
	}
}