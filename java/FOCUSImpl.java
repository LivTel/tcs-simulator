import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the FOCUS command.*/
public class FOCUSImpl extends TCSCommandImpl {

    private Focus focus;

    private double position;

    public FOCUSImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. position
	if (tokens.countTokens() < 1) {
	    fail(500002, "Missing args");  
	    return false;
	}
	
	try {
	    position = Double.parseDouble(tokens.nextToken());
	} catch (Exception e) {
	    fail(500003, "Error during parsing: "+e);
	    e.printStackTrace();
	    return false;
	}

	Subsystems.smf.move(position);

	return true;
    
    }

    public boolean monitor() {

	double ddfoc  = Math.abs(sdb.mechanisms.secMirrorDemand  - sdb.mechanisms.secMirrorPos);

	if (ddfoc < 0.5)
	    return true;

	return false;

    }

}
