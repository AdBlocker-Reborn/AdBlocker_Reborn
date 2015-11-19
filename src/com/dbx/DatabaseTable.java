package com.dbx;

import java.util.ArrayList;

import com.peerblock.PayloadReader;
import com.peerblock.PayloadWriter;

public class DatabaseTable
{
	private DatabaseColumn[] columns;
	private String tableName;
	private long incrementalId;
	private long TableId;
	private DatabaseX db;
	
	public DatabaseTable(DatabaseX db, String TableName, DatabaseColumn[] columns)
	{
		this.db = db;
		this.columns = columns;
		this.tableName = TableName;
		this.TableId = MurmurHash.hash64(TableName);
	}
	
	public DatabaseTable(DatabaseX db, PayloadReader pr) throws Exception
	{
		this.db = db;
		this.tableName = DatabaseX.ReadFinalString(pr);
		
		if(tableName.isEmpty())
			return;
		
		this.TableId = MurmurHash.hash64(this.tableName);
		this.incrementalId = pr.ReadLong();
		int ColumnCount = pr.ReadByte();
		ArrayList<DatabaseColumn> cols = new ArrayList<DatabaseColumn>();
		
		for(int i = 0; i < ColumnCount; i++)
		{
			cols.add(new DatabaseColumn(DatabaseX.ReadFinalString(pr), pr.ReadInteger()));
		}
		this.columns = cols.toArray(new DatabaseColumn[cols.size()]);
	}
	
	public DatabaseColumn[] getColumns()
	{
		return this.columns;
	}
	public String getTableName()
	{
		return this.tableName;
	}
	public long getIncrementalId()
	{
		return this.incrementalId;
	}
	public long getTableId()
	{
		return this.TableId;
	}
	public void IncIncrementalId()
	{
		incrementalId++;
		//write to disk
	}
	
	public byte[] Serialize() throws Exception
	{
		PayloadWriter pw = new PayloadWriter();
		pw.WriteString(DatabaseX.getFinalString(tableName));
		pw.WriteLong(incrementalId); //incremental id
		pw.WriteByte((byte)columns.length); //columns length
		
		for(int i = 0; i < columns.length; i++)
		{
			if(columns[i].Name.length() == 0)
				throw new Exception("Column name must contain atleast 1 or more in length");
			
			pw.WriteString(DatabaseX.getFinalString(columns[i].Name));
			pw.WriteInteger(columns[i].DataType);
		}
		return pw.ToByteArray();
	}
	
	public void Insert(Object... Values) throws Exception
	{
		db.Insert(this, Values);
	}
	
	public void Update()
	{
		
	}
	
	public DatabaseRow[] SelectAll()
	{
		return db.SelectAll(this);
	}
	
	public int getSize()
	{
		return DatabaseX.STRING_SIZE + DatabaseX.LONG + DatabaseX.BYTE + (DatabaseX.COLUMN_SIZE * columns.length);
	}
	public int getRowSize()
	{
		int size = 0;
		for(int i = 0; i < columns.length; i++)
		{
			size += columns[i].DataType;
		}		
		return size;
	}
}