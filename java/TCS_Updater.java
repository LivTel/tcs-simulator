import java.util.*;

import ngat.astrometry.*;
import ngat.message.RCS_TCS.*;

public class TCS_Updater extends Thread {

    boolean engflag = false;

    // AG-Focus parameters. E.g. at f = 23mm, W = 56 arcsec
    double agF0 = 5.0;   // AG-FOCUS optimum = 5.0 mm.
    double dF   = 3.0;   // AG-FOCUS width   = 3.0 mm.
    double agW0 = 20.00; // AG_FWHM  minimum = 20.00 arcsec.

    /** Used to update weather and record time into simulation.*/
    public  long simtime;

    private TCS_Simulation sim;

    private TCS_Status tel;

    public  TCS_Updater(TCS_Simulation sim) { 
	super("TCS_UPDATER");
	this.sim = sim;
	tel = sim.getStatus();
	simtime = 0L;
	Position.setViewpoint(sim.latitude, sim.longitude);
    }
    
    public void run() { 

	// Loop round incrementing time and adjusting TCS variables.

	while (true) {
	    tel.time.ut1  += sim.interval;
	    tel.timeStamp = System.currentTimeMillis();
	    simtime += sim.interval;
	    Date start    = null;
	    Date rightnow = new Date();
	    int  year     = rightnow.getYear();
	    
	    Date yearEquinox = new Date(year, 2, 21, 12, 0, 0); // this year's equinox
	    Date lastEquinox = new Date(year-1, 2, 21, 12, 0, 0); // previous equinox
	    
	    if (rightnow.after(yearEquinox)) {
		// use this year as start
		start = yearEquinox;
	    } else {
		// use last year as start
		start = lastEquinox;
	    }
	    
	    // Work out the (milli)seconds from equinox.
	    long e1 = start.getTime();
	    long e2 = rightnow.getTime();
	    
	    int sec = (int)((e2-e1)/1000);
	    // Convert to sidereal seconds.
	    // ********************************************************************
	    // NOTE: longitude here is measured EAST ALWAYS so W-longs are NEGATIVE
	    // ********************************************************************
	    int newsec = (int)(sec*(1+1/365.2425)+13750.98708*sim.longitude);
	    
	    int ilst = (newsec)%86400; // LST as seconds of day
	    double lst = (ilst/13750.98708); // LST as an angle.in Rads
	    tel.time.lst = ilst; // secs.



	    try {sleep((long)sim.interval);} catch (InterruptedException e) {}
	

	    // Now update the weather. This needs to be controlled externally by
	    // the TCS Simulation GUI when its written.
	    // -----------------------------------------------------------------

	    tel.meteorology.serrurierTrussTemperature = 
		randomize(tel.meteorology.serrurierTrussTemperature,-12.0, 20.0, 0.005);

	    tel.meteorology.oilTemperature = 
		randomize(tel.meteorology.oilTemperature ,-12.0, 20.0, 0.005);

	    tel.meteorology.primMirrorTemperature = 
		randomize(tel.meteorology.primMirrorTemperature ,-12.0, 20.0, 0.005);
	    
	    tel.meteorology.secMirrorTemperature = 
		randomize(tel.meteorology.secMirrorTemperature ,-12.0, 20.0, 0.005);

	    tel.meteorology.pressure = 
		randomize(tel.meteorology.pressure, 650.0, 850.0, 1.5);

	    tel.meteorology.lightLevel = 
		sinusoidal(40.0, 200.0, 400.0, 2.0);


	    // Now update the AG-FWHM based on AG-FOCUS position using model.
	    // Add on random (-10, +10) to the model results as noise.
	    // --------------------------------------------------------------
	    double ff = (tel.autoguider.agFocusPos - agF0)/dF;
	    tel.autoguider.fwhm = ff*ff + agW0 + (Math.random()*20.0) - 10.0; // In PIXELS.

	    // Now update the airmass based on current elevation.
	    double zd = 90.0 - tel.mechanisms.altPos;
	    if (zd > 86.0) zd = 86.0; // Arbitrary cutoff for singularity in sec(z).
	    tel.astrometry.airmass = 1.0/Math.cos(Math.toRadians(zd));

	}
    }

    private void track() {
	double lst  = tel.time.lst/13750.98708; // rads?
	double sphi = Math.sin(sim.latitude);
	double cphi = Math.cos(sim.latitude);
	double ha   = correct(lst - toRadians(tel.source.srcRa));
	double dec  = tel.source.srcDec; // Degrees	
    }

  
    /** Puts an angle (rads) into the correct (0 -> 2*PI) range.*/
    protected double correct(double angle) { 
	double a = angle + 2.0*Math.PI;
	while (a >= 2*Math.PI) a -= 2.0*Math.PI;
	return a;
    }
    
    public void log(String text) { System.out.println(getName()+": "+text);}

    private double toRadians(double args) {return args*Math.PI/180.0;}

    private double toDegrees(double args) {return args*180.0/Math.PI;}

    private double randomize(double data, double min, double max, double var) {
	double ndata = data + Math.random()*var - var/2.0;
	if (ndata < min) ndata = min;
	if (ndata > max) ndata = max;
	return ndata;
    }

    // Period is in seconds, simtime in millis
    private double sinusoidal(double min, double max, double period, double var) {
	double ndata = min + (max - min)*(1 + Math.sin(2.0*Math.PI*(double)(simtime/1000L)/period))/2.0;
	return randomize(ndata, min, max, var);
    }

}
