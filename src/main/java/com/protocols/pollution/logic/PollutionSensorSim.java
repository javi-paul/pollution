package com.protocols.pollution.logic;

public class PollutionSensorSim implements PollutionSensor {

	@Override
	public double read() {
		// TODO Auto-generated method stub
		return Math.random() * 100;
	}

}
