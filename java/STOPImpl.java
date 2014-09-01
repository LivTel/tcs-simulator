import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the STOP command.*/
public class STOPImpl extends TCSCommandImpl {

    public static final int AZIMUTH = 1;
    public static final int ALTITUDE = 2;
    public static final int ROTATOR = 3;

    private int mech;

    public STOPImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. STOP <mech> 
	if (tokens.countTokens() < 1) {
	    fail(500002, "Missing args");  
	    return false;
	}
	
	String strmech = tokens.nextToken();

	if ("AZIMUTH".equals(strmech)) {
	    mech = AZIMUTH;
	} else if
	    ("ALTITUDE".equals(strmech)) {
	    mech = ALTITUDE;
	} else if
	    ("ROTATOR".equals(strmech)) {
	    mech = ROTATOR;
	} else {
	    fail(500002, "Illegal mech: "+strmech);  
	    return false;
	}

	switch (mech) {
	case AZIMUTH:
	    Subsystems.azm.halt();
	    break;
	case ALTITUDE:
	    Subsystems.alt.halt();
	    break;
	case ROTATOR:
	    Subsystems.rot.halt();
	    break;
	}
		
	return true;

    }

    /** This is just a flag switch on the axis ?.*/
    public boolean monitor() {

	if (sdb.mechanisms.azStatus == TCS_Status.MOTION_STOPPED &&
	    sdb.mechanisms.altStatus == TCS_Status.MOTION_STOPPED &&
	    sdb.mechanisms.rotStatus == TCS_Status.MOTION_STOPPED)
	    return true;
	
	return false;

    }

}
