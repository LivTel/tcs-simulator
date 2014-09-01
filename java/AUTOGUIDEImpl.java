import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the AUTOGUIDE command.*/
public class AUTOGUIDEImpl extends TCSCommandImpl {

    String state;

    public AUTOGUIDEImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {
	// Args.
	if (tokens.countTokens() < 1) {
	    fail(500001, "Missing args");
	    return false;
	}

	state = tokens.nextToken();

	if ("ON".equals(state)) 
	    Subsystems.ag.acquire();
	else if ("OFF".equals(state))
	    Subsystems.ag.stopGuiding();
	else if ("FAIL".equals(state))
	    sdb.autoguider.agSwState = TCS_Status.STATE_FAILED;
	else {
	    fail(500001, "Illegal state: ["+state+"]");
	    return false;
	}

	return true;

    }

    public boolean monitor() {

	if ("OFF".equals(state)) {
	    if (sdb.autoguider.agStatus == TCS_Status.AG_UNLOCKED)
		return true;
	    return false;
	} else if
	    ("ON".equals(state)) {
	    if (sdb.autoguider.agStatus == TCS_Status.AG_LOCKED)
                return true;
            return false;
	}
	return false;

    }
    
}
