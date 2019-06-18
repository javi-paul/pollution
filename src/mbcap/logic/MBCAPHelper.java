package mbcap.logic;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.javatuples.Pair;

import api.API;
import api.ProtocolHelper;
import api.pojo.location.Location2DGeo;
import api.pojo.location.LogPoint;
import api.pojo.location.Location3DUTM;
import api.pojo.location.Location2DUTM;
import api.pojo.location.Waypoint;
import main.Param.SimulatorState;
import main.api.ArduSim;
import main.api.FileTools;
import main.api.GUI;
import main.api.ValidationTools;
import main.sim.board.BoardPanel;
import main.uavController.UAVParam;
import mbcap.gui.MBCAPConfigDialog;
import mbcap.gui.MBCAPGUIParam;
import mbcap.gui.MBCAPGUITools;
import mbcap.gui.MBCAPPCCompanionDialog;
import mbcap.pojo.Beacon;
import mbcap.pojo.ErrorPoint;
import mbcap.pojo.MBCAPState;
import mbcap.pojo.ProgressState;

/** Developed by: Francisco Jos&eacute; Fabra Collado, from GRC research group in Universitat Polit&egrave;cnica de Val&egrave;ncia (Valencia, Spain). */

public class MBCAPHelper extends ProtocolHelper {

	@Override
	public void setProtocol() {
		this.protocolString = MBCAPText.MBCAP_TEXT;
	}
	
	@Override
	public boolean loadMission() {
		return true;
	}

