package api.pojo;

import org.mavlink.messages.MAV_MODE_FLAG;

import main.Text;

/** UAV flight modes available.
 * <p>Developed by: Francisco José Fabra Collado, fron GRC research group in Universitat Politècnica de València (Valencia, Spain).</p> */

public enum FlightMode {
	STABILIZE(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			0, Text.STABILIZE),
	STABILIZE_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED, // only 209, not as the rest (217)
			0, Text.STABILIZE_ARMED),
	GUIDED(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			4, Text.GUIDED),
	GUIDED_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			4, Text.GUIDED_ARMED),
	AUTO(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			3, Text.AUTO),
	AUTO_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			3, Text.AUTO_ARMED),
	LOITER(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			5, Text.LOITER),
	LOITER_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			5, Text.LOITER_ARMED),
	RTL(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			6, Text.RTL),
	RTL_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			6, Text.RTL_ARMED),
	CIRCLE(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			7, Text.CIRCLE),
	CIRCLE_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			7, Text.CIRCLE_ARMED),
	POSHOLD(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			16, Text.POSHOLD),
	POSHOLD_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			16, Text.POSHOLD_ARMED),
	BRAKE(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			17, Text.BRAKE),
	BRAKE_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			17, Text.BRAKE_ARMED),
	AVOID_ADSB(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			19, Text.AVOID_ADSB),
	AVOID_ADSB_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_GUIDED_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			19, Text.AVOID_ADSB_ARMED),
	LAND(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			9, Text.LAND),
	LAND_ARMED(MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			9, Text.LAND_ARMED),
	THROW(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			18, Text.THROW),
	DRIFT(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			11, Text.DRIFT),
	ALT_HOLD(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			2, Text.ALT_HOLD),
	SPORT(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			13, Text.SPORT),
	ACRO(MAV_MODE_FLAG.MAV_MODE_FLAG_MANUAL_INPUT_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED
			+ MAV_MODE_FLAG.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED,
			1, Text.ACRO);

	private final int baseMode;
	private final int customMode;
	private final String modeName;

	private FlightMode(int baseMode, int customMode, String modeName) {
		this.baseMode = baseMode;
		this.customMode = customMode;
		this.modeName = modeName;
	}

	public int getBaseMode() {
		return baseMode;
	}

	public int getCustomMode() {
		return customMode;
	}
	
	public String getMode() {
		return modeName;
	}

	/**
	 * Return the ardupilot flight mode corresponding to the base and custom values.
	 * <p>If no valid flight mode is found, it returns null.</p> */
	public static FlightMode getMode(int base, long custom) {
		for (FlightMode p : FlightMode.values()) {
			if (p.baseMode == base && p.customMode == custom) {
				return p;
			}
		}
		return null;
	}
}
