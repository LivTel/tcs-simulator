import java.util.*;

import ngat.message.RCS_TCS.*;
import ngat.astrometry.*;
import ngat.phase2.*;
import ngat.util.*;

/** Simulate the telescope axes. */
public class Axes extends ControlThread {

	/** Telescope status block. */
	private TCS_Status telescope;

	/** Update interval (ms). */
	private long interval;

	/** Azimuth axis tracking request state. */
	private int azTrackingState;

	/** Altitude axis tracking request state. */
	private int altTrackingState;

	/** Rotator axis tracking request state. */
	private int rotTrackingState;

	/** Rotator request (MOUNT or SKY) angle (deg). */
	private double rotator;

	/** Set true to indicate that a fixed (alt/az) position is being targetted. */
	private boolean fixed;

	/**
	 * Create an Axes with given update interval.
	 * 
	 * @param telescope
	 *            Telescope status block.
	 * @param interval
	 *            Update interval (ms).
	 */
	public Axes(TCS_Status telescope, long interval) {
		super("Axes", true);

		this.telescope = telescope;
		this.interval = interval;

		azTrackingState = TCS_Status.STATE_OFF;
		altTrackingState = TCS_Status.STATE_OFF;
		rotTrackingState = TCS_Status.STATE_OFF;

		rotator = 0.0;

		fixed = true;

	}

	public void setAzTrackingState(boolean on) {
		azTrackingState = setState(on);
	}

	public void setAltTrackingState(boolean on) {
		altTrackingState = setState(on);
	}

	public void setRotTrackingState(boolean on) {
		rotTrackingState = setState(on);
	}

	private int setState(boolean on) {
		return (on ? TCS_Status.STATE_ON : TCS_Status.STATE_OFF);
	}

	/**
	 * Set the source parameters and calculate any new demands.
	 * 
	 * @param ra
	 *            Source RA (rads).
	 * @param dec
	 *            Source dec (rads).
	 */
	public void slew(double ra, double dec) {

		// calculate the new az,alt,rot demands.
		// taking into account wraps.
		// thats it..

		telescope.source.srcRa = Math.toDegrees(ra);
		telescope.source.srcDec = Math.toDegrees(dec);

		fixed = false;

		calculateAzDemand();
		calculateAltDemand();
		calculateRotDemand();

	}

	/** Move to wrap az (rads). */
	public void goAzimuth(double az) {

		fixed = true;

		azTrackingState = TCS_Status.STATE_OFF;
		altTrackingState = TCS_Status.STATE_OFF;
		rotTrackingState = TCS_Status.STATE_OFF;
		telescope.mechanisms.azDemand = Math.toDegrees(az);
	}

	/** Move to alt (rads). */
	public void goAltitude(double alt) {

		fixed = true;

		azTrackingState = TCS_Status.STATE_OFF;
		altTrackingState = TCS_Status.STATE_OFF;
		rotTrackingState = TCS_Status.STATE_OFF;
		telescope.mechanisms.altDemand = Math.toDegrees(alt);
	}

	/** Move to rot (rads). */
	public void goRotator(double rot) {

		fixed = true;

		azTrackingState = TCS_Status.STATE_OFF;
		altTrackingState = TCS_Status.STATE_OFF;
		rotTrackingState = TCS_Status.STATE_OFF;
		telescope.mechanisms.rotMode = TCS_Status.ROT_MOUNT;
		telescope.mechanisms.rotDemand = Math.toDegrees(rot);

	}

	/**
	 * Set the rotator parameters and calculate any new demands.
	 * 
	 * @param mode
	 *            Rotator mode.
	 * @param angle
	 *            Rotator angle (rads).
	 */
	public void rotate(int mode, double angle) {

		telescope.mechanisms.rotMode = mode;
		rotator = Math.toDegrees(angle);

		calculateAzDemand();
		calculateAltDemand();
		calculateRotDemand();

	}

