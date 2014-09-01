import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the TWEAK command.*/
public class TWEAKImpl extends TCSCommandImpl {

    private int state;

    public TWEAKImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. TWEAK dx dy
	if (tokens.countTokens() < 2) {
	    fail(500002, "Missing args");  
	    return false;
	}
	
	double dx =0.0;
	try {
	    dx = Double.parseDouble(tokens.nextToken());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail(50003, "Illegal value for dx");
	}

	double dy =0.0;
        try {
            dy = Double.parseDouble(tokens.nextToken());
        } catch (Exception e) {
            e.printStackTrace();
            fail(50003, "Illegal value for dy");
        }

	return true;

    }

    public boolean monitor() {

	try {Thread.sleep(500);} catch (InterruptedException e) {}

	return true;

    }

}
