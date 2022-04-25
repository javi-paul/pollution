package com.protocols.pollution.logic;

import com.api.API;
import es.upv.grc.mapper.DrawableSymbol;
import es.upv.grc.mapper.DrawableSymbolGeo;
import es.upv.grc.mapper.Location2DGeo;
import es.upv.grc.mapper.Location2DUTM;
import es.upv.grc.mapper.Location3D;
import es.upv.grc.mapper.LocationNotReadyException;
import es.upv.grc.mapper.Mapper;

import com.api.ArduSim;
import com.api.Copter;
import com.api.GUI;
import com.api.MoveTo;
import com.api.MoveToListener;
import com.api.pojo.FlightMode;
import com.protocols.pollution.pojo.DataPoint;
import com.protocols.pollution.pojo.Point;
import com.protocols.pollution.pojo.PointSet;
import com.protocols.pollution.pojo.Value;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Developed by: Javier Paul Minguez (Valencia, Spain). */

public class PollutionThread extends Thread{
	
	private final Copter copter;
	private final GUI gui;

	private boolean [][] visited;
	private int sizeX, sizeY;
	private DataPoint pMax, pCurrent;

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

		MoveTo moveTo = copter.moveTo(new Location3D(xTarget, yTarget, PollutionParam.altitude), new MoveToListener() {
			@Override
			public void onFailure() {
				gui.log("error on method move");
				endExperiment("Copter is unable to move.");
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

	private void moveAndRead(DataPoint p) throws LocationNotReadyException {
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
		p.setMeasurement(m);
		gui.log("Read: " + p.toString());
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
				pMax = new DataPoint(pCurrent);
				pCurrent.add(pCurrent.distVector(pTemp));
				points = new PointSet();

				if(pCurrent.isInside(sizeX, sizeY) && !(visited[pCurrent.getX()][pCurrent.getY()])) {
					moveAndRead(pCurrent);
				} else {
					pCurrent = new DataPoint(pMax);
				}
			} else { // Tumble
				gui.log("Pollution: Tumble");

				//We create the points that we need to visit in tumble phase
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
		PointSet points;

		/* Explore phase */
		gui.log("Explore - Start");

		boolean newMax = false;
		// Measure until radius covers all the grid or a new max is found
		while(!newMax && (radius < sizeX || radius < sizeY)) {
			/* Spiral */
			gui.log("Explore - Round " + (skip - 1));

			points = generatePointsExplore(radius, skip);
	        
			// Iterate until all points have been visited or a new maximum is found
			while(!points.isEmpty() && !newMax) {
				// ---- Read closest point
				pCurrent = findClosestPoint(points);
				moveAndRead(pCurrent);
				points.remove(pCurrent);

				// ---- If the point is a new max, exit explore and return to run & tumble
				if(pCurrent.getMeasurement() - pMax.getMeasurement() > PollutionParam.pThreshold) {
					newMax = true;
				}
			}
			if(points.isEmpty()) {
				markExploredArea(radius, skip);
			}
			if(newMax) {
				// Set pMax to pCurrent, keep both the same so algorithm goes to tumble on next step
				pMax = new DataPoint(pCurrent);
			}
			radius += skip;
			skip++;	
		}
		return newMax;
	}

	private DataPoint findClosestPoint(PointSet points) {
		Iterator<Point> pts = points.iterator();

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
		return new DataPoint(minPt.getX(),minPt.getY());
	}
	
	private PointSet generatePointsExplore(int radius, int skip) {
		PointSet setToPoblate = new PointSet();
		Point pTemp;
       
        for(int i = (-radius); i < radius; i+= skip) {
            pTemp = new Point(pMax).add(i, radius); // Top  //-r +r
            if(pTemp.isInside(sizeX, sizeY) && !visited[pTemp.getX()][pTemp.getY()]) setToPoblate.add(pTemp); 
            pTemp = new Point(pMax).add(-i, -radius); // Bottom  +r -r
            if(pTemp.isInside(sizeX, sizeY) && !visited[pTemp.getX()][pTemp.getY()]) setToPoblate.add(pTemp);
            pTemp = new Point(pMax).add(-radius, i); // Left  -r -r
            if(pTemp.isInside(sizeX, sizeY) && !visited[pTemp.getX()][pTemp.getY()]) setToPoblate.add(pTemp);
            pTemp = new Point(pMax).add(radius, -i); // Right +r +r
            if(pTemp.isInside(sizeX, sizeY) && !visited[pTemp.getX()][pTemp.getY()]) setToPoblate.add(pTemp);
        }
        return setToPoblate;
	}

	private void markExploredArea(int radius, int skip) {
		//We mark the whole area as visited
		for(int i = (pMax.getX() -radius) >= 0 ? pMax.getX() -radius : 0; i < ((pMax.getX() + radius) < sizeX ? pMax.getX() + radius : sizeX - 1); i++)
			for(int j = (pMax.getY() - radius) >= 0 ? pMax.getY() -radius : 0; j < ((pMax.getY() + radius) < sizeY ? pMax.getY() + radius : sizeY - 1); j++)
				visited[i][j] = true;	
	}
	
	@Override
	public void run() {
		// Calculate grid size
		sizeX = (int) ((double) PollutionParam.width / PollutionParam.density);
		sizeY = (int) ((double) PollutionParam.length / PollutionParam.density);

		// new booleans are initialized to false by default, this is what we want
		visited = new boolean[sizeX][sizeY];
		if (API.getArduSim().getArduSimRole() == ArduSim.SIMULATOR_GUI) {
			drawPerimeter();
		}

		/* Wait until takeoff has finished */
		try {
			while(!PollutionParam.ready) sleep(100);
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}

		/* Start Algorithm */
		gui.log("Start PdUC Algorithm");
		//Set initial measurement location to the grid center
		pMax = new DataPoint(sizeX / 2, sizeY / 2);

		//Make the first move
		pCurrent = new DataPoint(pMax);
		pCurrent.addY(1);
		
		boolean newMax = true;
		try {
			// Initial pMax and pCurrent measurement
			moveAndRead(pMax);
			moveAndRead(pCurrent);
			//Main loop. It stops when the explore phase finishes without finding a new max
			while(newMax) {
				/* Phase 1: Looking for the point with max pollution */
				runAndTumble();
				/* Phase 2: Exploring the area surrounding the point with max pollution */
				newMax = explore();
			}
		} catch (LocationNotReadyException e) {
			this.exit(e);
			return;
		}
		endExperiment("Experiment ended.");
	}

	private void exit(LocationNotReadyException e) {
		e.printStackTrace();
		if (copter.setFlightMode(FlightMode.RTL)) {
			gui.log("Landing for being unable to calculate the target coordinates.");
		} else {
			gui.log("Unable to return to land.");
		}
		gui.exit(e.getMessage());
	}

	private void endExperiment(String msg) {
		if (copter.setFlightMode(FlightMode.RTL)) {
			gui.log(msg + " Landing...");
		} else {
			gui.log(msg + " Unable to return to land.");
		}
	}

}
