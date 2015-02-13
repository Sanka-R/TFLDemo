package org.wso2.carbon.sample.tfl.Traffic.ML;

/**
 * Created by sanka on 2/6/15.
 */

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.xml.sax.SAXException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;

public class TrafficStream {

    private static GeometryFactory geometryFactory;

    public static void main(String[] args) {
        try {
            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/isuru/data.txt")));
            bw.write("day,hour,x,y,traffic\n");
            geometryFactory = JTSFactoryFinder.getGeometryFactory();
            //double latitudeStart = -0.35, latitudeEnd = 0.25;
            //double longitudeStart = 51.3, longitudeEnd = 51.6;
            double latitudeStart = -0.095, latitudeEnd = -0.003;
            double longitudeStart = 51.496, longitudeEnd = 51.524;


            double unit = 0.005;
            double latitude, longitude;

            int rows = (int) Math.round((latitudeEnd - latitudeStart) / unit);
            int cols = (int) Math.round((longitudeEnd - longitudeStart) / unit);
            System.out.println(rows * cols);

            Geometry[][] geometries = new Geometry[rows][cols];
            Coordinate[] areaC = new Coordinate[5];

            areaC[0] = new Coordinate(latitudeStart, longitudeStart);
            areaC[1] = new Coordinate(latitudeStart, longitudeEnd);
            areaC[2] = new Coordinate(latitudeEnd, longitudeEnd);
            areaC[3] = new Coordinate(latitudeEnd, longitudeStart);
            areaC[4] = new Coordinate(latitudeStart, longitudeStart);
            Geometry area = geometryFactory.createPolygon(geometryFactory.createLinearRing(areaC), null);


            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    latitude = latitudeStart + i * unit;
                    longitude = longitudeStart + j * unit;
                    Coordinate[] coords = new Coordinate[5];
                    coords[0] = new Coordinate(latitude, longitude);
                    coords[1] = new Coordinate(latitude + unit, longitude);
                    coords[2] = new Coordinate(latitude + unit, longitude + unit);
                    coords[3] = new Coordinate(latitude, longitude + unit);
                    coords[4] = new Coordinate(latitude, longitude);
                    geometries[i][j] = geometryFactory.createPolygon(geometryFactory.createLinearRing(coords), null);
                    System.out.println(geometries[i][j]);
                    //geometries[i][j] = geometryFactory.createPoint(new Coordinate(latitude, longitude));
                }
            }
            long time = System.currentTimeMillis();
            for (int day = 3; day < 13; day++) {
                if(day==7||day==8)
                    continue;
                for (int hour = 0; hour < 24; hour++) {
                    for (int minute = 0; minute < 60; minute += 60) {
                        ArrayList<Disruption> disruptionsList = new ArrayList<Disruption>();
                        // Get SAX Parser Factory
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        // Turn on validation, and turn off namespaces
                        factory.setValidating(false);
                        factory.setNamespaceAware(false);
                        SAXParser parser = factory.newSAXParser();
                        for (int i = 0; i < 4; i++) {
                            String name = "2015-02-" + convert(day) + "-" + convert(hour) + "." + convert(minute + i * 5);
                            String file = "/home/isuru/data/" + "2015-02-" + convert(day) + "/" + name + ".txt";
                            //System.out.println(file);
                            InputStream in = new FileInputStream(new File(file));

                            try {
                                parser.parse(in, new TrafficXMLHandler(disruptionsList));
                                break;
                            } catch (Exception e) {
                                System.out.println("Error in parsing");
                                disruptionsList.clear();
                            }
                        }
                        ArrayList<Disruption> disruptionsWithin = new ArrayList<Disruption>();
                        for (Disruption d : disruptionsList) {
                            if (d.state.contains("Active") && d.geometry.intersects(area)) {
                                disruptionsWithin.add(d);
                            }
                        }

                        Geometry traffic;
                        boolean found = true;
                        System.out.println((day - 3) + "," + (hour * 4 + minute / 15) +
                                "," + disruptionsWithin.size() + "," + (System.currentTimeMillis() - time));
                        for (int i = 0; i < rows; i++) {
                            for (int j = 0; j < cols; j++) {
                                found = false;
                                for (Disruption d : disruptionsWithin) {
                                    //System.out.println(d.geometry);
                                    if (geometries[i][j].intersects(d.geometry)) {
                                        bw.write((day - 3) + "," + (hour * 4 + minute / 15) + "," + i+","+ j + ",1\n");
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found)
                                    bw.write((day - 3) + "," + (hour * 4 + minute / 15) + "," + i+","+ j + ",0\n");


                            }
                        }

                    }
                }
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String convert(int d) {
        return (d + 100 + "").substring(1);
    }
}

