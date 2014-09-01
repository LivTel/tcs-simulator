import ngat.message.base.*;
import ngat.message.ISS_INST.*;
import ngat.util.*;
import ngat.net.*;
import ngat.fits.*;
import java.util.*;

public class IssCommandSender {

    String issHost;
    int issPort;

    long start;

    public IssCommandSender(String issHost, int issPort) {
	this.issHost = issHost;
	this.issPort = issPort;
    }

    public static void main(String args[]) {

	try {
	    
	    ConfigurationProperties cfg = CommandTokenizer.use("--").parse(args);
	    String issHost = cfg.getProperty("iss-host");
	    int issPort = cfg.getIntValue("iss-port");

	    IssCommandSender sender = new IssCommandSender(issHost, issPort);

	    String cmd = cfg.getProperty("command");
	    if (cmd.equals("move-fold")) {
		int port = cfg.getIntValue("port");

		sender.sendMoveFold(port);

	    } else if 
		  (cmd.equals("get-fits")) {

		COMMAND_DONE done = sender.sendGetFits();
		System.err.println("Received: "+done);
		Vector v = ((GET_FITS_DONE)done).getFitsHeader();
		Iterator ih = v.iterator();
		while (ih.hasNext()) {
		    FitsHeaderCardImage fits = (FitsHeaderCardImage)ih.next();
		    System.err.println("FITS ["+fits.toString()+"]");
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    private COMMAND_DONE sendMoveFold(int port) {

	MOVE_FOLD movefold = new MOVE_FOLD("test");
	movefold.setMirror_position(port);
	
	return sendIssCommand(movefold, 300000L);

    }

     private COMMAND_DONE sendGetFits() {

	 GET_FITS get = new GET_FITS("test");
	
	
	return sendIssCommand(get, 10000L);

    }

    /** Sends an ISS command. This call will block for timeout.
     * @return The reply to the command.
     */
    private COMMAND_DONE sendIssCommand(COMMAND command, long timeout) {
	System.err.println("SendIssCommand: Sending: "+command);
	
	IConnection issConnect = new SocketConnection(issHost, issPort);
	IssHandler  handler    = new IssHandler(command, timeout);
	JMSMA_ProtocolClientImpl impl = new JMSMA_ProtocolClientImpl(handler, issConnect);
	
	start = System.currentTimeMillis();

	impl.implement();
	
	return handler.getDone();
	
    }

    class IssHandler extends JMSMA_ClientImpl {
	
	private COMMAND_DONE done;
	
	IssHandler(COMMAND command, long timeout) {
	    super();
	    this.command = command;
	    this.timeout = timeout;
	}
	
	public void failedConnect(Exception e) {
	    long time = System.currentTimeMillis()-start;
	    e.printStackTrace();
	    done = failed(7501, "ICS: ["+(time/1000)+"] Failed to connect to ISS@"+issHost+":"+issPort+" for command: "+command);
	}
	
	public void failedDespatch(Exception e) {
	    long time = System.currentTimeMillis()-start;
	    e.printStackTrace();
	    done = failed(7502, "ICS: ["+(time/1000)+"] Failed to despatch command: ["+command);
	}
	
	public void failedResponse(Exception e) {
	    long time = System.currentTimeMillis()-start;
	    System.err.println("ICS: ["+(time/1000)+"] Failed response to command: "+command);
	    e.printStackTrace();
	    done = failed(7503, "Failed to receive reply");
	}
	
	public void exceptionOccurred(Object source, Exception e) {
	    long time = System.currentTimeMillis()-start;
	    System.err.println("ICS: ["+(time/1000)+"] exceptionOccurred: "+source+" : "+e);
	    e.printStackTrace();
	    done = failed(7504, "Exception occurred "+source+" : "+e);
	}
	
	public void handleAck(ACK ack) {
	    long time = System.currentTimeMillis()-start;
	    System.err.println("ICS: ["+(time/1000)+"] Ack:"+ack.getId()+" to="+ack.getTimeToComplete());
	}
	
	public void handleDone(COMMAND_DONE done) { 
	    long time = System.currentTimeMillis()-start;
	    System.err.println("ICS: ["+(time/1000)+"] Done: "+done.getId()+(done.getSuccessful() ? 
							   " DONE" : 
							   " FAILED: "+done.getErrorNum()+" : "+done.getErrorString()));
	    
	    this.done = done;
	}
	
	public void sendCommand(COMMAND command) {}
	
	public COMMAND_DONE getDone() {
	    return done;
	}
	
	private COMMAND_DONE failed(int code, String msg) {
	    COMMAND_DONE done = new COMMAND_DONE("Fail");
	    done.setSuccessful(false);
	    done.setErrorNum(code);
	    done.setErrorString(msg);
	    return done;
	}
	

    }

}

