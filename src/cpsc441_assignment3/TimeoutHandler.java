package cpsc441_assignment3;

import java.util.TimerTask;

/**
 * Simple timer to handle segment timeouts
 * 
 * @author Tyrone
 */
public class TimeoutHandler extends TimerTask {
	private FastFtp _FtpParent;
	
	public TimeoutHandler(FastFtp parent)
	{
		_FtpParent = parent;
	}
	public void run()
	{
		//do work
		_FtpParent.processTimeout();
	}
}
