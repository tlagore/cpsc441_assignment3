package cpsc441_assignment3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import cpsc441.a3.Segment;
import cpsc441.a3.TxQueue;

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
	private Socket _TCPSocket;
	private Logger _Logger;
	
	public FastFtp(int windowSize, int rtoTimer) {
		//
		// to be completed
		//
		this._TimeoutTimer = new Timer(true);
		this._RtoTimeout = rtoTimer;
		_PacketQueue = new TxQueue(windowSize);
		_Logger = Logger.getLogger(this.getClass().getName());
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
		DatagramPacket packet;
		
		int maxSegmentSize = Segment.MAX_PAYLOAD_SIZE;
		if (TcpHandshake(serverName, serverPort, fileName))
		{
			System.out.println(_TCPSocket.getLocalPort());
		}else
		{
			_Logger.log(Level.SEVERE, "Failed to initialize TCP Handshake");
		}
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
	
	public boolean TcpHandshake(String serverName, int serverPort, String fileName)
	{
		boolean success = false;
		byte retVal;
		DataOutputStream outputStream;
		DataInputStream inputStream;
		try{
			_TCPSocket = new Socket(serverName, serverPort);
			outputStream = new DataOutputStream(_TCPSocket.getOutputStream());
			inputStream = new DataInputStream(_TCPSocket.getInputStream());
			
			outputStream.writeUTF(fileName);
			outputStream.flush();
			
			retVal = inputStream.readByte();
			
			success = retVal == 0;
			
			//outputStream.writeByte(0);
			
			outputStream.close();
			inputStream.close();
		}catch(IOException ex)
		{
			_Logger.log(Level.SEVERE, "Error in TCP handshake.", ex);
		}
		
		return success;
	}
}
