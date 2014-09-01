import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

public class Altitude extends Axis {


    public Altitude(TCS_Status sdb, long interval) {
	super("ALT", sdb, interval);
    }

    
    /** Set the current position in sdb.*/
    protected void setCurrent(double position) {
	sdb.mechanisms.altPos = position;
    }

    /** Get current position from sdb.*/
    public double getCurrent() {
	return sdb.mechanisms.altPos;
    }

    /** Get demand position from sdb.*/
    public double getDemand() {
	return sdb.mechanisms.altDemand;
    }

    /** Set visible status in sdb.*/
    protected void setStatus(int status) {
	sdb.mechanisms.altStatus = status;
    }

    /** Time step forward.*/
    protected void advance() {
	
	//double ee = 1.5*slewRate*interval/1000.0;

	// Rate at which demand is changing - may need to keep pace. (NOT USED YET)
	demandRate = 1000.0*(sdb.mechanisms.altDemand - lastDemand)/interval;

	double delta = sdb.mechanisms.altDemand - sdb.mechanisms.altPos;

	System.err.println("ALTM::Step::Dmd="+sdb.mechanisms.altDemand+
			   ", Pos=" +sdb.mechanisms.altPos+
			   ", Delta="+delta+" Min("+minAcquireDiff+")"+
			   ", DmdRate="+demandRate+
			   ", Speed="+speed);
	
	if (Math.abs(delta) > minAcquireDiff) { // this is really the acquire error box NOT the tracking box.
	    
	    speed = slewRate;
	    
	    if (sdb.mechanisms.altDemand < sdb.mechanisms.altPos)
		speed = -speed;

	    // Calculate the slew distance - if clever use acceln/decceln and
	    // not just the max speed - for now,  not clever..
	
	} else {

	    speed = 1000.0*delta/(0.95*interval);
	    
	    //sdb.mechanisms.altPos = sdb.mechanisms.altDemand; 
	    
	}
        
	double ddalt = speed*((double)interval/1000.0);

	sdb.mechanisms.altPos += ddalt;
	sdb.astrometry.airmass = 1/Math.cos(Math.PI - Math.toRadians(ddalt));

	lastDemand = sdb.mechanisms.altDemand;
		
    }
    
}
