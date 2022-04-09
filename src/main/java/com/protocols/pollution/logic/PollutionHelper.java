package com.protocols.pollution.logic;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.javatuples.Pair;

import com.api.API;
import com.api.Copter;
import com.api.GUI;
import com.api.ProtocolHelper;
import com.api.TakeOff;
import com.api.TakeOffListener;
import com.api.formations.Formation;
import com.api.pojo.FlightMode;
import com.protocols.pollution.pojo.ValueSet;
import com.uavController.UAVParam;

import es.upv.grc.mapper.Location2D;
import es.upv.grc.mapper.Location2DGeo;
import es.upv.grc.mapper.Location2DUTM;
import es.upv.grc.mapper.LocationNotReadyException;



/** Developed by: Javier Paul Minguez (Valencia, Spain). */

public class PollutionHelper extends ProtocolHelper {

	@Override
	public void setProtocol() {
		//Name of the protocol
		this.protocolString = "Pollution";
	}

	@Override
	public boolean loadMission() {
		//In this case, UAVs do not follow a planned mission, they move depending on the pollution they measure. 
		return false;
	}

	@Override
	public JDialog openConfigurationDialog() {
		//Deprecated. DO NOT implement
		return null;
	}

	@Override
	public void openConfigurationDialogFX() {
		//TODO: change this comment when GUI implemented
		//Platform.runLater(()->new PollutionDialogApp().start(new Stage()));
		com.setup.Param.simStatus = com.setup.Param.SimulatorState.STARTING_UAVS; 
	}
	
	@Override
	public void configurationCLI() {
		//TODO: read the properties files, in case the simulator is run with CLI mode.
	}

	@Override
	public void initializeDataStructures() { 
		GUI gui = API.getGUI(0);
		// Sensor setup
		gui.log("Pollution sensor setup.");
		try {
			PollutionParam.sensor = PollutionParam.isSimulation ? new PollutionSensorSim() : null;
		} catch (Exception e) {
			e.printStackTrace();
			gui.log("Error loading sensor");
		}
		gui.log("Pollution sensor setup done.");
		
		// Coordinates setup
		PollutionParam.origin = new Location2DUTM(PollutionParam.InitialLatitude, PollutionParam.InitialLongitude);
		PollutionParam.origin.x -= PollutionParam.width/2.0;
		PollutionParam.origin.y -= PollutionParam.length/2.0;
		
		// Measurement structure
		PollutionParam.measurements_set = new ValueSet();
		
		PollutionParam.ready = false;
	}

	@Override
	public String setInitialState() {
		//TODO: Check if this method is needed.
		return null;
	}

	@Override
	public Pair<Location2DGeo, Double>[] setStartingLocation() {
		Location2D masterLocation = new Location2D(PollutionParam.InitialLatitude, PollutionParam.InitialLongitude);
		
		int numUAVs = API.getArduSim().getNumUAVs();
		
		@SuppressWarnings("unchecked")
		Pair<Location2DGeo, Double>[] startingLocation = new Pair[numUAVs];
		Location2DUTM locationUTM;

		startingLocation[0] = Pair.with(masterLocation.getGeoLocation(), PollutionParam.InitialYaw);;
		for (int i = 1; i < numUAVs; i++) {
			locationUTM = new Location2DUTM(masterLocation.getUTMLocation().x,
					masterLocation.getUTMLocation().y);
			try {
				startingLocation[i] = Pair.with(locationUTM.getGeo(), PollutionParam.InitialYaw);
			} catch (LocationNotReadyException e) {
				e.printStackTrace();
				API.getGUI(0).exit(e.getMessage());
			}
		}
		
		return startingLocation;
	
	}

	@Override
	public boolean sendInitialConfiguration(int numUAV) {
		return true;
	}

	@Override
	public void startThreads() {
		new PollutionThread().start();
	}

	@Override
	public void setupActionPerformed() {
	}

	@Override
	public void startExperimentActionPerformed() {
		Copter copter = API.getCopter(0);
		TakeOff takeOff = copter.takeOff(PollutionParam.altitude, new TakeOffListener() {
			
			@Override
			public void onFailure() {
				// TODO 
			}
			
			@Override
			public void onCompleteActionPerformed() {
				// Nothing to do, just waiting the end with Thread.join()
			}
		});
		takeOff.start();
		try {
			takeOff.join();
		} catch (InterruptedException ignored) {}
		
		PollutionParam.ready = true;
		
	}

	@Override
	public void forceExperimentEnd() {
		//Experiment ends in PollutionThread
	}

	@Override
	public String getExperimentResults() {
		//TODO: Show pollution measurements
		return null;
	}

	@Override
	public String getExperimentConfiguration() {
		return null;
	}

	@Override
	public void logData(String folder, String baseFileName, long baseNanoTime) {
		//TODO: store pollution measurements in a .log file
	}

	@Override
	public void openPCCompanionDialog(JFrame PCCompanionFrame) {
		
	}

}
