package cpsc441_assignment3;

import java.util.TimerTask;

public class TimeoutHandler extends TimerTask {
	private FastFtp _FtpParent;
	
	public void run()
	{
		//do work
		_FtpParent.processTimeout();
	}
}