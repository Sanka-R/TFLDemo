package org.wso2.carbon.sample.tfl;

public class BusStop {
	public String StopID;
	public double Longitude;
	public double Latitude;
	
	public BusStop(String StopID, double lon, double lat) {
		this.StopID = StopID;
		this.Latitude = lon;
		this.Latitude = lat;
	}
}