package cpsc441_assignment3;

import java.util.Timer;

/**
 * FastFtp Class
 * 
 * FastFtp implements a basic FTP application based on UDP data transmission.
 * The main mehtod is send() which takes a file name as input argument and send the file 
 * to the specified destination host.
 * 
 */
public class FastFtp {
	
    /**
     * Constructor to initialize the program 
     * 
     * @param windowSize	Size of the window for Go-Back_N (in segments)
     * @param rtoTimer		The time-out interval for the retransmission timer (in milli-seconds)
     */
	
	private Timer _TimeoutTimer;
	private int _RtoTimeout;
	private TxQueue _PacketQueue;
	
	public FastFtp(int windowSize, int rtoTimer) {
		//
		// to be completed
		//
		this._TimeoutTimer = new Timer(true);
		this._RtoTimeout = rtoTimer;
		_PacketQueue = new TxQueue(windowSize);
	}

    /**
     * Sends the specified file to the specified destination host:
     * 1. send file name and receiver server confirmation over TCP
     * 2. send file segment by segment over UDP
     * 3. send end of transmission over tcp
     * 3. clean up
     * 
     * @param serverName	Name of the remote server
     * @param serverPort	Port number of the remote server
     * @param fileName		Name of the file to be trasferred to the remote server
     */
	public void send(String serverName, int serverPort, String fileName) {
		//
		// to be completed
		//
	}
	
	public synchronized void processTimeout()
	{
		
	}
	
	public synchronized void processSegment(Segment seg)
	{
		
	}
	
	public synchronized void processACK(Segment ack)
	{
		
	}
	
	public boolean TcpHandshake(int serverName, int serverPort, String fileName)
	{
		boolean success = false;
		
		return success;
	}
}
