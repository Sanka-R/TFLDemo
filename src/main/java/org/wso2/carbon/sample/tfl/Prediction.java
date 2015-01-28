package org.wso2.carbon.sample.tfl;

public class Prediction implements Comparable<Prediction> {
	BusStop busStop;
	long time;
	
	@Override
	public int compareTo(Prediction arg0) {
		return (int) (time - arg0.time);
	}
	
}
