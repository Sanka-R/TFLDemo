package org.wso2.carbon.sample.tfl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GetData extends Thread {

	public GetData() {
		super();
	}

	public void run() {
		BusStopData b;
		for (int i = 1; i < 100; i++) {
			b = new BusStopData("http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=1&ReturnList=StopID,LineID,VehicleID,EstimatedTime");
			//b = new BusStopData("http://localhost/TFL/data" + i + ".txt");
			b.start();
			try {
	            Thread.sleep(30000);
            } catch (InterruptedException e) {
            }
		}
	}

}

class BusStopData extends Thread {
	String url;

	public BusStopData(String url) {
		super();
		this.url = url;
	}

	public void run() {
		// String url =
		// "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=1,2&ReturnList=StopID,LineID,VehicleID,EstimatedTime";
		try {
			long time = System.currentTimeMillis();
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			inputLine = in.readLine();
			inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
			String[] arr = inputLine.split(",");

			TflStream.lastTime = Long.parseLong(arr[2]) + TflStream.timeOffset;
			
			
			ArrayList<Bus> newBusses = new ArrayList<Bus>();
			while ((inputLine = in.readLine()) != null) {
				inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
				arr = inputLine.split(",");

				Bus bus = TflStream.busses.get(arr[3]);
				BusStop bs = TflStream.map.get(arr[1]);
				if (bus == null) {
					bus = new Bus(arr[3]);
					TflStream.busses.put(arr[3], bus);
					newBusses.add(bus);
				}
				bus.setData(bs, Long.parseLong(arr[4]));
			}
			for(Bus newBus:newBusses) {
				newBus.setNew();
			}

			in.close();
			System.out.println("Added busses to a hashmap. "+(System.currentTimeMillis() - time) + " millis");
		} catch (Exception e) {

		}
	}
}
