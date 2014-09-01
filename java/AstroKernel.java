import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;


public class AstroKernel extends ControlThread {
    
    public static final int ERROR_STATE  = 1;

    public static final int IDLE_STATE   = 2;

    public static final int ACTIVE_STATE = 3;

     
    /** Telescope status block.*/
    TCS_Status sdb;

    /** Update interval (ms).*/
    long interval;

    /** Current state.*/
    private int state;

    /** Set if intended to acquire/follow a target.*/
    private boolean followRequested = false;

    public AstroKernel(TCS_Status sdb, long interval) {
	super("AST", true);
	this.sdb = sdb;
	this.interval = interval;

	state = IDLE_STATE;

    }
   
    protected void initialise() {}

    protected void mainTask() {

	try { Thread.sleep(interval); } catch (InterruptedException ix) {System.err.println(getName()+"::interrupted");}

	System.err.println("AST::Current state: "+toStateString(state));

	
	switch (state) {
	case IDLE_STATE:
	    if (followRequested) {
		
		state = ACTIVE_STATE;
	    } else {


	    }
	    break;
	case ACTIVE_STATE:
	    if (followRequested) {

		calculateAzDemand();
		calculateAltDemand();
		calculateRotDemand();


	    } else {

		state = IDLE_STATE;

	    }
	    break;
	case ERROR_STATE:

	    break;

	}


    }


    protected void shutdown() {}

    /** Requets to stop following target.*/
    public void desist() {
	followRequested = false;
    }

    /** Slew to a position and follow if tracking enabled.
     * NOTE: If any axis is TRK_OFF we DONT calculate new demands after arriving at the
     * initially demanded position. We do however calculate for the others which are TRK_ON.
     */
    public void slew(double ra, double dec) {

	sdb.source.srcRa  = Math.toDegrees(ra);
	sdb.source.srcDec = Math.toDegrees(dec);

	followRequested = true;

	calculateAzDemand();
	calculateAltDemand();
	calculateRotDemand();

	Subsystems.azm.follow();
	Subsystems.alt.follow();
	Subsystems.rot.follow();
	
    }

    public void stopAxes() {
	desist();
	Subsystems.azm.halt();
	Subsystems.alt.halt();
	Subsystems.rot.halt();
    }

   /** Move to wrap az (rads).
    * Disable tracking on ALL axes.
    */
    public void goAzimuth(double az) {	

	followRequested = false;

	Subsystems.azm.setTrackingEnabled(false);
	Subsystems.alt.setTrackingEnabled(false);
	Subsystems.rot.setTrackingEnabled(false);
	sdb.mechanisms.azDemand = Math.toDegrees(az);

	Subsystems.azm.follow();

    }

    /** Move to alt (rads).
     * Disable tracking on ALL axes.
    */
    public void goAltitude(double alt) {	
	
     	followRequested = false;

	Subsystems.azm.setTrackingEnabled(false);
	Subsystems.alt.setTrackingEnabled(false);
	Subsystems.rot.setTrackingEnabled(false);
	sdb.mechanisms.altDemand = Math.toDegrees(alt);

	Subsystems.alt.follow();

    }

    /** Move to rot (rads).
     * Disable tracking on ALL axes.
    */
    public void goRotator(double rot) {	
	
       	followRequested = false;
	
	Subsystems.azm.setTrackingEnabled(false);
	Subsystems.alt.setTrackingEnabled(false);
	Subsystems.rot.setTrackingEnabled(false);
	sdb.mechanisms.rotMode = TCS_Status.ROT_MOUNT;
	sdb.mechanisms.rotDemand = Math.toDegrees(rot);

	Subsystems.rot.follow();

    }

  
    /** Set the rotator parameters and calculate any new demands.
     * @param mode Rotator mode.
     * @param angle Rotator angle (rads).	
     */
    public void rotate(int mode, double angle) {

	sdb.mechanisms.rotMode = mode;
	Subsystems.rot.setRotator(Math.toDegrees(angle));

	calculateAzDemand();
	calculateAltDemand();
	calculateRotDemand();

	Subsystems.rot.follow();
    }

    private void calculateAzDemand() {
	
	Position target  = new Position(Math.toRadians(sdb.source.srcRa), 
					Math.toRadians(sdb.source.srcDec));

	// This is the constrained value in std limits.
	double az  = target.getAzimuth();
	// Convert to degrees in wrap.
	double daz  = Math.toDegrees(az);		
	double caz  = sdb.mechanisms.azPos;
	double waz = azWrap(daz, caz); // degs.
	sdb.mechanisms.azDemand  = waz;
	System.err.println("AST::Azimuth demand: "+sdb.mechanisms.azDemand+", Current: "+sdb.mechanisms.azPos);
    }
    
