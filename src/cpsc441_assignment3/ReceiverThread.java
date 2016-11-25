package cpsc441_assignment3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import cpsc441.a3.Segment;

public class ReceiverThread extends Thread {
	private DatagramSocket _UDPSocket;
	private FastFtp _FtpParent;
	private boolean _Shutdown;
	
	public ReceiverThread(DatagramSocket socket, FastFtp parent)
	{
		_UDPSocket = socket;
		_FtpParent = parent;
		_Shutdown = false;
	}
	
	public void run()
	{
		DatagramPacket packet;
		byte[]packetData = new byte[Segment.MAX_SEGMENT_SIZE];
		while(!Thread.currentThread().isInterrupted() && !_Shutdown){
			packet = new DatagramPacket(packetData, packetData.length);
			try{
				_UDPSocket.receive(packet);
				_FtpParent.processACK(new Segment(packet));
			}catch(Exception ex){
				//
			}
		}
	}
	
	public void shutdown()
	{
		_Shutdown = true;
	}
}
