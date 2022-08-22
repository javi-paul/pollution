package com.protocols.pollution.pojo;


/**
 * Represents a 2d point on a grid that contains data.
 * @author Javier Paul Minguez
 *
 */
public class DataPoint{
	private int x, y;
	private double measurement;
	
	public DataPoint(int x, int y) {
		this.x = x;
		this.y = y;
		measurement = -1;
	}
	
	public DataPoint(int x, int y, double m) {
		this.x = x;
		this.y = y;
		measurement = m;
	}

	/**
	 * Make a deep copy of a DataPoint
	 * @param point DataPoint to copy
	 */
	public DataPoint(DataPoint point) {
		this.x = point.x;
		this.y = point.y;
		this.measurement = point.measurement;
	}
	
	/**
	 * @return The serialised data.
	 */
	public String toString() {
		return "[" + x + ", " + y +  ", " + measurement + "]";
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
	
	/**
	 * Calculate the distance vector to this Point from another Point.
	 * @param p The Point from which to calculate the distance.
	 * @return The distance vector as new Point object.
	 */
	public DataPoint distVector(DataPoint p) {
		return new DataPoint(x - p.x, y - p.y);
	}
	
	public double distance(DataPoint p) {
		int a = this.x - p.x;
		int b = this.y - p.y;
		return Math.sqrt(a*a + b*b);
	}
	
	/**
	 * Adds a Point to this Point.
	 * Useful for adding Points representing distance vectors.
	 * @param p The Point to add to this object.
	 * @return Itself to allow chaining.
	 */
	public DataPoint add(DataPoint p) {
		x += p.x;
		y += p.y;
		return this;
	}
	
	/**
	 * Adds to the x and y components of the Point.
	 * @param x The x to add.
	 * @param y The y to add.
	 * @return point
	 */
	public DataPoint add(int x, int y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	/**
	 * Checks if the point would be contained within a grid of specified size.
	 * @param sizeX x axis size of the grid.
	 * @param sizeY y axis size of the grid.
	 * @return True if the point is in the grid, false otherwise.
	 */
	public boolean isInside(int sizeX, int sizeY) {
		return x >= 0 && x < sizeX && y >= 0 && y < sizeY;
	}
	
	/**
	 * @return The x
	 */
	public int getX() {
		return x;
	}
	/**
	 * @param x The x to set
	 */
	public void setX(int x) {
		this.x = x;
	}
	/**
	 * @return The y
	 */
	public int getY() {
		return y;
	}
	/**
	 * @param y The y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Add to y
	 * @param i Amount to add to y
	 */
	public void addY(int i) {
		this.y += i;
	}
}
