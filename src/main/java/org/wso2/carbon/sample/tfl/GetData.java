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

        try {
            getDisruptions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        BusData b;
        try {
            getStops();
        } catch (Exception e1) {
            System.out.println(e1.getMessage());
        }
        for (int i = 0; i < 100; i++) {
            System.out.println("Getting Data");
            //b = new BusData("http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=61,62,63,64,65,66&ReturnList=StopID,LineID,VehicleID,EstimatedTime");
            b = new BusData("http://localhost/TFL/small/data" + i + ".txt");
            b.start();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
            }
        } */
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

        ArrayList<String> disruptionsList = new ArrayList<String>();
        TrafficStream td = new TrafficStream(con.getInputStream(), disruptionsList);
        td.getData();
        con.disconnect();

        System.out.println(disruptionsList.get(0));
        ArrayList<String> list = new ArrayList<String>();
        int count = 0;
        for(String s:disruptionsList) {
            if(count < 20) {
                list.add(s);
            }
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
}

class BusData extends Thread {
    String url;

    public BusData(String url) {
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
            for (Bus newBus : newBusses) {
                newBus.setNew();
            }

            in.close();
            System.out.println("Added busses to a hashmap. " + (System.currentTimeMillis() - time) + " millis");
        } catch (Exception e) {

        }
    }
}
