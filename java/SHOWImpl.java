import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.util.*;

/** Implements the SHOW command.*/
public class SHOWImpl extends TCSCommandImpl {

    private String key;

    public SHOWImpl(StringTokenizer tokens, TCS_Status sdb) {
	super(tokens, sdb);
    }

    /** Execute the command.*/
    public boolean demand() {

	// Args. SHOW key
	if (tokens.countTokens() < 1) {
	    fail(500002, "Missing args");  
	    return false;
	}
	
	key = tokens.nextToken();

	if 	   
	    (key.equals("ASTROMETRY"))
	    message = showAstrometry();
	else if
	    (key.equals("AUTOGUIDER"))
	    message = showAutoguider();
	else if
	    (key.equals("CALIBRATE"))
	    message = showCalibrate();
	else if
	    (key.equals("FOCUS"))
	    message = showFocus();
	else if
	    (key.equals("LIMITS"))
	    message = showLimits();
	else if
	    (key.equals("MECHANISMS"))
	    message = showMechanisms();
	else if
	    (key.equals("METEOROLOGY"))
	    message = showMeteorology();
	else if
	    (key.equals("SOURCE"))
	    message = showSource();
	else if
	    (key.equals("STATE"))
	    message = showState();
	else if
	    (key.equals("TIME"))
	    message = showTime();
	else if
	    (key.equals("VERSION"))
	    message = showVersion();
	else {
	    fail(500002, "Missing args");  
	    return false;
	}
		
	return true;

    }

    public boolean monitor() {

	
	return true;

    }


    private String showAstrometry() {
	StringBuffer buff = new StringBuffer();
	buff.append(" <<1>> "+   sdb.astrometry.refractionPressure);
	buff.append(" <<2>> "+   sdb.astrometry.refractionTemperature );	
	buff.append(" <<3>> "+   sdb.astrometry.refractionHumidity );	
	buff.append(" <<4>> "+   sdb.astrometry.refractionWavelength);	
	buff.append(" <<5>> "+   sdb.astrometry.ut1_utc);	
	buff.append(" <<6>> "+   sdb.astrometry.tdt_utc);	
	buff.append(" <<7>> "+   sdb.astrometry.polarMotion_X);	       
	buff.append(" <<8>> "+   sdb.astrometry.polarMotion_Y);
	buff.append(" <<9>> "+   sdb.astrometry.airmass);
	buff.append(" <<10>> "+  sdb.astrometry.agwavelength);
	return buff.toString();	
    }


    private String showAutoguider() {
	StringBuffer buff = new StringBuffer();
	buff.append(" <<1>> "+   sdb.autoguider.agSelected);
	buff.append(" <<2>> "+   TCS_Status.codeString(sdb.autoguider.agStatus));
	buff.append(" <<3>> "+   TCS_Status.codeString(sdb.autoguider.agSwState));
	buff.append(" <<4>> "+   sdb.autoguider.guideStarMagnitude);
	buff.append(" <<5>> "+   sdb.autoguider.fwhm);
	buff.append(" <<6>> "+   sdb.autoguider.agMirrorDemand);
	buff.append(" <<7>> "+   sdb.autoguider.agMirrorPos);
	buff.append(" <<8>> "+   TCS_Status.codeString(sdb.autoguider.agMirrorStatus));
	buff.append(" <<9>> "+   sdb.autoguider.agFocusDemand );
	buff.append(" <<10>> "+   sdb.autoguider.agFocusPos);
	buff.append(" <<11>> "+   TCS_Status.codeString(sdb.autoguider.agFocusStatus));
	buff.append(" <<12>> "+   TCS_Status.codeString(sdb.autoguider.agFilterDemand));
	buff.append(" <<13>> "+   TCS_Status.codeString(sdb.autoguider.agFilterPos));
	buff.append(" <<14>> "+   TCS_Status.codeString(sdb.autoguider.agFilterStatus));
	return buff.toString();		
    }

    private String showCalibrate() {
		return ("notavailable");
    }

    private String showFocus() {
		return ("notavailable");
    }

