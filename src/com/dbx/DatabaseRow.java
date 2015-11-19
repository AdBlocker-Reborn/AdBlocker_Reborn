package com.dbx;

import com.peerblock.PayloadReader;
import com.peerblock.PayloadWriter;

public class DatabaseRow
{
	private DatabaseX db;
	private DatabaseTable table;
	private Object[] Values;
	
	public DatabaseRow(DatabaseX db, DatabaseTable[] tables, PayloadReader pr, boolean ReadTableId, long TableId)
	{
		this.db = db;
		String errur = "";
		
		//read back all the values
		try
		{
			long tableId = ReadTableId ? pr.ReadLong() : TableId;
			
			for(int i = 0; i < tables.length; i++)
			{
				if(tables[i].getTableId() == tableId)
				{
					this.table = tables[i];
					break;
				}
			}
			
			this.Values = new Object[pr.ReadByte()];
			DatabaseColumn[] cols = table.getColumns();
			
			for(int i = 0; i < this.Values.length; i++)
			{
				switch(cols[i].DataType)
				{
					case DatabaseX.STRING:
					{
						this.Values[i] = DatabaseX.ReadFinalString(pr);
						break;
					}
					case DatabaseX.BYTE:
					{
						this.Values[i] = pr.ReadByte();
						break;
					}
					case DatabaseX.SHORT:
					{
						this.Values[i] = pr.ReadShort();
						break;
					}
					case DatabaseX.INT:
					{
						this.Values[i] = pr.ReadInteger();
						break;
					}
					case DatabaseX.LONG:
					{
						this.Values[i] = pr.ReadLong();
						break;
					}
					case DatabaseX.DOUBLE:
					{
						
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			errur = e.getMessage();
		}
	}
	
	public DatabaseRow(DatabaseX db, DatabaseTable table, Object[] Values)
	{
		this.db = db;
		this.table = table;
		this.Values = Values;
	}
	
	public byte[] Serialize()
	{
		PayloadWriter pw = new PayloadWriter();
		pw.WriteLong(table.getTableId());
		pw.WriteByte((byte)Values.length);
		DatabaseColumn[] cols = table.getColumns();
		
		for(int i = 0; i < Values.length; i++)
		{
			switch(cols[i].DataType)
			{
				case DatabaseX.STRING:
				{
					pw.WriteString(DatabaseX.getFinalString(Values[i].toString()));
					break;
				}
				case DatabaseX.BYTE:
				{
					pw.WriteByte(Byte.parseByte(Values[i].toString()));
					break;
				}
				case DatabaseX.SHORT:
				{
					pw.WriteShort(Short.parseShort(Values[i].toString()));
					break;
				}
				case DatabaseX.INT:
				{
					pw.WriteInteger(Integer.parseInt(Values[i].toString()));
					break;
				}
				case DatabaseX.LONG:
				{
					pw.WriteLong(Long.parseLong(Values[i].toString()));
					break;
				}
				case DatabaseX.DOUBLE:
				{
					pw.WriteDouble(Double.parseDouble(Values[i].toString()));
					break;
				}
			}
		}
		return pw.ToByteArray();
	}
}