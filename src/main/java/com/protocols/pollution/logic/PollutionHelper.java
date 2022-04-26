package com.protocols.pollution.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.javatuples.Pair;

import com.api.API;
import com.api.ArduSim;
import com.api.ArduSimTools;
import com.api.Copter;
import com.api.GUI;
import com.api.ProtocolHelper;
import com.api.TakeOff;
import com.api.TakeOffListener;
import com.api.pojo.FlightMode;
import com.protocols.pollution.gui.PollutionSimProperties;
import com.protocols.pollution.pojo.ValueSet;
import com.setup.Text;
import com.setup.sim.logic.SimParam;
import es.upv.grc.mapper.Location2D;
import es.upv.grc.mapper.Location2DGeo;
import es.upv.grc.mapper.Location2DUTM;
import es.upv.grc.mapper.LocationNotReadyException;
import smile.data.SparseDataset;



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
		PollutionSimProperties properties = new PollutionSimProperties();
		ResourceBundle resources;
		try {
			FileInputStream fis = new FileInputStream(SimParam.protocolParamFile);
			resources = new PropertyResourceBundle(fis);
			fis.close();
			Properties p = new Properties();
			for(String key: resources.keySet()){
				p.setProperty(key,resources.getString(key));
			}
			properties.storeParameters(p,resources);
		} catch (IOException e) {
			e.printStackTrace();
			ArduSimTools.warnGlobal(Text.LOADING_ERROR, Text.PROTOCOL_PARAMETERS_FILE_NOT_FOUND );
			System.exit(0);
		}
	}

	@Override
	public void initializeDataStructures() {
		configurationCLI();
		GUI gui = API.getGUI(0);
		// Sensor setup
		gui.log("Pollution sensor setup.");
		try {
			if (API.getArduSim().getArduSimRole() == ArduSim.MULTICOPTER) {
				PollutionParam.sensor = null;
			} else {
				PollutionParam.sensor = new PollutionSensorSim();
			}
		} catch (Exception e) {
			e.printStackTrace();
			gui.log("Error loading sensor");
		}
		gui.log("Pollution sensor setup done.");
		
		// Coordinates setup
		PollutionParam.origin = new Location2DGeo(PollutionParam.initialLatitude, PollutionParam.initialLongitude).getUTM();
		PollutionParam.origin.x -= PollutionParam.width/2.0;
		PollutionParam.origin.y -= PollutionParam.length/2.0;
		
		// Measurement structure
		PollutionParam.measurements_set = new ValueSet();
		PollutionParam.measurements = new SparseDataset();
		
		PollutionParam.ready = false;
		
		
	}

	@Override
	public String setInitialState() {
		return null;
	}

	@Override
	public Pair<Location2DGeo, Double>[] setStartingLocation() {
		Location2D masterLocation = new Location2D(PollutionParam.initialLatitude, PollutionParam.initialLongitude);
		
		int numUAVs = API.getArduSim().getNumUAVs();
		
		@SuppressWarnings("unchecked")
		Pair<Location2DGeo, Double>[] startingLocation = new Pair[numUAVs];
		Location2DUTM locationUTM;

		startingLocation[0] = Pair.with(masterLocation.getGeoLocation(), PollutionParam.initialYaw);;
		for (int i = 1; i < numUAVs; i++) {
			locationUTM = new Location2DUTM(masterLocation.getUTMLocation().x,
					masterLocation.getUTMLocation().y);
			try {
				startingLocation[i] = Pair.with(locationUTM.getGeo(), PollutionParam.initialYaw);
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
				GUI gui = API.getGUI(0);
				if (copter.setFlightMode(FlightMode.RTL)) {
					gui.log("Landing for being unable to calculate the target coordinates.");
				} else {
					gui.log("Unable to return to land.");
				}
				gui.exit("Finishing...");
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
		System.out.println("log method");
		try {
			FileOutputStream fis = new FileOutputStream(new File("/home/jav/Documents/results.log"));
			byte print = Byte.parseByte(PollutionParam.altitude + "");
			fis.write(print);
			fis.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public void openPCCompanionDialog(JFrame PCCompanionFrame) {
		
	}

}