    /** Calculate new axis demands.*/
    private void calculateAltDemand() {
		
	Position target  = new Position(Math.toRadians(sdb.source.srcRa), 
					Math.toRadians(sdb.source.srcDec));
	
	// This is the constrained value in std limits.
	double alt = target.getAltitude();		
	sdb.mechanisms.altDemand  = Math.toDegrees(alt);
	System.err.println("AST::Altitude demand: "+sdb.mechanisms.altDemand+", Current: "+sdb.mechanisms.altPos);
    }

    /** Calculate new axis demands.*/
    private void calculateRotDemand() {

	// snf changed to use az and alt demands rather than actuals as this can cause a spike 
	// at the start of a slew .

	Position target  = new Position(Math.toRadians(sdb.source.srcRa), 
					Math.toRadians(sdb.source.srcDec));


	double caz  = Math.toRadians(sdb.mechanisms.azDemand);
	double waz  = correct(3.0*Math.PI - caz);
	double calt = Math.toRadians(sdb.mechanisms.altDemand);
	double lat  = Math.toRadians(TelescopeStatus.lat);
	
	double cphi = Math.cos(lat);
	double sphi = Math.sin(lat);
	
	double para = JSlalib.calcBearing(waz, calt, Math.PI, lat);

	para = -para; // rads.
	System.err.println("AST::Paralytic angle is: "+Position.toDegrees(para, 3));
	
	double rot  = 0.0;
	double wrot = 0.0; 
	double drot = 0.0; 

	double crot = sdb.mechanisms.rotPos;
	double ttrot = 0.0;
	switch (sdb.mechanisms.rotMode) {
	case TCS_Status.ROT_MOUNT:
	    drot = Subsystems.rot.getRotator();		    
	    wrot = drot;
	    ttrot = Math.abs(wrot - crot)/2.0;
	    ttrot = ttrot*1000.0;
	    System.err.println("AST::Rotator slew mount: "+ttrot+" msec");	   	
	    break;
	case TCS_Status.ROT_SKY:
	    rot = correct(2*Math.PI+para+Math.toRadians(Subsystems.rot.getRotator())); // (0, 2*PI).
	    sdb.mechanisms.rotSkyAngle = Subsystems.rot.getRotator();
	    System.err.println("AST::Setting skyangle to: "+sdb.mechanisms.rotSkyAngle);	
	    drot = Math.toDegrees(rot);	   
	    wrot = rotWrap(drot, crot); // degs.
	    ttrot = Math.abs(wrot - crot)/2.0;
	    ttrot = ttrot*1000.0;
	    System.err.println("AST::Rotator slew sky: "+ttrot+" msec");	   
	    break;
	case TCS_Status.ROT_VERTICAL:	   
	    drot = 0.0;
	    wrot = rotWrap(drot, crot); // degs.
	    break;
	case TCS_Status.ROT_VFLOAT: 
	    rot = 0.0; 
	    drot =Math. toDegrees(rot);
	    wrot = rotWrap(drot, crot); // degs.
	    if (para < 0.0)
		sdb.mechanisms.rotSkyAngle = Math.toDegrees(-para);
	    else
		sdb.mechanisms.rotSkyAngle = Math.toDegrees(2.0*Math.PI-para);
	    break;
	}
	sdb.mechanisms.rotDemand  = wrot;
	System.err.println("AST::Rotator demand: "+wrot+", Current: "+sdb.mechanisms.rotPos);
    }

    /** Calculates azimuth wrap position.
     * @param daz Demand azimuth. (
     * @param caz Current azimuth (wrap).
     * @return wrap az demand (degs).
     */
     private double azWrap(double daz, double caz) {

	double waz  = 0.0;
	double waz1 = 0.0;
	double waz2 = 0.0;
	double limL = 0.0;
	double limR = 0.0;
	double deltaL = 0.0;
	double deltaR = 0.0;
	double ttaz = 0.0;
	if (daz > 0.0 && daz < 180.0) {
	    // Can only be here.
	    waz = daz;
	    //System.out.println("ONE CHOICE: ");
	    //System.out.println("WRAP to: "+waz);
	    if (waz < sdb.mechanisms.azPos) {
		ttaz =  (sdb.mechanisms.azPos - waz)/2.0;
	
		System.err.println("AST::Azwrap::Single wrap az-slew LEFT by"+(sdb.mechanisms.azPos - waz));
	    } else {
		ttaz = (waz - sdb.mechanisms.azPos)/2.0;
	
		System.err.println("AST::Azwrap::Single wrap az-slew RIGHT by"+(waz - sdb.mechanisms.azPos));
	    }
	}
	else {
	    // 2 Choices.
	    //System.out.println("TWO CHOICES: ");
	    waz2 = daz;
	    waz1 = daz - 360.0;
	    limL = sdb.mechanisms.azPos + 180.0;
	    limR = 360.0 - sdb.mechanisms.azPos;
	    deltaL = sdb.mechanisms.azPos - waz1 ;
	    deltaR = waz2 - sdb.mechanisms.azPos;
	    //System.out.println("WRAP1: "+waz1);
	    //System.out.println("WRAP2: "+waz2);
	    //System.out.println("LIMITS L: "+limL+" R: "+limR);
	    //System.out.println("DELTAS L: "+deltaL+" R: "+deltaR);
	    if 
		(deltaL > limL) {
	
		waz = waz2;
		ttaz  = deltaR/2.0;
		System.err.println("AST::Azwrap::Must az-slew RIGHT "+deltaR+" ("+deltaL+") to limit at "+limL);
	    } 
	    else if
		(deltaR > limR) {
	
		waz = waz1;
		ttaz = deltaL/2.0;
		System.err.println("AST::Azwrap::Must az-slew LEFT "+deltaL+" ("+deltaR+") to limit at "+limR);
	    } else {
		if (deltaL < deltaR) {
		  
		    waz = waz1;
		    ttaz = deltaL/2.0;
		    System.err.println("AST::Azwrap::Shortest az-Slew LEFT "+deltaL+" ("+deltaR+")");
		} else {
		  
		    waz = waz2;
		    ttaz  = deltaR/2.0;
		    System.err.println("AST::Azwrap::Shortest az-Slew RIGHT "+deltaR+" ("+deltaL+")");
		}
	    }
	}
	
	return waz;
     }

