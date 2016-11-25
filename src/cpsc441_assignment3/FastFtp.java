package cpsc441_assignment3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
	private DatagramSocket _UDPSocket;
	private Logger _Logger;
	private int _NextSegmentNumber;
	private ExecutorService _ExecutorService;
	
	public FastFtp(int windowSize, int rtoTimer) {
		//
		// to be completed
		//
		//this._TimeoutTimer = new Timer(true);
		this._RtoTimeout = rtoTimer;
		_PacketQueue = new TxQueue(windowSize);
		_Logger = Logger.getLogger(this.getClass().getName());
		_NextSegmentNumber = 0;
		_ExecutorService = Executors.newFixedThreadPool(1);
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
		int amountRead;
		byte[] fileInput = new byte[Segment.MAX_PAYLOAD_SIZE];
		byte[] extraBuffer;
		File file = new File(fileName);
		Segment segmentToSend;
		
		if (TcpHandshake(serverName, serverPort, fileName))
		{
			try{
				_UDPSocket = new DatagramSocket(_TCPSocket.getLocalPort());			
				FileInputStream fileIn;
				
				//ReceiverThread receiver = new ReceiverThread(_UDPSocket, this);
				//receiver.start();
				ReceiverThread receiver = new ReceiverThread(_UDPSocket, this);
				_ExecutorService.execute(receiver);
				//Future receiver = _ExecutorService.submit(new ReceiverThread(_UDPSocket, this));
				//ReceiverThread receiver = new ReceiverThread(_UDPSocket, this);
				//receiver.start();

				fileIn = new FileInputStream(file);
				//InetAddress IPAddress = InetAddress.getByName(serverName);
				
				while((amountRead = fileIn.read(fileInput)) != -1)
				{
					if(amountRead < Segment.MAX_PAYLOAD_SIZE)
					{
						extraBuffer = new byte[amountRead];
						System.arraycopy(fileInput, 0, extraBuffer, 0, amountRead);	
						segmentToSend = new Segment(_NextSegmentNumber++, extraBuffer);
					}else
						segmentToSend = new Segment(_NextSegmentNumber++, fileInput);
					
					while(_PacketQueue.isFull())
						Thread.yield();
					
					processSend(segmentToSend);
				}
				
				while(!_PacketQueue.isEmpty())
					Thread.yield();
				
				//stop receiver
				//_ExecutorService.awa
				//receiver.interrupt();
				
				receiver.shutdown();
				_ExecutorService.shutdown();
				System.out.println("Sent cancel signal to receiver.");
				//_ExecutorService.awaitTermination(5, TimeUnit.SECONDS);
				//receiver.interrupt();
				
				fileIn.close();
				//_TCPSocket.getOutputStream().close();
				//_TCPSocket.getInputStream().close();
				//_TCPSocket.close();
				
				endTransmission();
				System.out.println("Sent server shutdown signal. Server will close sockets.");
		
			}catch(Exception ex)
			{
				System.out.println(ex.getMessage());
			}
		}else
		{
			_Logger.log(Level.SEVERE, "Failed to initialize TCP Handshake");
		}
	}
	
	private void endTransmission()
	{
		DataOutputStream outputStream;
		
		try{
			outputStream = new DataOutputStream(_TCPSocket.getOutputStream());
			outputStream.writeByte(0);
		}catch(Exception ex)
		{
			_Logger.log(Level.SEVERE, "Failed to end transmission", ex);
		}
	}
	
	public synchronized void processSend(Segment seg)
	{
		DatagramPacket packet = new DatagramPacket(seg.getBytes(), seg.getLength(), _TCPSocket.getInetAddress(), _TCPSocket.getPort());
		try{
			_UDPSocket.send(packet);
			_PacketQueue.add(seg);
			if(_PacketQueue.size() == 1)
			{
				startTimer();
			}
		}catch(Exception ex)
		{
			_Logger.log(Level.SEVERE, "failed to send packet", ex);
		}
	}
	
	public synchronized void processTimeout()
	{
		System.out.println("Timeout");
		Segment[] segmentsInQueue = _PacketQueue.toArray();
		Segment seg;
		DatagramPacket packet;
		for(int i = 0; i < segmentsInQueue.length; i ++)
		{
			seg = segmentsInQueue[i];
			packet = new DatagramPacket(seg.getBytes(), seg.getLength(), _TCPSocket.getInetAddress(), _TCPSocket.getPort());
			try{
				_UDPSocket.send(packet);
			}catch(Exception ex)
			{
				_Logger.log(Level.SEVERE, "failed to resend packets in timeout", ex);
			}
		}
		
		if(!_PacketQueue.isEmpty())
			startTimer();
	}
	
	private synchronized void startTimer()
	{
		if(_TimeoutTimer != null)
			_TimeoutTimer.cancel();
		
		_TimeoutTimer = new Timer(true);
		_TimeoutTimer.schedule(new TimeoutHandler(this), _RtoTimeout);
	}
	
	/**
	 * 
	 * @param ack
	 */
	public synchronized void processACK(Segment ack)
	{
		//window has to be less than or equal to my index + windowSize, so if ack seq is greater than 
		//or equal to current front of queue, it's in window
		if(ack.getSeqNum() >= _PacketQueue.element().getSeqNum())
		{
			System.out.println("Processing ack: " + ack.getSeqNum());
			while(_PacketQueue.element().getSeqNum() <= ack.getSeqNum())
			{
				try{
					
					_PacketQueue.remove();
				}catch(Exception ex)
				{
					_Logger.log(Level.SEVERE, "failed to acknowledge ack", ex);
				}
			}
		}
		
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
			
		}catch(IOException ex)
		{
			_Logger.log(Level.SEVERE, "Error in TCP handshake.", ex);
		}
		
		return success;
	}
}
