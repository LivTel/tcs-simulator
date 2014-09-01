import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

/** Simulate the telescope axes.*/
public class Enclosure extends ControlThread {

    /** Telescope status block.*/
    private TCS_Status telescope;
    
    /** Update interval (ms).*/
    private long interval;

    /** Enclosure position from closed state: 80.0 = OPEN, 0.0 = CLOSED.*/
    private double position1;

    /** Enclosure position from closed state: 80.0 = OPEN, 0.0 = CLOSED.*/
    private double position2;

    /** Create an Axes with given update interval.
     * @param telescope Telescope status block.
     * @param interval Update interval (ms).
     */
    public Enclosure(TCS_Status telescope, long interval) {
	super("ENC", true);

	this.telescope = telescope;
	this.interval = interval;

	position1 = 0.0;
	position2 = 0.0;
    
    }

    
    public void close() {

	telescope.mechanisms.encShutter1Demand = TCS_Status.POSITION_CLOSED;
	telescope.mechanisms.encShutter2Demand = TCS_Status.POSITION_CLOSED;

    }

    public void open() {

	telescope.mechanisms.encShutter1Demand = TCS_Status.POSITION_OPEN;
	telescope.mechanisms.encShutter2Demand = TCS_Status.POSITION_OPEN;
	System.err.println("Enc::Requested to OPEN the ENCLOSURE");

    }


    protected void initialise() {}

    protected void mainTask() {

	try { Thread.sleep(interval); } catch (InterruptedException ix) {System.err.println("Enc::interrupted");}

	System.err.println("Enc::Update:Position: "+position1+"," +position2);

	// if we are asked to close then if pos > 0.0 pos -= dd

	//    if pos near 0.0, pos = 0.0, enc state = closed else state = partial

	// if we are asked to open the if pos < 80.0 pos += dd

	//   if pos near 80.0, pos - 80 enc state = open else state = partial	

	double ee = 0.3*interval/1000.0;

	switch (telescope.mechanisms.encShutter1Demand) {
	case TCS_Status.POSITION_CLOSED:
	      System.err.println("Enc1::Update:Closing");
	    if (position1 < 2.0*ee) {
		position1 = 0.0;
		telescope.mechanisms.encShutter1Status = TCS_Status.MOTION_INPOSITION;
		telescope.mechanisms.encShutter1Pos    = TCS_Status.POSITION_CLOSED;
		System.err.println("Enc1::Update:Setting to Closing completed");
	    } 
	       
	    if (position1 > 2.0*ee) {
		position1 -= ee;
		telescope.mechanisms.encShutter1Status = TCS_Status.POSITION_PARTIAL;
		telescope.mechanisms.encShutter1Pos    = TCS_Status.POSITION_PARTIAL;	
		System.err.println("Enc1::Update:Adding closing movement to enclosure");
	    }
	    break;
	case TCS_Status.POSITION_OPEN:
	    System.err.println("Enc1::Update:Opening");
	    if (position1 > 80.0 - 2.0*ee) {
		position1 = 80.0;
		telescope.mechanisms.encShutter1Status = TCS_Status.MOTION_INPOSITION;
		telescope.mechanisms.encShutter1Pos    = TCS_Status.POSITION_OPEN;	
		System.err.println("Enc1::Update:Setting to Opening completed");
	    } 
	       
	    if (position1 < 80.0 - 2.0*ee) {
		position1 += ee;
		telescope.mechanisms.encShutter1Status = TCS_Status.POSITION_PARTIAL;
		telescope.mechanisms.encShutter1Pos    = TCS_Status.POSITION_PARTIAL;		
		System.err.println("Enc1::Update:Adding opening movement to enclosure");
	    }
	    break;
	}


	switch (telescope.mechanisms.encShutter2Demand) {
	case TCS_Status.POSITION_CLOSED:
	      System.err.println("Enc2::Update:Closing");
	    if (position2 < 2.0*ee) {
		position2 = 0.0;	
		telescope.mechanisms.encShutter2Status = TCS_Status.MOTION_INPOSITION;
		telescope.mechanisms.encShutter2Pos    = TCS_Status.POSITION_CLOSED;
		System.err.println("Enc2::Update:Setting to Closing completed");
	    } 
	       
	    if (position2 > 2.0*ee) {
		position2 -= ee;	
		telescope.mechanisms.encShutter2Status = TCS_Status.POSITION_PARTIAL;
		telescope.mechanisms.encShutter2Pos    = TCS_Status.POSITION_PARTIAL;
		System.err.println("Enc2::Update:Adding closing movement to enclosure");
	    }
	    break;
	case TCS_Status.POSITION_OPEN:
	    System.err.println("Enc2::Update:Opening");
	    if (position2 > 80.0 - 2.0*ee) {
		position2 = 80.0;	
		telescope.mechanisms.encShutter2Status = TCS_Status.MOTION_INPOSITION;
		telescope.mechanisms.encShutter2Pos    = TCS_Status.POSITION_OPEN;
		System.err.println("Enc2::Update:Setting to Opening completed");
	    } 
	       
	    if (position2 < 80.0 - 2.0*ee) {
		position2 += ee;	
		telescope.mechanisms.encShutter2Status = TCS_Status.POSITION_PARTIAL;
		telescope.mechanisms.encShutter2Pos    = TCS_Status.POSITION_PARTIAL;
		System.err.println("Enc2::Update:Adding opening movement to enclosure");
	    }
	    break;
	}

	
    }

    protected void shutdown() {}		     

  
}
