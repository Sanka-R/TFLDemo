package org.wso2.carbon.sample.tfl;

import org.wso2.carbon.sample.tfl.Bus.BusStream;
import org.wso2.carbon.sample.tfl.BusStop.BusStop;
import org.wso2.carbon.sample.tfl.Traffic.Disruption;
import org.wso2.carbon.sample.tfl.Traffic.TrafficStream;

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

        try {
            getDisruptions();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            getStops();
        } catch (Exception e1) {
            System.out.println(e1.getMessage());
        }
    }

    private static void getDisruptions() throws Exception {
        //String url = "http://data.tfl.gov.uk/tfl/syndication/feeds/tims_feed.xml";
        String url = "http://localhost/TFL/tims_feed.xml";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        //BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        ArrayList<Disruption> disruptionsList = new ArrayList<Disruption>();
        TrafficStream td = new TrafficStream(con.getInputStream(), disruptionsList);
        td.getData();
        con.disconnect();

        System.out.println(disruptionsList.get(0));
        ArrayList<String> list = new ArrayList<String>();
        int count = 0;
        for(Disruption disruption:disruptionsList) {
            System.out.println(disruption.getState());
            //if(disruption.state.contains("Active")) {
                list.add(disruption.toString());
                //System.out.println("Added");
            //}
            count++;
        }
        System.out.println(list.get(0));
        TflStream.send(list, TflStream.endPointTraffic);
    }

    private static void getStops() throws Exception {

        String url = "http://localhost/TFL/small/stop.txt";
        //String url = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=61,62,63,64,65,66&ReturnList=StopID,Latitude,Longitude";
        String[] arr;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;

        long time = System.currentTimeMillis();
        System.out.println(time);
        inputLine = in.readLine();
        inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
        arr = inputLine.split(",");
        TflStream.timeOffset = time - Long.parseLong(arr[2]);


        ArrayList<String> stopJsonList = new ArrayList<String>();

        while ((inputLine = in.readLine()) != null) {
            inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
            arr = inputLine.split(",");
            //System.out.println(Double.parseDouble(arr[3]));
            //System.out.println(Double.parseDouble(arr[2]));
            BusStop temp = new BusStop(arr[1], Double.parseDouble(arr[2]),
                    Double.parseDouble(arr[3]));
            System.out.println(temp);
            TflStream.map.put(arr[1], temp);
            stopJsonList.add(temp.toString());
        }
        in.close();
        TflStream.send(stopJsonList, TflStream.endPointBus);
    }

    public void getBus() {
        BusStream b;
        for (int i = 0; i < 100; i++) {
            System.out.println("Getting Data");
            //b = new BusStream("http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=61,62,63,64,65,66&ReturnList=StopID,LineID,VehicleID,EstimatedTime");
            b = new BusStream("http://localhost/TFL/small/data" + i + ".txt");
            b.start();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
            }
        }

    }
}

