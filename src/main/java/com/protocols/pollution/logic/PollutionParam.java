package com.protocols.pollution.logic;

import com.protocols.pollution.pojo.ValueSet;

import es.upv.grc.mapper.Location2DUTM;
import smile.data.SparseDataset;

import java.awt.BasicStroke;
import java.awt.Stroke;

/** Developed by: Javier Paul Minguez (Valencia, Spain). */

public class PollutionParam {	
	// Simulation parameters
	public static double initialLatitude;	// (degrees) Latitude for simulations
	public static double initialLongitude;	// (degrees) Longitude for simulations
	public static double initialYaw;	// (rad) Initial heading for simulations
	public static double altitude;  // (m) altitude of the flight
	public static int width; // (m) horizontal size
	public static int length; // (m) vertical size
	public static double density; // (m) size of each division
	public static double pThreshold; // min value when the protocol detects an increment 
	public static String pollutionDataFile;	// path to the file when the data will be stored at the end of the experiment
	
	//For simulation use
	public static double[][] data;
	public static final Stroke STROKE_POINT = new BasicStroke(1f);
	public static Location2DUTM origin;
	public static PollutionSensor sensor;
	public static volatile boolean ready;	//True when the UAV takes off
	public static SparseDataset measurements;
	public static ValueSet measurements_set;
	public static double timeForMeasuring;
	
}
