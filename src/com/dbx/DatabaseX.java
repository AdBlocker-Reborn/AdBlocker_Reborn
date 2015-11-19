package com.dbx;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import com.peerblock.PayloadReader;
import com.peerblock.PayloadWriter;

public abstract class DatabaseX
{
	public final static int UNKNOWN = 0;
	public final static int STRING = 254;
	public final static int STRING_SIZE = (STRING*2)+2; //max size in bytes
	public final static int BYTE = 1;
	public final static int SHORT = 2;
	public final static int INT = 4;
	public final static int LONG = 8;
	public final static int DOUBLE = 16;
	public final static int HEADER_SIZE = 32768;
	public final static int BEGIN_TABLE_OFFSET = 0;
	public final static int BEGIN_DATA_OFFSET = BEGIN_TABLE_OFFSET + HEADER_SIZE;
	public final static int COLUMN_SIZE = STRING_SIZE + INT; //Name + Data Type (257) = 63 columns
	
	public abstract void onCreate();
	
	private String DatabasePath;
	private File TargetFile;
	private Object SyncLock = new Object();
	
	public DatabaseX(String DatabasePath)
	{
		this.DatabasePath = DatabasePath;
		this.TargetFile = new File(this.DatabasePath);
		
		this.TargetFile.delete();

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
			onCreate();
		}
		else if(TargetFile.length() == 0)
		{
			onCreate();
		}
	}
	
	public DatabaseTable CreateNewTable(String TableName, DatabaseColumn... columns) throws Exception
	{
		synchronized(SyncLock)
		{
			if(columns.length == 0)
				throw new Exception("Must atleast contain 1 column");
			if(getTable(TableName) != null)
				throw new Exception("The table(" + TableName + ") already exists");
			
			//write all the columns
			DatabaseTable table = new DatabaseTable(this, TableName, columns);
			int offset = GetNewTableOffset();
			RandomAccessFile file = openFile();
			file.seek(offset);
			file.write(table.Serialize());
			file.close();
			return table;
		}
	}
	
	public DatabaseTable getTable(String TableName)
	{
		synchronized(SyncLock)
		{
			try
			{
				byte[] header = ReadAllData(BEGIN_TABLE_OFFSET, HEADER_SIZE);
				PayloadReader pr = new PayloadReader(header);
				
				while(pr.getOffset() < header.length)
				{
					DatabaseTable table = new DatabaseTable(this, pr);
					if(table.getTableName().toLowerCase().equals(TableName.toLowerCase()))
						return table;
				}
			}
			catch (Exception e)
			{
				
			}
			return null;
		}
	}
	
	public DatabaseTable[] getAllTables()
	{
		synchronized(SyncLock)
		{
			try
			{
				ArrayList<DatabaseTable> tables = new ArrayList<DatabaseTable>();
				byte[] header = ReadAllData(BEGIN_TABLE_OFFSET, HEADER_SIZE);
				PayloadReader pr = new PayloadReader(header);
				
				while(pr.getOffset() < header.length)
				{
					DatabaseTable table = new DatabaseTable(this, pr);
					
					if(table.getTableName().isEmpty())
						break;
					
					tables.add(table);
				}
				return tables.toArray(new DatabaseTable[tables.size()]);
			}
			catch (Exception e)
			{
				
			}
			return new DatabaseTable[0];
		}
	}
	
	private int GetNewTableOffset() throws Exception
	{
		synchronized(SyncLock)
		{
			int offset = 0;
			byte[] header = ReadAllData(BEGIN_TABLE_OFFSET, HEADER_SIZE);
			PayloadReader pr = new PayloadReader(header);
			
			while(pr.getOffset() < header.length)
			{
				DatabaseTable table = new DatabaseTable(this, pr);
				offset += table.getSize();
			}
			return BEGIN_TABLE_OFFSET+offset;
		}
	}
	
	private byte[] ReadAllData(int offset, int length) throws Exception
	{
		synchronized(SyncLock)
		{
			RandomAccessFile file = openFile();
			
			if(file == null)
				return new byte[0];
			
			file.seek(offset);

			int read = 0;
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
	
	public static String getFinalString(String string)
	{
		if(string.length() > STRING)
		{
			return string.substring(0, STRING);
		}
		else
		{
			StringBuilder builder = new StringBuilder();
			builder.append(string);
			builder.setLength(STRING);
			return builder.toString();
		}
	}
	public static String ReadFinalString(PayloadReader pr)
	{
		try
		{
			String ret = pr.ReadString();
			int readLength = STRING_SIZE - ((ret.length() * 2) + 2);
			pr.skip(readLength);
			return ret;
		}
		catch(Exception ex) { return ""; }
	}
	
	protected void Insert(DatabaseTable table, Object... Values) throws Exception
	{
		synchronized(SyncLock)
		{
			if(Values.length != table.getColumns().length)
				throw new Exception("Values and Colum size did not match");

			DatabaseRow row = new DatabaseRow(this, table, Values);
			RandomAccessFile file = openFile();
			if(file.length() < HEADER_SIZE)
			{
				file.seek(HEADER_SIZE);
			}
			else
			{
				file.seek(file.length());
			}
			file.write(row.Serialize());
			file.close();
		}
	}
	
	protected DatabaseRow[] SelectAll(DatabaseTable table)
	{
		return _SelectAll(table);
	}
	
	private DatabaseRow[] _SelectAll(DatabaseTable TargetTable)
	{
		synchronized(SyncLock)
		{
			ArrayList<DatabaseRow> Rows = new ArrayList<DatabaseRow>();
			DatabaseTable[] tables = getAllTables();
			
			try
			{
				RandomAccessFile file = openFile();
				
				if(file.length() < HEADER_SIZE)
					return new DatabaseRow[0];
				
				byte[] data = new byte[32768];
				file.seek(HEADER_SIZE);
				
				while(file.read(data) > 0)
				{
					PayloadReader pr = new PayloadReader(data);
					
					for(int i = 0; i < 2; i++)
					{
						//let's process the data
						long tableId = pr.ReadLong();
						
						if(TargetTable.getTableId() == tableId)
						{
							Rows.add(new DatabaseRow(this, tables, pr, false, tableId));
						}
						else
						{
							DatabaseTable table = IdToTable(tableId, tables);
							if(table != null)
								pr.skip(table.getRowSize()-LONG);
						}
					}
				}
			}
			catch (Exception e)
			{
				
			}
			return Rows.toArray(new DatabaseRow[Rows.size()]);
		}
	}
	
	private DatabaseTable IdToTable(long tableId, DatabaseTable[] tables)
	{
		for(int i = 0; i < tables.length; i++)
		{
			if(tables[i].getTableId() == tableId)
			{
				return tables[i];
			}
		}
		
		return null;
	}
	
	private RandomAccessFile openFile() throws Exception
	{
		return new RandomAccessFile(this.TargetFile, "rws");
	}
}