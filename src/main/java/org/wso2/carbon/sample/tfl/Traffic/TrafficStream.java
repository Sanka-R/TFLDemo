package org.wso2.carbon.sample.tfl.Traffic;

/**
 * Created by sanka on 2/6/15.
 */

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;

public class TrafficStream {

    private InputStream in;
    ArrayList<Disruption> disruptionsList;

    public TrafficStream(InputStream in, ArrayList<Disruption> list) {
        this.in = in;
        this.disruptionsList = list;
    }

    public void getData() {

        try {
            double t = System.currentTimeMillis();
            System.out.println("TrafficStream");
            // Get SAX Parser Factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // Turn on validation, and turn off namespaces
            factory.setValidating(true);
            factory.setNamespaceAware(false);
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, new TrafficXMLHandler(disruptionsList));
            System.out.println("Number of Disruptions added to the list: " + disruptionsList.size());
            System.out.println("Time taken for parsing: " + (System.currentTimeMillis() - t));
        } catch (ParserConfigurationException e) {
            System.out.println("The underlying parser does not support " +
                    " the requested features.");
        } catch (FactoryConfigurationError e) {
            System.out.println("Error occurred obtaining SAX Parser Factory.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

