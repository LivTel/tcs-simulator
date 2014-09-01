import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

/** Simulate the telescope secondary mirror.*/
public class Focus extends ControlThread {

    /** Telescope status block.*/
    private TCS_Status telescope;
    
    /** Update interval (ms).*/
    private long interval;

    /** Focus position from 0.0 to 40.0.*/
    private double position;

    /** Create a secondary mirror with given update interval.
     * @param telescope Telescope status block.
     * @param interval Update interval (ms).
     */
    public Focus(TCS_Status telescope, long interval) {
	super("Focus", true);

	this.telescope = telescope;
	this.interval = interval;

	position = 0.0;

    }

    /** Request move to focus position.
     * @param focus The requested focus position (mm).
     */
    public void move(double focus) {

	telescope.mechanisms.secMirrorDemand = focus;

    }

    /** Request move to focus position.
     * @param focus The requested focus position (mm).
     */
    public void dfocus(double offset) {

	telescope.mechanisms.focusOffset = offset;

    }

    protected void initialise() {}

    protected void mainTask() {

	try { Thread.sleep(interval); } catch (InterruptedException ix) {System.err.println("Focus::interrupted");}

	System.err.println("Focus::Update:Position: "+telescope.mechanisms.secMirrorPos);

	double ee = 0.1*interval/1000.0;

	double delta = Math.abs(telescope.mechanisms.secMirrorDemand - telescope.mechanisms.secMirrorPos);

	if (delta < 5.0*ee) {
	    telescope.mechanisms.secMirrorPos = telescope.mechanisms.secMirrorDemand;
	    telescope.mechanisms.secMirrorStatus = TCS_Status.MOTION_INPOSITION;
	} else {
	    if (telescope.mechanisms.secMirrorPos < telescope.mechanisms.secMirrorDemand) {
		telescope.mechanisms.secMirrorPos += ee;
		telescope.mechanisms.secMirrorStatus = TCS_Status.MOTION_MOVING;
	    } else {
		telescope.mechanisms.secMirrorPos -= ee;
		telescope.mechanisms.secMirrorStatus = TCS_Status.MOTION_MOVING;
	    }
	}

    }

    protected void shutdown() {}		     

}