    /** Calculates rotator wrap position.
     * @param daz Demand rotator. (
     * @param caz Current rotator (wrap).
     * @return wrap rot demand (degs).
     */
    private double rotWrap(double drot, double crot) {

	double wrot  = 0.0;
	double wrot1 = 0.0;
	double wrot2 = 0.0;
	double limL  = 0.0;
	double limR  = 0.0;
	double deltaL = 0.0;
	double deltaR = 0.0;
	double ttrot = 0.0;
	if ((drot > 240.0 && drot < 360.0) ||
	    (drot > 0.0 && drot < 120.0)) {
	    // Can only be here.
	    if (drot > 240.0 && drot < 360.0)
		wrot = drot - 360.0;
	    if (drot > 0.0 && drot < 120.0)
		wrot = drot;

	    //System.out.println("ONE CHOICE: ");
	    //System.out.println("WRAP to: "+waz);
	    if (wrot < sdb.mechanisms.rotPos) {
		ttrot =  (sdb.mechanisms.rotPos - wrot)/2.0;
	
		System.err.println("AST::Rotwrap::Single wrap rot-slew LEFT by"+(sdb.mechanisms.rotPos - wrot));
	    } else {
		ttrot = (wrot - sdb.mechanisms.rotPos)/2.0;
		
		System.err.println("AST::Rotwrap::Single wrap rot-slew RIGHT by"+(wrot - sdb.mechanisms.rotPos));
	    }
	}
	else {
	    // 2 Choices.
	    //System.out.println("TWO CHOICES: ");
	    wrot2 = drot;
	    wrot1 = drot - 360.0;
	    limL = sdb.mechanisms.rotPos + 240.0;
	    limR = 240.0 - sdb.mechanisms.rotPos;
	    deltaL = sdb.mechanisms.rotPos - wrot1 ;
	    deltaR = wrot2 - sdb.mechanisms.rotPos;
	    //System.out.println("WRAP1: "+waz1);
	    //System.out.println("WRAP2: "+waz2);
	    //System.out.println("LIMITS L: "+limL+" R: "+limR);
	    //System.out.println("DELTAS L: "+deltaL+" R: "+deltaR);
	    if 
		(deltaL > limL) {
	
		wrot = wrot2;
		ttrot  = deltaR/2.0;
		System.err.println("AST::Rotwrap::Must rot-slew RIGHT "+deltaR+" ("+deltaL+") to limit at "+limL);
	    } 
	    else if
		(deltaR > limR) {
	
		wrot = wrot1;
		ttrot = deltaL/2.0;
		System.err.println("AST::Rotwrap::Must rot-slew LEFT "+deltaL+" ("+deltaR+") to limit at "+limR);
	    } else {
		if (deltaL < deltaR) {
		   
		    wrot = wrot1;
		    ttrot = deltaL/2.0;
		    System.err.println("AST::Rotwrap::Shortest rot-Slew LEFT "+deltaL+" ("+deltaR+")");
		} else {
		  
		    wrot = wrot2;
		    ttrot  = deltaR/2.0;
		    System.err.println("AST::Rotwrap::Shortest rot-Slew RIGHT "+deltaR+" ("+deltaL+")");
		}
	    }
	}
	
	return wrot;
    }

    
    /** Puts an angle (rads) into the correct (0 -> 2*PI) range.*/
    protected double correct(double angle) { 
	double a = angle + 2.0*Math.PI;
	while (a >= 2*Math.PI) a -= 2.0*Math.PI;
	return a;
    }

    public String toStateString(int state) {
	switch (state) {
	case ERROR_STATE:
	    return "ERROR";
	case IDLE_STATE:
	    return "IDLE";
	case ACTIVE_STATE:
	    return "ACTIVE";
	}
	return "UNKNOWN";
    }

     
}