	protected void initialise() {
	}

	protected void mainTask() {

		try {
			Thread.sleep(interval);
		} catch (InterruptedException ix) {
			System.err.println("Axes::interrupted");
		}

		System.err.println("Axes::Update");

		if (azTrackingState == TCS_Status.STATE_ON) {

			calculateAzDemand();

		}

		if (altTrackingState == TCS_Status.STATE_ON) {

			calculateAltDemand();

		}

		if (rotTrackingState == TCS_Status.STATE_ON) {

			calculateRotDemand();

		}

		stepAz();
		stepAlt();
		stepRot();

	}

	protected void shutdown() {
	}

	/** Calculate new axis demands. */
	private void calculateAzDemand() {

		Position target = new Position(Math.toRadians(telescope.source.srcRa),
				Math.toRadians(telescope.source.srcDec));

		// This is the constrained value in std limits.
		double az = target.getAzimuth();
		// Convert to degrees in wrap.
		double daz = Math.toDegrees(az);
		double caz = telescope.mechanisms.azPos;
		double waz = azWrap(daz, caz); // degs.
		telescope.mechanisms.azDemand = waz;
		System.err.println("Axes::Azimuth demand: "
				+ telescope.mechanisms.azDemand + ", Current: "
				+ telescope.mechanisms.azPos);
	}

	/** Calculate new axis demands. */
	private void calculateAltDemand() {

		Position target = new Position(Math.toRadians(telescope.source.srcRa),
				Math.toRadians(telescope.source.srcDec));

		// This is the constrained value in std limits.
		double alt = target.getAltitude();
		telescope.mechanisms.altDemand = Math.toDegrees(alt);
		System.err.println("Axes::Altitude demand: "
				+ telescope.mechanisms.altDemand + ", Current: "
				+ telescope.mechanisms.altPos);
	}

	/** Calculate new axis demands. */
	private void calculateRotDemand() {

		Position target = new Position(Math.toRadians(telescope.source.srcRa),
				Math.toRadians(telescope.source.srcDec));

		double caz = Math.toRadians(telescope.mechanisms.azPos);
		double waz = correct(3.0 * Math.PI - caz);
		double calt = Math.toRadians(telescope.mechanisms.altPos);
		double lat = Math.toRadians(TelescopeStatus.lat);

		double cphi = Math.cos(lat);
		double sphi = Math.sin(lat);

		double para = JSlalib.calcBearing(waz, calt, Math.PI, lat);

		para = -para; // rads.
		System.err
				.println("Paralactic angle is: " + Position.toDegrees(para, 3));

		double rot = 0.0;
		double wrot = 0.0;
		double drot = 0.0;

		double crot = telescope.mechanisms.rotPos;
		double ttrot = 0.0;
		switch (telescope.mechanisms.rotMode) {
		case TCS_Status.ROT_MOUNT:
			drot = rotator;
			wrot = drot;
			ttrot = Math.abs(wrot - crot) / 2.0;
			ttrot = ttrot * 1000.0;
			System.err.println("Rotator slew mount: " + ttrot + " msec");
			break;
		case TCS_Status.ROT_SKY:
			rot = correct(2 * Math.PI + para + Math.toRadians(rotator)); // (0,
																			// 2*PI).
			telescope.mechanisms.rotSkyAngle = rotator;
			System.err.println("Setting skyangle to: "
					+ telescope.mechanisms.rotSkyAngle);
			drot = Math.toDegrees(rot);
			wrot = rotWrap(drot, crot); // degs.
			ttrot = Math.abs(wrot - crot) / 2.0;
			ttrot = ttrot * 1000.0;
			System.err.println("Rotator slew sky: " + ttrot + " msec");
			break;
		case TCS_Status.ROT_VERTICAL:
			drot = 0.0;
			wrot = rotWrap(drot, crot); // degs.
			break;
		case TCS_Status.ROT_VFLOAT:
			rot = 0.0;
			drot = Math.toDegrees(rot);
			wrot = rotWrap(drot, crot); // degs.
			if (para < 0.0)
				telescope.mechanisms.rotSkyAngle = Math.toDegrees(-para);
			else
				telescope.mechanisms.rotSkyAngle = Math.toDegrees(2.0 * Math.PI
						- para);
			break;
		}
		telescope.mechanisms.rotDemand = wrot;
		System.err.println("Axes::Rotator demand: " + wrot + ", Current: "
				+ telescope.mechanisms.rotPos);
	}

