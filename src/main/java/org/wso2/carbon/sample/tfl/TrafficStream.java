package org.wso2.carbon.sample.tfl;

/**
 * Created by sanka on 2/6/15.
 */

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;

public class TrafficStream {

    private InputStream in;
    ArrayList<String> disruptionsList;

    public TrafficStream(InputStream in, ArrayList<String> list) {
        this.in = in;
        try {
            this.in = new FileInputStream(new File("/var/www/html/TFL/tims_feed.xml"));
        } catch (FileNotFoundException e) {

        }
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
            parser.parse(in, new MyHandler(disruptionsList));
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

class MyHandler extends DefaultHandler {
    // SAX callback implementations from DocumentHandler, ErrorHandler, etc.
    private ArrayList<String> list;
    private Disruption current = null;
    private StringBuilder sb = new StringBuilder();
    private boolean inLine = false;
    private boolean inPoly = false;
    private boolean startElement = true;

    public MyHandler(ArrayList<String> list) throws SAXException {
        this.list = list;
    }

    public void startDocument() throws SAXException {
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes atts)
            throws SAXException {
        startElement = true;
        if (qName.equals("Disruption")) {
            //System.out.println("disruption");
            if (current != null) {
                current.end();
                list.add(current.toString());
            }
            current = new Disruption();
            current.id = atts.getValue(0);
        } else if (qName.equals("Line")) {
            inLine = true;
        } else if (qName.equals("Polygon")) {
            inPoly = true;
        }
    }

    public void endElement(String uri, String localName,
                           String qName) throws SAXException {
        String string = sb.toString();
        if (qName.equals("severity")) {
            current.state = string;
        } else if (qName.equals("location")) {
            current.location = string;
        } else if (qName.equals("comments")) {
            current.location = string;
        } else if (qName.equals("coordinatesLL")) {
            if (inLine) {
                current.addCoordsLane(string);
            } else if (inPoly) {
                current.setCoordsPoly(string);
            }
            inLine = false;
            inPoly = false;
        }
        sb.setLength(0);
        startElement = false;
    }

    public void characters(char[] ch, int start, int len) throws SAXException {
        if(startElement) {
            sb.setLength(0);
            sb.append(new String(ch, start, len));
            startElement = false;
        }
        else {
            sb.append(new String(ch, start, len));
        }

    }

}