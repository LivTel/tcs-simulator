import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

/** Simulate the telescope ScienceFold.*/
public class ScienceFold extends ControlThread {

    /** Telescope status block.*/
    private TCS_Status telescope;
    
    /** Update interval (ms).*/
    private long interval;

    /** ScienceFold deployment position from STOWED = 0 to port = N.*/
    private double position;

    /** ScienceFold deployment demand from STOWED = 0 to port = N.*/
    private double demand;
    

    /** Rate of motion u/s.*/
    private double rate = 0.2;

    /** Flag to indicate travel up.*/
    private boolean up;

    /** Create a ScienceFold with given update interval.
     * @param telescope Telescope status block.
     * @param interval Update interval (ms).
     */
    public ScienceFold(TCS_Status telescope, long interval) {
	super("SCF", true);

	this.telescope = telescope;
	this.interval = interval;

	position = 0.0; //stowed
	demand = 0.0;
    }

    
    public void moveto(int port) { // these are TCS codes...

	telescope.mechanisms.foldMirrorDemand = port;
	switch (port) {
	case TCS_Status.POSITION_STOWED:
	    demand = 0.0;
	    break;
	case TCS_Status.POSITION_PORT_1:
	    demand = 1.0;
	    break;
	case TCS_Status.POSITION_PORT_2:
	    demand = 2.0;
	    break;
	case TCS_Status.POSITION_PORT_3:
	    demand = 3.0;
	    break;
	case TCS_Status.POSITION_PORT_4:
	    demand = 4.0;
	    break;
	case TCS_Status.POSITION_PORT_5:
            demand = 5.0;
            break;
        case TCS_Status.POSITION_PORT_6:
            demand = 6.0;
            break;
        case TCS_Status.POSITION_PORT_7:
            demand = 7.0;
            break;
        case TCS_Status.POSITION_PORT_8:
            demand = 8.0;
            break;
	}
    }

   

    protected void initialise() {}

    protected void mainTask() {
	
	try { Thread.sleep(interval); } catch (InterruptedException ix) {System.err.println("AGF::interrupted");}
	
	double dp = rate*interval/1000.0;
	double error = Math.abs(position-demand);	
	boolean inposn = false;
	System.err.println("SCF::Update:Position: "+position+", Demand: "+demand+" Error: "+error);
	
	// In position 
	if (error < 2.0*dp) {
	    position = demand;
	    inposn = true;
	    telescope.mechanisms.foldMirrorStatus = TCS_Status.MOTION_INPOSITION;
	    telescope.mechanisms.foldMirrorPos    = telescope.mechanisms.foldMirrorDemand;
	} else {
	    // still moving
	
	    if (position > demand) {   
		// moving left
		position -= dp;
		telescope.mechanisms.foldMirrorStatus = TCS_Status.MOTION_MOVING;
		telescope.mechanisms.foldMirrorPos    = TCS_Status.POSITION_PARTIAL;
	    } else {
		// moving right
		position += dp;
		telescope.mechanisms.foldMirrorStatus = TCS_Status.MOTION_MOVING;
		telescope.mechanisms.foldMirrorPos    = TCS_Status.POSITION_PARTIAL;
	    }
	}
		
    }

    protected void shutdown() {}		     

  
}
