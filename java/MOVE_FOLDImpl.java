import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the MOVE_FOLD command.*/
public class MOVE_FOLDImpl extends TCSCommandImpl {

    private int state;

    public MOVE_FOLDImpl(StringTokenizer tokens, TCS_Status sdb) {
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

	if ("STOW".equals(strstate)) {
	    state = TCS_Status.POSITION_STOWED;
	    Subsystems.scf.moveto(state);	    
	}  else if ("1".equals(strstate)) {
	    state = TCS_Status.POSITION_PORT_1;
	    Subsystems.scf.moveto(state); 
	}  else if ("2".equals(strstate)) {
	    state = TCS_Status.POSITION_PORT_2;
	    Subsystems.scf.moveto(state);
	}  else if ("3".equals(strstate)) {
	    state = TCS_Status.POSITION_PORT_3;
	    Subsystems.scf.moveto(state);
	}  else if ("4".equals(strstate)) {
	    state = TCS_Status.POSITION_PORT_4;
	    Subsystems.scf.moveto(state);
	}  else if ("5".equals(strstate)) {
            state = TCS_Status.POSITION_PORT_5;
            Subsystems.scf.moveto(state);
        }  else if ("6".equals(strstate)) {
            state = TCS_Status.POSITION_PORT_6;
            Subsystems.scf.moveto(state);
        }  else if ("7".equals(strstate)) {
            state = TCS_Status.POSITION_PORT_7;
            Subsystems.scf.moveto(state);
        }  else if ("8".equals(strstate)) {
            state = TCS_Status.POSITION_PORT_8;
	    Subsystems.scf.moveto(state);	    
	} else {
	    fail(500001, "Illegal state: ["+strstate+"]");
	    return false;
	}

	return true;

    }

    /** True if the fold is in the demand position.*/
    public boolean monitor() {
  
	return sdb.mechanisms.foldMirrorPos == state;

    }
    
}
