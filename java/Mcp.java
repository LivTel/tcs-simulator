import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

/** Simulate the telescope MCP. */
public class Mcp extends ControlThread {

	public static final int STATE_SUSPEND = 1;

	public static final int STATE_RECOVERING = 2;

	public static final int STATE_OPER_ON = 3;

	public static final int STATE_OPERATIONAL = 4;

	public static final int STATE_OPER_OFF = 5;

	public static final int STATE_WEATHERED = 6;

	public static final int STATE_STANDBY = 7;

	/** Telescope status block. */
	private TCS_Status sdb;

	/** Update interval (ms). */
	private long interval;

	/** Used to model the state of the SPLC and other item. */
	private int splcState = TCS_Status.STATE_OKAY;

	/** Models current state in MCP state model. */
	private int state = STATE_SUSPEND;

	private boolean operOnRequested = false;
	private boolean operOffRequested = false;

	/**
	 * Create an Mcp with given update interval.
	 * 
	 * @param sdb
	 *            Telescope status block.
	 * @param interval
	 *            Update interval (ms).
	 */
	public Mcp(TCS_Status sdb, long interval) {
		super("MCP", true);

		this.sdb = sdb;
		this.interval = interval;

	}

	/** Request OPER ON to be performed. */
	public void requestOperOn() {

		operOnRequested = true;

		// Set demands for various homing actions.

		// Home axes - omly if they need homing.

		Subsystems.azm.home();
		Subsystems.alt.home();
		Subsystems.rot.home();

		// Home focus.
		Subsystems.smf.move(5.0);

		// Move fold.

		// Move ag.

	}

	/** Request OPER OFF to be performed. */
	public void requestOperOff() {

		operOffRequested = true;

		// Zero focus.
		Subsystems.smf.move(0.0);

	}

	/** Request Axes to boot. */
	public void bootAxes() {

		if (sdb.mechanisms.azStatus == TCS_Status.STATE_ERROR)
			Subsystems.azm.boot();

		if (sdb.mechanisms.altStatus == TCS_Status.STATE_ERROR)
			Subsystems.alt.boot();

		if (sdb.mechanisms.rotStatus == TCS_Status.STATE_ERROR)
			Subsystems.rot.boot();

	}

	public void closeEnclosure() {

		Subsystems.enc.close();

	}

	public void stopAxes() {
		Subsystems.ast.stopAxes();
	}

	protected void initialise() {
	}

