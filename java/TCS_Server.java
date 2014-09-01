import java.net.*;
import java.io.*;
import ngat.message.RCS_TCS.*;

/** Implements a server for the TCS_Simulation.
 */
public class TCS_Server extends Thread {

   
    TCS_Simulation sim;
    
    DatagramSocket socket;    

    TCS_Status sdb;

    public TCS_Server(TCS_Simulation sim, int port, TCS_Status sdb) {
	super();
	this.sim       = sim;
	this.sdb = sdb;

	setPriority(6);
	socket = null;
	try {
	    socket = new DatagramSocket(port);
	    System.out.println("Done initializing SERVER on port: "+port);
	} catch (IOException e) {
	    System.out.println("Error initializing server:" + e);
	}

	// Set SO-Timeout to check whether Launcher has been terminated() yet.
	try {
	    socket.setSoTimeout(60000); 
	} catch (SocketException e) {
	    log("Error setting Server DatagramSocket timeout: " + e);
	}
    }

    public void run() {
	
	
	while (true) {
	    // Listen for Client connections. 
	    // Timeout regularly to check for termination signal.
	    while (true) { 
		
		// ********************************************************
		// START RECEIVING NOW.
		// ********************************************************
		
		byte[] buff1 = new byte[2000];
		DatagramPacket packet = new DatagramPacket(buff1, buff1.length);
		try {
		    socket.receive(packet);
		    // log("Packet received:");
		} catch (InterruptedIOException e3) {
		    //log("Timed out");
		    continue;
		    // Socket timed-out so try again
		} catch (IOException e) {
		    log("Error reading packet:" + e);
		    continue;
		}

		
		//log("Creating Connection Thread for Client at: ["+
		//  packet.getAddress() + "] on port: [" + 
		//  packet.getPort() + "]");
		
		TCS_ConnectionThread tcs_ConnectionThread = new TCS_ConnectionThread(socket, 
										     packet, 
										     sdb);
		
		if (tcs_ConnectionThread == null) {
		    log("Error generating TCS Connection Thread for client: ");
		} else {
		    //log("Starting TCS Connection Thread for client connection: ");
		    tcs_ConnectionThread.start();;
		}
	    }
	}
    }
    
    public void log(String text) { System.out.println("TCS_Server: "+text);}
    
}
