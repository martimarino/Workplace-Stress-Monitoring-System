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
	private static String subTopic = "humidity_sample";
	private static String subTopic1 = "brightness_sample";
	private static String pubTopic = "humidity";
	private static String pubTopic1 = "brightness";
	private static MqttClient mqttClient = null;
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
				System.out.println("subscribe done to " + subTopic);
				System.out.println("subscribe done to " + subTopic1);
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
			mqttClient.publish(topic+"_"+node, message);
			logger.info("MQTT switch published.");
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
				mqttClient.subscribe(subTopic1);
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
				Integer value = Integer.parseInt(sensorMessage.get("humidity").toString());
				String nodeId = sensorMessage.get("node").toString();
				Integer mode = Integer.parseInt(sensorMessage.get("mode").toString());
				if(!th.checkSensorExistence(nodeId)) {
					th.addSensor(nodeId, "humidity");
				}
				th.addObservation(nodeId, value, timestamp);
				int lower = 30;
				int upper = 60;
				boolean on = false;
				String reply;

				if(mode == 1)
				{
					reply = "off_h";
					publish(pubTopic, reply, nodeId);
					logger.info("[MANUAL MODE] - "+nodeId+" - the humidity is controlled manually");
					System.out.println("[MANUAL MODE] - "+nodeId+" - the humidity is controlled manually");
				}
				else if (value > lower && value <= upper) {
					reply = "good";
					publish(pubTopic, reply, nodeId);
					logger.info("[NORMAL] - "+nodeId+" - the humidity is comfortable!");
					System.out.println("[NORMAL] - "+nodeId+" - the humidity is comfortable!");
				} else if(value > upper)
				{
					reply = "dec";
					publish(pubTopic, reply, nodeId);
					logger.info("[CRITICAL] - "+nodeId+" - the humidity is too low!");
					th.updateSensorState(nodeId, (short)0);
				} else {
					reply = "inc";
					publish(pubTopic, reply, nodeId);
					logger.info("[CRITICAL] - "+nodeId+" - the humidity is too low.");
				}

			}
			if (sensorMessage.containsKey("brightness")) {
				long timestamp = Long.parseLong(sensorMessage.get("timestamp").toString());
				Integer value = Integer.parseInt(sensorMessage.get("brightness").toString());
				String nodeId = sensorMessage.get("node").toString();
				Integer mode = Integer.parseInt(sensorMessage.get("mode").toString());
				if(!th.checkSensorExistence(nodeId)) {
					th.addSensor(nodeId, "brightness");
				}
				th.addObservation(nodeId, value, timestamp);
				int lower = 300;
				int upper = 400;
				boolean on = false;
				String reply;
				if(mode == 1)
				{
					reply = "off_h";
					publish(pubTopic, reply, nodeId);
					logger.info("[MANUAL MODE] - "+nodeId+" - the brightness is controlled manually");
					System.out.println("[MANUAL MODE] - "+nodeId+" - the brightness is controlled manually");
				}
				else if (value > lower && value <= upper) {
					reply = "good";
					publish(pubTopic1, reply, nodeId);
					logger.info("[NORMAL] - "+nodeId+" - the brightness is comfortable!");
				} else if(value > upper)
				{
					reply = "dec";
					publish(pubTopic1, reply, nodeId);
					logger.info("[CRITICAL] - "+nodeId+" - the brightness is too high!");
				} else {
					reply = "inc";
					publish(pubTopic1, reply, nodeId);
					logger.info("[CRITICAL] - "+nodeId+" - the brightness is too low.");
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