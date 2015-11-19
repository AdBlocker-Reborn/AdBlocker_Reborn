package com.peerblock;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

import com.activities.UpdateConfigActivity;
import com.root.ShellCommand;

import android.os.Environment;

public class BlockListStream
{
	public static final String BlockListPath = Environment.getExternalStorageDirectory() + "/PeerBlock/BlockList.dat";
	public File TargetFile;
	private Object SyncLock = new Object();
	
	public BlockListStream()
	{
		this.TargetFile = new File(BlockListPath);
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
	
	public void RebuildCache(UpdateConfigActivity updateActivity)
	{
		synchronized(SyncLock)
		{
			String errorMsg = "";
			updateActivity.RangesToScan = getMaxLineCount();
			updateActivity.ScanStatus = "Parsing PeerBlock Lists...";
			updateActivity.UpdateProgress();
			
			File blockLists = new File(Environment.getExternalStorageDirectory() + "/PeerBlock/PeerBlockLists");
		    File list[] = blockLists.listFiles();
		    Date date = new Date();
		    long Time = date.getTime();
		    
		    if(TargetFile.exists())
		    {
		    	if(!TargetFile.delete())
		    	{
					updateActivity.ErrorMsg = "Unable to remove the PeerBlock.dat file... in use?";
					updateActivity.ErrorOccured = true;
					updateActivity.UpdateProgress();
					return;
		    	}
		    }
		    
		    try
		    {
				if(!TargetFile.createNewFile())
				{
					updateActivity.ErrorMsg = "Unable to create the PeerBlock.dat file...";
					updateActivity.ErrorOccured = true;
					updateActivity.UpdateProgress();
					return;
				}
			}
		    catch (Exception e2)
			{
				updateActivity.ErrorMsg = "Unable to create the PeerBlock.dat file...";
				updateActivity.ErrorOccured = true;
				updateActivity.UpdateProgress();
				return;
			}
		    
		    RandomAccessFile targetIn = null;
		    
		    try
		    {
				targetIn = openFile();
			}
		    catch (Exception e2)
			{
				updateActivity.ErrorMsg = e2.getMessage();
				updateActivity.ErrorOccured = true;
				updateActivity.UpdateProgress();
				return;
			}
		    
		    long PrevRange = -1;

		    PayloadWriter pw = new PayloadWriter();
		    for( int i=0; i< list.length; i++)
		    {
		    	InputStream instream = null;
		    	BufferedReader buffreader = null;
				try
				{
					instream = new FileInputStream(list[i]);
					InputStreamReader inputreader = new InputStreamReader(instream);
					buffreader = new BufferedReader(inputreader);
					String line = null;
					
					//read the file and read all the ip ranges
					do
					{
						try
						{
							line = buffreader.readLine();
							
							if(line != null)
							{
								int offset = line.lastIndexOf(":");
								
								if(offset < 0)
									continue;
								
								String RangeLine = line.substring(offset+1);
								StringTokenizer tokens = new StringTokenizer(RangeLine, "-");
								if(tokens.hasMoreElements())
								{
									String BeginRange = tokens.nextElement().toString();
									String EndRange = tokens.nextElement().toString();
									
									long beginRange = IpToLong(BeginRange);
									long endRange = IpToLong(EndRange);
	
									pw.WriteBytes(BitConverter.getBytes(beginRange));
									pw.WriteBytes(BitConverter.getBytes(endRange));
									
									if(pw.payload.size() >= 65535)
									{
										targetIn.write(pw.ToByteArray());
										pw.Dispose();
										pw = new PayloadWriter();
									}
									
									updateActivity.progressBarStatus++;
									if(new Date().getTime() - Time > 1000)
									{
										updateActivity.ScanStatus = "Analyzing PeerBlock Lists...\r\nAnalyzed " + updateActivity.progressBarStatus + " Ip Ranges";
									    updateActivity.UpdateProgress();
									    Time = new Date().getTime();
									}
								}
							}
						}
						catch(Exception ex)
						{
							updateActivity.FormatErrors++;
						}
					 } while (line != null);
				}
				catch (Exception e1)
				{
					errorMsg = e1.getMessage();
				}
				
				if(instream != null)
				{
					try { instream.close(); } catch (IOException e) { }
					try { buffreader.close(); } catch (IOException e) { }
				}
		    }
		    
		    try
		    {
		    	if(pw.payload.size() >= 0)
				{
					targetIn.write(pw.ToByteArray());
					pw.Dispose();
					pw = new PayloadWriter();
				}
				targetIn.close();
			}
		    catch (Exception e)
		    {
		    	errorMsg = e.getMessage();
			}
		}
	}
	
	public void ApplyIpTable(UpdateConfigActivity updateActivity)
	{		
	    File ScriptFile = null;
	    RandomAccessFile ScriptFileAccess = null;
	    
	    try
	    {
	    	ScriptFile = new File(Environment.getExternalStorageDirectory() + "/PeerBlock/apply.sh");
			if(!ScriptFile.exists())
			{
				String parent = ScriptFile.getParent();
				File dir = new File(parent);
				if(!dir.exists())
					dir.mkdirs();
				
				try
				{
					ScriptFile.createNewFile();
				} catch (Exception e)
				{
					
				}
			}
			ScriptFileAccess = new RandomAccessFile(ScriptFile, "rws");
		}
	    catch (Exception e2)
		{
			return;
		}
	    
	    String ScriptTable = "";
	    int LinesApplied = 0;

		updateActivity.ScanStatus = "Generating IP Table list...";
		updateActivity.ErrorOccured = false;
		updateActivity.UpdateProgress();
		
	    try
	    {
	    	PayloadReader pr = new PayloadReader(ReadAllData(0));
		    while(pr.offset+16 <= pr.data.length)
		    {
		    	String beginRange = intToIp(pr.ReadLong());
		    	String endRange = intToIp(pr.ReadLong());
		    	
		    	//stream.write(("iptables -I INPUT -m iprange --src-range " + BeginRange + "-" + EndRange + " -j DROP\n").getBytes("UTF-8"));
				ScriptTable += "iptables -I INPUT -m iprange --src-range " + beginRange + "-" + endRange + " -j DROP\n";
				LinesApplied++;
				
				if(LinesApplied > 1000)
				{
					ScriptFileAccess.write((ScriptTable.getBytes("UTF-8")));
					ScriptTable = "";
					LinesApplied = 0;
					

					updateActivity.ScanStatus = "Generating IP Table list...\nBegin Range:" + beginRange + "\nEnd Range:" + endRange;
					updateActivity.UpdateProgress();
				}
		    }
		    pr.Dispose();
	    }
	    catch(Exception ex)
	    {
	    	
	    }
	    
		try
	    {
	    	if(LinesApplied > 1000)
			{
	    		ScriptFileAccess.write((ScriptTable.getBytes("UTF-8")));
				ScriptTable = "";
				LinesApplied = 0;
			}
	    	

			updateActivity.ScanStatus = "Applying IP Tables...";
			updateActivity.UpdateProgress();
	    	
	    	ScriptFileAccess.close();
	    	ShellCommand cmd = new ShellCommand(new String[] { "su", "-c", "sh " + ScriptFile.getAbsolutePath() });
	    	cmd.start(true);
	    	cmd.finish();
	    }
	    catch(Exception e)
	    {
	    	
	    }

		updateActivity.ScanStatus = "Done Applying!";
		updateActivity.UpdateProgress();
	}
	
	public String IsIpBlocked(String IpAddress) throws Exception
	{
		long TargetIp = IpToLong(IpAddress);
	    PayloadReader pr = new PayloadReader(ReadAllData(0));
	    
	    while(pr.offset+16 <= pr.data.length)
	    {
	    	long beginRange = pr.ReadLong();
	    	long endRange = pr.ReadLong();
	    	
	    	if(new IpRange(beginRange, endRange).IsInRange(TargetIp))
	    		return intToIp(beginRange) + "-" + intToIp(endRange);
	    }
	    pr.Dispose();
	    return "";
	}
	
	public long getIpCountBlocked() throws Exception
	{
		long RetCount = 0;
		
		PayloadReader pr = new PayloadReader(ReadAllData(0));
		while(pr.offset+16 <= pr.data.length)
	    {
	    	long beginRange = pr.ReadLong();
	    	long endRange = pr.ReadLong();
	    	long count = endRange - beginRange;
	    	if(count == 0)
	    		count = 1;
	    	RetCount += count;
	    }
	    pr.Dispose();		
		return RetCount;
	}
	
	public String[] GetList()
	{
		String root_sd = Environment.getExternalStorageDirectory().toString();
		File blockLists = new File(root_sd + "/PeerBlockLists");
		
		if(!blockLists.exists())
			blockLists.mkdirs();
		
	    File list[] = blockLists.listFiles();
	    
	    if(list == null)
	    	return new String[0];
	    
	    String[] fileNames = new String[list.length];

	    for( int i = 0; i< list.length; i++)
	    	fileNames[i] = list[i].getAbsolutePath();
		return fileNames;
	}
	
	private byte[] ReadAllData(int offset) throws IOException
	{
		synchronized(SyncLock)
		{
			RandomAccessFile file = openFile();
			
			if(file == null)
				return new byte[0];
			
			file.seek(offset);

			int read = 0;
			int length = (int)file.length();
	        byte[] ret = new byte[length];
	        while(read != length)
	        {
	            int result = file.read(ret, read, length-read);
	            if(result == -1)
	            {
	            	byte[] ReadedBytes = Arrays.copyOf(ret, read);
	            	file.close();
	                return ReadedBytes;
	            }
	            read += result;
	        }
			file.close();
			return ret;
		}
	}
	
	private int getMaxLineCount()
	{
		int count = 0;
		
		File blockLists = new File(Environment.getExternalStorageDirectory() + "/PeerBlock/PeerBlockLists");
	    File list[] = blockLists.listFiles();

	    for( int i=0; i< list.length; i++)
	    {
	    	InputStream instream = null;
	    	BufferedReader buffreader = null;
			try
			{
				instream = new FileInputStream(list[i]);
				InputStreamReader inputreader = new InputStreamReader(instream);
				buffreader = new BufferedReader(inputreader);
				String line = null;
				
				//read the file and read all the ip ranges
				do
				{
					line = buffreader.readLine();
					
					if(line != null)
					{
						int offset = line.indexOf(":");
						if(offset < 0)
							continue;
						
						String RangeLine = line.substring(offset+1);
						StringTokenizer tokens = new StringTokenizer(RangeLine, "-");
						if(!tokens.hasMoreElements())
						{
							continue;
						}
						
						count++;
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
	    }
		return count;
	}
	
	private RandomAccessFile openFile() throws FileNotFoundException
	{
		return new RandomAccessFile(this.TargetFile, "rws");
	}
	
	public static String intToIp(long i)
	{
		return ((i >> 24 ) & 0xFF) + "." +
			   ((i >> 16 ) & 0xFF) + "." +
			   ((i >> 8 ) & 0xFF) + "." +
			   ( i & 0xFF);
	}
	
	public static long IpToLong(String IpAddress)
	{
		String[] ranges = IpAddress.split("\\.");
		long ip = 0;
		for(int i = 0; i < 4; i++)
			ip = ip << 8 | (Integer.parseInt(ranges[i]) & 0xFF);
		return ip;
	}
}



