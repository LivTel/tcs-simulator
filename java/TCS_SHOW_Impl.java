import ngat.message.RCS_TCS.*;
import java.util.*;

public class TCS_SHOW_Impl {

    protected TCS_Status status;

    protected int key;

    StringBuffer buff;

    public TCS_SHOW_Impl(TCS_Status status, String keyName) {
	this.status = status;
       
	if 
	    (keyName.equals("ALL"))
	    key = SHOW.ALL;
	else if
	    (keyName.equals("ASTROMETRY"))
	    key = SHOW.ASTROMETRY;
	else if
	    (keyName.equals("AUTOGUIDER"))
	    key = SHOW.AUTOGUIDER;
	else if
	    (keyName.equals("CALIBRATE"))
	    key = SHOW.CALIBRATE;
	else if
	    (keyName.equals("FOCUS"))
	    key = SHOW.FOCUS;
	else if
	    (keyName.equals("LIMITS"))
	    key = SHOW.LIMITS;
	else if
	    (keyName.equals("MECHANISMS"))
	    key = SHOW.MECHANISMS;
	else if
	    (keyName.equals("METEOROLOGY"))
	    key = SHOW.METEOROLOGY;
	else if
	    (keyName.equals("SOURCE"))
	    key = SHOW.SOURCE;
	else if
	    (keyName.equals("STATE"))
	    key = SHOW.STATE;
	else if
	    (keyName.equals("TIME"))
	    key = SHOW.TIME;
	else if
	    (keyName.equals("VERSION"))
	    key = SHOW.VERSION;
	else 
	    key = SHOW.ALL;
    }

    public String getDoneString() {

	buff = new StringBuffer(); 
	buff.append(" &time "+status.time);
	switch (key) {
	    
	case SHOW.ASTROMETRY:	
	    // Astrometry.	
	    getAstrometry();
	    break;
	case SHOW.AUTOGUIDER:
	    // Autoguider.
	    getAutoguider();
	    break;
	case SHOW.CALIBRATE:
	    // Calibrate.
	    getCalibrate();
	    break;
	case SHOW.FOCUS:
	    // Focus.
	    getFocus();
	    break;
	case SHOW.LIMITS:
	    // Limits.
	    getLimits();
	    break;
	case SHOW.MECHANISMS:
	    // Mechanisms.
	    getMechanisms();
	    break;
	case SHOW.METEOROLOGY:
	    // Meteorology.
	    getMeteorology();
	    break;
	case SHOW.SOURCE:
	    // Source.
	    getSource();
	    break;
	case SHOW.STATE:
	    // State.
	    getState();
	    break;
	case SHOW.VERSION:
	    // TCS Version.
	    getVersion();
	    break;
	case SHOW.TIME:
	    // Time.
	    getTime();
	    break;
	default:
	    getAstrometry();
	    getAutoguider(); 
	    getCalibrate(); 
	    getFocus(); 
	    getLimits();
	    getMechanisms();
	    getMeteorology();
	    getSource(); 
	    getState();
	    getVersion(); 
	    getTime();
	    break;
	}

	return buff.toString();
     
    }

    private void getAstrometry () {
	
	buff.append(" &refractionPressure "+status.astrometry.refractionPressure);      
	buff.append(" &refractionTemperature "+status.astrometry.refractionTemperature);	
	buff.append(" &refractionHumidity "+status.astrometry.refractionHumidity);	
	buff.append(" &refractionWavelength "+status.astrometry.refractionWavelength);	
	buff.append(" &ut1_utc "+status.astrometry.ut1_utc);		
	buff.append(" &tdt_utc "+status.astrometry.tdt_utc);		
	buff.append(" &polarMotion_X "+status.astrometry.polarMotion_X);		
	buff.append(" &polarMotion_Y "+status.astrometry.polarMotion_Y);		
	buff.append(" &airmass "+status.astrometry.airmass);	
	buff.append(" &agwavelength "+status.astrometry.agwavelength);

    }

