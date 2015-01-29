package org.wso2.carbon.sample.tfl;

import java.util.HashMap;
import java.util.PriorityQueue;

public class Bus {
	String BusID;
	double Longitude;
	double Latitude;
	double LonSpeed;
	double LatSpeed;
	BusStop lastStop;
	PriorityQueue<Prediction> predictions;
	HashMap<BusStop, Prediction> predictionsMap;

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
		if (p.busStop == lastStop && (p.busStop.Latitude != this.Latitude || p.busStop.Longitude != this.Longitude)) {
			predictions.poll();
			move(from, period);
			return;
		}
		this.Latitude = (long)((this.Latitude*(p.time - from - period) + p.busStop.Latitude*(period) )/(p.time-from+0.0));
		this.Longitude = (long)((this.Longitude*(p.time - from - period) + p.busStop.Longitude*(period) )/(p.time-from+0.0));
	}

}
