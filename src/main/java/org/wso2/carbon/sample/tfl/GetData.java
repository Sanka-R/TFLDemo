package org.wso2.carbon.sample.tfl;

import org.wso2.carbon.sample.tfl.Bus.BusStream;
import org.wso2.carbon.sample.tfl.BusStop.BusStop;
import org.wso2.carbon.sample.tfl.Traffic.DisruptionStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class GetData extends Thread {

    public static final String RecordedBusStopURL = "http://localhost/TFL/stop.txt";
    public static final String RecordedTrafficURL = "http://localhost/TFL/tims_feed.xml";
    public static final String RecordedBusURL = "http://localhost/TFL/data";

    public static final String LiveTrafficURL = "http://data.tfl.gov.uk/tfl/syndication/feeds/tims_feed.xml";
    public static final String LiveBusStopURL = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?ReturnList=StopPointName,Latitude,Longitude";
    public static final String LiveBusURL = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?ReturnList=StopPointName,LineName,VehicleID,EstimatedTime";


    public String TrafficURL;
    public String BusURL;
    public String BusStopURL;

    private boolean isbus;

    public GetData(boolean isbus, boolean playback, String lineNames) {
        super();
        this.isbus = isbus;
        if(playback){
            TrafficURL = RecordedTrafficURL;
            BusURL = RecordedBusURL;
            BusStopURL = RecordedBusStopURL;
        }else{
            TrafficURL = LiveTrafficURL;
            BusURL = LiveBusURL+lineNames;
            BusStopURL = LiveBusStopURL+lineNames;
            System.out.println(BusStopURL);
        }
    }
    public GetData(boolean isbus, boolean playback) {
        super();
        this.isbus = isbus;
        if(playback){
            TrafficURL = RecordedTrafficURL;
            BusURL = RecordedBusURL;
            BusStopURL = RecordedBusStopURL;
        }else{
            TrafficURL = LiveTrafficURL;
            BusURL = LiveBusURL;
            BusStopURL = LiveBusStopURL;
        }
    }

    public void run() {

        if(isbus){
            getStops();
            getBus();
        }
        else {
            getDisruptions();
        }

    }

    private void getBus() {
        BusStream b;
        long time = System.currentTimeMillis();
        int i = 0;
        while (true){
            //System.out.println("Getting Data");
            String url = BusURL;
            if (BusURL.contains("localhost"))
                url += i + ".txt";
            System.out.println(url);
            b = new BusStream(url);
            b.start();
            try {
                time += 30000;
                Thread.sleep(time - System.currentTimeMillis());
            } catch (InterruptedException e) {
            }

            i = (i+1) % 100;
        }

    }

    private void getDisruptions() {
        DisruptionStream ds;
        long time = System.currentTimeMillis();

        while(true){
            ds = new DisruptionStream(TrafficURL);
            //System.out.println("Getting Disruption Data ");
            ds.start();
            try{
                time += 300000;
                Thread.sleep(time - System.currentTimeMillis());
            }catch(InterruptedException e){
            }
        }

    }

    private void getStops() {
        try {
            String[] arr;

            URL obj = new URL(BusStopURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + BusStopURL);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            long time = System.currentTimeMillis();
            //System.out.println(time);
            inputLine = in.readLine().trim();
            inputLine = inputLine.substring(1, inputLine.length()-1);
            arr = inputLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            TflStream.timeOffset = time - Long.parseLong(arr[2]);


            ArrayList<String> stopJsonList = new ArrayList<String>();

            while ((inputLine = in.readLine()) != null) {
                inputLine = inputLine.trim();
                inputLine = inputLine.substring(1, inputLine.length()-1);
                arr = inputLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                BusStop temp = new BusStop(arr[1], Double.parseDouble(arr[2]),
                        Double.parseDouble(arr[3]));
                TflStream.map.put(arr[1], temp);
                stopJsonList.add(temp.toString());
            }
            TflStream.send(stopJsonList, TflStream.endPointBus);
            in.close();
            con.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

