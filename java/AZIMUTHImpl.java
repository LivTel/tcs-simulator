import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the AZIMUTH command.*/
public class AZIMUTHImpl extends TCSCommandImpl {

    public static final int BOOT = 1;
    public static final int HOME = 2;
    public static final int DEHOME = 3;
    public static final int FAIL = 4;
    public static final int MOVE = 5;
    
    private int type;

    private double position;

    public AZIMUTHImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. 
	if (tokens.countTokens() < 1) {
	    fail(500002, "Missing args");  
	    return false;
	}
	
	String strtype = tokens.nextToken();

	if ("HOME".equals(strtype)) {
	    
	    if (! Subsystems.azm.home()) {
		fail(500004, "Unable to home AZM in present state");
		return false;
	    }
	    type = HOME;
	    return true;

	} else if
	    ("BOOT".equals(strtype)) {
	   
	    if (! Subsystems.azm.boot()) {
		fail(500004, "Unable to boot AZM in present state");
		return false;
	    }
	    type = BOOT;
	    return true;

	} else if
	    ("DEHOME".equals(strtype)) {
	    
	    //  Subsystems.azm.dehome();
	    // 	    type = DEHOME;
	    return true;

	} else if
	    ("FAIL".equals(strtype)) {
	    
	    Subsystems.azm.fail();
	    type = FAIL;
	    return true;
	    
	} else {
	    
	    try {
		
		if (sdb.state.telescopeState != TCS_Status.STATE_OKAY ||
		    sdb.state.networkControlState != TCS_Status.STATE_ENABLED) {
		    fail(500005, "Command not allowed in present system state");
		    return false;
		}
		
		position = Double.parseDouble(strtype);

		Subsystems.ast.goAzimuth(Math.toRadians(position));
		
		type = MOVE;
	    } catch (Exception e) {
		e.printStackTrace();
		fail(500001, "Error parsing: "+e);
		return false;
	    }
	    return true;

	}


    }

    public boolean monitor() {

	switch (type) {
	case BOOT:
	    return true;
	case HOME:
	    return true;
	case FAIL:
	    return true;
	case MOVE:
	    double dd = Math.abs(sdb.mechanisms.azPos - sdb.mechanisms.azDemand);
	    if (dd < 3.0)
		return true;
	    return false;
	}

	return false;
    }

}