	protected void mainTask() {

		try {
			Thread.sleep(interval);
		} catch (InterruptedException ix) {
			System.err.println("MCP::interrupted");
		}

		boolean wmsOkay = true;
		boolean axesOkay = true;
		boolean opHours = true;

		Position sun = Astrometry.getSolarPosition();
		System.err.println("MCP::Sun elev: "
				+ Position.toDegrees(sun.getAltitude(), 3));
		if (sun.isRisen())
			opHours = false;

		if (sdb.meteorology.wmsStatus != TCS_Status.STATE_OKAY)
			wmsOkay = false;

		if (sdb.mechanisms.azStatus == TCS_Status.STATE_ERROR
				|| sdb.mechanisms.altStatus == TCS_Status.STATE_ERROR
				|| sdb.mechanisms.rotStatus == TCS_Status.STATE_ERROR)
			axesOkay = false;

		System.err.println("MCP::Current state:" + toStateString(state)
				+ " WMS: " + (wmsOkay ? "OKAY" : "SUSPEND") + " AXES: "
				+ (axesOkay ? "OKAY" : "SUSPEND") + " HOURS: "
				+ (opHours ? "OKAY" : "SUSPEND"));

		switch (state) {
		// Axes problem.
		case STATE_SUSPEND:
			if (wmsOkay && axesOkay) {

				sdb.state.telescopeState = TCS_Status.STATE_STANDBY;
				sdb.state.networkControlState = TCS_Status.STATE_ENABLED;

				state = STATE_STANDBY;

			} else if (!axesOkay) {

				bootAxes();
				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;
				sdb.state.networkControlState = TCS_Status.STATE_DISABLED;

				state = STATE_RECOVERING;

			}
			break;
		case STATE_RECOVERING:
			if (wmsOkay && axesOkay) {

				sdb.state.telescopeState = TCS_Status.STATE_STANDBY;
				sdb.state.networkControlState = TCS_Status.STATE_ENABLED;

				state = STATE_STANDBY;

			} else if (!wmsOkay) {

				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;
				sdb.state.networkControlState = TCS_Status.STATE_DISABLED;

				state = STATE_SUSPEND;

			}
			break;
		case STATE_STANDBY:
			if (!wmsOkay || !axesOkay) {

				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;
				sdb.state.networkControlState = TCS_Status.STATE_DISABLED; // ???

				state = STATE_SUSPEND;

			} else if (operOnRequested) {

				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;
				sdb.state.networkControlState = TCS_Status.STATE_DISABLED;

				state = STATE_OPER_ON;

			}
			break;
		case STATE_OPER_ON:
			if (!axesOkay) {

				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;

				state = STATE_SUSPEND;

			} else if (operOnOkay()) {

				if (!wmsOkay) {

					sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;
					sdb.state.networkControlState = TCS_Status.STATE_DISABLED;

					state = STATE_WEATHERED;

				} else {

					sdb.state.telescopeState = TCS_Status.STATE_OKAY;
					sdb.state.networkControlState = TCS_Status.STATE_ENABLED;

					state = STATE_OPERATIONAL;

				}
			}
			break;
		case STATE_OPERATIONAL:
			if (!axesOkay) {

				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;

				state = STATE_SUSPEND;

			} else if (!wmsOkay) {

				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;
				sdb.state.networkControlState = TCS_Status.STATE_DISABLED;

				closeEnclosure();
				stopAxes();

				state = STATE_WEATHERED;

			} else if (operOffRequested) {

				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;
				sdb.state.networkControlState = TCS_Status.STATE_DISABLED;

				closeEnclosure();
				stopAxes();

				state = STATE_OPER_OFF;

			}
			break;
		case STATE_OPER_OFF:
			if (operOffOkay()) {

				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;
				sdb.state.networkControlState = TCS_Status.STATE_DISABLED;

				state = STATE_SUSPEND;

			}

			break;
		case STATE_WEATHERED:
			if (!axesOkay) {

				sdb.state.telescopeState = TCS_Status.STATE_SUSPENDED;

				state = STATE_SUSPEND;

			} else if (wmsOkay) {

				sdb.state.telescopeState = TCS_Status.STATE_OKAY;
				sdb.state.networkControlState = TCS_Status.STATE_ENABLED;

				state = STATE_OPERATIONAL;

			}
			break;
		}

	}

	public boolean operOnOkay() {
		double ddfoc = Math.abs(sdb.mechanisms.secMirrorDemand
				- sdb.mechanisms.secMirrorPos);

		// Focus in posn, all axes stopped.
		if ((ddfoc < 0.5)
				&& sdb.mechanisms.azStatus == TCS_Status.MOTION_STOPPED
				&& sdb.mechanisms.altStatus == TCS_Status.MOTION_STOPPED
				&& sdb.mechanisms.rotStatus == TCS_Status.MOTION_STOPPED) {

			return true;
		}

		return false;

	}

	public boolean operOffOkay() {
		double ddfoc = Math.abs(sdb.mechanisms.secMirrorDemand
				- sdb.mechanisms.secMirrorPos);

		if (ddfoc < 0.5)
			return true;

		return false;

	}

	public String toStateString(int state) {
		switch (state) {

		case STATE_SUSPEND:
			return "SUSPEND";
		case STATE_RECOVERING:
			return "RECOVERING";
		case STATE_OPER_ON:
			return "OPER_ON";
		case STATE_OPERATIONAL:
			return "OPERATIONAL";
		case STATE_OPER_OFF:
			return "OPER_OFF";
		case STATE_WEATHERED:
			return "WEATHERED";
		case STATE_STANDBY:
			return "STANDBY";
		}
		return "UNKNOWN";
	}

	protected void shutdown() {
	}

}
