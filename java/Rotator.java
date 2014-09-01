import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

public class Rotator extends Axis {

    /** Records the demand angle (degs) - this could be a MOUNT or SKY angle.*/
    private double rotator;

    public Rotator(TCS_Status sdb, long interval) {
	super("ROT", sdb, interval);
    }

    public void setRotator(double r) {
	this.rotator = r;
    }

    public double getRotator() { 
	return rotator;
    }
    
    /** Set the current position in sdb.*/
    protected void setCurrent(double position) {
	sdb.mechanisms.rotSkyAngle = position;
    }

    /** Get current position from sdb.*/
    public double getCurrent() {
	return sdb.mechanisms.rotPos;
    }

    /** Get demand position from sdb.*/
    public double getDemand() {
   return sdb.mechanisms.rotDemand;
    }

    /** Set visible status in sdb.*/
    protected void setStatus(int status) {
	sdb.mechanisms.rotStatus = status;
    }

    /** Time step forward.*/
    protected void advance() {
	
	// ee = 1/2*acceln*interval**2 - but need to anticipate overshoot so use 1.5*v*int
	
	//double ee = 1.5*slewRate*interval/1000.0;

	// Rate at which demand is changing - may need to keep pace. (NOT USED YET)
	demandRate = 1000.0*(sdb.mechanisms.rotDemand - lastDemand)/interval;

	double delta = sdb.mechanisms.rotDemand - sdb.mechanisms.rotPos;

	System.err.println("ROTM::Step::Dmd="+sdb.mechanisms.rotDemand+
			   ", Pos=" +sdb.mechanisms.rotPos+
			   ", Delta="+delta+" Min("+minAcquireDiff+")"+
			   ", DmdRate="+demandRate+			  
			   ", Speed="+speed);
	
	if (Math.abs(delta) > minAcquireDiff) { // this is really the acquire error box NOT the tracking box.
	    
	    speed = slewRate;

	    if (sdb.mechanisms.rotDemand < sdb.mechanisms.rotPos)
		speed = -speed;

	    // Calculate the slew distance - if clever use acceln/decceln and
	    // not just the max speed - for now,  not clever..
	
	} else {

	    speed = 1000.0*delta/(0.95*interval);
	    
	    //sdb.mechanisms.rotPos = sdb.mechanisms.rotDemand; 

	}
        
	if (stick)
	    speed = 0.0;

	double ddrot = speed*((double)interval/1000.0);

	sdb.mechanisms.rotPos += ddrot;
	    
	lastDemand = sdb.mechanisms.rotDemand;
		
    }
    
}
