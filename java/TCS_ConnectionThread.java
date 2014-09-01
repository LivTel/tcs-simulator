import ngat.astrometry.*;
import ngat.message.base.*;
import ngat.message.RCS_TCS.*;
import ngat.util.*;

import java.net.*;
import java.text.*;
import java.util.*;
import java.io.*;

public class TCS_ConnectionThread extends Thread {

    final int LEFT = -1;
    final int RIGHT = +1;

    final double lat = 28.0;

    final double domelimit = Math.toRadians(20.0);
    
    DatagramSocket socket;

    DatagramPacket packet;

    TCS_Status sdb;

    boolean LTSIM = true;

    String command;
    
   
    public TCS_ConnectionThread(DatagramSocket socket, 
				DatagramPacket packet, 
				TCS_Status sdb) {	    
	super("TCS_CONNECT");	    
	this.socket = socket;
	this.packet = packet;	
	this.sdb = sdb;
	
	setPriority(7);
    } // (Constructor).
    
    public void run(){
	
	// The data length is packet length - CIL header length.
	int length = packet.getLength() - 28;
	byte[] buff1 = packet.getData();
	ByteArrayInputStream bais = new ByteArrayInputStream(buff1);
	
	DataInputStream dis = new DataInputStream(bais);
	
	try {
	    // Read and ignore the routing info - its for us !
	    int txId    = dis.readInt();
	    int rxId    = dis.readInt();
	    int mClass  = dis.readInt();
	    int sClass  = dis.readInt();
	    int seqno   = dis.readInt();
	    int ts1     = dis.readInt(); // ignore.
	    int ts2     = dis.readInt(); // ignore.
	    
	    byte[] data = new byte[length];
	    
	    dis.read(data);
	    
	    String msg = new String(data);
	    
	    int         replyPort    = packet.getPort();
	    InetAddress replyAddress = packet.getAddress();
	    
	    int reply = TCS_Simulation.DONE_CLASS;
	    
	    log("Message received ["+msg+"]");
	    String args = null;
	    CommandParser parser = new CommandParser("&");
	    // 1. Extract COMMAND and args from message string.
	   	    
	    int pos = msg.indexOf(" ");	   
	    command = msg.substring(0, pos);	       
	    args = msg.substring(pos+1).trim();
	    log("RGO Format:: ["+seqno+"] Command ["+command+"] Args ["+args+"]");	
	         
	    // ********************************************************
	    // START REPLYING NOW. THIS IS THE ACK.
	    // ********************************************************
	    
	    try { Thread.sleep(250L);} catch (InterruptedException e){}
	    
	    byte[] buff2 = new byte[28];
	    
	    ByteArrayOutputStream baos = new ByteArrayOutputStream(buff2.length);
	    
	    DataOutputStream dos = new DataOutputStream(baos);
	    
	    // Pack the CIL Routing Header.
	    dos.writeInt(TCS_Simulation.TCS_ID);
	    dos.writeInt(TCS_Simulation.RCS_ID);
	    dos.writeInt(TCS_Simulation.ACK_CLASS);
	    dos.writeInt(TCS_Simulation.SERVICE_TYPE);
	    dos.writeInt(seqno);
	    
	    // Make up a timestamp as UTC from 1980 5th Jan.
	    // Note: This should be GPS time !
	    //      - so leapsecs need subtracting somehow!!
	    
	    Calendar start = Calendar.getInstance();
	    start.set(1980,0,5,0,0,0);
	    long startms = start.getTime().getTime();
	    
	    Calendar now   = Calendar.getInstance();
	    long nowms   = now.getTime().getTime();	
	    
	    long msecs = (nowms - startms);	
	    int secs = (int)(msecs / 1000);	
	    int nanos = (1000000)*(int)(msecs - (long)(1000 * secs));
	    
	    dos.writeInt(secs);
	    dos.writeInt(nanos);
	    
	    buff2 = baos.toByteArray();
	    
	    packet = new DatagramPacket(buff2, buff2.length, replyAddress, replyPort);
	    
	    socket.send(packet);
	    log("Message (ACK) sent to "+replyAddress+" on "+replyPort);
	    
	    // 2. Process the COMMAND.
	    
       
	    boolean success = false;

	    StringTokenizer tokens = new StringTokenizer(args);

	    long timeout = 5000L; // The delay for command to execute.
    
	    TCSCommandImpl impl = null;

	    if
		(command.equals("AUTOGUIDE")) {
		
		log("Command is a AUTOGUIDE");
	       impl = new AUTOGUIDEImpl(tokens, sdb);
	       timeout = 50000L;
	     
	    } else if
		(command.equals("AZIMUTH")) {
 			
		impl = new AZIMUTHImpl(tokens, sdb);
		timeout = 200000L;

	    } else if
		(command.equals("DARKSLIDE")) {
 			
		impl = new DARKSLIDEImpl(tokens, sdb);
		timeout = 5000L;
		

	    } else if
		(command.equals("AGFILTER")) {
 			
		impl = new AGFILTERImpl(tokens, sdb);
		timeout = 45000L;

	    }
	    else if
		(command.equals("MOVE_FOLD")) {
				
		impl = new MOVE_FOLDImpl(tokens, sdb);
		timeout = 15000L;

	    }	
	    else if 
		(command.equals("GOTO")) {
		
		log("Command is a GOTO");		
		impl = new GOTOImpl(tokens, sdb);
		timeout = 180000L;
			
	    }
	    else if
		(command.equals("ROTATOR")) {

		log("Command is a ROTATOR");
		impl = new ROTATORImpl(tokens, sdb);
		timeout = 90000L;
			
	    }
	    else if
		(command.equals("SHOW")) {
		
		log("Command is a SHOW");
		impl = new SHOWImpl(tokens, sdb);
		timeout = 1000L;
	      		
	    }
	    else if
		(command.equals("TRACK")) {

		log("Command is a TRACK");
		impl = new TRACKImpl(tokens, sdb);
		timeout = 5000L;
		
	    } else if
		    (command.equals("TWEAK")) {

		    log("Command is a TWEAK");
		    impl = new TWEAKImpl(tokens, sdb);
		    timeout = 5000L;

	    } else if
		(command.equals("STOP")) {

		log("Command is a STOP");
		impl = new STOPImpl(tokens, sdb);
		timeout = 15000L;

	    }
	    else if
		(command.equals("ENCLOSURE")) {
		
		log("Command is an ENCLOSURE");
		impl = new ENCLOSUREImpl(tokens, sdb);
		timeout = 360000L;

	    }	
	    else if
		(command.equals("MIRROR_COVER")) {

		log("Command is a MIRRCOVER");
		impl = new MIRRCOVERImpl(tokens, sdb);
		timeout = 360000L;

	    } else if
		(command.equals("OPERATIONAL")) {
		
		impl = new OPERATIONALImpl(tokens, sdb);
		timeout = 600000L;
	    
	    }
	    else if
		(command.equals("WAVELENGTH")) {
		log("Command is a WAVELENGTH");
// 	    double wave = props.getDoubleValue("wavelength", -1.0);
// 	    if (wave >= 0.0)
// 		sdb.astrometry.refractionWavelength = wave;
// 	    msg = new String("OK REF-WAVELENGTH:");
// 	    tt = 400.0;
	    }
	    else if
		(command.equals("PRESSURE")) {
		log("Command is a PRESSURE");
// 	    double pressure = props.getDoubleValue("pressure", -1.0);
// 	    if (pressure >= 0.0)
// 		sdb.astrometry.refractionPressure = pressure;
// 	    msg = new String("OK REF-PRESSURE:");
// 	    tt = 400.0;
	    }
	    else if
		(command.equals("HUMIDITY")) {
		log("Command is a HUMIDITY");
		// 	    double humid = props.getDoubleValue("humidity", -1.0);
// 	    if (humid >= 0.0)
// 		sdb.astrometry.refractionHumidity = humid;
// 	    msg = new String("OK REF-HUMIDITY:");
// 	    tt = 400.0;
	    }
	    else if
		(command.equals("TEMPERATURE")) {
		log("Command is a TEMPERATURE");
// 	    double temp = props.getDoubleValue("temperature", 0.0 );
// 	    sdb.astrometry.refractionTemperature = temp;
// 	    msg = new String("OK REF-TEMPERATURE:");
// 	    tt = 400.0;
	    }
	    else if
		(command.equals("OFFBY")) {
		log("Command is a OFFBY");
// 	    double dra  = Double.parseDouble((String)props.get("offsetRA")); // degs
// 	    double ddec = Double.parseDouble((String)props.get("offsetDec"));// degs
// 	    sdb.source.srcRa  = sdb.source.srcRa + dra;
// 	    sdb.source.srcDec = sdb.source.srcDec + ddec;
// 	    log("OFFBY setting source RA/dec to: "+sdb.source.srcRa+", "+sdb.source.srcDec);
// 	    msg = new String("Server OFFBY TO: ("+sdb.source.srcRa+" "+sdb.source.srcDec+") OK " + seqno);
// 	    tt = 3000.0;
	    }
	    else if
		(command.equals("UNWRAP")) {
		log("Command is an UNWRAP");		
// 	    msg = new String("Server UNWRAP OK " + seqno);
// 	    tt = 30000.0;
	    }
	    else if
		(command.equals("DFOCUS")) {
		log("Command is a DFOCUS");	
		String strOffset = tokens.nextToken();
		double dfocus = Double.parseDouble(strOffset); // mm.
		sdb.mechanisms.focusOffset = dfocus;
		log("DFOCUS setting focus to: "+sdb.mechanisms.focusOffset+" mm.");
		msg = new String("Server DFOCUS OK " + seqno);
 	 
	    }
	    else if
		(command.equals("AGFOCUS")) {
		log("Command is a AGFOCUS");
// 	    double agfocus = Double.parseDouble((String)props.get("focus")); // mm.
// 	    sdb.autoguider.agFocusPos = agfocus;
// 	    log("AGFOCUS setting focus to: "+sdb.autoguider.agFocusPos+" mm.");
// 	    msg = new String("Server AGFOCUS OK " + seqno);
// 	    tt = 500.0;
	    }
	    else if
		(command.equals("FOCUS")) {
		log("Command is a FOCUS");

		impl = new FOCUSImpl(tokens, sdb);
		timeout = 450000L;

	    }
	    else if
		(command.equals("AGCENTROID")) {
		log("Command is an AGCENTROID");
// 	    msg =
// 		" &pixelX 0.0 "+
// 		" &pixelY 0.0 "+		
// 		" &fwhm "+sdb.autoguider.fwhm+
// 		" &guideStarMagnitude "+sdb.autoguider.guideStarMagnitude;
// 	    tt = 1500.0;
	    }
	    else if
		(command.equals("CALIBRATE")) {
		log("Command is a CALIBRATE");
// 	    msg =
// 		" &skyRms "+ (8.0 + Math.random()*20.0) ;
// 	    tt = 300000.0;

	    } else if
		(command.equals("WMS")) {
		log("Command is a WMS");
		
		impl = new WMSImpl(tokens, sdb);
		timeout = 3000L;

	    }
	    else {
		log("Command "+seqno+" is GENERIC");
	
			
	    }

	  
	
	    // ********************************************************
	    // START REPLYING NOW. THIS IS THE DONE.
	    // ********************************************************
	    
	    if (impl == null) {
		msg = "<<500001>> Unknown command: "+command;
	    } else {  

		if (impl.demand()) {
	  
		    if (exec(impl, timeout)) {
		    
			if (impl.getSuccessful()) {
			    msg = impl.getMessage();
			} else {
			    msg = "<<"+impl.getErrorNumber()+">> "+impl.getMessage();
			}

		    } else {
			msg = "<<500000>> Timed out before command completion";
			reply = TCS_Simulation.ERROR_CLASS;
		    }

		} else {
		    msg = "<<"+impl.getErrorNumber()+">> "+impl.getMessage();
		    reply = TCS_Simulation.ERROR_CLASS;
		}
		
	    }

	    // Sleep for the expected duration of the command
	    log("Reply to: "+seqno+" in "+(timeout/1000)+" secs.");
	 	
	    buff2 = new byte[28 + (msg != null ? msg.length(): 0)];
	    
	    baos = new ByteArrayOutputStream(buff2.length);
	
	    dos = new DataOutputStream(baos);
	    
	    // Pack the CIL Routing Header.
	    dos.writeInt(TCS_Simulation.TCS_ID);
	    dos.writeInt(TCS_Simulation.RCS_ID);
	    dos.writeInt(reply);
	    dos.writeInt(TCS_Simulation.SERVICE_TYPE);
	    dos.writeInt(seqno);
	    
	    // Make up a timestamp as UTC from 1980 5th Jan.
	    // Note: This should be GPS time !
	    //      - so leapsecs need subtracting somehow!!
	    
	    start = Calendar.getInstance();
	    start.set(1980,0,5,0,0,0);
	    startms = start.getTime().getTime();
	    
	    now   = Calendar.getInstance();
	    nowms   = now.getTime().getTime();	
	    
	    msecs = (nowms - startms);	
	    secs = (int)(msecs / 1000);	
	    nanos = (1000000)*(int)(msecs - (long)(1000 * secs));
	    
	    dos.writeInt(secs);
	    dos.writeInt(nanos);
	    
	    if (msg != null) {
		// Write the message string as a sequence of bytes.
		dos.writeBytes(msg);
		// Not forgetting the C/C++ string terminator.
		dos.writeByte((byte)0);
	    }

	    buff2 = baos.toByteArray();
	    
	    packet = new DatagramPacket(buff2, buff2.length, replyAddress, replyPort);
	    
	    socket.send(packet);
	    
	    log("Message (DONE) ["+seqno+"] sent to "+replyAddress+" on "+replyPort+" :: ["+msg+"]");
	    
	    
	} catch (IOException e) {
	    System.out.println("Error unpacking or sending response: " + e);
	}
	
	shutdown();
    }
    
    /** Returns true only if the command appears to have successfully completed
     * within the timeout period.
     */
    private boolean exec(TCSCommandImpl impl, long timeout) {

	long start = System.currentTimeMillis();
	long time = start;
	while (time < (start + timeout)) {
	    
	    if (impl.monitor()) {
	
		System.err.println("TCSCommandImpl:: DONE");
		return true;
	    }

	    try { Thread.sleep(2000L);} catch (InterruptedException ix) {}

	    time = System.currentTimeMillis();

	}

	return false;

    }

    /** Puts an angle (rads) into the correct (0 -> 2*PI) range.*/
    protected double correct(double angle) { 
	double a = angle + 2.0*Math.PI;
	while (a >= 2*Math.PI) a -= 2.0*Math.PI;
	return a;
    }

    protected void shutdown() {
	log("Shutting down now after: "+command);
    }
    
    public void terminate() {	
	socket.close();	
	socket = null;
    }

    public void log(String text) { System.out.println(getName()+": "+text);}

    private double toRadians(double args) {return args*Math.PI/180.0;}

    private double toDegrees(double args) {return args*180.0/Math.PI;}


}
