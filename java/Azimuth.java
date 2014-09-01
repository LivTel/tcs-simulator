import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

public class Azimuth extends Axis {


    public Azimuth(TCS_Status sdb, long interval) {
	super("AZM", sdb, interval);
    }

    
    /** Set the current position in sdb.*/
    protected void setCurrent(double position) {
	sdb.mechanisms.azPos = position;
    }

    /** Get current position from sdb.*/
    public double getCurrent() {
	return sdb.mechanisms.azPos;
    }

    /** Get demand position from sdb.*/
    public double getDemand() {
   return sdb.mechanisms.azDemand;
    }

    /** Set visible status in sdb.*/
    protected void setStatus(int status) {
	sdb.mechanisms.azStatus = status;
    }

    /** Time step forward.*/
    protected void advance() {
	
	// ee = 1/2*acceln*time_interval**2 - but need to anticipate overshoot 
	// so use 1.5*v*time_interval
	
	//double ee = 1.5*slewRate*interval/1000.0;

	// Rate at which demand is changing - may need to keep pace. (NOT USED YET)
	demandRate = 1000.0*(sdb.mechanisms.azDemand - lastDemand)/interval;

	double delta = sdb.mechanisms.azDemand - sdb.mechanisms.azPos;

	System.err.println("AZM::Step::Dmd="+sdb.mechanisms.azDemand+
			   ", Pos=" +sdb.mechanisms.azPos+
			   ", Delta="+delta+" Min("+minAcquireDiff+")"+
			   ", DmdRate="+demandRate+
			   ", Speed="+speed);
	
	if (Math.abs(delta) > minAcquireDiff) { // this is really the acquire error box NOT the tracking box.
	    System.err.println("AZM: Select SLEW rate");
	    speed = slewRate ;

	    if (sdb.mechanisms.azDemand < sdb.mechanisms.azPos)
		speed = -speed;

	    // Calculate the slew distance - if clever use acceln/decceln and
	    // not just the max speed - for now,  not clever..
	
	} else {
	    System.err.println("AZM: Select CLOSING rate");
	    speed = 1000.0*delta/(0.95*interval);
	    	    
	    //sdb.mechanisms.azPos = sdb.mechanisms.azDemand; 
	    
	}
     
	// Choose direction.
	//if (sdb.mechanisms.azDemand < sdb.mechanisms.azPos)
	//  speed = -speed;
   	
	// 0.5 weight is just a guess.
	double ddaz = speed*((double)interval/1000.0) + 0.5*demandRate;

	sdb.mechanisms.azPos += ddaz;

	lastDemand = sdb.mechanisms.azDemand;
	    
    }
    
}
