import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the TRACK command.*/
public class ROTATORImpl extends TCSCommandImpl {

    /** Rotator mode.*/
    private int mode;

    /** Requested angle (mount or sky) (rads).*/
    private double angle;

    public ROTATORImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. TRACK <mech> <ON| OFF>
	if (tokens.countTokens() < 1) {
	    fail(500002, "Missing args");  
	    return false;
	}
	
	// Extract the mode.
	String strmode = tokens.nextToken();

	if ("MOUNT".equals(strmode)) {
	    mode = TCS_Status.ROT_MOUNT;
	} else if
	    ("SKY".equals(strmode)) {
	    mode = TCS_Status.ROT_SKY;
	} else if
	    ("FLOAT".equals(strmode)) {
	    mode = TCS_Status.ROT_FLOAT;
	} else if
	    ("VFLOAT".equals(strmode)) {
	    mode = TCS_Status.ROT_VFLOAT;
	} else if
	    ("VERTICAL".equals(strmode)) {
	    mode = TCS_Status.ROT_VERTICAL;
	} else if
	      ("STICK".equals(strmode)) {
	    Subsystems.rot.setStick(true);
	    return true;
	} else if
	      ("UNSTICK".equals(strmode)) {
	    Subsystems.rot.setStick(false);
            return true;
	} else if 
	      ("WARNING".equals(strmode)) {
	    Subsystems.rot.warning();
	} else {
	    fail(500002, "Illegal mode: "+strmode);  
	    return false;
	} 

	// Extract the angle if applicable
	switch (mode) {
	case TCS_Status.ROT_MOUNT:  
	case TCS_Status.ROT_SKY:
	    String strangle = tokens.nextToken();

	    try {
		angle = Math.toRadians(Double.parseDouble(strangle));	
	    } catch (Exception e) {
		fail(500003, "Angle format: "+strangle);
		return false;
	    }
	    break;
	}

	// Set mode, tracking and angle.

	// ### CHECK THESE THEY MAY NOT BE QUITE CORRECT ####

	switch (mode) {
	case TCS_Status.ROT_MOUNT:   
	    Subsystems.rot.setTrackingEnabled(false);  
	    Subsystems.ast.rotate(mode, angle);
	    break;
	case TCS_Status.ROT_SKY:
	    Subsystems.rot.setTrackingEnabled(true);
	    Subsystems.ast.rotate(mode, angle);
	      break;
	case TCS_Status.ROT_FLOAT:
	    Subsystems.rot.setTrackingEnabled(true);
	    break;
	case TCS_Status.ROT_VFLOAT:
	    Subsystems.rot.setTrackingEnabled(true);
	    Subsystems.ast.rotate(mode, 0.0);
	    break;
	case TCS_Status.ROT_VERTICAL:
	    Subsystems.rot.setTrackingEnabled(false);
	    Subsystems.ast.rotate(mode, 0.0);	    
	    break;
	}

	return true;

    }

    public boolean monitor() {

	double ddrot = Math.abs(sdb.mechanisms.rotDemand - sdb.mechanisms.rotPos);

	System.err.println("ROT:Mon::Delta: "+ddrot);
	
	if (ddrot < 3.0)
	    return true;
	  
	return false;

    }

}
