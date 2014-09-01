import ngat.message.base.*;
import ngat.message.SMS.*;
import ngat.util.*;
import ngat.net.*;

public class SmsCommandSender {

    String smsHost;
    int smsPort;

    public SmsCommandSender(String smsHost, int smsPort) {
	this.smsHost = smsHost;
	this.smsPort = smsPort;
    }

    public static void main(String args[]) {

	try {
	    
	    ConfigurationProperties cfg = CommandTokenizer.use("--").parse(args);
	    String smsHost = cfg.getProperty("sms-host");
	    int smsPort = cfg.getIntValue("sms-port");

	    SmsCommandSender sender = new SmsCommandSender(smsHost, smsPort);


	    if (cfg.getProperty("command").equals("schedule")) {
		
		sender.sendSchedule();

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    private COMMAND_DONE sendSchedule() {

	SCHEDULE_REQUEST sched = new SCHEDULE_REQUEST("test");
	
	return sendSmsCommand(sched, 300000L);

    }

    /** Sends an SMS command. This call will block for timeout.
     * @return The reply to the command.
     */
    private COMMAND_DONE sendSmsCommand(COMMAND command, long timeout) {
	System.err.println("SendSmsCommand: Sending: "+command);
	
	IConnection smsConnect = new SocketConnection(smsHost, smsPort);
	SmsHandler  handler    = new SmsHandler(command, timeout);
	JMSMA_ProtocolClientImpl impl = new JMSMA_ProtocolClientImpl(handler, smsConnect);
	
	impl.implement();
	
	return handler.getDone();
	
    }

    class SmsHandler extends JMSMA_ClientImpl {
	
	private COMMAND_DONE done;
	
	SmsHandler(COMMAND command, long timeout) {
	    super();
	    this.command = command;
	    this.timeout = timeout;
	}
	
	public void failedConnect(Exception e) {
	    e.printStackTrace();
	    done = failed(7501, "Failed to connect to SMS@"+smsHost+":"+smsPort+" for command: "+command);
	}
	
	public void failedDespatch(Exception e) {
	    e.printStackTrace();
	    done = failed(7502, "Failed to despatch command: "+command);
	}
	
	public void failedResponse(Exception e) {
	    System.err.println("ICS: Failed response to command: "+command);
	    e.printStackTrace();
	    done = failed(7503, "Failed to receive reply");
	}
	
	public void exceptionOccurred(Object source, Exception e) {
	    System.err.println("ICS: exceptionOccurred: "+source+" : "+e);
	    e.printStackTrace();
	    done = failed(7504, "Exception occurred "+source+" : "+e);
	}
	
	public void handleAck(ACK ack) {
	    System.err.println("ICS: Ack:"+ack);
	}
	
	public void handleDone(COMMAND_DONE done) {
	    System.err.println("ICS: Done: "+done);
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

