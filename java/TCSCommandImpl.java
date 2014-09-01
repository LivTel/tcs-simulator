import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Base class for TCS Command implementors.*/
public abstract class TCSCommandImpl {

    protected int errorNumber;

    protected String message;

    protected boolean success;

    /** Telescope status information.*/
    protected TCS_Status sdb;

    /** Tokenized command arguments.*/
    protected StringTokenizer tokens;
    
    /** Create a TCSCommandImpl with the given parameters:-
     * @param tokens    Tokenized command arguments.
     * @param sdb Telescope status information.
     */
    public TCSCommandImpl(StringTokenizer tokens, TCS_Status sdb) {
	this.tokens = tokens;
	this.sdb    = sdb;
    }

    /** Set demands based on parameters. If fail then set errorNumber and message.
     * Return false if failed to set demands.
     */
    public abstract boolean demand();

    /** Return true only when the conditions have been met.*/
    public abstract boolean monitor();

    /** Return the completion message or error message.*/
    public String getMessage() { return message; }

    /** Return an error number if applicable (else zero).*/
    public int getErrorNumber() { return errorNumber; }

    /** Return true if this command succeeded.*/
    public boolean getSuccessful() { return success; }

    /** Set the state to failed.*/
    protected void fail(int errorNumber, String message) {
	this.errorNumber = errorNumber;
	this.message = message;
	this.success = false;
    }

    protected void setSuccess(String message) {
	this.success = true;
	this.message = message;
    }


}
