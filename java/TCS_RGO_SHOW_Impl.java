import ngat.message.RCS_TCS.*;
import java.util.*;

public class TCS_RGO_SHOW_Impl {

    protected TCS_Status status;

    protected int key;

    StringBuffer buff;

    public TCS_RGO_SHOW_Impl(TCS_Status status, String keyName) {
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
	
	buff.append(" <<X>>");

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

	// #### WATCH THREADING HERE !!
	// #### prepare(buff); // sets index to 0 and prepares buff for incoming data. 
	// #### add(status.limits.azPosLimit); // increment index and add "<<index>>" "value" to buff
	// #### add(status.limits.azNegLimit);
	// #### etc.......

	buff.append(" <<1>> "+    status.limits.azPosLimit);	
	buff.append(" <<2>> "+    status.limits.azNegLimit);
	
	buff.append(" <<3>> "+   status.limits.altPosLimit);	
	buff.append(" <<4>> "+   status.limits.altNegLimit);
	
	buff.append(" <<5>> "+   status.limits.rotPosLimit);	
	buff.append(" <<6>> "+   status.limits.rotNegLimit);
		
	buff.append(" <<7>> "+   status.limits.timeToAzLimit);
	buff.append(" <<8>> "+   TCS_Status.codeString(status.limits.azLimitSense));

	buff.append(" <<9>> "+   status.limits.timeToAltLimit);
	buff.append(" <<10>> "+  TCS_Status.codeString(status.limits.altLimitSense));
	
	buff.append(" <<11>> "+  status.limits.timeToRotLimit);
	buff.append(" <<12>> "+  TCS_Status.codeString(status.limits.rotLimitSense));
		
    }

    private void getMechanisms () {
	
	buff.append(" <<1>> "+  status.mechanisms.azDemand);	
	buff.append(" <<2>> "+  status.mechanisms.azPos);	
	buff.append(" <<3>> "+  TCS_Status.codeString(status.mechanisms.azStatus));	
	
	buff.append(" <<4>> "+  status.mechanisms.altDemand);	
	buff.append(" <<5>> "+  status.mechanisms.altPos);	
	buff.append(" <<6>> "+  TCS_Status.codeString(status.mechanisms.altStatus));	
	
	buff.append(" <<7>> "+  status.mechanisms.rotDemand);	
	buff.append(" <<8>> "+  status.mechanisms.rotPos);	
	buff.append(" <<9>> "+  TCS_Status.codeString(status.mechanisms.rotMode));	
	buff.append(" <<10>> "+ status.mechanisms.rotSkyAngle);	
	buff.append(" <<11>> "+ TCS_Status.codeString(status.mechanisms.rotStatus));
	
	buff.append(" <<12>> "+ TCS_Status.codeString(status.mechanisms.encShutter1Demand));	
	buff.append(" <<13>> "+ TCS_Status.codeString(status.mechanisms.encShutter1Pos));	
	buff.append(" <<14>> "+ TCS_Status.codeString(status.mechanisms.encShutter1Status));
	
	buff.append(" <<15>> "+ TCS_Status.codeString(status.mechanisms.encShutter2Demand));	
	buff.append(" <<16>> "+ TCS_Status.codeString(status.mechanisms.encShutter2Pos));	
	buff.append(" <<17>> "+ TCS_Status.codeString(status.mechanisms.encShutter2Status));
	
	buff.append(" <<18>> "+ TCS_Status.codeString(status.mechanisms.foldMirrorDemand));	
	buff.append(" <<19>> "+ TCS_Status.codeString(status.mechanisms.foldMirrorPos));
	buff.append(" <<20>> "+ TCS_Status.codeString(status.mechanisms.foldMirrorStatus));
		
	buff.append(" <<21>> "+ TCS_Status.codeString(status.mechanisms.primMirrorCoverDemand));	
	buff.append(" <<22>> "+ TCS_Status.codeString(status.mechanisms.primMirrorCoverPos));
	buff.append(" <<23>> "+ TCS_Status.codeString(status.mechanisms.primMirrorCoverStatus));
	       
	buff.append(" <<24>> "+ status.mechanisms.secMirrorDemand);	
	buff.append(" <<25>> "+ status.mechanisms.secMirrorPos);	
	buff.append(" <<26>> "+ status.mechanisms.focusOffset);	
	buff.append(" <<27>> "+ TCS_Status.codeString(status.mechanisms.secMirrorStatus));
	
	buff.append(" <<28>> "+ TCS_Status.codeString(status.mechanisms.primMirrorSysStatus));	
	
    }

    private void getMeteorology () {
	
	buff.append(" <<1>>"+  TCS_Status.codeString(status.meteorology.wmsStatus));
	buff.append(" <<2>>"+  TCS_Status.codeString(status.meteorology.rainState));

	buff.append(" <<3>>"+  status.meteorology.serrurierTrussTemperature);
	buff.append(" <<4>>"+  status.meteorology.oilTemperature);
	buff.append(" <<5>>"+  status.meteorology.primMirrorTemperature);
	buff.append(" <<6>>"+  status.meteorology.secMirrorTemperature);
	buff.append(" <<7>>"+  status.meteorology.extTemperature);

	buff.append(" <<8>>"+  status.meteorology.windSpeed);
	buff.append(" <<9>>"+  status.meteorology.pressure);
	buff.append(" <<10>>"+ status.meteorology.humidity);
	buff.append(" <<11>>"+ status.meteorology.windDirn);
	buff.append(" <<12>>"+ status.meteorology.lightLevel);
		
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
	buff.append(" <<1>> "+   TCS_Status.codeString(status.state.networkControlState)); 
	buff.append(" <<2>> "+   TCS_Status.codeString(status.state.engineeringOverrideState));
	buff.append(" <<3>> "+   TCS_Status.codeString(status.state.telescopeState));
	buff.append(" <<4>> "+   TCS_Status.codeString(status.state.tcsState));
	buff.append(" <<5>> "+   status.state.systemRestartFlag);
	buff.append(" <<6>> "+   status.state.systemShutdownFlag);		
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
