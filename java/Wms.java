import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

public class Wms extends ControlThread {

    /** Telescope status block.*/
    private TCS_Status sdb;
    
    /** Update interval (ms).*/
    private long interval;

    public Variable humidity;

    public Variable windSpeed;

    public Variable windDirection;

    public Variable pressure;

    public Variable temperature;

    /** Records the fact that wms state has been bad.*/
    private boolean wasbad = true;

    /** Period number when wms state went good.*/
    private int wentgood;

    /** Period need to be good before WMS Good.*/
    private double gooddelay = 60000L;

    private int ip = 0;

    public Wms(TCS_Status sdb, long interval) {
	super("WMS", true);

	this.sdb = sdb;
	this.interval = interval;

	humidity      = new Variable(0.5,   0.2,  3600000.0,  0.12, -1.0,  0.77, 2, 5);
	windSpeed     = new Variable(7.0,   5.0,  1800000.0,  5.0,  -10.0, 15.0, 3, 10);
	windDirection = new Variable(180.0, 80.0, 600000.0,   20.0, -10.0, 400.0, 10,10);
	pressure      = new Variable(777.0, 30.0, 86400000.0, 0.2,  -1.0,  1000.0,10,10 );
	temperature   = new Variable(15.0,  10.0, 86400000.0, 2.0,  -1.0,  40.0,  2, 10);

	//humidity.debug = true;

    }

    protected void initialise() {}

    protected void mainTask() {

	try { Thread.sleep(interval); } catch (InterruptedException ix) {System.err.println("WMS::interrupted");}

	ip++;

	boolean good = true;

	sdb.meteorology.rainState = TCS_Status.RAIN_CLEAR;
	sdb.meteorology.moistureFraction = 0.05;

	humidity.computeValue();
	sdb.meteorology.humidity = humidity.getValue();

	//System.err.println("WMS::["+ip+"] Humidity: "+(sdb.meteorology.humidity*100.0)+"% : "+
	//(humidity.isBad() ? "THREAT" :"CLEAR"));
	if (humidity.isBad()) good  = false;

	windSpeed.computeValue();
	sdb.meteorology.windSpeed = windSpeed.getValue();
	if (windSpeed.isBad()) good  = false;

	windDirection.computeValue();
	sdb.meteorology.windDirn = windDirection.getValue();	

	pressure.computeValue();
	sdb.meteorology.pressure = pressure.getValue();
	
	temperature.computeValue();
	sdb.meteorology.extTemperature = temperature.getValue();
	if (temperature.isBad()) good  = false;

	if (good) {

	    if (wasbad) {
		wentgood = ip;
	    }
	    wasbad = false;
	    
	    if ((ip - wentgood)*interval > gooddelay) {
		sdb.meteorology.wmsStatus = TCS_Status.STATE_OKAY;
	    }

	} else {
	    
	    wasbad = true;
	    sdb.meteorology.wmsStatus = TCS_Status.STATE_ERROR;

	}

	System.err.println("WMS::["+ip+"] WMS State: "+TCS_Status.codeString(sdb.meteorology.wmsStatus));
	
    }

    protected void shutdown() {}		       

    public class Variable {

	/** Records the start time.*/
	long start;

	/** Instantaneous value.*/
	double value;

	Parameter level;
	Parameter amp;
	Parameter period;
	Parameter delta;

	double max;

	double min;

	int nbX;

	int nsX;

	int ns, nb;
	
	double sum, av;
	boolean debug = false;
	/** Create the variable starting off at level.*/
	public Variable(double l,
			double a,
			double p,
			double d,
			double min, 
			double max, 
			int nbX, 
			int nsX) {
	    
	    level = new Parameter(l);
	    amp   = new Parameter(a);
	    period= new Parameter(p);
	    delta = new Parameter(d);
	    
	    this.min = min;
	    this.max = max;
	    this.nbX = nbX;
	    this.nsX = nsX;
	    sum = 0.0;
	    av  = 0.0;
	    ns  = 0;
	    nb  = 0;

	   

	    start = System.currentTimeMillis();

	    value = level.getCurrent() + delta.getCurrent()*(Math.random()-0.5);

	}

	public void shiftLevel(double target, double dt) {	   
	    int n  = (int)(dt/(double)interval);
	    level.shift(target, n);	
	}

	public void shiftAmp(double target, double dt) {	   
	    int n  = (int)(dt/(double)interval);
	    amp.shift(target, n);	
	}

	public void shiftPeriod(double target, double dt) {	   
	    int n  = (int)(dt/(double)interval);
	    period.shift(target, n);	
	}
       
	public void shiftDelta(double target, double dt) {	   
	    int n  = (int)(dt/(double)interval);
	    delta.shift(target, n);	
	}

	public void computeValue() {
	    level.computeValue();
	    amp.computeValue();
	    period.computeValue();
	    delta.computeValue();

	    // work out if bad.
	    double current = getValue();
	    ns++;
	    sum += current;

	   if (debug)
	       System.err.println("WMS::Humidity: Curr="+current+", RunAv="+(sum/ns)+", Ns="+ns);

	    if (ns != nsX) return;

	     if (debug)
		 System.err.println("WMS::Humidity: NS == "+nsX);
	    
	    av = sum/ns;

	     if (debug)
		 System.err.println("WMS::Humidity:Testing Av:"+av+" in ("+min+","+max+")");
	    
	    if (av > min && av < max) {
		 if (debug)
		     System.err.println("WMS::Humidity:Test OK: Resetting counters");
		sum = 0.0;
		ns  = 0;
		av  = 0.0; 
		nb  = 0;
		return;
	    }
	    
	    nb++;
	     if (debug)
		 System.err.println("WMS::Humidity: Test failed: NB="+nb);
	  
	    sum = 0.0;
	    ns  = 0;
	    av  = 0.0;	
	}


	/** Return the current value.*/
	public double getValue() {
	    long time = System.currentTimeMillis();
	    return 
		level.getCurrent() + 
		amp.getCurrent()*Math.sin(2.0*Math.PI*(double)(time-start)/period.getCurrent()) + 
		delta.getCurrent()*(Math.random()-0.5);

	    // a + b*sin(2pi(t-t0)/dt) + rnd(-d/2, +d/2)

	}

	/** Returns true if the variable is out of range.*/
	public boolean isBad() {
	    return (nb >= nbX);
	}


    }

    private class Parameter {

	double current;

	double target;

	double rate;


	Parameter(double c) {
	    current = c;
	    target = c;
	    rate   = 0.0;	 
	}

	public void shift(double target, int steps) {	  	  
	    this.target = target;
	    rate = (target - current)/(double)steps;
	}

	/** Called at interval to recompute the level.*/
	private void computeValue() {
	    
	    if (rate > 0.0) {
		// ramp up
		if ((current - target) < rate) {
		    current += rate;
		} else {
		    current = target;
		}
	    } else {
		// ramp down
		if ((current - target) > rate) {
		    current += rate;
		} else {
		    current = target;
		}
	    }

	   
	}

	public double getCurrent() { return current; }


    }

}
