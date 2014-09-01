import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the AGFILTER command.*/
public class AGFILTERImpl extends TCSCommandImpl {

    private int state;

    public AGFILTERImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {
	// Args.
	if (tokens.countTokens() < 1) {
	    fail(500001, "Missing args");
	    return false;
	}

	String strstate = tokens.nextToken();

	if ("OUT".equals(strstate)) {
	    state = TCS_Status.POSITION_RETRACT;
	    Subsystems.agf.out();	    
	} else if ("IN".equals(strstate)) {
	    state = TCS_Status.POSITION_INLINE;
	    Subsystems.agf.in();
	} else if ("FAIL".equals(strstate)) {
	    Subsystems.agf.fail();
	}  else if ("NORMAL".equals(strstate)) {
	    Subsystems.agf.normal();
	} else {
	    fail(500001, "Illegal state: ["+state+"]");
	    return false;
	}

	return true;

    }

    public boolean monitor() {
	
	return (sdb.autoguider.agFilterPos == state);	

    }
    
}
