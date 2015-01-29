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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;


public class TflStream {
	private static QueueConnectionFactory queueConnectionFactory = null;

	private static final List<String> xmlMsgs = new ArrayList();
	private static String msg;
	static HashMap<String, BusStop> map = new HashMap<String, BusStop>();
	static ConcurrentHashMap<String, Bus> busses = new ConcurrentHashMap<String, Bus>();
	public static long timeOffset;
	public static long lastTime = 0;
	public static SendData sendData = new SendData();

	public static void main(String[] args) throws XMLStreamException {
		queueConnectionFactory = JNDIContext.getInstance().getQueueConnectionFactory();
		String queueName = "";
		if (args.length == 0 || args[0] == null || args[0].trim().equals("")) {
			queueName = "TflStream";
		} else {
			queueName = args[0];
		}

		publish(queueName, xmlMsgs);
		try {
			long time = System.currentTimeMillis();
			sendGetStops();
			System.out.println("Time to get stops: " + (System.currentTimeMillis() - time));
			sendData.currentTime = System.currentTimeMillis();
			GetData g = new GetData();
			g.start();
			Thread.sleep(1000);
			sendData.start();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Received the bus stop data.");
	}

	// HTTP GET request
	private static void sendGetStops() throws Exception {

		//String url = "http://localhost/TFL/stop.txt";
		String url = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?LineID=1&ReturnList=StopID,Latitude,Longitude";
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
		timeOffset = time - Long.parseLong(arr[2]);
		
		
		while ((inputLine = in.readLine()) != null) {
			inputLine = inputLine.replaceAll("[\\[\\]\"]", "");
			arr = inputLine.split(",");
			//System.out.println(Double.parseDouble(arr[3]));
			//System.out.println(Double.parseDouble(arr[2]));
			BusStop temp = new BusStop(arr[1], Double.parseDouble(arr[2]),
			                           Double.parseDouble(arr[3]));
			System.out.println(temp);
			map.put(arr[1], temp);
		}
		in.close();
		// print result
		// System.out.println(response.toString());

	}

	// stop, line, bus, time
	private static void sendGetBusses(String url) throws Exception {
		
	}

	/**
	 * Publish message to given queue
	 *
	 * @param queueName
	 *            - queue name to publish messages
	 * @param msgList
	 *            - message to send
	 */

	public static void publish(String queueName, List<String> msgList) throws XMLStreamException {
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
			System.out.println(msgList.size());
			for (int i = 0, msgsLength = msgList.size(); i < msgsLength; i++) {
				String xmlMessage = msgList.get(i);
				XMLStreamReader reader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
				                                                                                  xmlMessage.getBytes()));
				StAXOMBuilder builder = new StAXOMBuilder(reader);
				OMElement OMMessage = builder.getDocumentElement();
				TextMessage jmsMessage = session.createTextMessage(OMMessage.toString());
				producer.send(jmsMessage);
				//System.out.println("Tfl stream data " + (i + 1) + " sent");
			}
			producer.close();
			session.close();
			queueConnection.stop();
			queueConnection.close();
			System.out.println("Send all Tfl geodata messages.");
		} catch (JMSException e) {
			System.out.println("Can not subscribe." + e);
		}
	}
}
