package org.wso2.carbon.sample.tfl;

public class BusStop {
	public String StopID;
	public double Longitude;
	public double Latitude;
	
	public BusStop(String StopID, double lat, double lon) {
		this.StopID = StopID;
		this.Latitude = lat;
		this.Longitude = lon;
	}
	@Override
	public String toString() {
	    return "BusStop " + StopID + " Lat: "+Latitude+" Long: " + Longitude+" ";
	}
}
