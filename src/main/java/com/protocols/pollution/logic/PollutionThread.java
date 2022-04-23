package com.protocols.pollution.logic;

import com.api.API;
import es.upv.grc.mapper.DrawableSymbol;
import es.upv.grc.mapper.DrawableSymbolGeo;
import es.upv.grc.mapper.Location2DGeo;
import es.upv.grc.mapper.Location2DUTM;
import es.upv.grc.mapper.Location3D;
import es.upv.grc.mapper.LocationNotReadyException;
import es.upv.grc.mapper.Mapper;
import smile.data.SparseDataset;

import com.api.ArduSim;
import com.api.Copter;
import com.api.GUI;
import com.api.MoveTo;
import com.api.MoveToListener;
import com.api.pojo.FlightMode;
import com.protocols.pollution.pojo.Point;
import com.protocols.pollution.pojo.PointSet;
import com.protocols.pollution.pojo.Value;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Developed by: Javier Paul Minguez (Valencia, Spain). */

public class PollutionThread extends Thread{

	private boolean [][] visited;

	private int sizeX, sizeY;

	private final Copter copter;
	private final GUI gui;

	private Point pMax, pCurrent;

	public PollutionThread() {
		this.copter = API.getCopter(0);
		this.gui = API.getGUI(0);
	}

	// Move to a point within the grid
	private void move(Point p) throws LocationNotReadyException {
		move(p.getX(), p.getY());
	}
	private void move(int x, int y) throws LocationNotReadyException {
		Double xTarget = PollutionParam.origin.x + (x * PollutionParam.density);
		Double yTarget = PollutionParam.origin.y + (y * PollutionParam.density);
		//gui.log("moving to " + xTarget + " " + yTarget);
		MoveTo moveTo = copter.moveTo(new Location3D(xTarget, yTarget, PollutionParam.altitude), new MoveToListener() {

			@Override
			public void onFailure() {
				// TODO
				gui.log("error on method move");
			}

			@Override
			public void onCompleteActionPerformed() {
				// Not necessary because we wait for the thread to finish
			}
		});
		moveTo.start();
		try {
			moveTo.join();
		} catch (InterruptedException ignored) {}

	}

	private void moveAndRead(Point p) throws LocationNotReadyException {
		double m;
		move(p);
		m = PollutionParam.sensor.read();
		synchronized(PollutionParam.measurements_set) {
			PollutionParam.measurements.set(p.getX(), p.getY(), m);

			PollutionParam.measurements_set.add(new Value(p.getX(), p.getY(), m));
			if (API.getArduSim().getArduSimRole() == ArduSim.SIMULATOR_GUI) {

				this.drawPoint(p, m, PollutionParam.measurements_set.getMin(), PollutionParam.measurements_set.getMax());
			}
		}
		visited[p.getX()][p.getY()] = true;
		gui.log("Read: [" + p.getX() + ", " + p.getY() + "] = " + m);
		p.setMeasurement(m);
	}

