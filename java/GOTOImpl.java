import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the GOTO command.*/
public class GOTOImpl extends TCSCommandImpl {

    final int LEFT = -1;
    final int RIGHT = +1;

    private double waz  = 0.0;
    private double dalt = 0.0;
    private double drot = 0.0;
    private double wrot = 0.0;

    private String targetName;

    public GOTOImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. GOTO ra ra ra dec dec dec epoch
	if (tokens.countTokens() < 8) {
	    fail(500002, "Missing args");  
	    return false;
	}

	if (sdb.state.telescopeState != TCS_Status.STATE_OKAY ||
	    sdb.state.networkControlState != TCS_Status.STATE_ENABLED) {
	    fail(500005, "Command not allowed in present system state");
	    return false;
	}
	
	try {
	    
	    targetName = tokens.nextToken();

	    double dra1  = Double.parseDouble(tokens.nextToken()); // degs
	    double dra2  = Double.parseDouble(tokens.nextToken()); // degs
	    double dra3  = Double.parseDouble(tokens.nextToken()); // degs
	    double dra = 15.0*(dra1 + dra2/60.0 + dra3/3600.0);    // HMS -> degs

	    double ddec1 = Double.parseDouble(tokens.nextToken());// degs
	    double ddec2 = Double.parseDouble(tokens.nextToken());// degs
	    double ddec3 = Double.parseDouble(tokens.nextToken());// degs
	    double ddec = ddec1 + ddec2/60.0 + ddec3/3600.0;      // DMS -> degs
	    
	    Subsystems.ast.slew(Math.toRadians(dra), Math.toRadians(ddec));

	    sdb.source.srcName = targetName;
	    sdb.source.srcRa   = dra;
	    sdb.source.srcDec  = ddec ;

	    // Make these up for now.
	    sdb.source.srcEquinoxLetter = "J";
	    sdb.source.srcEquinox       = 2000.0;
	    sdb.source.srcEpoch         = 2000.0;
	    sdb.source.srcPmRA           = 0.0;
	    sdb.source.srcPmDec          = 0.0;
	    sdb.source.srcParallax       = 0.0;
	    sdb.source.srcRadialVelocity = 0.0;
	    sdb.source.srcNsTrackRA      = 0.0;
	    sdb.source.srcNsTrackDec     = 0.0;


	    return true;
	    
	} catch (Exception e) {
	    fail(500003, "Error during parsing: "+e);
	    e.printStackTrace();
	    return false;
	}

    }

    public boolean monitor() {

	double ddaz  = Math.abs(sdb.mechanisms.azDemand  - sdb.mechanisms.azPos);
	double ddalt = Math.abs(sdb.mechanisms.altDemand - sdb.mechanisms.altPos);
	double ddrot = Math.abs(sdb.mechanisms.rotDemand - sdb.mechanisms.rotPos);

	double dd = ddaz*ddaz + ddalt*ddalt + ddrot*ddrot;
	System.err.println("GOTO:Mon::At: "+sdb.mechanisms.azPos+", "+
			   sdb.mechanisms.altPos+", "+
			   sdb.mechanisms.rotPos);
	System.err.println("GOTO:Mon::Delta: "+ddaz+", "+ddalt+", "+ddrot+" = "+dd);

	// We require all axes to be tracking (except maybe rot- and dd < 10.0sq.degs
	if (sdb.mechanisms.azStatus  == TCS_Status.MOTION_TRACKING &&
	    sdb.mechanisms.altStatus == TCS_Status.MOTION_TRACKING &&
	    (sdb.mechanisms.rotStatus == TCS_Status.MOTION_TRACKING ||
	     sdb.mechanisms.rotStatus == TCS_Status.MOTION_INPOSITION) &&
	    (dd < 10.0))
	    return true;
	
	return false;

    }

}
