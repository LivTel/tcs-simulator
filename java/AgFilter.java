import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

/** Simulate the telescope AG Filter.*/
public class AgFilter extends ControlThread {

    /** Telescope status block.*/
    private TCS_Status telescope;
    
    /** Update interval (ms).*/
    private long interval;

    /** AG Filter deployment position from IN state: 1.0 = OUT, 0.0 = IN.*/
    private double position;

    /** Rate of closing u/s.*/
    private double closeRate = 0.1;

    /** Rate of opening u/s.*/
    private double openRate = 0.1;

    private boolean opening;

    private boolean failure = false;

    /** Create an Agfilter with given update interval.
     * @param telescope Telescope status block.
     * @param interval Update interval (ms).
     */
    public AgFilter(TCS_Status telescope, long interval) {
	super("AGF", true);

	this.telescope = telescope;
	this.interval = interval;

	position = 0.0; //IN

    }

    
    public void in() {

	telescope.autoguider.agFilterDemand = TCS_Status.POSITION_INLINE;

    }

    public void out() {

	telescope.autoguider.agFilterDemand = TCS_Status.POSITION_RETRACT;

    }

    public void fail() {
	failure = true;
    }

    public void normal() {
	failure = false;
    }

    protected void initialise() {}

    protected void mainTask() {

	double dp = 0.0;

	try { Thread.sleep(interval); } catch (InterruptedException ix) {System.err.println("AGF::interrupted");}

	System.err.println("AGF::Update:Position: "+position);

	if (failure) {
	    position = 0.5;
	} else {
	    
	    switch (telescope.autoguider.agFilterDemand) {
	    case TCS_Status.POSITION_INLINE:
		
		dp = closeRate*interval/1000.0;
		
		if (position < 2.0*dp) {
		    position = 0.0;
		    telescope.autoguider.agFilterStatus = TCS_Status.MOTION_INPOSITION;
		    telescope.autoguider.agFilterPos    = TCS_Status.POSITION_INLINE;
		} 
		
		if (position > 2.0*dp) {
		    position -= dp;
		    telescope.autoguider.agFilterStatus = TCS_Status.MOTION_MOVING;
		    telescope.autoguider.agFilterPos    = TCS_Status.POSITION_PARTIAL;
		}
		break;
	    case TCS_Status.POSITION_RETRACT:
		
		dp = openRate*interval/1000.0;
		
		if (position > 1.0 - 2.0*dp) {
		    position = 1.0;
		    telescope.autoguider.agFilterStatus = TCS_Status.MOTION_INPOSITION;
		    telescope.autoguider.agFilterPos    = TCS_Status.POSITION_RETRACT;
		} 
		
	    if (position < 1.0 - 2.0*dp) {
		position += dp;
		telescope.autoguider.agFilterStatus = TCS_Status.MOTION_MOVING;
		telescope.autoguider.agFilterPos    = TCS_Status.POSITION_PARTIAL;
	    }
	    break;
	    }
	}
    }

    protected void shutdown() {}		     

  
}
