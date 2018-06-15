package followme.logic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import followme.pojo.RecursoCompartido;

public class FollowMeParam {
	
	public static volatile boolean setupFinished = false;
	
	
	
	

	public static final long MASTER_ID_SIM = 0;
	public static final String[] MASTER_MAC = { "b8:27:eb:74:0c:d1", "00:c0:ca:90:32:05" };
	public static final long[] MASTER_ID_REAL = { 202481593486545L, 202481593486545L };

	public static final int MsgIDs = 0;
	public static final int MsgTakeOff = 1;
	public static final int MsgReady = 2;
	public static final int MsgCoordenadas = 3;
	public static final int MsgLanding = 4;

	public static final double AlturaInitFollowers = 10.0;
	public static final double AlturaMiminaFollowers = 0.75;
	public static final double AlturaInitSend = 0.5;
	public static final long TiempoMaxExperimento = 60000 * 2;
	
	public static final int DistanciaSeparacionHorizontal = 50;
	public static final int DistanciaSeparacionVertical = 20;
	public static final double DistanciaSeparacionRadio = 100;


	public static final int FormacionLinea = 0;
	public static final int FormacionMatriz = 1;
	public static final int FormacionCircular = 3;
	public static final int FormacionUsada = FormacionMatriz;

	public static final int TypemsgTargetCoordinates = 1;
	public static final int TypemsgTargetSpeeds = 2;
	public static final int TypemsgTargetCoordinatesAndSpeeds = 3;

	public static int posMaster = -1;

	public static boolean realUAVisMaster = false;

	public static AtomicReference<RecursoCompartido> recurso = new AtomicReference<RecursoCompartido>(null);

	public static FollowMeState[] uavs;

	public static ConcurrentHashMap<Integer, Integer> posFormacion;

	public enum FollowMeState {
		START((short) 1, FollowMeText.START),

		// Master
		LISTEN_ID((short) 2, FollowMeText.LISTEN_IDs), 
		WAIT_TAKE_OFF_MASTER((short) 3,FollowMeText.WAIT_TAKE_OFF_MASTER), 
		READY_TO_START((short) 4, FollowMeText.READY_TO_START), 
		SENDING((short) 5, FollowMeText.SENDING), 
		LANDING_MASTER((short) 6, FollowMeText.LANDING_MASTER),

		// Followers
		SEND_ID((short) 7, FollowMeText.SEND_ID),
		WAIT_TAKE_OFF_SLAVE((short) 8, FollowMeText.WAIT_TAKE_OFF_SLAVE), 
		TAKE_OFF((short) 9,	FollowMeText.TAKE_OFF),
		GOTO_POSITION((short) 10, FollowMeText.GOTO_POSITION),
		WAIT_MASTER((short) 11, FollowMeText.WAIT_MASTER), 
		FOLLOW((short) 12,	FollowMeText.FOLLOW), 
		LANDING_FOLLOWERS((short) 13, FollowMeText.LANDING_FOLLOWERS),

		FINISH((short) 14, FollowMeText.FINISH);

		private final short id;
		private final String name;

		private FollowMeState(short id, String name) {
			this.id = id;
			this.name = name;
		}

		public short getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static FollowMeState getStateById(short id) {
			FollowMeState res = null;
			for (FollowMeState p : FollowMeState.values()) {
				if (p.getId() == id) {
					res = p;
					break;
				}
			}
			return res;
		}

	}
	
	public static String getTypeMessage(int type) {
		switch (type) {
		case MsgIDs:
			return "MsgIDs";
		case MsgTakeOff:
			return "MsgTakeOff";
		case MsgReady:
			return "MsgReady";
		case MsgCoordenadas:
			return "MsgCoordenadas";
		case MsgLanding:
			return "MsgLanding";
		default:
			break;
		}
		
		return "Type not found";
	}

}
