package com.protocols.pollution.pojo;

/**
 * Represents a 2d point on a grid that contains data.
 * @author Carlos Martínez Úbeda, Javier Paul Minguez
 *
 */
public class Point {
	int x, y;
	double measurement;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Make a deep copy of a Point
	 * @param point Point to copy
	 */
	public Point(Point point) {
		this.x = point.x;
		this.y = point.y;
		this.measurement = point.measurement;
	}
	
	/**
	 * @return The serialised data.
	 */
	@Override
	public String toString() {
		return "[" + x + ", " + y + "]";
	}

	
	/**
	 * Calculate the distance vector to this Point from another Point.
	 * @param p The Point from which to calculate the distance.
	 * @return The distance vector as new Point object.
	 */
	public Point distVector(Point p) {
		return new Point(x - p.x, y - p.y);
	}
	
	public double distance(Point p) {
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
	public Point add(Point p) {
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
	public Point add(int x, int y) {
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
	 * Add to y
	 * @param i Amount to add to y
	 */
	public void addY(int i) {
		this.y += i;
	}
}
