package com.peerblock;

public class IpRange
{
	public long BeginRange = 0;
	public long EndRange = 0;
	public long BlockCount = 0;
	
	public IpRange(String beginRange, String endRange)
	{
		String[] BeginRanges = beginRange.split("\\.");
		String[] EndRanges = endRange.split("\\.");
		
		for(int i = 0; i < 4; i++)
		{
			int begin = Integer.parseInt(BeginRanges[i]);
			int end = Integer.parseInt(EndRanges[i]);

			BeginRange = BeginRange << 8 | (begin & 0xFF);
			EndRange = EndRange << 8 | (end & 0xFF);
		}
		BlockCount = EndRange - BeginRange;
		if(BlockCount == 0)
			BlockCount = 1;
	}

	public IpRange(long beginRange, long endRange)
	{
		this.BeginRange = beginRange;
		this.EndRange = endRange;
		BlockCount = EndRange - BeginRange;
		if(BlockCount == 0)
			BlockCount = 1;
	}
	
	public boolean IsInRange(long IpAddress)
	{
		if(IpAddress >= BeginRange && IpAddress <= EndRange)
			return true;
		return false;
	}
	
	public boolean IsInRange(String IpAddress)
	{
		return IsInRange(BlockListStream.IpToLong(IpAddress));
	}
}