	@Override
	public void openConfigurationDialog() {
		new MBCAPConfigDialog();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initializeDataStructures() {
		int numUAVs = API.getArduSim().getNumUAVs();
		MBCAPGUIParam.predictedLocation = new AtomicReference[numUAVs];

		MBCAPParam.event = new AtomicInteger[numUAVs];
		MBCAPParam.deadlockSolved = new AtomicInteger[numUAVs];
		MBCAPParam.deadlockFailed = new AtomicInteger[numUAVs];
		MBCAPParam.state = new AtomicReference[numUAVs];
		MBCAPParam.idAvoiding = new AtomicLong[numUAVs];
		MBCAPParam.projectPath = new AtomicInteger[numUAVs];

		MBCAPParam.selfBeacon = new AtomicReference[numUAVs];
		MBCAPParam.beacons = new ConcurrentHashMap[numUAVs];
		MBCAPParam.impactLocationUTM = new ConcurrentHashMap[numUAVs];
		MBCAPParam.impactLocationPX = new ConcurrentHashMap[numUAVs];

		MBCAPParam.targetLocationUTM = new AtomicReference[numUAVs];
		MBCAPParam.targetLocationPX = new AtomicReference[numUAVs];
		MBCAPParam.beaconsStored = new ArrayList[numUAVs];

		for (int i = 0; i < numUAVs; i++) {
			MBCAPGUIParam.predictedLocation[i] = new AtomicReference<List<Location3DUTM>>();
			MBCAPParam.event[i] = new AtomicInteger();
			MBCAPParam.deadlockSolved[i] = new AtomicInteger();
			MBCAPParam.deadlockFailed[i] = new AtomicInteger();
			MBCAPParam.state[i] = new AtomicReference<MBCAPState>(MBCAPState.NORMAL);
			MBCAPParam.idAvoiding[i] = new AtomicLong(MBCAPParam.ID_AVOIDING_DEFAULT);
			MBCAPParam.projectPath[i] = new AtomicInteger(1);	// Begin projecting the predicted path over the theoretical mission
			MBCAPParam.selfBeacon[i] = new AtomicReference<Beacon>();
			MBCAPParam.beacons[i] = new ConcurrentHashMap<Long, Beacon>();
			MBCAPParam.impactLocationUTM[i] = new ConcurrentHashMap<Long, Location3DUTM>();
			MBCAPParam.impactLocationPX[i] = new ConcurrentHashMap<Long, Point2D.Double>();
			MBCAPParam.targetLocationUTM[i] = new AtomicReference<Location2DUTM>();
			MBCAPParam.targetLocationPX[i] = new AtomicReference<Point2D.Double>();
			MBCAPParam.beaconsStored[i] =  new ArrayList<Beacon>();
		}

		MBCAPParam.progress = new ArrayList[numUAVs];

		for (int i=0; i < numUAVs; i++) {
			MBCAPParam.progress[i] = new ArrayList<ProgressState>();
		}
	}

	@Override
	public String setInitialState() {
		return MBCAPState.NORMAL.getName();
	}

	@Override
	public void rescaleDataStructures() {
		// Rescale the safety circles diameter
		Location2DUTM locationUTM = null;
		boolean found = false;
		int numUAVs = API.getArduSim().getNumUAVs();
		for (int i=0; i<numUAVs && !found; i++) {
			locationUTM = API.getCopter(i).getLocationUTM();
			if (locationUTM != null) {
				found = true;
			}
		}
		GUI gui = API.getGUI(0);
		Point2D.Double a = gui.locatePoint(locationUTM.x, locationUTM.y);
		Point2D.Double b = gui.locatePoint(locationUTM.x + MBCAPParam.collisionRiskDistance, locationUTM.y);
		MBCAPParam.collisionRiskScreenDistance =b.x - a.x;
	}

	@Override
	public void loadResources() {
		// Load the image used to show the risk location
		URL url = MBCAPHelper.class.getResource(MBCAPGUIParam.EXCLAMATION_IMAGE_PATH);
		try {
			MBCAPGUIParam.exclamationImage = ImageIO.read(url);
			MBCAPGUIParam.exclamationDrawScale = MBCAPGUIParam.EXCLAMATION_PX_SIZE
					/ MBCAPGUIParam.exclamationImage.getWidth();
		} catch (IOException e) {
			API.getGUI(0).exit(MBCAPText.WARN_IMAGE_LOAD_ERROR);
		}
	}
	
	@Override
	public void rescaleShownResources() {
		Iterator<Map.Entry<Long, Location3DUTM>> entries;
		Map.Entry<Long, Location3DUTM> entry;
		Location3DUTM riskLocationUTM;
		Location2DUTM targetLocationUTM;
		Point2D.Double riskLocationPX, targetLocationPX;
		int numUAVs = API.getArduSim().getNumUAVs();
		for (int i = 0; i < numUAVs; i++) {
			// Collision risk locations
			entries = MBCAPParam.impactLocationUTM[i].entrySet().iterator();
			MBCAPParam.impactLocationPX[i].clear();
			GUI gui = API.getGUI(i);
			while (entries.hasNext()) {
				entry = entries.next();
				riskLocationUTM = entry.getValue();
				riskLocationPX = gui.locatePoint(riskLocationUTM.x, riskLocationUTM.y);
				MBCAPParam.impactLocationPX[i].put(entry.getKey(), riskLocationPX);
			}
			
			// Target locations
			targetLocationUTM = MBCAPParam.targetLocationUTM[i].get();
			if (targetLocationUTM == null) {
				MBCAPParam.targetLocationPX[i].set(null);
			} else {
				targetLocationPX = gui.locatePoint(targetLocationUTM.x, targetLocationUTM.y);
				MBCAPParam.targetLocationPX[i].set(targetLocationPX);
			}
		}
	}

	@Override
	public void drawResources(Graphics2D g2, BoardPanel p) {
		if (!API.getArduSim().collisionIsDetected()) {
			g2.setStroke(MBCAPParam.STROKE_POINT);
			MBCAPGUITools.drawPredictedLocations(g2);
			MBCAPGUITools.drawImpactRiskMarks(g2, p);
			MBCAPGUITools.drawSafetyLocation(g2);
		}
	}

	@Override
	public Pair<Location2DGeo, Double>[] setStartingLocation() {
		// Gets the current coordinates from the mission when it is loaded, and the heading pointing towards the next waypoint
		int numUAVs = API.getArduSim().getNumUAVs();
		@SuppressWarnings("unchecked")
		Pair<Location2DGeo, Double>[] startingLocations = new Pair[numUAVs];
		double heading = 0.0;
		Waypoint waypoint1, waypoint2;
		waypoint1 = waypoint2 = null;
		int waypoint1pos = 0;
		boolean waypointFound;
		Location2DUTM p1UTM, p2UTM;
		double incX, incY;
		List<Waypoint>[] missions = API.getCopter(0).getMissionHelper().getMissionsLoaded();
		List<Waypoint> mission;
		for (int i = 0; i < numUAVs; i++) {
			mission = missions[i];
			if (mission != null) {
				waypointFound = false;
				for (int j=0; j<mission.size() && !waypointFound; j++) {
					waypoint1 = mission.get(j);
					if (waypoint1.getLatitude()!=0 || waypoint1.getLongitude()!=0) {
						waypoint1pos = j;
						waypointFound = true;
					}
				}
				if (!waypointFound) {
					API.getGUI(0).exit(MBCAPText.UAVS_START_ERROR_2 + " " + API.getCopter(i).getID());
				}
				waypointFound = false;
				for (int j=waypoint1pos+1; j<mission.size() && !waypointFound; j++) {
					waypoint2 = mission.get(j);
					if (waypoint2.getLatitude()!=0 || waypoint2.getLongitude()!=0) {
						waypointFound = true;
					}
				}
				if (waypointFound) {
					// We only can set a heading if at least two points with valid coordinates are found
					p1UTM = waypoint1.getUTM();
					p2UTM = waypoint2.getUTM();
					incX = p2UTM.x - p1UTM.x;
					incY = p2UTM.y - p1UTM.y;
					if (incX != 0 || incY != 0) {
						if (incX == 0) {
							if (incY > 0)	heading = 0.0;
							else			heading = 180.0;
						} else if (incY == 0) {
							if (incX > 0)	heading = 90;
							else			heading = 270.0;
						} else {
							double gamma = Math.atan(incY/incX);
							if (incX >0)	heading = 90 - gamma * 180 / Math.PI;
							else 			heading = 270.0 - gamma * 180 / Math.PI;
						}
					}
				}
			} else {
				// Assuming that all UAVs have a mission loaded
				API.getGUI(0).exit(MBCAPText.APP_NAME + ": " + MBCAPText.UAVS_START_ERROR_1 + " " + API.getCopter(i).getID() + ".");
			}
			startingLocations[i] = Pair.with(new Location2DGeo(waypoint1.getLatitude(), waypoint1.getLongitude()), heading);
		}
		return startingLocations;
	}

	@Override
	public boolean sendInitialConfiguration(int numUAV) {
		// No special configuration is needed, as the missions are automatically loaded
		return true;
	}

	@Override
	public void startThreads() {
		int numUAVs = API.getArduSim().getNumUAVs();
		for (int i = 0; i < numUAVs; i++) {
			new ReceiverThread(i).start();
		}
		API.getGUI(0).log(MBCAPText.ENABLING);
	}

	@Override
	public void setupActionPerformed() {
		
	}

	@Override
	public void startExperimentActionPerformed() {
		
		// We start the experiment for different UAVs in different Threads to make it simultaneous
		// This is useful when running a huge amount of UAVs, as it avoid a heavy CPU overhead in simulations.
		StartExperimentThread[] threads = null;
		int numUAVs = API.getArduSim().getNumUAVs();
		if (numUAVs > 1) {
			threads = new StartExperimentThread[numUAVs - 1];
			for (int i=1; i<numUAVs; i++) {
				threads[i-1] = new StartExperimentThread(i);
			}
			for (int i=1; i<numUAVs; i++) {
				threads[i-1].start();
			}
		}
		if (API.getCopter(0).getMissionHelper().start()) {
			StartExperimentThread.UAVS_TESTING.incrementAndGet();
		}
		if (numUAVs > 1) {
			for (int i=1; i<numUAVs; i++) {
				try {
					threads[i-1].join();
				} catch (InterruptedException e) {
				}
			}
		}
		if (StartExperimentThread.UAVS_TESTING.get() < numUAVs) {
			API.getGUI(0).warn(this.protocolString, MBCAPText.START_MISSION_ERROR);
		}
	}

	@Override
	public void forceExperimentEnd() {
		// When the UAVs are close to the last waypoint a LAND command is issued
		int numUAVs = API.getArduSim().getNumUAVs();
		for (int i = 0; i < numUAVs; i++) {
			API.getCopter(i).getMissionHelper().landIfEnded(UAVParam.LAST_WP_THRESHOLD);
		}
	}

	@Override
	public String getExperimentResults() {
		// 1. Calculus of the experiment length and protocol times
		ArduSim ardusim = API.getArduSim();
		long startTime = ardusim.getExperimentStartTime();
		int numUAVs = ardusim.getNumUAVs();
		long[] uavsTotalTime = new long[numUAVs];
		for (int i = 0; i < numUAVs; i++) {
			uavsTotalTime[i] = ardusim.getExperimentEndTime()[i] - startTime;
		}
		StringBuilder sb = new StringBuilder(2000);
		long[] uavNormalTime = new long[numUAVs];
		long[] uavStandStillTime = new long[numUAVs];
		long[] uavMovingTime = new long[numUAVs];
		long[] uavGoOnPleaseTime = new long[numUAVs];
		long[] uavPassingTime = new long[numUAVs];
		long[] uavEmergencyLandTime = new long[numUAVs];
		for (int i = 0; i < numUAVs; i++) {
			long endTime = ardusim.getExperimentEndTime()[i];
			sb.append(MBCAPText.UAV_ID).append(" ").append(API.getCopter(i).getID()).append("\n");
			if (MBCAPParam.progress[i].size() == 0) {
				// In this case, only the global time is available
				uavNormalTime[i] = endTime - startTime;
			} else {
				// Different steps are available, and calculated
				ProgressState[] progress = MBCAPParam.progress[i].toArray(new ProgressState[MBCAPParam.progress[i].size()]);

				MBCAPState iniState = MBCAPState.NORMAL;
				long iniTime = startTime;
				MBCAPState curState;
				long curTime;
				for (int j = 0; j < progress.length; j++) {
					curState = progress[j].state;
					curTime = progress[j].time;
					switch (iniState) {
					case NORMAL:
						uavNormalTime[i] = uavNormalTime[i] + curTime - iniTime;
						break;
					case STAND_STILL:
						uavStandStillTime[i] = uavStandStillTime[i] + curTime - iniTime;
						break;
					case MOVING_ASIDE:
						uavMovingTime[i] = uavMovingTime[i] + curTime - iniTime;
						break;
					case GO_ON_PLEASE:
						uavGoOnPleaseTime[i] = uavGoOnPleaseTime[i] + curTime - iniTime;
						break;
					case OVERTAKING:
						uavPassingTime[i] = uavPassingTime[i] + curTime - iniTime;
						break;
					case EMERGENCY_LAND:
						uavEmergencyLandTime[i] = uavEmergencyLandTime[i] + curTime - iniTime;
						break;
					}
					iniState = curState;
					iniTime = curTime;
				}
				// From the last to the testEndTime
				switch (progress[progress.length - 1].state) {
				case NORMAL:
					uavNormalTime[i] = uavNormalTime[i] + endTime - progress[progress.length - 1].time;
					break;
				case STAND_STILL:
					uavStandStillTime[i] = uavStandStillTime[i] + endTime - progress[progress.length - 1].time;
					break;
				case MOVING_ASIDE:
					uavMovingTime[i] = uavMovingTime[i] + endTime - progress[progress.length - 1].time;
					break;
				case GO_ON_PLEASE:
					uavGoOnPleaseTime[i] = uavGoOnPleaseTime[i] + endTime - progress[progress.length - 1].time;
					break;
				case OVERTAKING:
					uavPassingTime[i] = uavPassingTime[i] + endTime - progress[progress.length - 1].time;
					break;
				case EMERGENCY_LAND:
					uavEmergencyLandTime[i] = uavEmergencyLandTime[i] + endTime - progress[progress.length - 1].time;
					break;
				}
			}
			ValidationTools validationTools = API.getValidationTools();
			sb.append(MBCAPState.NORMAL.getName()).append(" = ").append(validationTools.timeToString(0, uavNormalTime[i])).append(" (")
			.append(String.format("%.2f%%", 100 * uavNormalTime[i] / (double) uavsTotalTime[i])).append(")\n");
			sb.append(MBCAPState.STAND_STILL.getName()).append(" = ").append(validationTools.timeToString(0, uavStandStillTime[i])).append(" (")
			.append(String.format("%.2f%%", 100 * uavStandStillTime[i] / (double) uavsTotalTime[i])).append(")\n");
			sb.append(MBCAPState.MOVING_ASIDE.getName()).append(" = ").append(validationTools.timeToString(0, uavMovingTime[i])).append(" (")
			.append(String.format("%.2f%%", 100 * uavMovingTime[i] / (double) uavsTotalTime[i])).append(")\n");
			sb.append(MBCAPState.GO_ON_PLEASE.getName()).append(" = ").append(validationTools.timeToString(0, uavGoOnPleaseTime[i])).append(" (")
			.append(String.format("%.2f%%", 100 * uavGoOnPleaseTime[i] / (double) uavsTotalTime[i])).append(")\n");
			sb.append(MBCAPState.OVERTAKING.getName()).append(" = ").append(validationTools.timeToString(0, uavPassingTime[i])).append(" (")
			.append(String.format("%.2f%%", 100 * uavPassingTime[i] / (double) uavsTotalTime[i])).append(")\n");
			sb.append(MBCAPState.EMERGENCY_LAND.getName()).append(" = ").append(validationTools.timeToString(0, uavEmergencyLandTime[i])).append(" (")
			.append(String.format("%.2f%%", 100 * uavEmergencyLandTime[i] / (double) uavsTotalTime[i])).append(")\n");
			sb.append(MBCAPText.SITUATIONS_SOLVED).append(" ").append(MBCAPParam.event[i].get()).append("\n");
			sb.append(MBCAPText.DEADLOCKS).append(" ").append(MBCAPParam.deadlockSolved[i].get()).append("\n");
			sb.append(MBCAPText.DEADLOCKS_FAILED).append(" ").append(MBCAPParam.deadlockFailed[i].get()).append("\n");
		}
		return sb.toString();
	}

	@Override
	public String getExperimentConfiguration() {
		StringBuilder sb = new StringBuilder(2000);
		sb.append(MBCAPText.BEACONING_PARAM);
		sb.append("\n\t").append(MBCAPText.BEACON_INTERVAL).append(" ").append(MBCAPParam.beaconingPeriod).append(" ").append(MBCAPText.MILLISECONDS);
		sb.append("\n\t").append(MBCAPText.BEACON_REFRESH).append(" ").append(MBCAPParam.numBeacons);
		sb.append("\n\t").append(MBCAPText.INTERSAMPLE).append(" ").append(MBCAPParam.hopTime).append(" ").append(MBCAPText.MILLISECONDS);
		sb.append("\n\t").append(MBCAPText.MIN_ADV_SPEED).append(" ").append(MBCAPParam.minSpeed).append(" ").append(MBCAPText.METERS_PER_SECOND);
		sb.append("\n\t").append(MBCAPText.BEACON_EXPIRATION).append(" ").append(String.format( "%.2f", MBCAPParam.beaconExpirationTime*0.000000001 )).append(" ").append(MBCAPText.SECONDS);
		sb.append("\n").append(MBCAPText.AVOID_PARAM);
		sb.append("\n\t").append(MBCAPText.WARN_DISTANCE).append(" ").append(MBCAPParam.collisionRiskDistance).append(" ").append(MBCAPText.METERS);
		sb.append("\n\t").append(MBCAPText.WARN_ALTITUDE).append(" ").append(MBCAPParam.collisionRiskAltitudeDifference).append(" ").append(MBCAPText.METERS);
		sb.append("\n\t").append(MBCAPText.WARN_TIME).append(" ").append(String.format( "%.2f", MBCAPParam.collisionRiskTime*0.000000001 )).append(" ").append(MBCAPText.SECONDS);
		sb.append("\n\t").append(MBCAPText.CHECK_PERIOD).append(" ").append(String.format( "%.2f", MBCAPParam.riskCheckPeriod*0.000000001 )).append(" ").append(MBCAPText.SECONDS);
		sb.append("\n\t").append(MBCAPText.PACKET_LOSS_THRESHOLD).append(" ").append(MBCAPParam.packetLossThreshold);
		sb.append("\n\t").append(MBCAPText.GPS_ERROR).append(" ").append(MBCAPParam.gpsError).append(" ").append(MBCAPText.METERS);
		sb.append("\n\t").append(MBCAPText.HOVERING_TIMEOUT).append(" ").append(String.format( "%.2f", MBCAPParam.standStillTimeout*0.000000001 )).append(" ").append(MBCAPText.SECONDS);
		sb.append("\n\t").append(MBCAPText.OVERTAKE_TIMEOUT).append(" ").append(String.format( "%.2f", MBCAPParam.passingTimeout*0.000000001 )).append(" ").append(MBCAPText.SECONDS);
		sb.append("\n\t").append(MBCAPText.RESUME_MODE_DELAY).append(" ").append(String.format( "%.2f", MBCAPParam.resumeTimeout*0.000000001 )).append(" ").append(MBCAPText.SECONDS);
		sb.append("\n\t").append(MBCAPText.RECHECK_DELAY).append(" ").append(String.format( "%.2f", MBCAPParam.recheckTimeout*0.001 )).append(" ").append(MBCAPText.SECONDS);
		sb.append("\n\t").append(MBCAPText.DEADLOCK_TIMEOUT).append(" ").append(String.format( "%.2f", MBCAPParam.globalDeadlockTimeout*0.000000001 )).append(" ").append(MBCAPText.SECONDS);
		return sb.toString();
	}

	@Override
	public void logData(String folder, String baseFileName, long baseNanoTime) {
		// Logging to file the error predicting the location during the experiment (and the beacons itself if needed).
		ArduSim ardusim = API.getArduSim();
		int numUAVs = ardusim.getNumUAVs();
		@SuppressWarnings("unchecked")
		List<ErrorPoint>[] realUAVPaths = new ArrayList[numUAVs];

		// 1. UAV path calculus (only experiment path, and ignoring repeated positions)
		LogPoint realPostLocation, realPrevLocation;
		double time;
		int inTestState = SimulatorState.TEST_IN_PROGRESS.getStateId();
		Double x, y;
		for (int i=0; i<numUAVs; i++) {
			realPrevLocation = null;
			realUAVPaths[i] = new ArrayList<ErrorPoint>();

			List<LogPoint> fullPath = ardusim.getUTMPath()[i];
			for (int j = 0; j < fullPath.size(); j++) {
				realPostLocation = fullPath.get(j);

				// Considers only not repeated locations and only generated during the experiment
				if (realPostLocation.getSimulatorState() == inTestState) {
					time = realPostLocation.getTime();
					x = realPostLocation.x;
					y = realPostLocation.y;
					if (realPrevLocation == null) {
						// First test location
						realUAVPaths[i].add(new ErrorPoint(0, x, y));
						if (realPostLocation.getTime() != 0) {
							realUAVPaths[i].add(new ErrorPoint(time, x, y));
						}
						realPrevLocation = realPostLocation;
					} else if (realPostLocation.x!=realPrevLocation.x || realPostLocation.y!=realPrevLocation.y || realPostLocation.z!=realPrevLocation.z) {
						// Moved
						realUAVPaths[i].add(new ErrorPoint(realPostLocation.getTime(), x, y));
						realPrevLocation = realPostLocation;
					}
				}
			}
		}
		
		// 2. Predicted positions calculus and storage of beacons
		File beaconsFile = null;
		File maxErrorFile = null;
		File beaconsErrorFile, timeErrorFile;
		StringBuilder sb1 = null;
		StringBuilder sb2 = null;
		StringBuilder sb3, sb4;
		int j;
		ErrorPoint predictedLocation;
		@SuppressWarnings("unchecked")
		List<List<ErrorPoint>>[] totalPredictedLocations = new ArrayList[numUAVs];
		boolean verboseStore = ardusim.isVerboseStorageEnabled();
		// For each UAV
		FileTools fileTools = API.getFileTools();
		ValidationTools validationTools = API.getValidationTools();
		for (int i=0; i<numUAVs; i++) {
			List<Beacon> beacons = MBCAPParam.beaconsStored[i];
			totalPredictedLocations[i] = new ArrayList<List<ErrorPoint>>(beacons.size());
			
			long id = API.getCopter(i).getID();
			if (verboseStore) {
				// Store each beacon also
				beaconsFile = new File(folder + File.separator + baseFileName + "_" + id + "_" + MBCAPText.BEACONS_SUFIX);
				sb1 = new StringBuilder(2000);
				sb1.append("time(s),x1,y2,x2,y2,...,xn,yn\n");
			}
			
			// For each beacon
			Beacon beacon;
			for (j=0; j<beacons.size(); j++) {
				beacon = beacons.get(j);
				time = validationTools.roundDouble(((double) (beacon.time - baseNanoTime)) / 1000000000l, 9);
				if (time >= 0 && time <= realUAVPaths[i].get(realUAVPaths[i].size() - 1).time
						&& beacon.points!=null && beacon.points.size()>0) {
					if (verboseStore) {
						sb1.append(time);
					}
					
					List<Location3DUTM> locations = beacon.points;
					List<ErrorPoint> predictions = new ArrayList<ErrorPoint>(beacon.points.size());
					// For each point in each beacon
					for (int k=0; k<locations.size(); k++) {
						if (verboseStore) {
							sb1.append(",").append(validationTools.roundDouble(locations.get(k).x, 3))
								.append(",").append(validationTools.roundDouble(locations.get(k).y, 3));
						}
						predictedLocation = new ErrorPoint(time + MBCAPParam.hopTime*k, locations.get(k).x, locations.get(k).y);
						// Predicted positions for later calculus of the error in prediction
						predictions.add(predictedLocation);
					}
					totalPredictedLocations[i].add(predictions);
					if (verboseStore) {
						sb1.append("\n");
					}
				}
			}
			if (verboseStore) {
				fileTools.storeFile(beaconsFile, sb1.toString());
			}

			// 3. Calculus of the mean and maximum distance error on each beacon
			//    Only store information if useful beacons were found
			int numBeacons = totalPredictedLocations[i].size();
			if (numBeacons > 0) {
				ErrorPoint[] realUAVPath = realUAVPaths[i].toArray(new ErrorPoint[realUAVPaths[i].size()]);
				List<List<ErrorPoint>> predictedLocations = totalPredictedLocations[i];
				double[] maxBeaconDistance = new double[numBeacons]; // One per beacon
				double[] meanBeaconDistance = new double[numBeacons];
				if (ardusim.isVerboseStorageEnabled()) {
					// Log the line from the real to the predicted location, with maximum error
					maxErrorFile = new File(folder + File.separator + baseFileName + "_" + id + "_" + MBCAPText.MAX_ERROR_LINES_SUFIX);
					sb2 = new StringBuilder(2000);
				}
				// For each beacon get the max and mean distance errors
				for (j=0; j<predictedLocations.size(); j++) {
					Pair<Location2DUTM, Location2DUTM> pair =
							beaconErrorCalculation(realUAVPath, predictedLocations, j,
									maxBeaconDistance, j, meanBeaconDistance);
					if (pair!=null && ardusim.isVerboseStorageEnabled()) {
						sb2.append("._LINE\n");
						sb2.append(validationTools.roundDouble(pair.getValue0().x, 3)).append(",")
							.append(validationTools.roundDouble(pair.getValue0().y, 3)).append("\n");
						sb2.append(validationTools.roundDouble(pair.getValue1().x, 3)).append(",")
							.append(validationTools.roundDouble(pair.getValue1().y, 3)).append("\n\n");
					}
				}
				if (ardusim.isVerboseStorageEnabled()) {
					fileTools.storeFile(maxErrorFile, sb2.toString());
				}
				
				// 4. Storage of the mean and maximum distance error on each beacon
				beaconsErrorFile = new File(folder + File.separator + baseFileName + "_" + id + "_" + MBCAPText.BEACON_TOTAL_ERROR_SUFIX);
				sb3 = new StringBuilder(2000);
				sb3.append("max(m),mean(m)\n");
				for (int k=0; k<maxBeaconDistance.length-1; k++) {
					sb3.append(validationTools.roundDouble(maxBeaconDistance[k], 3)).append(",")
						.append(validationTools.roundDouble(meanBeaconDistance[k], 3)).append("\n");
				}
				sb3.append(validationTools.roundDouble(maxBeaconDistance[maxBeaconDistance.length-1], 3)).append(",")
					.append(validationTools.roundDouble(meanBeaconDistance[meanBeaconDistance.length-1], 3));
				fileTools.storeFile(beaconsErrorFile, sb3.toString());
				
				// 5. Calculus and storage of the mean and maximum distance error on each position of each beacon
				// First, get the maximum size of the beacons
				int size = 0;
				for (int m = 0; m<predictedLocations.size(); m++) {
					if (predictedLocations.get(m).size()>size) {
						size = predictedLocations.get(m).size();
					}
				}
				
				double[] maxTimeDistance = new double[size];
				double[] meanTimeDistance = new double[size];
				timeErrorCalculation(realUAVPath, predictedLocations, maxTimeDistance, meanTimeDistance);
				timeErrorFile = new File(folder + File.separator + baseFileName + "_" + id + "_" + MBCAPText.BEACON_POINT_ERROR_SUFIX);
				sb4 = new StringBuilder(2000);
				sb4.append("max(m),mean(m)\n");
				for (int k=0; k<maxTimeDistance.length-1; k++) {
					sb4.append(validationTools.roundDouble(maxTimeDistance[k], 3)).append(",")
						.append(validationTools.roundDouble(meanTimeDistance[k], 3)).append("\n");
				}
				sb4.append(validationTools.roundDouble(maxTimeDistance[maxTimeDistance.length-1], 3)).append(",")
					.append(validationTools.roundDouble(meanTimeDistance[meanTimeDistance.length-1], 3));
				fileTools.storeFile(timeErrorFile, sb4.toString());
			}
		}
	}

	@Override
	public void openPCCompanionDialog(JFrame PCCompanionFrame) {
		MBCAPPCCompanionDialog.mbcap = new MBCAPPCCompanionDialog(PCCompanionFrame);
	}
	
	/** Auxiliary method to calculate the mean and maximum error in the prediction error of each beacon.
	 * Also returns the starting and ending point of the line that represents the maximum distance error in the beacon. */
	private static Pair<Location2DUTM, Location2DUTM> beaconErrorCalculation(
			ErrorPoint[] realUAVPath, List<List<ErrorPoint>> predictedLocations, int predictedPos,
			double[] maxBeaconDistance, int distancePos, double[] meanBeaconDistance) {
		double dist;
		int num = 0;
		Location2DUTM ini, fin = ini = null;
		List<ErrorPoint> predictedLocs = predictedLocations.get(predictedPos);
		ErrorPoint predictedLocation, realPrev, realPost;
		predictedLocation = null;
		Location2DUTM realIntersection;
		int prev, post;

		// Calculus for each one of the predicted locations
		for (int i=0; i<predictedLocs.size(); i++) {
			predictedLocation = predictedLocs.get(i);
			// Ignores points too far on time or outside the real path
			if (predictedLocation.time >= realUAVPath[0].time
					&& predictedLocation.time <= realUAVPath[realUAVPath.length-1].time) {
				// Locating the real previous and next points
				prev = -1;
				post = -1;
				boolean found = false;
				for (int j=1; j<realUAVPath.length && !found; j++) {
					if (predictedLocation.time >= realUAVPath[j-1].time && predictedLocation.time <= realUAVPath[j].time) {
						found = true;
						prev = j - 1;
						post = j;
					}
				}
				// Interpolating the point in the segment over the real path
				realPrev = realUAVPath[prev];
				realPost = realUAVPath[post];
				double incT = Math.abs((predictedLocation.time-realPrev.time)/(realPost.time-realPrev.time));
				double x = realPrev.x + (realPost.x-realPrev.x) * incT;
				double y = realPrev.y + (realPost.y-realPrev.y) * incT;
				realIntersection = new Location2DUTM(x, y);
				
				dist = predictedLocation.distance(realIntersection);
				meanBeaconDistance[distancePos] = meanBeaconDistance[distancePos] + dist;
				// Checking if the distance is greater than the previous
				if (dist > maxBeaconDistance[distancePos]) {
					maxBeaconDistance[distancePos] = dist;
					ini = predictedLocation;
					fin = realIntersection;
				}
				
				num++;
			}
		}
		// Due to concurrence, the first beacon can be sent before real[0].time, so no points are useful
		if (num > 0) {
			meanBeaconDistance[distancePos] = meanBeaconDistance[distancePos]/num;
		}

		if (ini!=null && fin!=null) {
			return Pair.with(ini, fin);
		} else {
			return null;
		}
	}

	/** Auxiliary method to calculate the maximum and mean error on each position of each beacon. */
	private static void timeErrorCalculation(ErrorPoint[] realUAVPath, List<List<ErrorPoint>> predictedLocations,
			double[] maxTimeDistance, double[] meanTimeDistance) {
		double dist;
		int[] num = new int[maxTimeDistance.length];
		List<ErrorPoint> predictedLocs;
		ErrorPoint predictedLocation, realPrev, realPost;
		Location2DUTM realIntersection;
		int prev, post;

		// For each beacon
		for (int i=0; i<predictedLocations.size(); i++) {
			predictedLocs = predictedLocations.get(i);
			// For each position in the beacon
			for (int j=0; j<predictedLocs.size(); j++) {
				predictedLocation = predictedLocs.get(j);
				// Ignores points out of the real path
				if (predictedLocation.time >= realUAVPath[0].time
						&& predictedLocation.time <= realUAVPath[realUAVPath.length-1].time) {
					// Locating the real previous and next points
					prev = -1;
					post = -1;
					boolean found = false;
					for (int k=1; k<realUAVPath.length && !found; k++) {
						if (predictedLocation.time >= realUAVPath[k-1].time && predictedLocation.time <= realUAVPath[k].time) {
							found = true;
							prev = k - 1;
							post = k;
						}
					}
					// Interpolating the point in the segment over the real path
					realPrev = realUAVPath[prev];
					realPost = realUAVPath[post];
					double incT = Math.abs((predictedLocation.time-realPrev.time)/(realPost.time-realPrev.time));
					double x = realPrev.x + (realPost.x-realPrev.x) * incT;
					double y = realPrev.y + (realPost.y-realPrev.y) * incT;
					realIntersection = new Location2DUTM(x, y);
					
					dist = predictedLocation.distance(realIntersection);
					meanTimeDistance[j] = meanTimeDistance[j] + dist;
					// Checking if the distance is greater than the previous
					if (dist>maxTimeDistance[j]) {
						maxTimeDistance[j] = dist;
					}
					num[j]++;
				}
			}
		}

		for (int i=0; i<meanTimeDistance.length; i++) {
			if (num[i] != 0) {
				meanTimeDistance[i] = meanTimeDistance[i]/num[i];
			}
		}
	}
	
	/** Gets the intersection point of a line (prev->post) and the perpendicular one which includes a third point (currentUTMLocation). */
	public static Location2DUTM getIntersection(Point2D.Double currentUTMLocation, Point2D.Double prev, Point2D.Double post) {
		double currentX = currentUTMLocation.x;
		double currentY = currentUTMLocation.y;
		double prevX = prev.x;
		double prevY = prev.y;
		double postX = post.x;
		double postY = post.y;
		
		double x, y;
		// Vertical line case
		if (postX - prev.x == 0) {
			x = prev.x;
			y = currentY;
		} else if (postY - prev.y == 0) {
			// Horizontal line case
			y = prev.y;
			x = currentX;
		} else {
			// General case
			
			double slope = (postY - prevY) / (postX - prevX);
			x = (currentY - prevY + currentX / slope + slope * prevX) / (slope + 1 / slope);
			y = prevY + slope*(x - prevX);
		}
		return new Location2DUTM(x, y);
	}
	
	/** Calculates if the current UAV is moving away from a target point. */
	public static boolean isMovingAway(Location2DUTM[] lastLocations, Point2D.Double target) {
		// true if the distance to the target is increasing
		if (lastLocations.length <= 1) {
			return false;	// There is no information enough to decide
		}
		double distPrevToTarget, distPostToTarget;
		distPrevToTarget = lastLocations[0].distance(target);
		for (int i = 1; i < lastLocations.length; i++) {
			distPostToTarget = lastLocations[i].distance(target);
			if (distPrevToTarget >= distPostToTarget) {
				return false;
			}
			distPrevToTarget = distPostToTarget;
		}
		return true;
	}
	
}
