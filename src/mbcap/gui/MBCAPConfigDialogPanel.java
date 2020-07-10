package mbcap.gui;

import api.API;
import api.pojo.location.Waypoint;
import main.api.MissionHelper;
import mbcap.logic.MBCAPText;
import org.javatuples.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/** This class generates the panel to input the MBCAP protocol configuration in the corresponding dialog.
 * <p>Developed by: Francisco Jos&eacute; Fabra Collado, from GRC research group in Universitat Polit&egrave;cnica de Val&egrave;ncia (Valencia, Spain).</p> */

public class MBCAPConfigDialogPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public JTextField beaconingPeriodTextField;
	public JTextField numBeaconsTextField;
	public JTextField hopTimeTextField;
	public JTextField minSpeedTextField;
	public JTextField beaconExpirationTimeTextField;
	public JTextField collisionRiskDistanceTextField;
	public JTextField collisionRiskAltitudeDifferenceTextField;
	public JTextField maxTimeTextField;
	public JTextField riskCheckPeriodTextField;
	public JTextField packetLossTextField;
	public JTextField gpsErrorTextField;
	public JTextField standStillTimeTextField;
	public JTextField passingTimeTextField;
	public JTextField solvedTimeTextField;
	public JTextField recheckTextField;
	public JTextField deadlockTimeoutTextField;
	public JTextField missionsTextField;
	public JComboBox<String> UAVsComboBox;

	public MBCAPConfigDialogPanel() {

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 5 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblSimpam = new JLabel(MBCAPText.SIMULATION_PARAMETERS);
		GridBagConstraints gbc_lblSimpam = new GridBagConstraints();
		gbc_lblSimpam.gridwidth = 2;
		gbc_lblSimpam.anchor = GridBagConstraints.WEST;
		gbc_lblSimpam.insets = new Insets(0, 0, 5, 5);
		gbc_lblSimpam.gridx = 0;
		gbc_lblSimpam.gridy = 0;
		add(lblSimpam, gbc_lblSimpam);

		JLabel lblmissions = new JLabel(MBCAPText.MISSIONS_SELECTION);
		lblmissions.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblmissions = new GridBagConstraints();
		gbc_lblmissions.gridwidth = 2;
		gbc_lblmissions.insets = new Insets(0, 0, 5, 5);
		gbc_lblmissions.anchor = GridBagConstraints.EAST;
		gbc_lblmissions.gridx = 0;
		gbc_lblmissions.gridy = 1;
		add(lblmissions, gbc_lblmissions);

		missionsTextField = new JTextField();
		missionsTextField.setEditable(false);
		GridBagConstraints gbc_missionsTextField = new GridBagConstraints();
		gbc_missionsTextField.gridwidth = 2;
		gbc_missionsTextField.insets = new Insets(0, 0, 5, 5);
		gbc_missionsTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_missionsTextField.gridx = 2;
		gbc_missionsTextField.gridy = 1;
		add(missionsTextField, gbc_missionsTextField);
		missionsTextField.setColumns(10);

		JButton missionsButton = new JButton(MBCAPText.BUTTON_SELECT);
		missionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] fileArray = API.getGUI(0).searchMissionFiles();
				final Pair<String, List<Waypoint>[]> missions = API.getGUI(0).loadMissions(fileArray);
				MissionHelper missionHelper = API.getCopter(0).getMissionHelper();
				if (missions == null) {
					missionHelper.setMissionsLoaded(null);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							missionsTextField.setText("");
							UAVsComboBox.removeAllItems();
						}
					});
					return;
				}

				// Missions are stored
				missionHelper.setMissionsLoaded(missions.getValue1());
				// The number of UAVs is updated
				final int numUAVs = Math.min(missions.getValue1().length, API.getArduSim().getNumUAVs());
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						missionsTextField.setText(missions.getValue0());
						UAVsComboBox.removeAllItems();
						for (int i = 0; i < numUAVs; i++) {
							UAVsComboBox.addItem("" + (i + 1));
						}
						UAVsComboBox.setSelectedIndex(UAVsComboBox.getItemCount() - 1);
					}
				});
			}
		});
		GridBagConstraints gbc_missionsButton = new GridBagConstraints();
		gbc_missionsButton.insets = new Insets(0, 0, 5, 0);
		gbc_missionsButton.gridx = 4;
		gbc_missionsButton.gridy = 1;
		add(missionsButton, gbc_missionsButton);

		JLabel lblNumberOfUAVs = new JLabel(MBCAPText.UAV_NUMBER);
		lblNumberOfUAVs.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblNumberOfUAVs = new GridBagConstraints();
		gbc_lblNumberOfUAVs.anchor = GridBagConstraints.EAST;
		gbc_lblNumberOfUAVs.gridwidth = 2;
		gbc_lblNumberOfUAVs.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumberOfUAVs.gridx = 0;
		gbc_lblNumberOfUAVs.gridy = 2;
		add(lblNumberOfUAVs, gbc_lblNumberOfUAVs);

		UAVsComboBox = new JComboBox<String>();
		GridBagConstraints gbc_UAVsComboBox = new GridBagConstraints();
		gbc_UAVsComboBox.gridwidth = 2;
		gbc_UAVsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_UAVsComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_UAVsComboBox.gridx = 2;
		gbc_UAVsComboBox.gridy = 2;
		add(UAVsComboBox, gbc_UAVsComboBox);

		JLabel lblBeaconingParameters = new JLabel(MBCAPText.BEACONING_PARAM);
		GridBagConstraints gbc_lblBeaconingParameters = new GridBagConstraints();
		gbc_lblBeaconingParameters.anchor = GridBagConstraints.WEST;
		gbc_lblBeaconingParameters.gridwidth = 2;
		gbc_lblBeaconingParameters.insets = new Insets(0, 0, 5, 5);
		gbc_lblBeaconingParameters.gridx = 0;
		gbc_lblBeaconingParameters.gridy = 3;
		add(lblBeaconingParameters, gbc_lblBeaconingParameters);

		JLabel lblTimeBetweenSuccessive = new JLabel(MBCAPText.BEACON_INTERVAL);
		lblTimeBetweenSuccessive.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblTimeBetweenSuccessive = new GridBagConstraints();
		gbc_lblTimeBetweenSuccessive.gridwidth = 2;
		gbc_lblTimeBetweenSuccessive.anchor = GridBagConstraints.EAST;
		gbc_lblTimeBetweenSuccessive.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeBetweenSuccessive.gridx = 0;
		gbc_lblTimeBetweenSuccessive.gridy = 4;
		add(lblTimeBetweenSuccessive, gbc_lblTimeBetweenSuccessive);

		beaconingPeriodTextField = new JTextField();
		beaconingPeriodTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_beaconingPeriodTextField = new GridBagConstraints();
		gbc_beaconingPeriodTextField.insets = new Insets(0, 0, 5, 5);
		gbc_beaconingPeriodTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_beaconingPeriodTextField.gridx = 2;
		gbc_beaconingPeriodTextField.gridy = 4;
		add(beaconingPeriodTextField, gbc_beaconingPeriodTextField);
		beaconingPeriodTextField.setColumns(10);

		JLabel lblMs_1 = new JLabel(MBCAPText.MILLISECONDS);
		lblMs_1.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblMs_1 = new GridBagConstraints();
		gbc_lblMs_1.anchor = GridBagConstraints.WEST;
		gbc_lblMs_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMs_1.gridx = 3;
		gbc_lblMs_1.gridy = 4;
		add(lblMs_1, gbc_lblMs_1);

		JLabel lblNumberOfRepetitions = new JLabel(MBCAPText.BEACON_REFRESH);
		lblNumberOfRepetitions.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblNumberOfRepetitions = new GridBagConstraints();
		gbc_lblNumberOfRepetitions.gridwidth = 2;
		gbc_lblNumberOfRepetitions.anchor = GridBagConstraints.EAST;
		gbc_lblNumberOfRepetitions.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumberOfRepetitions.gridx = 0;
		gbc_lblNumberOfRepetitions.gridy = 5;
		add(lblNumberOfRepetitions, gbc_lblNumberOfRepetitions);

		numBeaconsTextField = new JTextField();
		numBeaconsTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_numBeaconsTextField = new GridBagConstraints();
		gbc_numBeaconsTextField.insets = new Insets(0, 0, 5, 5);
		gbc_numBeaconsTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_numBeaconsTextField.gridx = 2;
		gbc_numBeaconsTextField.gridy = 5;
		add(numBeaconsTextField, gbc_numBeaconsTextField);
		numBeaconsTextField.setColumns(10);

		JLabel lblTimeBetweenPoints = new JLabel(MBCAPText.INTERSAMPLE);
		lblTimeBetweenPoints.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblTimeBetweenPoints = new GridBagConstraints();
		gbc_lblTimeBetweenPoints.gridwidth = 2;
		gbc_lblTimeBetweenPoints.anchor = GridBagConstraints.EAST;
		gbc_lblTimeBetweenPoints.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeBetweenPoints.gridx = 0;
		gbc_lblTimeBetweenPoints.gridy = 6;
		add(lblTimeBetweenPoints, gbc_lblTimeBetweenPoints);

		hopTimeTextField = new JTextField();
		hopTimeTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_hopTimeTextField = new GridBagConstraints();
		gbc_hopTimeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_hopTimeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_hopTimeTextField.gridx = 2;
		gbc_hopTimeTextField.gridy = 6;
		add(hopTimeTextField, gbc_hopTimeTextField);
		hopTimeTextField.setColumns(10);

		JLabel lblS_5 = new JLabel(MBCAPText.SECONDS);
		lblS_5.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS_5 = new GridBagConstraints();
		gbc_lblS_5.anchor = GridBagConstraints.WEST;
		gbc_lblS_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblS_5.gridx = 3;
		gbc_lblS_5.gridy = 6;
		add(lblS_5, gbc_lblS_5);

		JLabel lblMinimumSpeedTo = new JLabel(MBCAPText.MIN_ADV_SPEED);
		lblMinimumSpeedTo.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblMinimumSpeedTo = new GridBagConstraints();
		gbc_lblMinimumSpeedTo.gridwidth = 2;
		gbc_lblMinimumSpeedTo.anchor = GridBagConstraints.EAST;
		gbc_lblMinimumSpeedTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinimumSpeedTo.gridx = 0;
		gbc_lblMinimumSpeedTo.gridy = 7;
		add(lblMinimumSpeedTo, gbc_lblMinimumSpeedTo);

		minSpeedTextField = new JTextField();
		minSpeedTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_minSpeedTextField = new GridBagConstraints();
		gbc_minSpeedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_minSpeedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minSpeedTextField.gridx = 2;
		gbc_minSpeedTextField.gridy = 7;
		add(minSpeedTextField, gbc_minSpeedTextField);
		minSpeedTextField.setColumns(10);

		JLabel lblMs_2 = new JLabel(MBCAPText.METERS_PER_SECOND);
		lblMs_2.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblMs_2 = new GridBagConstraints();
		gbc_lblMs_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblMs_2.anchor = GridBagConstraints.WEST;
		gbc_lblMs_2.gridx = 3;
		gbc_lblMs_2.gridy = 7;
		add(lblMs_2, gbc_lblMs_2);

		JLabel lblBeaconExpirationTime = new JLabel(MBCAPText.BEACON_EXPIRATION);
		lblBeaconExpirationTime.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblBeaconExpirationTime = new GridBagConstraints();
		gbc_lblBeaconExpirationTime.gridwidth = 2;
		gbc_lblBeaconExpirationTime.anchor = GridBagConstraints.EAST;
		gbc_lblBeaconExpirationTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblBeaconExpirationTime.gridx = 0;
		gbc_lblBeaconExpirationTime.gridy = 8;
		add(lblBeaconExpirationTime, gbc_lblBeaconExpirationTime);

		beaconExpirationTimeTextField = new JTextField();
		beaconExpirationTimeTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_beaconExpirationTimeTextField = new GridBagConstraints();
		gbc_beaconExpirationTimeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_beaconExpirationTimeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_beaconExpirationTimeTextField.gridx = 2;
		gbc_beaconExpirationTimeTextField.gridy = 8;
		add(beaconExpirationTimeTextField, gbc_beaconExpirationTimeTextField);
		beaconExpirationTimeTextField.setColumns(10);

		JLabel lblS = new JLabel(MBCAPText.SECONDS);
		lblS.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS = new GridBagConstraints();
		gbc_lblS.anchor = GridBagConstraints.WEST;
		gbc_lblS.insets = new Insets(0, 0, 5, 5);
		gbc_lblS.gridx = 3;
		gbc_lblS.gridy = 8;
		add(lblS, gbc_lblS);

		JLabel lblCollisionAvoidanceProtocol = new JLabel(MBCAPText.AVOID_PARAM);
		GridBagConstraints gbc_lblCollisionAvoidanceProtocol = new GridBagConstraints();
		gbc_lblCollisionAvoidanceProtocol.anchor = GridBagConstraints.WEST;
		gbc_lblCollisionAvoidanceProtocol.gridwidth = 2;
		gbc_lblCollisionAvoidanceProtocol.insets = new Insets(0, 0, 5, 5);
		gbc_lblCollisionAvoidanceProtocol.gridx = 0;
		gbc_lblCollisionAvoidanceProtocol.gridy = 10;
		add(lblCollisionAvoidanceProtocol, gbc_lblCollisionAvoidanceProtocol);

		JLabel lblDistanceBetweenPaths = new JLabel(MBCAPText.WARN_DISTANCE);
		lblDistanceBetweenPaths.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblDistanceBetweenPaths = new GridBagConstraints();
		gbc_lblDistanceBetweenPaths.gridwidth = 2;
		gbc_lblDistanceBetweenPaths.anchor = GridBagConstraints.EAST;
		gbc_lblDistanceBetweenPaths.insets = new Insets(0, 0, 5, 5);
		gbc_lblDistanceBetweenPaths.gridx = 0;
		gbc_lblDistanceBetweenPaths.gridy = 11;
		add(lblDistanceBetweenPaths, gbc_lblDistanceBetweenPaths);

		collisionRiskDistanceTextField = new JTextField();
		collisionRiskDistanceTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_collisionRiskDistanceTextField = new GridBagConstraints();
		gbc_collisionRiskDistanceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_collisionRiskDistanceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_collisionRiskDistanceTextField.gridx = 2;
		gbc_collisionRiskDistanceTextField.gridy = 11;
		add(collisionRiskDistanceTextField, gbc_collisionRiskDistanceTextField);
		collisionRiskDistanceTextField.setColumns(10);

		JLabel lblM_1 = new JLabel(MBCAPText.METERS);
		lblM_1.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblM_1 = new GridBagConstraints();
		gbc_lblM_1.anchor = GridBagConstraints.WEST;
		gbc_lblM_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblM_1.gridx = 3;
		gbc_lblM_1.gridy = 11;
		add(lblM_1, gbc_lblM_1);

		JLabel lblAltitudeDifferenceTo = new JLabel(MBCAPText.WARN_ALTITUDE);
		lblAltitudeDifferenceTo.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblAltitudeDifferenceTo = new GridBagConstraints();
		gbc_lblAltitudeDifferenceTo.gridwidth = 2;
		gbc_lblAltitudeDifferenceTo.anchor = GridBagConstraints.EAST;
		gbc_lblAltitudeDifferenceTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblAltitudeDifferenceTo.gridx = 0;
		gbc_lblAltitudeDifferenceTo.gridy = 12;
		add(lblAltitudeDifferenceTo, gbc_lblAltitudeDifferenceTo);

		collisionRiskAltitudeDifferenceTextField = new JTextField();
		collisionRiskAltitudeDifferenceTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_collisionRiskAltitudeDifferenceTextField = new GridBagConstraints();
		gbc_collisionRiskAltitudeDifferenceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_collisionRiskAltitudeDifferenceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_collisionRiskAltitudeDifferenceTextField.gridx = 2;
		gbc_collisionRiskAltitudeDifferenceTextField.gridy = 12;
		add(collisionRiskAltitudeDifferenceTextField, gbc_collisionRiskAltitudeDifferenceTextField);
		collisionRiskAltitudeDifferenceTextField.setColumns(10);

		JLabel lblM_2 = new JLabel(MBCAPText.METERS);
		lblM_2.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblM_2 = new GridBagConstraints();
		gbc_lblM_2.anchor = GridBagConstraints.WEST;
		gbc_lblM_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblM_2.gridx = 3;
		gbc_lblM_2.gridy = 12;
		add(lblM_2, gbc_lblM_2);

		JLabel lblTimeDifferenceTo = new JLabel(MBCAPText.WARN_TIME);
		lblTimeDifferenceTo.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblTimeDifferenceTo = new GridBagConstraints();
		gbc_lblTimeDifferenceTo.gridwidth = 2;
		gbc_lblTimeDifferenceTo.anchor = GridBagConstraints.EAST;
		gbc_lblTimeDifferenceTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeDifferenceTo.gridx = 0;
		gbc_lblTimeDifferenceTo.gridy = 13;
		add(lblTimeDifferenceTo, gbc_lblTimeDifferenceTo);

		maxTimeTextField = new JTextField();
		maxTimeTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_maxTimeTextField = new GridBagConstraints();
		gbc_maxTimeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_maxTimeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxTimeTextField.gridx = 2;
		gbc_maxTimeTextField.gridy = 13;
		add(maxTimeTextField, gbc_maxTimeTextField);
		maxTimeTextField.setColumns(10);

		JLabel lblS_9 = new JLabel(MBCAPText.SECONDS);
		lblS_9.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS_9 = new GridBagConstraints();
		gbc_lblS_9.anchor = GridBagConstraints.WEST;
		gbc_lblS_9.insets = new Insets(0, 0, 5, 5);
		gbc_lblS_9.gridx = 3;
		gbc_lblS_9.gridy = 13;
		add(lblS_9, gbc_lblS_9);

		JLabel lblCollisionRiskCheck = new JLabel(MBCAPText.CHECK_PERIOD);
		lblCollisionRiskCheck.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblCollisionRiskCheck = new GridBagConstraints();
		gbc_lblCollisionRiskCheck.gridwidth = 2;
		gbc_lblCollisionRiskCheck.anchor = GridBagConstraints.EAST;
		gbc_lblCollisionRiskCheck.insets = new Insets(0, 0, 5, 5);
		gbc_lblCollisionRiskCheck.gridx = 0;
		gbc_lblCollisionRiskCheck.gridy = 14;
		add(lblCollisionRiskCheck, gbc_lblCollisionRiskCheck);

		riskCheckPeriodTextField = new JTextField();
		riskCheckPeriodTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_riskCheckPeriodTextField = new GridBagConstraints();
		gbc_riskCheckPeriodTextField.insets = new Insets(0, 0, 5, 5);
		gbc_riskCheckPeriodTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_riskCheckPeriodTextField.gridx = 2;
		gbc_riskCheckPeriodTextField.gridy = 14;
		add(riskCheckPeriodTextField, gbc_riskCheckPeriodTextField);
		riskCheckPeriodTextField.setColumns(10);

		JLabel lblS_3 = new JLabel(MBCAPText.SECONDS);
		lblS_3.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS_3 = new GridBagConstraints();
		gbc_lblS_3.anchor = GridBagConstraints.WEST;
		gbc_lblS_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblS_3.gridx = 3;
		gbc_lblS_3.gridy = 14;
		add(lblS_3, gbc_lblS_3);

		JLabel lblNewLabel = new JLabel(MBCAPText.PACKET_LOSS_THRESHOLD);
		lblNewLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 15;
		add(lblNewLabel, gbc_lblNewLabel);

		packetLossTextField = new JTextField();
		packetLossTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_packetLossTextField = new GridBagConstraints();
		gbc_packetLossTextField.insets = new Insets(0, 0, 5, 5);
		gbc_packetLossTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_packetLossTextField.gridx = 2;
		gbc_packetLossTextField.gridy = 15;
		add(packetLossTextField, gbc_packetLossTextField);
		packetLossTextField.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel(MBCAPText.GPS_ERROR);
		lblNewLabel_1.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridwidth = 2;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 16;
		add(lblNewLabel_1, gbc_lblNewLabel_1);

		gpsErrorTextField = new JTextField();
		gpsErrorTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_gpsErrorTextField = new GridBagConstraints();
		gbc_gpsErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_gpsErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_gpsErrorTextField.gridx = 2;
		gbc_gpsErrorTextField.gridy = 16;
		add(gpsErrorTextField, gbc_gpsErrorTextField);
		gpsErrorTextField.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel(MBCAPText.METERS);
		lblNewLabel_2.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 3;
		gbc_lblNewLabel_2.gridy = 16;
		add(lblNewLabel_2, gbc_lblNewLabel_2);

		JLabel lblMinimumWaitingTime = new JLabel(MBCAPText.HOVERING_TIMEOUT);
		lblMinimumWaitingTime.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblMinimumWaitingTime = new GridBagConstraints();
		gbc_lblMinimumWaitingTime.gridwidth = 2;
		gbc_lblMinimumWaitingTime.anchor = GridBagConstraints.EAST;
		gbc_lblMinimumWaitingTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinimumWaitingTime.gridx = 0;
		gbc_lblMinimumWaitingTime.gridy = 17;
		add(lblMinimumWaitingTime, gbc_lblMinimumWaitingTime);

		standStillTimeTextField = new JTextField();
		standStillTimeTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_standStillTimeTextField = new GridBagConstraints();
		gbc_standStillTimeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_standStillTimeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_standStillTimeTextField.gridx = 2;
		gbc_standStillTimeTextField.gridy = 17;
		add(standStillTimeTextField, gbc_standStillTimeTextField);
		standStillTimeTextField.setColumns(10);

		JLabel lblS_6 = new JLabel(MBCAPText.SECONDS);
		lblS_6.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS_6 = new GridBagConstraints();
		gbc_lblS_6.anchor = GridBagConstraints.WEST;
		gbc_lblS_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblS_6.gridx = 3;
		gbc_lblS_6.gridy = 17;
		add(lblS_6, gbc_lblS_6);

		JLabel lblWaitingTimeTo = new JLabel(MBCAPText.OVERTAKE_TIMEOUT);
		lblWaitingTimeTo.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblWaitingTimeTo = new GridBagConstraints();
		gbc_lblWaitingTimeTo.gridwidth = 2;
		gbc_lblWaitingTimeTo.anchor = GridBagConstraints.EAST;
		gbc_lblWaitingTimeTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblWaitingTimeTo.gridx = 0;
		gbc_lblWaitingTimeTo.gridy = 18;
		add(lblWaitingTimeTo, gbc_lblWaitingTimeTo);

		passingTimeTextField = new JTextField();
		passingTimeTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_passingTimeTextField = new GridBagConstraints();
		gbc_passingTimeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_passingTimeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passingTimeTextField.gridx = 2;
		gbc_passingTimeTextField.gridy = 18;
		add(passingTimeTextField, gbc_passingTimeTextField);
		passingTimeTextField.setColumns(10);

		JLabel lblS_7 = new JLabel(MBCAPText.SECONDS);
		lblS_7.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS_7 = new GridBagConstraints();
		gbc_lblS_7.anchor = GridBagConstraints.WEST;
		gbc_lblS_7.insets = new Insets(0, 0, 5, 5);
		gbc_lblS_7.gridx = 3;
		gbc_lblS_7.gridy = 18;
		add(lblS_7, gbc_lblS_7);

		JLabel lblMinimumWaitingTime_1 = new JLabel(MBCAPText.RESUME_MODE_DELAY);
		lblMinimumWaitingTime_1.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblMinimumWaitingTime_1 = new GridBagConstraints();
		gbc_lblMinimumWaitingTime_1.gridwidth = 2;
		gbc_lblMinimumWaitingTime_1.anchor = GridBagConstraints.EAST;
		gbc_lblMinimumWaitingTime_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinimumWaitingTime_1.gridx = 0;
		gbc_lblMinimumWaitingTime_1.gridy = 19;
		add(lblMinimumWaitingTime_1, gbc_lblMinimumWaitingTime_1);

		solvedTimeTextField = new JTextField();
		solvedTimeTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_solvedTimeTextField = new GridBagConstraints();
		gbc_solvedTimeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_solvedTimeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solvedTimeTextField.gridx = 2;
		gbc_solvedTimeTextField.gridy = 19;
		add(solvedTimeTextField, gbc_solvedTimeTextField);
		solvedTimeTextField.setColumns(10);

		JLabel lblS_8 = new JLabel(MBCAPText.SECONDS);
		lblS_8.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS_8 = new GridBagConstraints();
		gbc_lblS_8.anchor = GridBagConstraints.WEST;
		gbc_lblS_8.insets = new Insets(0, 0, 5, 5);
		gbc_lblS_8.gridx = 3;
		gbc_lblS_8.gridy = 19;
		add(lblS_8, gbc_lblS_8);

		JLabel lblNewLabel_3 = new JLabel(MBCAPText.RECHECK_DELAY);
		lblNewLabel_3.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.gridwidth = 2;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 20;
		add(lblNewLabel_3, gbc_lblNewLabel_3);

		recheckTextField = new JTextField();
		recheckTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_recheckTextField = new GridBagConstraints();
		gbc_recheckTextField.insets = new Insets(0, 0, 5, 5);
		gbc_recheckTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_recheckTextField.gridx = 2;
		gbc_recheckTextField.gridy = 20;
		add(recheckTextField, gbc_recheckTextField);
		recheckTextField.setColumns(10);

		JLabel lblNewLabel_4 = new JLabel(MBCAPText.SECONDS);
		lblNewLabel_4.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 3;
		gbc_lblNewLabel_4.gridy = 20;
		add(lblNewLabel_4, gbc_lblNewLabel_4);

		JLabel lblDeadlockTimeout = new JLabel(MBCAPText.DEADLOCK_TIMEOUT);
		lblDeadlockTimeout.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblDeadlockTimeout = new GridBagConstraints();
		gbc_lblDeadlockTimeout.gridwidth = 2;
		gbc_lblDeadlockTimeout.anchor = GridBagConstraints.EAST;
		gbc_lblDeadlockTimeout.insets = new Insets(0, 0, 0, 5);
		gbc_lblDeadlockTimeout.gridx = 0;
		gbc_lblDeadlockTimeout.gridy = 21;
		add(lblDeadlockTimeout, gbc_lblDeadlockTimeout);

		deadlockTimeoutTextField = new JTextField();
		deadlockTimeoutTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_deadlockTimeoutTextField = new GridBagConstraints();
		gbc_deadlockTimeoutTextField.insets = new Insets(0, 0, 0, 5);
		gbc_deadlockTimeoutTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_deadlockTimeoutTextField.gridx = 2;
		gbc_deadlockTimeoutTextField.gridy = 21;
		add(deadlockTimeoutTextField, gbc_deadlockTimeoutTextField);
		deadlockTimeoutTextField.setColumns(10);

		JLabel lblS_4 = new JLabel(MBCAPText.SECONDS);
		lblS_4.setFont(new Font("Dialog", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS_4 = new GridBagConstraints();
		gbc_lblS_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblS_4.anchor = GridBagConstraints.WEST;
		gbc_lblS_4.gridx = 3;
		gbc_lblS_4.gridy = 21;
		add(lblS_4, gbc_lblS_4);
	}

}
