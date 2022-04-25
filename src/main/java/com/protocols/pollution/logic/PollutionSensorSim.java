package com.protocols.pollution.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.api.API;

import es.upv.grc.mapper.Location2DUTM;

public class PollutionSensorSim implements PollutionSensor {
	
	double [][] data;
	
	
    public PollutionSensorSim() {
    	data = new double[(int)((double) PollutionParam.length / PollutionParam.density)][(int)((double) PollutionParam.width / PollutionParam.density)];
    	
		/*try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(PollutionParam.pollutionDataFile))));
			String line;
			String[] tokens;
			for (int i = 0; i < data[0].length; i++) {
				line = reader.readLine();
				tokens = line.split(" ");
				for (int j = 0; j < data.length; j++) {
					data[j][i] = Double.parseDouble(tokens[j]);
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}*/
    	
    	try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(PollutionParam.pollutionDataFile))));
			String line;
			String[] tokens;
			int x = 0;
			for (int i = 0; i < data[0].length; i++) {
				x = 0;
				line = reader.readLine();
				line = reader.readLine();
				line = reader.readLine();
				tokens = line.split(" ");
				for (int j = 0; j < data.length; j++) {
					data[j][i] = Double.parseDouble(tokens[x]);
					x += 2;
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public double read() {
		//return Math.random() * 100;
		Location2DUTM location = API.getCopter(0).getLocationUTM();
		Location2DUTM startLocation = PollutionParam.origin;
		int pointX =  (int) Math.round((location.x - startLocation.x) / PollutionParam.width * data[0].length);;
		int pointY = (int) Math.round((location.y - startLocation.y) / PollutionParam.length * data.length);
		return (data[pointX][pointY] -300) * 2;
	}

}
