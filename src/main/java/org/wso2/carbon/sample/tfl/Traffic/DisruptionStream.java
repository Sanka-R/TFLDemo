package org.wso2.carbon.sample.tfl.Traffic;

import org.wso2.carbon.sample.tfl.Bus.Bus;
import org.wso2.carbon.sample.tfl.BusStop.BusStop;
import org.wso2.carbon.sample.tfl.TflStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by isuru on 2/9/15.
 */
public class DisruptionStream extends Thread {
    String TrafficURL;

    public DisruptionStream(String url) {
        super();
        this.TrafficURL = url;
    }

    public void run() {
        try {
            URL obj = new URL(TrafficURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + TrafficURL);
            System.out.println("Response Code : " + responseCode);

            //BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            ArrayList<Disruption> disruptionsList = new ArrayList<Disruption>();
            TrafficStream td = new TrafficStream(con.getInputStream(), disruptionsList);
            td.getData();
            con.disconnect();

            //System.out.println(disruptionsList.get(0));
            ArrayList<String> list = new ArrayList<String>();
            int count = 0;
            for (Disruption disruption : disruptionsList) {
                //System.out.println(disruption.getState());
                //if(disruption.state.contains("Active")) {
                //list.add(disruption.toStringSeverityMinimal());
                list.add(disruption.toString());
                //}
                count++;
            }
            //System.out.println(list.get(0));
            TflStream.send(list, TflStream.endPointTraffic);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
