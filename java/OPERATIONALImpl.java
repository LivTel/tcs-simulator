import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the OPERATIONAL command.*/
public class OPERATIONALImpl extends TCSCommandImpl {

    private int state;

    public OPERATIONALImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. ENC OPEN/CLOSE
	if (tokens.countTokens() < 1) {
	    fail(500002, "Missing args");  
	    return false;
	}
	
	String strstate = tokens.nextToken();

	if ("ON".equals(strstate)) {

	    if (sdb.state.networkControlState == TCS_Status.STATE_DISABLED) {
		fail(500008, "Command not allowed in present system state: NET_DIS");
		return false;
	    }

	    state = TCS_Status.STATE_ON;

	    Subsystems.mcp.requestOperOn();
	} else if
	    ("OFF".equals(strstate)) {
	    state = TCS_Status.STATE_OFF;
	    Subsystems.mcp.requestOperOff();
	} else {
	    fail(500003, "Illegal state: "+strstate);
	    return false;
	}

	return true;

    }
 
    public boolean monitor() {

	switch (state) {
	case TCS_Status.STATE_ON:
	    
	    return Subsystems.mcp.operOnOkay();
	       
	case TCS_Status.STATE_OFF: 
	    
	    return Subsystems. mcp.operOffOkay();

	}
	
	return false;
	
    }

}
