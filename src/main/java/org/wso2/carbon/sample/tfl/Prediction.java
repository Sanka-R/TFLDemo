package org.wso2.carbon.sample.tfl;

public class Prediction implements Comparable<Prediction> {
	public BusStop busStop;
	public long time;
	

	public Prediction(BusStop busStop, long time) {
	    this.busStop = busStop;
	    this.time = time;
    }


	@Override
	public int compareTo(Prediction arg0) {
		return (int) (time - arg0.time);
	}
	
}
