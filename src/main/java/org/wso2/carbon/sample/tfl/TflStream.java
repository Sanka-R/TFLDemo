/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.tfl;

import javax.jms.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;

import java.util.Random;

public class TflStream {
	private static QueueConnectionFactory queueConnectionFactory = null;

	private static final List<String> xmlMsgs = new ArrayList();
	private static String msg;
	static HashMap<String, BusStop> map = new HashMap<String, BusStop>();
	static HashMap<String, Bus> busses = new HashMap<String, Bus>();

	public static void main(String[] args) throws XMLStreamException {
		Random random = new Random();
		queueConnectionFactory = JNDIContext.getInstance().getQueueConnectionFactory();
		TflStream publisher = new TflStream();
		String queueName = "";
		if (args.length == 0 || args[0] == null || args[0].trim().equals("")) {
			queueName = "TflStream";
		} else {
			queueName = args[0];
		}

		publisher.publish(queueName, xmlMsgs);
		try {
			long time = System.currentTimeMillis();
			map = sendGetStops();
			System.out.println("Time to get stops: " + (System.currentTimeMillis() - time));
			time = System.currentTimeMillis();
			sendGetBusses();
			System.out.println("Time to get busses: " + (System.currentTimeMillis() - time));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Received the bus stop data.");
	}

	// HTTP GET request
	private static HashMap<String, BusStop> sendGetStops() throws Exception {

		String url =
		             "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=1,2&ReturnList=StopID,Latitude,Longitude";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;

		HashMap<String, BusStop> map = new HashMap<String, BusStop>();
		in.readLine();
		while ((inputLine = in.readLine()) != null) {
			inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
			String arr[] = inputLine.split(",");
			BusStop temp = new BusStop(arr[1], Double.parseDouble(arr[2]),
			                           Double.parseDouble(arr[3]));
			map.put(arr[1], temp);
		}
		in.close();
		return map;
		// print result
		// System.out.println(response.toString());

	}

	// stop, lat, lon, line, bus, time
	private static void sendGetBusses() throws Exception {
		String url = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=1,2&ReturnList=StopID,Latitude,Longitude,LineID,VehicleID,EstimatedTime";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		in.readLine();
		while ((inputLine = in.readLine()) != null) {
			inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
			String arr[] = inputLine.split(",");

			Bus bus = busses.get(arr[5]);
			if (bus == null) {
				bus = new Bus(arr[5], Double.parseDouble(arr[3]), Double.parseDouble(arr[2]));
				busses.put(arr[5], bus);
			} else {

			}
			// System.out.println(count ++);
		}
		in.close();
		System.out.println("Added busses to a hashmap.");
	}

	/**
	 * Publish message to given queue
	 *
	 * @param queueName
	 *            - queue name to publish messages
	 * @param msgList
	 *            - message to send
	 */

	public void publish(String queueName, List<String> msgList) throws XMLStreamException {
		// create queue connection
		QueueConnection queueConnection = null;
		try {
			queueConnection = queueConnectionFactory.createQueueConnection();
			queueConnection.start();
		} catch (JMSException e) {
			System.out.println("Can not create queue connection." + e);
			return;
		}
		// create session, producer, message and send message to given
		// destination(queue)
		// OMElement message text is published here.
		Session session = null;
		try {
			session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(queueName);
			MessageProducer producer = session.createProducer(queue);
			System.out.println("Sending XML messages on '" + queueName + "' queue");
			for (int i = 0, msgsLength = msgList.size(); i < msgsLength; i++) {
				String xmlMessage = msgList.get(i);
				XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
				                                                                                  xmlMessage.getBytes()));
				StAXOMBuilder builder = new StAXOMBuilder(reader);
				OMElement OMMessage = builder.getDocumentElement();
				TextMessage jmsMessage = session.createTextMessage(OMMessage.toString());
				producer.send(jmsMessage);
				System.out.println("Tfl stream data " + (i + 1) + " sent");
			}
			producer.close();
			session.close();
			queueConnection.stop();
			queueConnection.close();
		} catch (JMSException e) {
			System.out.println("Can not subscribe." + e);
		}
	}
}