	/**
	 * Calculates azimuth wrap position.
	 * 
	 * @param daz
	 *            Demand azimuth. (
	 * @param caz
	 *            Current azimuth (wrap).
	 * @return wrap az demand (degs).
	 */
	private double azWrap(double daz, double caz) {

		double waz = 0.0;
		double waz1 = 0.0;
		double waz2 = 0.0;
		double limL = 0.0;
		double limR = 0.0;
		double deltaL = 0.0;
		double deltaR = 0.0;
		double ttaz = 0.0;
		if (daz > 0.0 && daz < 180.0) {
			// Can only be here.
			waz = daz;
			// System.out.println("ONE CHOICE: ");
			// System.out.println("WRAP to: "+waz);
			if (waz < telescope.mechanisms.azPos) {
				ttaz = (telescope.mechanisms.azPos - waz) / 2.0;

				System.err.println("Axes::Azwrap::Single wrap az-slew LEFT by"
						+ (telescope.mechanisms.azPos - waz));
			} else {
				ttaz = (waz - telescope.mechanisms.azPos) / 2.0;

				System.err.println("Axes::Azwrap::Single wrap az-slew RIGHT by"
						+ (waz - telescope.mechanisms.azPos));
			}
		} else {
			// 2 Choices.
			// System.out.println("TWO CHOICES: ");
			waz2 = daz - 180.0;
			waz1 = daz - 360.0;
			limL = telescope.mechanisms.azPos + 180.0;
			limR = 360.0 - telescope.mechanisms.azPos;
			deltaL = telescope.mechanisms.azPos - waz1;
			deltaR = waz2 - telescope.mechanisms.azPos;
			// System.out.println("WRAP1: "+waz1);
			// System.out.println("WRAP2: "+waz2);
			// System.out.println("LIMITS L: "+limL+" R: "+limR);
			// System.out.println("DELTAS L: "+deltaL+" R: "+deltaR);
			if (deltaL > limL) {

				waz = waz2;
				ttaz = deltaR / 2.0;
				System.err.println("Axes::Azwrap::Must az-slew RIGHT " + deltaR
						+ " (" + deltaL + ") to limit at " + limL);
			} else if (deltaR > limR) {

				waz = waz1;
				ttaz = deltaL / 2.0;
				System.err.println("Axes::Azwrap::Must az-slew LEFT " + deltaL
						+ " (" + deltaR + ") to limit at " + limR);
			} else {
				if (deltaL < deltaR) {

					waz = waz1;
					ttaz = deltaL / 2.0;
					System.err.println("Axes::Azwrap::Shortest az-Slew LEFT "
							+ deltaL + " (" + deltaR + ")");
				} else {

					waz = waz2;
					ttaz = deltaR / 2.0;
					System.err.println("Axes::Azwrap::Shortest az-Slew RIGHT "
							+ deltaR + " (" + deltaL + ")");
				}
			}
		}

		return waz;
	}

