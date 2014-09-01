import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the WMS command.*/
public class WMSImpl extends TCSCommandImpl {

    public WMSImpl(StringTokenizer tokens, TCS_Status telescope) {
	super(tokens, telescope);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. WMS <var> <level> <time-sec>
	if (tokens.countTokens() < 3) {
	    fail(500002, "Missing args");  
	    return false;
	}
	Wms.Variable var = null;
	String strvar = tokens.nextToken();
	if ("HUMIDITY".equals(strvar)) 
	    var = Subsystems.wms.humidity;
	else if
	    ("WINDSPEED".equals(strvar))
	    var = Subsystems.wms.windSpeed;
	else if
	    ("WINDDIRN".equals(strvar))
	    var = Subsystems.wms.windDirection;
	else if
	    ("PRESSURE".equals(strvar))
	    var = Subsystems.wms.pressure;
	else if
	    ("TEMPERATURE".equals(strvar))
	    var = Subsystems.wms.temperature;
	else {
	    fail(500002, "Unknown WMS variable: "+strvar);  
	    return false;
	}

	String strmode = tokens.nextToken();


	double value = 0.0;
	try {
	    value = Double.parseDouble(tokens.nextToken());
	} catch (Exception e) {
	    fail(500003, "Error parsing: "+e);
	    return false;
	}

	double dt = 0.0;
	try {
	    dt = Double.parseDouble(tokens.nextToken());
	} catch (Exception e) {
	    fail(500003, "Error parsing: "+e);
	    return false;
	}

	if ("LEVEL".equals(strmode)) 
	    var.shiftLevel(value, dt);
	else if
	    ("PERIOD".equals(strmode)) 
	    var.shiftPeriod(value, dt);
	else if
	    ("AMP".equals(strmode)) 
	    var.shiftAmp(value, dt);
	else if 
	    ("DELTA".equals(strmode)) 
	    var.shiftDelta(value, dt);
	else {
	    fail(500005, "Unknown shift class: "+strmode);
	    return false;
	}

	return true;
    }

    public boolean monitor() {

	return true;

    }

}
