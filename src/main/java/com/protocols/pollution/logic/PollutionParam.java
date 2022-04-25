package com.protocols.pollution.logic;

import com.protocols.pollution.pojo.ValueSet;

import es.upv.grc.mapper.Location2DUTM;
import smile.data.SparseDataset;

import java.awt.BasicStroke;
import java.awt.Stroke;

/** Developed by: Javier Paul Minguez (Valencia, Spain). */

public class PollutionParam {
	// General parameters
	public static double StartingAltitude = 15;	// (m) Relative altitude where the UAVs finish the take off process
	
	// Simulation parameters
	public static double InitialLatitude = 39.482594;	// (degrees) Latitude for simulations
	public static double InitialLongitude = -0.346265;	// (degrees) Longitude for simulations
	public static double InitialYaw = 0.0;				// (rad) Initial heading for simulations
	
	
	public static double altitude = 15;  //To be prompted with GUI
	public static int width = 200;		//To be prompted with GUI
	public static int length = 200;	//To be prompted with GUI
	public static double density = 5;	//To be prompted with GUI
	public static Location2DUTM origin;	//Starting origin is formed with InitialLatitude and InitialLongitude
	public static boolean isSimulation = true;	//TODO
	public static String pollutionDataFile = "/home/jav/data.txt";		
	public static PollutionSensor sensor;
	public static volatile boolean ready;	//True when the UAV takes off
	public static SparseDataset measurements;
	public static final double pThreshold = 5.0;
	public static ValueSet measurements_set;
	
	public static final Stroke STROKE_POINT = new BasicStroke(1f);
	
}
