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

public class MQTT implements MqttCallback {
	
	private static String broker = "tcp://127.0.0.1:1883";
	private static String clientId = "JavaCollector";
	private static String subTopic = "temperature";
	private static String pubTopic = "switch";
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
	
	public void publish (String content, String node) {
		try {
			MqttMessage message = new MqttMessage(content.getBytes());
			mqttClient.publish(pubTopic+node, message);
			logger.info("MQTT temperature switch published.");
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
		try {
			JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(payload));
			if (sensorMessage.containsKey("temperature")) {
				int timestamp = Integer.parseInt(sensorMessage.get("timestamp").toString());
				Integer value = Integer.parseInt(sensorMessage.get("temperature").toString());
				String nodeId = sensorMessage.get("node").toString();
				if(!th.checkSensorExistence("mqtt://"+nodeId)) {
					th.addSensor("mqtt://"+nodeId);
				}
				th.addObservation("mqtt://"+nodeId, value, timestamp);
				int lower = 99;
				int upper = 125;
				boolean on = false;
				String reply;
				
				if (value > lower && value <= upper) {
					if(state != 1) {
						state = 1;
						reply = "y";
						publish(reply, nodeId);
                    	logger.info("[WARNING] - "+nodeId+" - the temperature is too high!");
                    	th.updateSensorState("mqtt://"+nodeId, state);
					}
				} else if(value > upper)
				{
					if(state != 2) {
						state = 2;
						reply = "r";
						publish(reply, nodeId);
                    	logger.info("[CRITICAL] - "+nodeId+" - the temperature is too low!");
                    	th.updateSensorState("mqtt://"+nodeId, state);
					}
				} else {
					if(state != 0) {
						state = 0;
						reply = "g";
						publish(reply, nodeId);
                    	logger.info("[NORMAL] - "+nodeId+" - the temperature is comfortable.");
                    	th.updateSensorState("mqtt://"+nodeId, state);
					}
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
