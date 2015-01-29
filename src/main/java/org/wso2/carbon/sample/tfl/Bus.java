package org.wso2.carbon.sample.tfl;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Bus {
	String BusID;
	double Longitude =0 ;
	double Latitude =0;
	double LonSpeed;
	double LatSpeed;
	BusStop lastStop;
	PriorityQueue<Prediction> predictions;
	HashMap<BusStop, Prediction> predictionsMap;

	private static String msg;
	private static String part1, part2, part3, part4, part5;

	static {
		part1 =
		        "<geodata:rawInputStream xmlns:geodata=\"http://samples.wso2.org/\">\n" + " <geodata:data>\n" + "  <geodata:id>";

		part2 = "</geodata:id>\n" + "  <geodata:timestamp>";

		part3 = "</geodata:timestamp>\n" + "  <geodata:latitude>";

		part4 = "</geodata:latitude>\n" + "  <geodata:longitude>";

		part5 = "</geodata:longitude>\n" + " </geodata:data>\n" + "</geodata:rawInputStream>\n";

	}

	public Bus(String id, double lat, double lon, double latspeed, double lonspeed) {
		this.BusID = id;
		this.Longitude = lon;
		this.Latitude = lat;
		this.LonSpeed = lonspeed;
		this.LatSpeed = latspeed;
		predictions = new PriorityQueue<Prediction>();
		predictionsMap = new HashMap<BusStop, Prediction>();
	}

	public Bus(String id) {
		this.BusID = id;
		predictions = new PriorityQueue<Prediction>();
		predictionsMap = new HashMap<BusStop, Prediction>();
	}

	public void setData(BusStop bt, long time) {
		Prediction p = predictionsMap.get(bt);
		if (p == null) {
			p = new Prediction(bt, time);
			predictionsMap.put(bt, p);
			predictions.add(p);
		} else {
			p.time = time + TflStream.timeOffset;
		}
	}

	public void setNew() {
		Prediction p = predictions.peek();
		if (p != null) {
			this.Latitude = p.busStop.Latitude;
			this.Longitude = p.busStop.Longitude;
			lastStop = p.busStop;
		}
	}

	public void move(long from, long period) {
		Prediction p = predictions.peek();
		if (p == null) return;
		if (lastStop == null) return;
		if (BusID.trim().equals("16433")) {
			//System.out.println(BusID + " " + (from + period) + " " + this.Latitude + " " +
			                   //this.Longitude + " " + this.lastStop);

			//System.out.println(p.time + " " + p.busStop);
			//System.out.println();
		}
		// if (p.busStop == lastStop && (p.busStop.Latitude != this.Latitude ||
		// p.busStop.Longitude != this.Longitude)) {
		// predictions.poll();
		// move(from, period);
		// }
		if (p.time < from + period && p.busStop != lastStop) {
			this.Latitude = p.busStop.Latitude;
			this.Longitude = p.busStop.Longitude;
			p.busStop = lastStop;
			predictions.poll();
			move(from + p.time, period - p.time);
		}
		this.Latitude =
		                ((this.Latitude * (p.time - from - period) + p.busStop.Latitude * (period)) / (p.time -
		                                                                                               from + 0.0));
		this.Longitude =
		                 ((this.Longitude * (p.time - from - period) + p.busStop.Longitude *
		                                                               (period)) / (p.time - from + 0.0));

		msg =
		      part1 + BusID + part2 + (from + period) + part3 + Latitude + part4 + Longitude +
		              part5;
		SendData.xmlMsg.add(msg);
		System.out.println(msg);
	}

}
