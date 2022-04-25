package com.protocols.pollution.pojo;


/**
 * Represents a 2d point on a grid that contains data.
 * @author Javier Paul Minguez
 *
 */
public class DataPoint extends Point{
	
	double measurement;
	
	public DataPoint(int x, int y) {
		super(x,y);
	}

	/**
	 * Make a deep copy of a DataPoint
	 * @param point DataPoint to copy
	 */
	public DataPoint(DataPoint point) {
		super(point.x, point.y);
		this.measurement = point.measurement;
	}
	
	/**
	 * @return The serialised data.
	 */
	public String toString() {
		return super.toString() + " = " + measurement;
	}
	
	/**
	 * @return The measurement
	 */
	public double getMeasurement() {
		return measurement;
	}
	/**
	 * @param measurement The measurement to set
	 */
	public void setMeasurement(double measurement) {
		this.measurement = measurement;
	}

}
