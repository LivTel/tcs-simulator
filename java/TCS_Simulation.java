import java.util.*;
import java.text.*;
import ngat.message.RCS_TCS.*;
import ngat.util.*;

/** Simulates a TCS. Holds state variables and updates these in a thread on response
 * to incoming commands and generated events placed in an event queue.
 */

public class TCS_Simulation {

    /** The RCS ID - as specified in CIL Header Specification Doc.*/
    public static final int RCS_ID = 18;
    
    /** The TCS ID - as specified in CIL Header Specification Doc.*/
    public static final int TCS_ID = 17;
    
    /** The Command message class - as specified in CIL Header Specification Doc.*/
    public static final int COMMAND_CLASS  = 1; 
    public static final int RESPONSE_CLASS = 2;
    public static final int ACK_CLASS      = 3; 
    public static final int ACTION_CLASS   = 4;
    public static final int DONE_CLASS     = 5;
    public static final int ERROR_CLASS    = 6;
    
    /** The TCS command port - ## TBD to config ##.*/
    public static final int TCS_PORT = 5678;
    
    /** The TCS host address - ## TBD to config ##.*/
    public static final String TCS_HOST = "staru1.livjm.ac.uk";
    
    /** The Service type -  as specified in CIL Header Specification Doc.*/
    public static final int SERVICE_TYPE = 5;
    
    /** The TCS environment variables.*/
    private TCS_Status sdb;         

    /**Server for RCS commands.*/
    private TCS_Server server;      

    /** Generates weather and other environment.*/
    private TCS_Updater updater;    

    /** Holds a list of future events.*/
    private TCS_Event_Queue events; 

    /** Telescope axes.*/
    private Axes axes;

    /** Telescope enclosure.*/
    private Enclosure enclosure;
    
    /** Secondary mirror.*/
    private Focus focus;

    /** MCP.*/
    private Mcp mcp;

    /** WMS.*/
    private Wms wms;

    /** Sim interval (ms).*/
    public long interval; 

    public double latitude;

    public double longitude;

    public static long simStartTime;

    private static void usage() {
	System.out.println("java TCS_Simulation -port <port> -interval <interval>"+
			   "\n port: the server port (1024 < port < 65534)."+
			   "\n interval: the update interval (ms)."+
			   "\n latitude: the site latitude (deg)"+
			   "\n longitude: the site longitude (E+) (deg)");
    }
    
    public static void main(String args[]) {
	if (args == null || args.length == 0) {
	    usage();
	    return;
	}
	TCS_Simulation sim = new TCS_Simulation(args);
    }
    
    public TCS_Simulation(String args[]) {
	CommandTokenizer parser = new CommandTokenizer("--");	
	parser.parse(args);

	ConfigurationProperties map = parser.getMap();

	simStartTime = System.currentTimeMillis();

	int port = map.getIntValue("port", TCS_PORT);
	interval = map.getLongValue("interval", 1000L);	
	
	latitude  = Math.toRadians(map.getDoubleValue("latitude", 28.76));
	longitude = Math.toRadians(map.getDoubleValue("longitude",-17.8816));
	
	// Map state codes.
	TCS_Status.mapCodes();

	// Create and initialize the environment variables.
	sdb = new TCS_Status();
	
	initialize();
	
	// Create the eventQueue.
	events = new TCS_Event_Queue(this);

	// Create and start the environment updater.
	// #### TBD - Add proper weather generation and control stuff.
	updater = new TCS_Updater(this);
	updater.start();

	//axes = new Axes(sdb, interval);
	//axes.start();

	Subsystems.azm = new Azimuth(sdb, interval);
	Subsystems.azm.start();

	Subsystems.alt = new Altitude(sdb, interval);
	Subsystems.alt.start();

	Subsystems.rot = new Rotator(sdb, interval);
	Subsystems.rot.start();
	
	Subsystems.pmc = new MirrorCover(sdb, interval);
	Subsystems.pmc.start();

	Subsystems.enc = new Enclosure(sdb, interval);
	Subsystems.enc.start();

	Subsystems.smf = new Focus(sdb, interval);
	Subsystems.smf.start();

	Subsystems.mcp = new Mcp(sdb, interval);
	Subsystems.mcp.start();

	Subsystems.wms = new Wms(sdb, interval);
	Subsystems.wms.start();

	Subsystems.ast = new AstroKernel(sdb, interval);
	Subsystems.ast.start();

	Subsystems.ag  = new Autoguider(sdb, interval);
	Subsystems.ag.start();

	Subsystems.agf  = new AgFilter(sdb, interval);
	Subsystems.agf.start();

	Subsystems.scf   = new ScienceFold(sdb, interval);
	Subsystems.scf.start();

	// Create and start the Server.
	server = new TCS_Server(this, port, sdb);
	server.start();

    }

    public TCS_Status getStatus() { return sdb;}

    public TCS_Server getServer() { return server;}

    public TCS_Updater getUpdater() { return updater;}

    public TCS_Event_Queue getEventQueue() { return events;}
    
    public void log(String text) { System.out.println(text);}