	private void drawPoint(Point p, double measure, double min, double max) {
		Color color = new Color((int) ((measure - min) / (max - min) * 255), 0, 0);
		try {
			DrawableSymbolGeo point = Mapper.Drawables.addSymbolGeo(1, copter.getLocationGeo(),
					DrawableSymbol.CIRCLE, 5, color, PollutionParam.STROKE_POINT);
			point.updateUpRightText(String.format("%.2f", measure));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void drawPerimeter() {
		try {
			Location2DGeo ini = PollutionParam.origin.getGeo();
			Location2DGeo fin = Location2DUTM.getGeo(PollutionParam.origin.x + PollutionParam.width, PollutionParam.origin.y + PollutionParam.length);
			List<Location2DGeo> vertex = new ArrayList<>();
			vertex.add(new Location2DGeo(ini.latitude, ini.longitude));
			vertex.add(new Location2DGeo(ini.latitude, fin.longitude));
			vertex.add(new Location2DGeo(fin.latitude, fin.longitude));
			vertex.add(new Location2DGeo(fin.latitude, ini.longitude));
			vertex.add(new Location2DGeo(ini.latitude, ini.longitude));
			Mapper.Drawables.addLinesGeo(2, vertex, Color.BLACK, PollutionParam.STROKE_POINT);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void spiral(int xini, int yini) {
		int xpos = xini;
		int ypos = yini;
		int maxRadius = 0;
		boolean circle = false;
		boolean border = false;
		int dx = 1;
		int dy = 0;
		for (int i = 1; xpos <= sizeX-1 && xpos >= 0 && ypos <= sizeY -1 && ypos >= 0; i++) {
			//System.out.println("------Bucle i" + i);
			circle = false;
			maxRadius += i;
			if (!border){
				dx = 1;
				dy = 0;
			}
			for (int j = 1; !circle; j++) {

				int xrad = xpos - xini;
				int yrad = ypos - yini;

				//if (!border) {
				if (Math.abs(xrad) < maxRadius) {
					dy = 0;
				} else if (Math.abs(yrad) < maxRadius && xrad > 0) {
					dy = 1;
					dx = 0;
				} else if (Math.abs(yrad) < maxRadius && xrad < 0) {
					dx = 0;
				} else if (!(xrad == maxRadius && yrad * -1 == maxRadius)) {
					int aux = dy;
					dy = dx;
					dx = aux * -1;
				} else {
					circle = true;
				}
				// } else {
				//     System.out.println("##################");
				//     int aux = dy;
				//     dy = dx;
				//     dx = aux;
				//     border = false;
				// }
				xpos += dx;
				ypos += dy;
				try {
					moveAndRead(new Point(xpos,ypos));
				} catch (Exception e) {
					gui.log("jaja la liaste" + e.getMessage());
				} 

				if (xpos >= sizeX - 1 || xpos < 0 || ypos >= sizeY - 1 || ypos < 0) {
					circle = true;
					//    border = true;
				}
			}

		}
	}

	private void runAndTumble() throws LocationNotReadyException {

		Point pTemp;
		PointSet points = new PointSet();

		boolean finished = false;
		while(!finished) {
			/* Run & Tumble */
			if(pCurrent.getMeasurement() - pMax.getMeasurement() > PollutionParam.pThreshold) { // Run
				gui.log("Pollution: Run");

				//We move in the same direction as the pollution has increased 
				pTemp = new Point(pMax);
				pMax = new Point(pCurrent);
				pCurrent.add(pCurrent.distVector(pTemp));
				points = new PointSet();

				if(pCurrent.isInside(sizeX, sizeY) && !(visited[pCurrent.getX()][pCurrent.getY()])) {
					moveAndRead(pCurrent);
				} else {
					pCurrent = new Point(pMax);
				}
			} else { // Tumble
				gui.log("Pollution: Tumble");

				//We create the points that we need to visit
				if(points.isEmpty()) {
					for(int i = -1; i < 2; i++)
						for(int j = -1; j< 2; j++) {
							pTemp = new Point(pMax.getX() + i, pMax.getY() + j);
							if(pTemp.isInside(sizeX, sizeY) && !(visited[pTemp.getX()][pTemp.getY()])) points.add(pTemp);
						}
				}
				if(!points.isEmpty()) {
					// ---- Read closest point
					pCurrent = findClosestPoint(points);
					moveAndRead(pCurrent);
					points.remove(pCurrent);
				} else {
					finished = true;
				}
			}
		}
	}

	private boolean explore() throws LocationNotReadyException {
		// Initial round. Initial radius = 3 to take into account Tumble
		int skip = 2;
		int radius = 3;
		PointSet points = new PointSet();
		Point pTemp;

		/* Explore fase */
		gui.log("Explore - Start");

		boolean newMax = false;
		// Measure until radius covers all the grid
		while(!newMax && (radius < sizeX || radius < sizeY)) {
			/* Spiral */
			gui.log("Explore - Round " + (skip - 1));

			// Generate points for this round
			for(int i = (-radius); i < radius; i+= skip) {
				pTemp = new Point(pMax).add(i, radius); // Top
				if(pTemp.isInside(sizeX, sizeY) && !visited[pTemp.getX()][pTemp.getY()]) points.add(pTemp);
				pTemp = new Point(pMax).add(i, -radius); // Bottom
				if(pTemp.isInside(sizeX, sizeY) && !visited[pTemp.getX()][pTemp.getY()]) points.add(pTemp);
				pTemp = new Point(pMax).add(-radius, i); // Left
				if(pTemp.isInside(sizeX, sizeY) && !visited[pTemp.getX()][pTemp.getY()]) points.add(pTemp);
				pTemp = new Point(pMax).add(radius, i); // Right
				if(pTemp.isInside(sizeX, sizeY) && !visited[pTemp.getX()][pTemp.getY()]) points.add(pTemp);
			}

			// Iterate until all points have been visited or a new maximum is found
			while(!points.isEmpty() && !newMax) {
				// ---- Read closest point
				pCurrent = findClosestPoint(points);
				moveAndRead(pCurrent);
				points.remove(pCurrent);

				// ---- If the point is a new max, exit explore and return to run & tumble
				if(pCurrent.getMeasurement() - pMax.getMeasurement() > PollutionParam.pThreshold) {
					newMax = true;
					// Set pMax to pCurrent, keep both the same so algorithm goes to tumble on next step
					pMax = new Point(pCurrent);
				}
			}
			
			//we mark the whole area as visited
			
			for(int i = (pMax.getX() -radius) >= 0 ? pMax.getX() -radius : 0; i < ((pMax.getX() + radius) < sizeX ? pMax.getX() + radius : sizeX - 1); i++)
				for(int j = (pMax.getY() - radius) >= 0 ? pMax.getY() -radius : 0; j < ((pMax.getY() + radius) < sizeY ? pMax.getY() + radius : sizeY - 1); j++)
					visited[i][j] = true;
			
			radius += skip;
			skip++;
		}
		return newMax;
	}

	private Point findClosestPoint(PointSet points) {
		Iterator<Point> pts;
		pts = points.iterator();

		// -- Get closest point
		Point pt, minPt;
		double dist, minDist;

		// ---- First element is temporary closest point
		minPt = pts.next();
		minDist = pCurrent.distance(minPt);
		// ---- Iterate through all elements to find closest point
		while(pts.hasNext()) {
			pt = pts.next();
			dist = pCurrent.distance(pt);
			if (dist < minDist) {
				minDist = dist;
				minPt = pt;
			}
		}
		return minPt;
	}

	@Override
	public void run() {
		// TODO Implement PdUC
		//long startTime = System.currentTimeMillis();

		// Calculate grid size
		sizeX = (int) ((double) PollutionParam.width / PollutionParam.density);
		sizeY = (int) ((double) PollutionParam.length / PollutionParam.density);

		System.out.println("X" + sizeX + " Y" + sizeY);
		// new booleans are initialized to false by default, this is what we want
		visited = new boolean[sizeX][sizeY];

		drawPerimeter();

		/* Wait until takeoff has finished */
		try {
			while(!PollutionParam.ready) sleep(100);
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}

		/* Start Algorithm */
		gui.log("Start PdUC Algorithm");
		//Set initial measurement location to the grid centre
		pMax = new Point(sizeX / 2, sizeY / 2);

		//Make the first move
		pCurrent = new Point(pMax);
		pCurrent.addY(1);
		
		boolean newMax = true;
		try {
			// Initial pMax and pCurrent measurement
			moveAndRead(pMax);
			moveAndRead(pCurrent);
			while(newMax) {
				runAndTumble();
				newMax = explore();
			}
		} catch (LocationNotReadyException e) {
			this.exit(e);
			return;
		}

		//spiral(sizeX / 2, sizeY / 2);

		endExperiment();
	}

	//Private method to return to land and exit from ArduSim
	private void exit(LocationNotReadyException e) {
		e.printStackTrace();
		if (copter.setFlightMode(FlightMode.RTL)) {
			gui.log("Landing for being unable to calculate the target coordinates.");
		} else {
			gui.log("Unable to return to land.");
		}
		gui.exit(e.getMessage());
	}

	private void endExperiment() {
		if (copter.setFlightMode(FlightMode.RTL)) {
			gui.log("Experiment Ended. Landing...");
		} else {
			gui.log("Experiment Ended. Unable to return to land.");
		}
	}

}
