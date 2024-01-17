package it.unipi.adii.iot.wsms.services;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Timestamp;

public class MQTT implements MqttCallback {
	
	private static String broker = "tcp://127.0.0.1:1883";
	private static String clientId = "JavaCollector";
	private static String subTopic = "humidity_sample";
	private static String subTopic1 = "temperature_sample";
	private static String subTopic2 = "noise_sample";
	private static String pubTopic = "humidity";
	private static String pubTopic1 = "temperature";
	private static String pubTopic2 = "noise";
	private static MqttClient mqttClient = null;
	private short state = 0;
	private static final Logger logger = LogManager.getLogger(MQTT.class);
	private static final DBService th = DBService.getInstance();

	public MQTT() {
		do {
			int timeWindow = 50000;
			try {
				mqttClient = new MqttClient(broker, clientId);
				mqttClient.setCallback( this );
				mqttClient.connect();
				mqttClient.subscribe(subTopic);
				mqttClient.subscribe(subTopic1);
				mqttClient.subscribe(subTopic2);
				System.out.println("subscribe done to " + subTopic);
				System.out.println(mqttClient.isConnected());
			}catch(MqttException me) {
				logger.error("Retrying to connect to MQTT...", me);
				try {
					Thread.sleep(timeWindow);
				} catch (InterruptedException e) {
					logger.error("MQTT: Thread sleep error.", e);
				}
			}
		}while(!mqttClient.isConnected());
		logger.info("MQTT Connected!");
	}
	
	public void publish (String topic, String content, String node) {
		try {
			MqttMessage message = new MqttMessage(content.getBytes());
			mqttClient.publish(topic, message);
			logger.info("MQTT humidity switch published.");
		} catch(MqttException e) {
			logger.error("Publish failed.", e);
		}
	}

	
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		logger.error("Connection lost");
		int timeWindow = 3000;
		while (!mqttClient.isConnected()) {
			try {
				logger.warn("Trying to reconnect in " + timeWindow/1000 + " seconds.");
				Thread.sleep(timeWindow);
				logger.warn("Reconnecting...");
				timeWindow *= 2;
				mqttClient.connect();
				mqttClient.subscribe(subTopic);
				logger.warn("Connection restored");
			}catch(MqttException me) {
				logger.error("Unable to connect to MQTT collector", me);
			} catch (InterruptedException e) {
				logger.error("MQTT: thread sleep error.", e);
			}
		}
	}

	public void messageArrived(String topic, MqttMessage message) {
		byte[] payload = message.getPayload();
		logger.info("Message arrived: " + new String(payload));
		System.out.println("Message arrived: " + new String(payload));
		try {
			JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(payload));
			System.out.println("message parsed");
			if (sensorMessage.containsKey("humidity")) {
				long timestamp = Long.parseLong(sensorMessage.get("timestamp").toString());
				Timestamp ts = new Timestamp(timestamp);
				Integer value = Integer.parseInt(sensorMessage.get("humidity").toString());
				String nodeId = sensorMessage.get("node").toString();
				if(!th.checkSensorExistence(nodeId)) {
					th.addSensor(nodeId, "humidity");
				}
				th.addObservation(nodeId, value, ts);
				int lower = 30;
				int upper = 60;
				boolean on = false;
				String reply;
				
				if (value > lower && value <= upper) {
						reply = "good_h";
						publish(pubTopic, reply, nodeId);
                    	logger.info("[NORMAL] - "+nodeId+" - the humidity is comfortable!");
			System.out.println("[NORMAL] - "+nodeId+" - the humidity is comfortable!");
                    	//th.updateSensorState(nodeId, (short)0);
				} else if(value > upper)
				{
						reply = "dec_h";
						publish(pubTopic, reply, nodeId);
                    	logger.info("[CRITICAL] - "+nodeId+" - the humidity is too low!");
                    	th.updateSensorState(nodeId, (short)0);
				} else {
						reply = "inc_h";
						publish(pubTopic, reply, nodeId);
                    	logger.info("[CRITICAL] - "+nodeId+" - the humidity is too low.");
                    	//th.updateSensorState(nodeId, (short)0);
				}
				 
			}
			if (sensorMessage.containsKey("temperature")) {
				long timestamp = Long.parseLong(sensorMessage.get("timestamp").toString());
				Timestamp ts = new Timestamp(timestamp);
				Integer value = Integer.parseInt(sensorMessage.get("temperature").toString());
				String nodeId = sensorMessage.get("node").toString();
				if(!th.checkSensorExistence(nodeId)) {
					th.addSensor(nodeId, "temperature");
				}
				th.addObservation(nodeId, value, ts);
				int lower = 19;
				int upper = 24;
				boolean on = false;
				String reply;

				if (value > lower && value <= upper) {
						reply = "good_t";
						publish(pubTopic1, reply, nodeId);
						logger.info("[NORMAL] - "+nodeId+" - the temperature is comfortable!");
						//th.updateSensorState(nodeId, (short)0);
					
				} else if(value > upper)
				{
						reply = "dec_t";
						publish(pubTopic1, reply, nodeId);
						logger.info("[CRITICAL] - "+nodeId+" - the temperature is too high!");
						//th.updateSensorState(nodeId, (short)0);
				} else {
						reply = "inc_t";
						publish(pubTopic1, reply, nodeId);
						logger.info("[NORMAL] - "+nodeId+" - the temperature is too low.");
						//th.updateSensorState(nodeId, (short)0);
				}

			}
			if (sensorMessage.containsKey("noise")) {
				long timestamp = Long.parseLong(sensorMessage.get("timestamp").toString());
				Timestamp ts = new Timestamp(timestamp);
				Integer value = Integer.parseInt(sensorMessage.get("noise").toString());
				String nodeId = sensorMessage.get("node").toString();
				if(!th.checkSensorExistence(nodeId)) {
					th.addSensor(nodeId, "noise");
				}
				th.addObservation(nodeId, value, ts);
				int upper = 60;
				boolean on = false;
				String reply;

				if(value > upper)
				{
						reply = "dec_n";
						publish(pubTopic2, reply, nodeId);
						logger.info("[CRITICAL] - "+nodeId+" - the noise is too high!");
						//th.updateSensorState(nodeId, (short)0);
				} else {
						state = 0;
						reply = "good_n";
						publish(pubTopic2, reply, nodeId);
						logger.info("[NORMAL] - "+nodeId+" - the noise level is comfortable.");
						//th.updateSensorState(nodeId, (short)0);
				}

			}
		} catch (ParseException e) {
			logger.error("Parse exception", e);
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		logger.info("Delivery completed");
	}

}
