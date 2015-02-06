package org.wso2.carbon.sample.tfl;

/**
 * Created by sanka on 2/6/15.
 */
public class Disruption {
    String id;
    String state;
    String location;
    String comments;
    StringBuilder coordinates = null;
    StringBuilder jsonMsg = null;
    final static double tolerance = 0.005;

    public Disruption() {
        jsonMsg = new StringBuilder();
    }

    public Disruption(String id, String severity, String location, String comments) {
        this.id = id;
        this.state = severity;
        this.comments = comments;
        this.location = location;
    }

    public void setCoordsPoly(String coords) {
        String[] temp = coords.split(",");
        coordinates = new StringBuilder("{ \n \"type\": \"Polygon\", \n \"coordinates\": [[");
        for (int i = 0; i < temp.length; i += 2) {
            if (i != 0) {
                coordinates.append(",");
            }
            coordinates.append("[").append(temp[i]).append(",").append(temp[i + 1]).append("]");
        }
    }

    public void addCoordsLane(String coords) {
        String[] temp = coords.split(",");
        if (temp.length != 4) {
            System.out.println(coords);
            return;
        }
        double x1 = Double.parseDouble(temp[0]);
        double y1 = Double.parseDouble(temp[1]);
        double x2 = Double.parseDouble(temp[2]);
        double y2 = Double.parseDouble(temp[3]);

        double f = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow(y1 - y2, 2));
        if (coordinates == null) {
            coordinates = new StringBuilder("{ \n \"type\": \"multiPolygon\", \n \"coordinates\": [[");
        } else {
            coordinates.append(",");
        }
        coordinates.append("\n[");
        for (int i = 0; i < temp.length; i += 2) {
            if (i != 0) {
                coordinates.append(",");
            }
            coordinates.append("[").append(Double.parseDouble(temp[i]) - tolerance * (y2 - y1) / f).append(",").append(Double.parseDouble(temp[i + 1]) - tolerance * (x1 - x2) / f).append("]").append(",");
            coordinates.append("[").append(Double.parseDouble(temp[i]) + tolerance * (y2 - y1) / f).append(",").append(Double.parseDouble(temp[i + 1]) + tolerance * (x1 - x2) / f).append("]");
        }
        coordinates.append("]");
    }

    public void createMsg() {
        System.out.println("creating jsonMsg");
        jsonMsg = new StringBuilder("{ \n");
        jsonMsg.append("\"id\": ").append(id).append(", \n")
                .append("\"properties\": { \n")
                .append(" \"timeStamp\": ").append(System.currentTimeMillis()).append(", \n")
                .append(" \"state\": \"").append(state).append("\", \n")
                .append(" \"information\": ").append("\"Location: ").append(location).append(" Comments: ").append(comments).append("\"").append("\n")
                .append(" } \n")
                .append("\"geometry\": ").append(coordinates).append("]] \n } \n}");
    }

    @Override
    public String toString() {
        /*if (jsonMsg == null) {
            createMsg();
        }*/
        createMsg();
        //System.out.println("toString" + String.valueOf(jsonMsg));
        return String.valueOf(jsonMsg);
    }
}