	/**
	 * Calculates rotator wrap position.
	 * 
	 * @param daz
	 *            Demand rotator. (
	 * @param caz
	 *            Current rotator (wrap).
	 * @return wrap rot demand (degs).
	 */
	private double rotWrap(double drot, double crot) {

		double wrot = 0.0;
		double wrot1 = 0.0;
		double wrot2 = 0.0;
		double limL = 0.0;
		double limR = 0.0;
		double deltaL = 0.0;
		double deltaR = 0.0;
		double ttrot = 0.0;
		if ((drot > 240.0 && drot < 360.0) || (drot > 0.0 && drot < 120.0)) {
			// Can only be here.
			if (drot > 240.0 && drot < 360.0)
				wrot = drot - 360.0;
			if (drot > 0.0 && drot < 120.0)
				wrot = drot;

			// System.out.println("ONE CHOICE: ");
			// System.out.println("WRAP to: "+waz);
			if (wrot < telescope.mechanisms.rotPos) {
				ttrot = (telescope.mechanisms.rotPos - wrot) / 2.0;

				System.err
						.println("Axes::Rotwrap::Single wrap rot-slew LEFT by"
								+ (telescope.mechanisms.rotPos - wrot));
			} else {
				ttrot = (wrot - telescope.mechanisms.rotPos) / 2.0;

				System.err
						.println("Axes::Rotwrap::Single wrap rot-slew RIGHT by"
								+ (wrot - telescope.mechanisms.rotPos));
			}
		} else {
			// 2 Choices.
			// System.out.println("TWO CHOICES: ");
			wrot2 = drot;
			wrot1 = drot - 360.0;
			limL = telescope.mechanisms.rotPos + 240.0;
			limR = 240.0 - telescope.mechanisms.rotPos;
			deltaL = telescope.mechanisms.rotPos - wrot1;
			deltaR = wrot2 - telescope.mechanisms.rotPos;
			// System.out.println("WRAP1: "+waz1);
			// System.out.println("WRAP2: "+waz2);
			// System.out.println("LIMITS L: "+limL+" R: "+limR);
			// System.out.println("DELTAS L: "+deltaL+" R: "+deltaR);
			if (deltaL > limL) {

				wrot = wrot2;
				ttrot = deltaR / 2.0;
				System.err.println("Axes::Rotwrap::Must rot-slew RIGHT "
						+ deltaR + " (" + deltaL + ") to limit at " + limL);
			} else if (deltaR > limR) {

				wrot = wrot1;
				ttrot = deltaL / 2.0;
				System.err.println("Axes::Rotwrap::Must rot-slew LEFT "
						+ deltaL + " (" + deltaR + ") to limit at " + limR);
			} else {
				if (deltaL < deltaR) {

					wrot = wrot1;
					ttrot = deltaL / 2.0;
					System.err.println("Axes::Rotwrap::Shortest rot-Slew LEFT "
							+ deltaL + " (" + deltaR + ")");
				} else {

					wrot = wrot2;
					ttrot = deltaR / 2.0;
					System.err
							.println("Axes::Rotwrap::Shortest rot-Slew RIGHT "
									+ deltaR + " (" + deltaL + ")");
				}
			}
		}

		return wrot;
	}

	/** Timestep the azimuth axis. */
	private void stepAz() {

		// ee = 1/2*acceln*interval**2 - but need to anticipate overshoot so use
		// 1.5*v*int

		double ee = 3.0 * interval / 1000.0;
		double delta = Math.abs(telescope.mechanisms.azPos
				- telescope.mechanisms.azDemand);

		System.err.println("Axes::StepAz::AzDmd: "
				+ telescope.mechanisms.azDemand + ", AzPos:"
				+ telescope.mechanisms.azPos + ", Delta: " + delta
				+ ", MinDelta: " + ee);

		if (delta > ee) {

			telescope.mechanisms.azStatus = TCS_Status.MOTION_MOVING;

			// Calculate the slew distance - if clever use acceln/decceln and
			// not just the max speed - for now, not clever..

			double ddaz = 2.0 * (double) interval / 1000.0;
			// Choose direction.
			if (telescope.mechanisms.azDemand < telescope.mechanisms.azPos)
				ddaz = -ddaz;
			telescope.mechanisms.azPos += ddaz;

		} else {

			if (azTrackingState == TCS_Status.STATE_ON) {

				telescope.mechanisms.azStatus = TCS_Status.MOTION_TRACKING;
				telescope.mechanisms.azPos = telescope.mechanisms.azDemand;

			} else {

				telescope.mechanisms.azStatus = TCS_Status.MOTION_INPOSITION;
				if (fixed) {
					telescope.mechanisms.azPos = telescope.mechanisms.azDemand;
				}
			}

		}
	}

