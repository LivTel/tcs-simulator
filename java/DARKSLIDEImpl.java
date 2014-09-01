import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the DARKSLIDE command.*/
public class DARKSLIDEImpl extends TCSCommandImpl {

    public DARKSLIDEImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {
	// Args.
	if (tokens.countTokens() < 1) {
	    fail(500001, "Missing args");
	    return false;
	}

	String state = tokens.nextToken();

	if ("OPEN".equals(state)) 
	    ;
	else if ("CLOSE".equals(state))
	    ;   
	else {
	    fail(500001, "Illegal state: ["+state+"]");
	    return false;
	}

	return true;

    }

    public boolean monitor() {

	return true;

    }
    
}