    private void initialize() {
	// Astrometry.
	sdb.astrometry.refractionTemperature = 20.1;
	sdb.astrometry.refractionPressure    = 700.0;
	sdb.astrometry.refractionHumidity    = 67.0;
	sdb.astrometry.refractionWavelength  = 0.55;
	sdb.astrometry.airmass               = 1.0;
	sdb.astrometry.ut1_utc               = 10;
	sdb.astrometry.tdt_utc               = 15;

	sdb.astrometry.polarMotion_X         = +12.0;
	sdb.astrometry.polarMotion_Y         = -12.0;
	sdb.astrometry.agwavelength          = 0.56;

	// Autoguider.
	sdb.autoguider.agStatus       = TCS_Status.AG_UNLOCKED;
	//sdb.autoguider.agMode         = TCS_Status.AG_MODE_RANK;
	sdb.autoguider.agSwState      = TCS_Status.STATE_OKAY;
	sdb.autoguider.agMirrorStatus = TCS_Status.MOTION_INPOSITION;
	sdb.autoguider.agFocusStatus  = TCS_Status.MOTION_INPOSITION;
	sdb.autoguider.agFilterStatus = TCS_Status.MOTION_INPOSITION;
	sdb.autoguider.agFilterPos    = TCS_Status.POSITION_INLINE;

	// Calibrate.
	sdb.calibrate.currAltError  = 0.43;
	sdb.calibrate.currAzError   = 0.4;
	sdb.calibrate.currCollError = 1.1;


	sdb.calibrate.lastAltRms    = 1.5;
	sdb.calibrate.lastCollRms   = 1.5;
	sdb.calibrate.lastAzRms     = 1.5;
	
	sdb.calibrate.lastSkyRms    = 2.3;

	// Focus.
	sdb.focalStation.station = "CASSEGRAIN";
	sdb.focalStation.instr   = "RATCam";
	sdb.focalStation.ag      = "MSI-AUTOGUIDER";

	//Limits.
	sdb.limits.azPosLimit = 360.0;
	sdb.limits.azNegLimit = -180.0;
	sdb.limits.altPosLimit = 89.5;
	sdb.limits.altNegLimit = 20.0;
	sdb.limits.rotPosLimit = +240;
	sdb.limits.rotNegLimit = -240;
	sdb.limits.timeToRotLimit = 1000.0;
	sdb.limits.rotLimitSense  = TCS_Status.LIMIT_POSITIVE;
	sdb.limits.timeToAzLimit  = 1000.0;
	sdb.limits.azLimitSense   = TCS_Status.LIMIT_POSITIVE;
	sdb.limits.timeToAltLimit = 1000.0;
	sdb.limits.altLimitSense  = TCS_Status.LIMIT_POSITIVE;

	// Mechanisms.
	sdb.mechanisms.azStatus          = TCS_Status.MOTION_STOPPED;
	sdb.mechanisms.altStatus         = TCS_Status.MOTION_STOPPED;
	sdb.mechanisms.rotMode           = TCS_Status.ROT_SKY;
	sdb.mechanisms.rotStatus         = TCS_Status.MOTION_STOPPED;
	// start with shutter 1 open and 2 closed.
	sdb.mechanisms.encShutter1Demand = TCS_Status.POSITION_OPEN;
	sdb.mechanisms.encShutter1Pos    = TCS_Status.POSITION_OPEN;
	sdb.mechanisms.encShutter1Status = TCS_Status.MOTION_INPOSITION;

	sdb.mechanisms.encShutter2Demand = TCS_Status.POSITION_OPEN;
	sdb.mechanisms.encShutter2Pos    = TCS_Status.POSITION_OPEN;
	sdb.mechanisms.encShutter2Status = TCS_Status.MOTION_INPOSITION;

	sdb.mechanisms.foldMirrorDemand  = TCS_Status.POSITION_STOWED; 
	sdb.mechanisms.foldMirrorPos     = TCS_Status.POSITION_STOWED; 
	sdb.mechanisms.foldMirrorStatus  = TCS_Status.MOTION_STOPPED;

	sdb.mechanisms.primMirrorCoverDemand = TCS_Status.POSITION_CLOSED;
	sdb.mechanisms.primMirrorCoverPos    = TCS_Status.POSITION_CLOSED;	
	sdb.mechanisms.primMirrorCoverStatus = TCS_Status.MOTION_INPOSITION;

	sdb.mechanisms.secMirrorDemand = 0.0;
	sdb.mechanisms.secMirrorPos    = 0.0;
	sdb.mechanisms.focusOffset     = 0.0;
	sdb.mechanisms.secMirrorStatus     = TCS_Status.MOTION_STOPPED;
	sdb.mechanisms.primMirrorSysStatus = TCS_Status.STATE_OKAY; 

	// Meteorology.
	sdb.meteorology.wmsStatus                 = TCS_Status.STATE_OKAY;
	sdb.meteorology.rainState                 = TCS_Status.RAIN_CLEAR;
	sdb.meteorology.extTemperature            = 15.5;
	sdb.meteorology.serrurierTrussTemperature = 12.0;
	sdb.meteorology.oilTemperature            = 25.0;
	sdb.meteorology.primMirrorTemperature     = 8.0;
	sdb.meteorology.secMirrorTemperature      = 11.0;
	sdb.meteorology.windSpeed                 = 20.0;
	sdb.meteorology.windDirn                  = 90.0;
	sdb.meteorology.humidity                  = 0.12;
	sdb.meteorology.pressure                  = 802.0;
	sdb.meteorology.lightLevel                = 34.5;
	sdb.meteorology.agBoxTemperature          = 33.33;

	// Source.
	sdb.source.srcName = "no-source";
	
	// Services.
	sdb.services.powerState = TCS_Status.STATE_OKAY;

	// State.
	sdb.state.networkControlState      = TCS_Status.STATE_ENABLED;
	sdb.state.engineeringOverrideState = TCS_Status.STATE_DISABLED;
	sdb.state.telescopeState           = TCS_Status.STATE_INIT;
	sdb.state.tcsState                 = TCS_Status.STATE_INIT;
	sdb.state.systemRestartFlag        = false;
	sdb.state.systemShutdownFlag       = false;
    
    }

}