    private void getAutoguider () {
	
	buff.append(" &agSelected "+      status.autoguider.agSelected);
	buff.append(" &agStatus "+        status.autoguider.agStatus);		
	buff.append(" &agMode "+          status.autoguider.agMode);			
	buff.append(" &fwhm "+            status.autoguider.fwhm);	
	
	buff.append(" &agMirrorDemand "+  status.autoguider.agMirrorDemand);		
	buff.append(" &agMirrorPos "+     status.autoguider.agMirrorPos);		
	buff.append(" &agMirrorStatus "+  status.autoguider.agMirrorStatus);
		
	buff.append(" &agFocusDemand "+   status.autoguider.agFocusDemand);		
	buff.append(" &agFocusPos "+      status.autoguider.agFocusPos);		
	buff.append(" &agFocusStatus "+   status.autoguider.agFocusStatus);	
	
	buff.append(" &agFilterDemand "+  status.autoguider.agFilterDemand);	
	buff.append(" &agFilterPos "+     status.autoguider.agFilterDemand);	
	buff.append(" &agFilterStatus "+  status.autoguider.agFilterStatus);
	
    }

    private void getCalibrate () {
	
	buff.append(" &defAzError "+   status.calibrate.defAzError);		
	buff.append(" &defAltError "+  status.calibrate.defAltError);		
	buff.append(" &defCollError "+ status.calibrate.defCollError);	
	
	buff.append(" &currAzError "+  status.calibrate.currAzError);		
	buff.append(" &currAltError "+ status.calibrate.currAltError);	
	buff.append(" &currCollError "+status.calibrate.currCollError);	

	buff.append(" &lastAzError "+  status.calibrate.lastAzError);		
	buff.append(" &lastAltError "+ status.calibrate.lastAltError);	
	buff.append(" &lastCollError "+status.calibrate.lastCollError);	
 
	buff.append(" &currAzRms "+    status.calibrate.lastAzRms);		
	buff.append(" &currAltRms "+   status.calibrate.lastAltRms);	
	buff.append(" &currCollRms "+  status.calibrate.lastCollRms);
		
	buff.append(" &lastSkyRms "+   status.calibrate.lastSkyRms);

    }
    
    private void getFocus() {
	
	buff.append(" &station "+status.focalStation.station);		
	buff.append(" &instr "+  status.focalStation.instr);		
	buff.append(" &ag "+     status.focalStation.ag);
		
    }

    private void getLimits () {
	
	buff.append(" &azPosLimit "+    status.limits.azPosLimit);	
	buff.append(" &azNegLimit "+    status.limits.azNegLimit);
	
	buff.append(" &altPosLimit "+   status.limits.altPosLimit);	
	buff.append(" &altNegLimit "+   status.limits.altNegLimit);
	
	buff.append(" &rotPosLimit "+   status.limits.rotPosLimit);	
	buff.append(" &rotNegLimit "+   status.limits.rotNegLimit);
		
	buff.append(" &timeToAzLimit "+ status.limits.timeToAzLimit);
	buff.append(" &azLimitSense "+  status.limits.azLimitSense);

	buff.append(" &timeToAltLimit "+status.limits.timeToAltLimit);
	buff.append(" &altLimitSense "+ status.limits.altLimitSense);
	
	buff.append(" &timeToRotLimit "+status.limits.timeToRotLimit);
	buff.append(" &rotLimitSense "+ status.limits.rotLimitSense);
		
    }

