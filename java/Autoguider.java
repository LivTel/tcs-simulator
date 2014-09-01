import ngat.message.RCS_TCS.*;
import ngat.util.*;

public class Autoguider extends ControlThread {

    public static final int LOCK = 1;
    public static final int UNLOCK = 2;

    private TCS_Status sdb;

    private long interval;

    private int mode;
    
    private long timeToAcquire;

    public Autoguider(TCS_Status sdb, long interval) {
	super("AUTOGUIDER", true);
	this.sdb = sdb;
	this.interval = interval;
	mode = UNLOCK;
    }

    public void initialise() {}

    public void mainTask() {

	try { Thread.sleep(interval); } catch (InterruptedException ix) {System.err.println("PMC::interrupted");}

	switch (mode) {

	case UNLOCK:
	    sdb.autoguider.agStatus = TCS_Status.AG_UNLOCKED;
	    break;
	case LOCK:
	    // we will acquire when time has passed..
	    if (System.currentTimeMillis() > timeToAcquire) 
		sdb.autoguider.agStatus = TCS_Status.AG_LOCKED;	    
	    break;
	}

    }

    public void shutdown() {}

    public void acquire() {
	mode = LOCK;
        timeToAcquire = System.currentTimeMillis() + (long)(Math.random()*30000.0);
    }

    public void stopGuiding() { 
	mode = UNLOCK;
    }

}
