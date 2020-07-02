package shakeup.logic;

import api.API;
import main.api.ArduSim;
import main.api.communications.CommLink;
import shakeup.pojo.Param;

public class ShakeupTalkerThread extends Thread{
	
	private ArduSim ardusim;
	private CommLink link;
	private volatile shakeup.logic.state.State currentState;
	private int selfId;
	
	public ShakeupTalkerThread(int numUAV) {
		this.ardusim = API.getArduSim();
		this.link = API.getCommLink(numUAV);
		this.selfId = numUAV;
	}
	
	public void run() {
		while (!ardusim.isAvailable()) {ardusim.sleep(Param.TIMEOUT);}
		while(API.getCopter(selfId).isFlying()) {
			long before = System.currentTimeMillis();
			
			byte[][] message = currentState.createMessage();
			try {
				for(int i = 0; i< message.length; i++) {
						link.sendBroadcastMessage(message[i]);
				}
			}catch(NullPointerException e) {/* do nothing the message was just empty */ }
			
			long after = System.currentTimeMillis();
			long elapsedTime = after - before;
			
			long waitingTime = Param.SENDING_TIMEOUT - elapsedTime;
			if (waitingTime > 0) {
				ardusim.sleep(waitingTime);
			}
		}
	}
	
	public synchronized void setState(shakeup.logic.state.State s) {
		// TODO only write when they are not the same but that issues nullpointerexception
		currentState = s;
	}
}