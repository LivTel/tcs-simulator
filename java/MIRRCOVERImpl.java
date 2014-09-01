import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the ENCLOSURE command.*/
public class MIRRCOVERImpl extends TCSCommandImpl {

    private int state;

    public MIRRCOVERImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. MIRRCOVER OPEN/CLOSE
	if (tokens.countTokens() < 1) {
	    fail(500002, "Missing args");  
	    return false;
	}
	
	String strstate = tokens.nextToken();

	if ("OPEN".equals(strstate)) {
	    state = TCS_Status.POSITION_OPEN;
	    Subsystems.pmc.open();
	} else if
	    ("CLOSE".equals(strstate)) {
	    state = TCS_Status.POSITION_CLOSED;
	    Subsystems.pmc.close();
	}

	return true;

    }

    public boolean monitor() {

	switch (state) {
	case TCS_Status.POSITION_OPEN:
	    return (sdb.mechanisms.primMirrorCoverPos == TCS_Status.POSITION_OPEN);	
	case TCS_Status.POSITION_CLOSED:
	    return (sdb.mechanisms.primMirrorCoverPos == TCS_Status.POSITION_CLOSED);
	}
	
	return false;

    }

}
