package org.wso2.carbon.sample.tfl;

import java.awt.List;
import java.util.ArrayList;

public class SendData extends Thread {
	public long currentTime;
	public long period = 1000;

	public static ArrayList<String> xmlMsg = new ArrayList<String>();
	public SendData() {
		super();
	}

	public void run() {
		while (true) {
			try {
				for (Bus bus : TflStream.busses.values()) {
					bus.move(currentTime, period);
				}
				TflStream.publish("geodata", xmlMsg);
				currentTime += period;

				long a = currentTime - System.currentTimeMillis();

				System.out.println("a " + a);
				if (a >= 0)
					Thread.sleep(a);

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("asd");
			}
		}
	}

}