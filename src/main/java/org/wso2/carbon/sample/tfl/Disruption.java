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
    boolean isMultiPolygon = true;
    final static double tolerance = 0.0005;

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
        isMultiPolygon = false;
        String[] temp = coords.split(",");
        coordinates = new StringBuilder("{ \n 'type': 'Polygon', \n 'coordinates': [[");
        for (int i = 0; i < temp.length - 1; i += 2) {
            if (i != 0) {
                coordinates.append(",");
            }
            coordinates.append("[").append(temp[i]).append(",").append(temp[i + 1]).append("]");
        }
        coordinates.append("]] \n }");
    }

    public void addCoordsLane(String coords) {
        String[] temp = coords.split(",");
        if (temp.length != 4) {
            System.out.println("length " + coords);
            return;
        }
        double x1, x2, y1, y2;
        x1 = Double.parseDouble(temp[0]);
        y1 = Double.parseDouble(temp[1]);
        x2 = Double.parseDouble(temp[2]);
        y2 = Double.parseDouble(temp[3]);

        double f = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow(y1 - y2, 2));
        if (coordinates == null) {
            coordinates = new StringBuilder("{ \n 'type': 'MultiPolygon', \n 'coordinates': [");
        } else {
            coordinates.append(",");
        }
        coordinates.append("\n[[");
        coordinates.append("[").append(Double.parseDouble(temp[0]) - tolerance * (y2 - y1) / f).append(",").append(Double.parseDouble(temp[1]) - tolerance * (x1 - x2) / f).append("]").append(",");
        coordinates.append("[").append(Double.parseDouble(temp[0]) + tolerance * (y2 - y1) / f).append(",").append(Double.parseDouble(temp[1]) + tolerance * (x1 - x2) / f).append("]").append(",");
        coordinates.append("[").append(Double.parseDouble(temp[2]) + tolerance * (y2 - y1) / f).append(",").append(Double.parseDouble(temp[3]) + tolerance * (x1 - x2) / f).append("]").append(",");
        coordinates.append("[").append(Double.parseDouble(temp[2]) - tolerance * (y2 - y1) / f).append(",").append(Double.parseDouble(temp[3]) - tolerance * (x1 - x2) / f).append("]");

        coordinates.append("]]");
    }

    public void end() {
        if (coordinates != null) {
            if (isMultiPolygon) {
                coordinates.append("] \n }");
            }
        }
    }

    public void createMsg() {
        jsonMsg = new StringBuilder("{ \n");
        jsonMsg.append("'id': ").append(id).append(", \n")
                .append("'properties': { \n")
                .append(" 'timeStamp': ").append(System.currentTimeMillis()).append(", \n")
                .append(" 'state': '").append(state).append("', \n")
                .append(" 'information': ").append("'Location- ").append(location).append(" Comments- ").append(comments).append("'").append("\n")
                .append(" }, \n")
                .append("'geometry' : ").append(coordinates).append("\n}");
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