	/** Timestep the altitude axis. */
	private void stepAlt() {

		// ee = 1/2*acceln*interval**2 - but need to anticipate overshoot so use
		// 1.5*v*int

		double ee = 3.0 * interval / 1000.0;
		double delta = Math.abs(telescope.mechanisms.altPos
				- telescope.mechanisms.altDemand);

		System.err.println("Axes::StepAlt::AltDmd: "
				+ telescope.mechanisms.altDemand + ", AltPos:"
				+ telescope.mechanisms.altPos + ", Delta: " + delta
				+ ", MinDelta: " + ee);

		if (delta > ee) {

			telescope.mechanisms.altStatus = TCS_Status.MOTION_MOVING;

			// Calculate the slew distance - if clever use acceln/decceln and
			// not just the max speed - for now, not clever..

			double ddalt = 2.0 * (double) interval / 1000.0;
			// Choose direction.
			if (telescope.mechanisms.altDemand < telescope.mechanisms.altPos)
				ddalt = -ddalt;
			telescope.mechanisms.altPos += ddalt;

		} else {

			if (altTrackingState == TCS_Status.STATE_ON) {

				telescope.mechanisms.altStatus = TCS_Status.MOTION_TRACKING;
				telescope.mechanisms.altPos = telescope.mechanisms.altDemand;

			} else {

				telescope.mechanisms.altStatus = TCS_Status.MOTION_INPOSITION;
				if (fixed) {
					telescope.mechanisms.altPos = telescope.mechanisms.altDemand;
				}
			}

		}
	}

	/** Timestep the rotator axis. */
	private void stepRot() {
		// ee = 1/2*acceln*interval**2 - but need to anticipate overshoot so use
		// 1.5*v*int

		double ee = 3.0 * interval / 1000.0;
		double delta = Math.abs(telescope.mechanisms.rotPos
				- telescope.mechanisms.rotDemand);

		System.err.println("Axes::StepRot::RotDmd: "
				+ telescope.mechanisms.rotDemand + ", RotPos:"
				+ telescope.mechanisms.rotPos + ", Delta: " + delta
				+ ", MinDelta: " + ee);

		if (delta > ee) {

			telescope.mechanisms.rotStatus = TCS_Status.MOTION_MOVING;

			// Calculate the slew distance - if clever use acceln/decceln and
			// not just the max speed - for now, not clever..

			double ddrot = 2.0 * (double) interval / 1000.0;
			// Choose direction.
			if (telescope.mechanisms.rotDemand < telescope.mechanisms.rotPos)
				ddrot = -ddrot;
			telescope.mechanisms.rotPos += ddrot;

		} else {

			if (rotTrackingState == TCS_Status.STATE_ON) {

				telescope.mechanisms.rotStatus = TCS_Status.MOTION_TRACKING;
				telescope.mechanisms.rotPos = telescope.mechanisms.rotDemand;

			} else {

				telescope.mechanisms.rotStatus = TCS_Status.MOTION_INPOSITION;
				if (fixed) {
					telescope.mechanisms.rotPos = telescope.mechanisms.rotDemand;
				}
			}

		}
	}

	/** Puts an angle (rads) into the correct (0 -> 2*PI) range. */
	protected double correct(double angle) {
		double a = angle + 2.0 * Math.PI;
		while (a >= 2 * Math.PI)
			a -= 2.0 * Math.PI;
		return a;
	}

}
