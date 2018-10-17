package uavController;

import org.javatuples.Quintet;
import org.javatuples.Triplet;

import api.pojo.GeoCoordinates;
import api.pojo.UTMCoordinates;

/** This class generates and object that contains the most recent information received from the UAV. */

public class UAVCurrentData {

	private long time;					// (ns) Local time when the location was retrieved from the UAV
	private GeoCoordinates locationGeo;	// (degrees) longitude,latitude coordinates
	private UTMCoordinates locationUTM;	// (m) X,Y UTM coordinates
	private double z, zRelative;		// (m) Altitude
	private Triplet<Double, Double, Double> speed;	// (m/s) Current speed in the three axes
	private double groundSpeed;			// (m/s) Currrent ground speed
	private double acceleration;		// (m/s^2) Current acceleration
	private double heading;				// (rad) Current heading

	/** Updates the UAV object data. */
	public synchronized void update(long time, GeoCoordinates locationGeo, UTMCoordinates locationUTM, double z,
			double zRelative, Triplet<Double, Double, Double> speed, double groundSpeed, double heading) {
		this.locationGeo = locationGeo;
		this.locationUTM = locationUTM;
		this.z = z;
		this.zRelative = zRelative;

		double acceleration;
		if (this.time != 0) {
			acceleration = (groundSpeed - this.groundSpeed)/(time - this.time)*1000000000l;
		} else {
			acceleration = 0.0;
		}
		this.time = time;
		this.speed = speed;
		this.groundSpeed = groundSpeed;
		this.heading = heading;

		// Filtering the acceleration
		double abs = Math.abs(acceleration);
		if (abs <= UAVParam.MAX_ACCELERATION) { // Upper limit
			if (abs<UAVParam.MIN_ACCELERATION) { // White noise
				this.acceleration = 0;
			} else {
				// Filter
				this.acceleration = UAVParam.ACCELERATION_THRESHOLD * acceleration + (1-UAVParam.ACCELERATION_THRESHOLD) * this.acceleration;
			}
		} else {
			if (acceleration > 0) {
				this.acceleration = UAVParam.MAX_ACCELERATION;
			} else {
				this.acceleration = - UAVParam.MAX_ACCELERATION;
			}
		}
	}

	/** Returns the current value of the most relevant data:
	 * <p>Long. time.
	 * <p>UTMCoordinates. UTM coordinates.
	 * <p>double. Absolute altitude.
	 * <p>double. Speed.
	 * <p>double. Acceleration. */
	public synchronized Quintet<Long, UTMCoordinates, Double, Double, Double> getData() {
		return Quintet.with(this.time, this.locationUTM, this.z, this.groundSpeed, this.acceleration);
	}

	/** Returns the current location in UTM coordinates (x,y). */
	public synchronized UTMCoordinates getUTMLocation() {
		return this.locationUTM;
	}
	
	/** Returns the current location in Geographic coordinates.
	 * <p>x=longitude, y=latitude. */
	public synchronized GeoCoordinates getGeoLocation() {
		return this.locationGeo;
	}

	/** Returns the current relative altitude (m). */
	public synchronized double getZRelative() {
		return this.zRelative;
	}

	/** Returns the current absolute altitude (m). */
	public synchronized double getZ() {
		return this.z;
	}

	/** Returns the current ground speed (m/s). */
	public synchronized double getSpeed() {
		return this.groundSpeed;
	}
	
	/** Returns the current speed in the three axes (m/s). */
	public synchronized Triplet<Double, Double, Double> getSpeeds() {
		return this.speed;
	}
	
	/** Returns the current heading (rad). */
	public synchronized double getHeading() {
		return this.heading;
	}
}
