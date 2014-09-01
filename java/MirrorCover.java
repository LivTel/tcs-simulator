import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

/** Simulate the telescope Mirror Cover.*/
public class MirrorCover extends ControlThread {

    /** Telescope status block.*/
    private TCS_Status telescope;
    
    /** Update interval (ms).*/
    private long interval;

    /** Mirror Cover deployment position from closed state: 1.0 = OPEN, 0.0 = CLOSED.*/
    private double position;

    /** Rate of closing u/s.*/
    private double closeRate = 0.1;

    /** Rate of opening u/s.*/
    private double openRate = 0.1;

    private boolean opening;

    /** Create an MirrorCover with given update interval.
     * @param telescope Telescope status block.
     * @param interval Update interval (ms).
     */
    public MirrorCover(TCS_Status telescope, long interval) {
	super("PMC", true);

	this.telescope = telescope;
	this.interval = interval;

	position = 0.0; //closed

    }

    
    public void close() {

	telescope.mechanisms.primMirrorCoverDemand = TCS_Status.POSITION_CLOSED;
	System.err.println("PMC::Request: CLOSE");
    }

    public void open() {

	telescope.mechanisms.primMirrorCoverDemand = TCS_Status.POSITION_OPEN;
	System.err.println("PMC::Request: OPEN");
    }


    protected void initialise() {}

    protected void mainTask() {

	double dp = 0.0;

	try { Thread.sleep(interval); } catch (InterruptedException ix) {System.err.println("PMC::interrupted");}

	System.err.println("PMC::Update:Position: "+position);

	switch (telescope.mechanisms.primMirrorCoverDemand) {
	case TCS_Status.POSITION_CLOSED:
	    System.err.println("PMC: Direction: CLOSE");

	    dp = closeRate*interval/1000.0;

	    if (position < 2.0*dp) {
		position = 0.0;
		telescope.mechanisms.primMirrorCoverStatus = TCS_Status.MOTION_INPOSITION;
		telescope.mechanisms.primMirrorCoverPos    = TCS_Status.POSITION_CLOSED;
		System.err.println("PMC:State: CLOSED");
	    } 
	       
	    if (position > 2.0*dp) {
		position -= dp;
		telescope.mechanisms.primMirrorCoverStatus = TCS_Status.MOTION_MOVING;
		telescope.mechanisms.primMirrorCoverPos    = TCS_Status.POSITION_PARTIAL;
		System.err.println("PMC:State: CLOSING");
	    }
	    break;
	case TCS_Status.POSITION_OPEN:
	    System.err.println("PMC: Direction: OPEN");

	    dp = openRate*interval/1000.0;

	    if (position > 1.0 - 2.0*dp) {
		position = 1.0;
		telescope.mechanisms.primMirrorCoverStatus = TCS_Status.MOTION_INPOSITION;
		telescope.mechanisms.primMirrorCoverPos    = TCS_Status.POSITION_OPEN;
		System.err.println("PMC:State: OPEN");
	    } 
	       
	    if (position < 1.0 - 2.0*dp) {
		position += dp;
		telescope.mechanisms.primMirrorCoverStatus = TCS_Status.MOTION_MOVING;
		telescope.mechanisms.primMirrorCoverPos    = TCS_Status.POSITION_PARTIAL;
		System.err.println("PMC:State: OPENING");
	    }
	    break;
	}
	
    }

    protected void shutdown() {}		     

  
}
