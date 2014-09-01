import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the TRACK command.*/
public class TRACKImpl extends TCSCommandImpl {

    public static final int AZIMUTH = 1;
    public static final int ALTITUDE = 2;
    public static final int ROTATOR = 3;

    public static final int ON =1;
    public static final int OFF =2;

    private int mech;

    private int state;

    private Axes axes;

    public TRACKImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. TRACK <mech> <ON| OFF>
	if (tokens.countTokens() < 2) {
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

	String strstate = tokens.nextToken();

	if ("ON".equals(strstate)) {
	    state = ON;
	} else if
	    ("OFF".equals(strstate)) {
	    state = OFF;
	} else {
	    fail(500002, "Illegal state: "+strstate);  
	    return false;
	}

	switch (state) {
	case ON:
	    switch (mech) {
	    case AZIMUTH:
		Subsystems.azm.setTrackingEnabled(true);
		Subsystems.azm.follow();
		break;
	    case ALTITUDE:
		Subsystems.alt.setTrackingEnabled(true);
		Subsystems.alt.follow();
		break;
	    case ROTATOR:
		Subsystems.rot.setTrackingEnabled(true);
		Subsystems.rot.follow();
		break;
	    }
	    break;
	case OFF:
	    switch (mech) {
	    case AZIMUTH:
		Subsystems.azm.setTrackingEnabled(false);
		break;
	    case ALTITUDE:
		Subsystems.alt.setTrackingEnabled(false);
		break;
	    case ROTATOR:
		Subsystems.rot.setTrackingEnabled(false);
		break;
	    }
	    break;
	}
	
	return true;

    }

    /** This is just a flag switch on the axis ?.*/
    public boolean monitor() {

	return true;

    }

}