    private String showLimits() {
	StringBuffer buff = new StringBuffer();
	buff.append(" <<1>> "+    sdb.limits.azPosLimit);	
	buff.append(" <<2>> "+    sdb.limits.azNegLimit);
	
	buff.append(" <<3>> "+   sdb.limits.altPosLimit);	
	buff.append(" <<4>> "+   sdb.limits.altNegLimit);
	
	buff.append(" <<5>> "+   sdb.limits.rotPosLimit);	
	buff.append(" <<6>> "+   sdb.limits.rotNegLimit);
		
	buff.append(" <<7>> "+   sdb.limits.timeToAzLimit);
	buff.append(" <<8>> "+   TCS_Status.codeString(sdb.limits.azLimitSense));

	buff.append(" <<9>> "+   sdb.limits.timeToAltLimit);
	buff.append(" <<10>> "+  TCS_Status.codeString(sdb.limits.altLimitSense));
	
	buff.append(" <<11>> "+  sdb.limits.timeToRotLimit);
	buff.append(" <<12>> "+  TCS_Status.codeString(sdb.limits.rotLimitSense));
	return buff.toString();	
    }
    
    private String showMechanisms() {
	StringBuffer buff = new StringBuffer();
	buff.append(" <<1>> "+  sdb.mechanisms.azDemand);	
	buff.append(" <<2>> "+  sdb.mechanisms.azPos);	
	buff.append(" <<3>> "+  TCS_Status.codeString(sdb.mechanisms.azStatus));	
	
	buff.append(" <<4>> "+  sdb.mechanisms.altDemand);	
	buff.append(" <<5>> "+  sdb.mechanisms.altPos);	
	buff.append(" <<6>> "+  TCS_Status.codeString(sdb.mechanisms.altStatus));	
	
	buff.append(" <<7>> "+  sdb.mechanisms.rotDemand);	
	buff.append(" <<8>> "+  sdb.mechanisms.rotPos);	
	buff.append(" <<9>> "+  TCS_Status.codeString(sdb.mechanisms.rotMode));	
	buff.append(" <<10>> "+ sdb.mechanisms.rotSkyAngle);	
	buff.append(" <<11>> "+ TCS_Status.codeString(sdb.mechanisms.rotStatus));
	
	buff.append(" <<12>> "+ TCS_Status.codeString(sdb.mechanisms.encShutter1Demand));	
	buff.append(" <<13>> "+ TCS_Status.codeString(sdb.mechanisms.encShutter1Pos));	
	buff.append(" <<14>> "+ TCS_Status.codeString(sdb.mechanisms.encShutter1Status));
	
	buff.append(" <<15>> "+ TCS_Status.codeString(sdb.mechanisms.encShutter2Demand));	
	buff.append(" <<16>> "+ TCS_Status.codeString(sdb.mechanisms.encShutter2Pos));	
	buff.append(" <<17>> "+ TCS_Status.codeString(sdb.mechanisms.encShutter2Status));
	
	buff.append(" <<18>> "+ TCS_Status.codeString(sdb.mechanisms.foldMirrorDemand));	
	buff.append(" <<19>> "+ TCS_Status.codeString(sdb.mechanisms.foldMirrorPos));
	buff.append(" <<20>> "+ TCS_Status.codeString(sdb.mechanisms.foldMirrorStatus));
		
	buff.append(" <<21>> "+ TCS_Status.codeString(sdb.mechanisms.primMirrorCoverDemand));	
	buff.append(" <<22>> "+ TCS_Status.codeString(sdb.mechanisms.primMirrorCoverPos));
	buff.append(" <<23>> "+ TCS_Status.codeString(sdb.mechanisms.primMirrorCoverStatus));
	       
	buff.append(" <<24>> "+ sdb.mechanisms.secMirrorDemand);	
	buff.append(" <<25>> "+ sdb.mechanisms.secMirrorPos);	
	buff.append(" <<26>> "+ sdb.mechanisms.focusOffset);	
	buff.append(" <<27>> "+ TCS_Status.codeString(sdb.mechanisms.secMirrorStatus));
	
	buff.append(" <<28>> "+ TCS_Status.codeString(sdb.mechanisms.primMirrorSysStatus));	
		return buff.toString();
    }
    