    private void getMechanisms () {
	
	buff.append(" &azName "+               status.mechanisms.azName);	
	buff.append(" &azDemand "+             status.mechanisms.azDemand);	
	buff.append(" &azPos "+                status.mechanisms.azPos);	
	buff.append(" &azStatus "+             status.mechanisms.azStatus);
	
	buff.append(" &altName "+              status.mechanisms.altName);	
	buff.append(" &altDemand "+            status.mechanisms.altDemand);	
	buff.append(" &altPos "+               status.mechanisms.altPos);	
	buff.append(" &altStatus "+            status.mechanisms.altStatus);
	
	buff.append(" &rotName "+              status.mechanisms.rotName);	
	buff.append(" &rotDemand "+            status.mechanisms.rotDemand);	
	buff.append(" &rotPos "+               status.mechanisms.rotPos);	
	buff.append(" &rotMode "+              status.mechanisms.rotMode);	
	buff.append(" &rotSkyAngle "+          status.mechanisms.rotSkyAngle);	
	buff.append(" &rotStatus "+            status.mechanisms.rotStatus);
	
	buff.append(" &encShutter1Name "+      status.mechanisms.encShutter1Name);	
	buff.append(" &encShutter1Demand "+    status.mechanisms.encShutter1Demand);	
	buff.append(" &encShutter1Pos "+       status.mechanisms.encShutter1Pos);	
	buff.append(" &encShutter1Status "+    status.mechanisms.encShutter1Status);

	buff.append(" &encShutter2Name "+      status.mechanisms.encShutter2Name);	
	buff.append(" &encShutter2Demand "+    status.mechanisms.encShutter2Demand);	
	buff.append(" &encShutter2Pos "+       status.mechanisms.encShutter2Pos);	
	buff.append(" &encShutter2Status "+    status.mechanisms.encShutter2Status);

	buff.append(" &foldMirrorName "+       status.mechanisms.foldMirrorName);	
	buff.append(" &foldMirrorDemand "+     status.mechanisms.foldMirrorDemand);	
	buff.append(" &foldMirrorPos "+        status.mechanisms.foldMirrorPos);
	buff.append(" &foldMirrorStatus "+     status.mechanisms.foldMirrorStatus);
	
	buff.append(" &primMirrorName "+       status.mechanisms.primMirrorName);	
	buff.append(" &primMirrorCoverDemand "+status.mechanisms.primMirrorCoverDemand);	
	buff.append(" &primMirrorCoverPos "+   status.mechanisms.primMirrorCoverPos);
	buff.append(" &primMirrorCoverStatus "+status.mechanisms.primMirrorCoverStatus);
	
	buff.append(" &secMirrorName "+        status.mechanisms.secMirrorName);	
	buff.append(" &secMirrorDemand "+      status.mechanisms.secMirrorDemand);	
	buff.append(" &secMirrorPos "+         status.mechanisms.secMirrorPos);	
	buff.append(" &focusOffset "+          status.mechanisms.focusOffset);	
	buff.append(" &secMirrorStatus "+      status.mechanisms.secMirrorStatus);
	
	buff.append(" &primMirrorSysName "+    status.mechanisms.primMirrorSysName);	
	buff.append(" &primMirrorSysStatus "+  status.mechanisms.primMirrorSysStatus);	
	
    }

    private void getMeteorology () {
	
	buff.append(" &wmsStatus "+                status.meteorology.wmsStatus);
	buff.append(" &rainState "+                status.meteorology.rainState);
	
	buff.append(" &extTemperature "+           status.meteorology.extTemperature);
	buff.append(" &serrurierTrussTemperature "+status.meteorology.serrurierTrussTemperature);
	buff.append(" &oilTemperature "+           status.meteorology.oilTemperature);
	buff.append(" &primMirrorTemperature "+    status.meteorology.primMirrorTemperature);
	buff.append(" &secMirrorTemperature "+     status.meteorology.secMirrorTemperature);

	buff.append(" &windSpeed "+                status.meteorology.windSpeed);
	buff.append(" &windDirn "+                 status.meteorology.windDirn);
	buff.append(" &humidity "+                 status.meteorology.humidity);
	buff.append(" &pressure "+                 status.meteorology.pressure);
	buff.append(" &lightLevel "+               status.meteorology.lightLevel);
		
    }

    private void getSource () {
	
	buff.append(" &srcName "+       status.source.srcName);   
	buff.append(" &srcRa "+         status.source.srcRa);	
	buff.append(" &srcDec "+        status.source.srcDec);	
	buff.append(" &srcEquinox "+    status.source.srcEquinox);	
	buff.append(" &srcEpoch "+      status.source.srcEpoch);	
	buff.append(" &srcNsTrackRA "+  status.source.srcNsTrackRA);
	buff.append(" &srcNsTrackDec "+ status.source.srcNsTrackDec);
	buff.append(" &srcPmRA "+       status.source.srcPmRA);
	buff.append(" &srcPmDec "+      status.source.srcPmDec);	
	buff.append(" &srcParallax "+   status.source.srcParallax);
	buff.append(" &radialVelocity "+status.source.srcRadialVelocity);

    }

    private void getState () {
	
	buff.append(" &networkControlState "+     status.state.networkControlState);
	buff.append(" &engineeringOverrideState "+status.state.engineeringOverrideState);
	buff.append(" &telescopeState "+          status.state.telescopeState);	
	buff.append(" &tcsState "+                status.state.tcsState);
	buff.append(" &systemRestartFlag "+       status.state.systemRestartFlag);
	buff.append(" &systemShutdownFlag "+      status.state.systemShutdownFlag);
    
    }

    private void getTime () {
	
	buff.append(" &mjd "+status.time.mjd);
	buff.append(" &ut "+ status.time.ut1);
	buff.append(" &lst "+status.time.lst);
	
    }
    
    private void getVersion () {
	
	buff.append(" &tcsVersion "+status.version.tcsVersion);		
    }

}
