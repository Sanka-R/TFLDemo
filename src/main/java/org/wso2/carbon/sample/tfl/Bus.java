package org.wso2.carbon.sample.tfl;

import java.util.PriorityQueue;

public class Bus {
	String BusID;
	double Longitude;
	double Latitude;
	double Speed;
	double Direction;
	PriorityQueue<Prediction> predictions;
	
	public Bus(String id, double lon, double lat, double speed, double direction){
		this.BusID = id;
		this.Longitude = lon;
		this.Latitude = lat;
		this.Speed = speed;
		this.Direction = direction;
		predictions = new PriorityQueue<Prediction>();
	}
	
	public Bus(String id, double lon, double lat){
		this.BusID = id;
		this.Longitude = lon;
		this.Latitude = lat;
		predictions = new PriorityQueue<Prediction>();
	}
	
}