    private String showMeteorology() {

		
	StringBuffer buff = new StringBuffer();
	buff.append(" <<1>>"+  TCS_Status.codeString(sdb.meteorology.wmsStatus));
	buff.append(" <<2>>"+  TCS_Status.codeString(sdb.meteorology.rainState));
	buff.append(" <<3>>"+  sdb.meteorology.serrurierTrussTemperature);
	buff.append(" <<4>>"+  sdb.meteorology.oilTemperature);
	buff.append(" <<5>>"+  sdb.meteorology.primMirrorTemperature);
	buff.append(" <<6>>"+  sdb.meteorology.secMirrorTemperature);
	buff.append(" <<7>>"+  sdb.meteorology.extTemperature);
	buff.append(" <<8>>"+  sdb.meteorology.windSpeed);
	buff.append(" <<9>>"+  sdb.meteorology.pressure);
	buff.append(" <<10>>"+ sdb.meteorology.humidity);
	buff.append(" <<11>>"+ sdb.meteorology.windDirn);
	buff.append(" <<12>>"+ sdb.meteorology.moistureFraction);
	buff.append(" <<13>>"+ sdb.meteorology.dewPointTemperature);
	buff.append(" <<14>>"+ sdb.meteorology.agBoxTemperature);
	buff.append(" <<15>>"+ sdb.meteorology.lightLevel);

    	return buff.toString();
    }

    private String showSource() {
	StringBuffer buff = new StringBuffer();
	buff.append(" <<1>>"+  sdb.source.srcName);   
	buff.append(" <<2>>"+  Position.formatHMSString(Math.toRadians(sdb.source.srcRa), " "));   
	buff.append(" <<3>>"+  Position.formatDMSString(Math.toRadians(sdb.source.srcDec)," "));   
	buff.append(" <<4>>"+  sdb.source.srcEquinoxLetter+sdb.source.srcEquinox );   
	buff.append(" <<5>>"+  sdb.source.srcEpoch );   
	buff.append(" <<6>>"+  sdb.source.srcPmRA );   
	buff.append(" <<7>>"+  sdb.source.srcPmDec );   
	buff.append(" <<8>>"+  sdb.source.srcNsTrackRA );   
	buff.append(" <<9>>"+  sdb.source.srcNsTrackDec );   
	buff.append(" <<10>>"+ sdb.source.srcParallax );   
	buff.append(" <<11>>"+ sdb.source.srcRadialVelocity ); 
	// DONT KNOW HOW TO WORK THIS OUT SO USE THE srcra/dec
	buff.append(" <<12>>"+  Position.formatHMSString(Math.toRadians(sdb.source.srcRa), " "));   
	buff.append(" <<13>>"+  Position.formatDMSString(Math.toRadians(sdb.source.srcDec)," "));   

	return buff.toString();
    }
    
    private String showState() {
	StringBuffer buff = new StringBuffer();
	buff.append(" <<1>> "+   TCS_Status.codeString(sdb.state.networkControlState)); 
	buff.append(" <<2>> "+   TCS_Status.codeString(sdb.state.engineeringOverrideState));

	if (System.getProperty("state.flicker") != null) {
	    if (sdb.state.telescopeState == TCS_Status.STATE_OKAY) {
		if (Math.random() > 0.5)
		    buff.append(" <<3>> "+   TCS_Status.codeString(TCS_Status.STATE_WARN));
		else
		    buff.append(" <<3>> "+   TCS_Status.codeString(TCS_Status.STATE_OKAY));
	    } else {
		buff.append(" <<3>> "+   TCS_Status.codeString(sdb.state.telescopeState));
	    }
	} else {
	    buff.append(" <<3>> "+   TCS_Status.codeString(sdb.state.telescopeState));
	}
	buff.append(" <<4>> "+   TCS_Status.codeString(sdb.state.tcsState));
	buff.append(" <<5>> "+   sdb.state.systemRestartFlag);
	buff.append(" <<6>> "+   sdb.state.systemShutdownFlag);
	return buff.toString();
    }
    
    private String showTime() {	
	StringBuffer buff = new StringBuffer(); // mjd ut1 lst
	buff.append(" <<1>> 54000.0 <<2>> 0.87 <<3>> 0.45");
	return buff.toString();
    }
    
    private String showVersion() {
		return ("notavailable");
    }

}
