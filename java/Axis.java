import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

public abstract class Axis  extends ControlThread {

    public static final int ERROR_STATE       = 1;

    public static final int BOOTING_STATE     = 2;

    public static final int OFFLINE_STATE     = 3;

    public static final int HOMING_STATE      = 4;

    public static final int STOPPED_STATE     = 5;

    public static final int FOLLOWING_STATE   = 6;

    public static final int IN_POSITION_STATE = 7;

    /*** Indicates whether tracking has been enabled for this axis.*/
    boolean trackingEnabled = false;

    boolean stick = false;

    protected double slewRate = 2.0; // deg/sec

    /** Telescope status block.*/
    TCS_Status sdb;

    /** Update interval (ms).*/
    long interval;

    /** Combined Axis drive and controller state.*/
    protected int state;

    protected double  bootProgress = 0.0;
    protected long    bootTime = 60000L;
    protected boolean bootRequested = false;

    protected double  homingProgress = 0.0;
    protected long    homingTime = 100000L;
    protected boolean homingRequested = false;

    protected boolean followRequested = false;

    protected boolean failRequested = false;
    protected boolean warnRequested = false;

    protected boolean stopRequested = false;

    protected double minAcquireDiff;
    protected double minTrackingDiff;

    protected double speed = slewRate; // degs/sec

    protected double lastDemand = 0.0;

    protected double demandRate = 0.0;

    public Axis(String name, TCS_Status sdb, long interval) {
	super(name, true);
	this.sdb = sdb;
	this.interval = interval;

	// If slewing then we need to 'catch' the axis as it gets close to the demand
	// position and does not overshoot - i.e. need largish box ~ 1.5*slewrate*interval.
	// This will not be so good for tracking...
	minAcquireDiff  = 3.0*interval/1000.0;
	minTrackingDiff = 0.05;

	state = OFFLINE_STATE;
	setStatus(TCS_Status.MOTION_OFF_LINE);

    }

    /** Make the axis stick.*/
    public void setStick(boolean stick) {
	this.stick = stick;
	System.err.println(getName()+"::"+(stick ? "STICKING axis" : "UNSTICKING axis"));
    }
    

    protected void initialise() {}

    protected void mainTask() {

	try { Thread.sleep(interval); } catch (InterruptedException ix) {System.err.println(getName()+"::interrupted");}

	System.err.println(getName()+"::Current state: "+toStateString(state));

	// Always..
	if (failRequested) {

	    failRequested = false;
	    state = ERROR_STATE;
	    setStatus(TCS_Status.STATE_ERROR);
	}

	if (warnRequested) {
	    setStatus(TCS_Status.MOTION_WARNING);
	    return;
	}
	
	switch (state) {
	case ERROR_STATE:
	    if (bootRequested) {
		bootProgress = 0.0;
		state = BOOTING_STATE;
	    }
	    break;
	case BOOTING_STATE:
	    if (bootProgress >= 1.0) {
		bootRequested = false;
		state = OFFLINE_STATE;
	    } else {
		bootProgress += interval/(double)bootTime;
	    }
	    break;
	case OFFLINE_STATE:
	    if (homingRequested) {
		homingProgress = 0.0;
		state = HOMING_STATE;
	    }
	    setStatus(TCS_Status.MOTION_OFF_LINE);
	    break;
	case HOMING_STATE:
	 if (homingProgress >= 1.0) {
	     homingRequested = false;
	     state = STOPPED_STATE;
	    } else {
		homingProgress += interval/(double)bootTime;
	    }	
	    break;
	case STOPPED_STATE:
	    if (followRequested) {
		state = FOLLOWING_STATE;
	    } 
	    setStatus(TCS_Status.MOTION_STOPPED);
	    break;
	case FOLLOWING_STATE:

	    if (stopRequested) {
		stopRequested = false;
		setStatus(TCS_Status.MOTION_STOPPED);
		state = STOPPED_STATE;

	    } else {

		double diff = Math.abs(getDemand() - getCurrent());
		
		if ( diff < minTrackingDiff ) {
		    
		    if (trackingEnabled) {
			setStatus(TCS_Status.MOTION_TRACKING);
			advance();
		    } else {
			followRequested = false;
			setStatus(TCS_Status.MOTION_INPOSITION);
			state = IN_POSITION_STATE;
		    }
		} else {
		    
		    setStatus(TCS_Status.MOTION_MOVING);		
		    advance();
		    
		} 
	    }

	    break;
	case IN_POSITION_STATE:
	    if (followRequested) {
		state = FOLLOWING_STATE;
	    } else {

	    }	    
	    break;
	}


    }

   protected void shutdown() {}

    /** Set whether tracking is enabled.*/
    public void setTrackingEnabled(boolean on) {
	trackingEnabled = on;
    }

    public boolean isTrackingEnabled() { 
	return trackingEnabled;
    }

    /** Return true and initiate boot if feasible.*/
    public boolean boot() {
	if (state == ERROR_STATE ||
	    state == BOOTING_STATE) 
	    bootRequested = true;
	else 
	    bootRequested = false;

	return bootRequested;	
    }

    /** Return true and initiate homing if feasible.*/
    public boolean home() {
	if (state == OFFLINE_STATE ||
	    state == HOMING_STATE) 
	    homingRequested = true;
	else
	    homingRequested = false;
	
	return homingRequested;
    }

    /** Return true and initiate slew to position.*/
    public boolean follow() {
	if (state == FOLLOWING_STATE ||
	    state == IN_POSITION_STATE ||
	    state == STOPPED_STATE)
	    followRequested = true;
	else
	    followRequested = false;

	stick = false;

	return followRequested;
    }

    /** Initiate stop.*/
    public boolean halt() { 
	followRequested = false;
	stopRequested = true;
	return stopRequested;
    }

    /** Initiate failure.*/
    public void fail() {
	failRequested = true;
    }

     /** Initiate warning.*/
    public void warning() {
	warnRequested = true;
    }



    /** Return the state description.*/
    public String toStateString(int state) {
	switch (state) {
	case ERROR_STATE:
	    return "ERROR";
	case BOOTING_STATE:
	    return "BOOTING";
	case OFFLINE_STATE:
	    return "OFFLINE";
	case HOMING_STATE:
	    return "HOMING";
	case STOPPED_STATE:
	    return "STOPPED";
	case FOLLOWING_STATE:
	    return "FOLLOWING";
	case IN_POSITION_STATE:
	    return "IN_POSITION";
	}
	return "UNKNOWN";
    }


    /** Set the current position in sdb.*/
    protected abstract void setCurrent(double position);

    /** Get current position from sdb.*/
    public abstract double getCurrent();

    /** Get demand position from sdb.*/
    public abstract double getDemand();

    /** Set visible status in sdb.*/
    protected abstract void setStatus(int status);

    /** Time step forward.*/
    protected abstract void advance();

}

