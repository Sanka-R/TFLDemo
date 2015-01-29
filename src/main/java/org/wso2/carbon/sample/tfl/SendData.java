package org.wso2.carbon.sample.tfl;



public class SendData extends Thread {
	public long currentTime;
	public long period = 1000;
	public SendData() {
		super();
	}

	public void run() {
		while(true) {
			for (Bus bus: TflStream.busses.values()) {
				bus.move(currentTime, period);
			}
			currentTime += period;
			try {
	            Thread.sleep(currentTime - System.currentTimeMillis());
            } catch (InterruptedException e) {
            	
            }
		}
	}

}