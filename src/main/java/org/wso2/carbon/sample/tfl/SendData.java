package org.wso2.carbon.sample.tfl;

import java.util.ArrayList;
import java.util.Collection;

public class SendData extends Thread {
	public long currentTime;
	public long period = 10000;

	public static ArrayList<String> xmlMsg = new ArrayList<String>();

	public SendData() {
		super();
	}

	public void run() {
		while (true) {
			try {
				if (TflStream.lastTime != 0) {

					long a = currentTime - System.currentTimeMillis();
					long from = currentTime - period;
					System.out.println("a " + a);
					if (a >= 0) {
						Thread.sleep(a);
					} else {
						currentTime = System.currentTimeMillis();
					}
					Collection<Bus> busses = TflStream.busses.values();
					for (Bus bus : busses) {
						bus.move(from + period, currentTime - from+period);
					}
					currentTime += period;
					TflStream.publish("geodata", xmlMsg);

				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("asd");
			}
		}
	}

}