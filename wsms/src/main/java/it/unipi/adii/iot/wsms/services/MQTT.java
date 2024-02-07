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
import java.util.HashMap;

import static it.unipi.adii.iot.wsms.utils.Parameters.*;
import it.unipi.adii.iot.wsms.utils.Parameters;
import java.sql.Timestamp;

public class MQTT implements MqttCallback {

	private static final String subTopic = "humidity_sample";
	private static final String subTopic1 = "brightness_sample";
	private static String commandH = "";

	private static final HashMap<String, String> commandB = new HashMap<>();
	private static MqttClient mqttClient = null;
	private static final Logger logger = LogManager.getLogger(MQTT.class);
	private static final DBService th = DBService.getInstance();

	public MQTT() {
		do {
			int timeWindow = 50000;
			try {
				String broker = "tcp://127.0.0.1:1883";
				String clientId = "Collector";
				mqttClient = new MqttClient(broker, clientId);
				mqttClient.setCallback( this );
				mqttClient.connect();
				mqttClient.subscribe(subTopic);
				mqttClient.subscribe(subTopic1);
				System.out.println("subscribe done to " + subTopic);
				System.out.println("subscribe done to " + subTopic1);
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
		Timestamp ts;
		String reply = "";
		try {
			JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(payload));
			//ts = Parameters.adjustTime(15);
			if (sensorMessage.containsKey("humidity")) {
				int value = Integer.parseInt(sensorMessage.get("humidity").toString());
				String nodeId = sensorMessage.get("node").toString();
				int mode = Integer.parseInt(sensorMessage.get("mode").toString());
				if(!th.checkSensorExistence(nodeId)) {
					th.addSensor(nodeId, "humidity");
				}
				DBService.addObservation(nodeId, value);
				String pubTopic = "humidity";
				if(mode == 1)
				{
					reply = offCommand;
					if(!commandH.equals(offCommand))
						publish(pubTopic, reply, nodeId);
					logger.info("[MANUAL MODE] - "+nodeId+" - the humidity is controlled manually");
				}
				else if (value >= getLowerBound("humidity") && value <= getUpperBound("humidity")) {
					reply = goodCommand;
					if(!commandH.equals(goodCommand))
						publish(pubTopic, reply, nodeId);
					logger.info("[NORMAL] - "+nodeId+" - the humidity is comfortable!");
				} else if(value > getUpperBound("humidity"))
				{
					reply = decCommand;
					if(!commandH.equals(decCommand))
						publish(pubTopic, reply, nodeId);
					logger.warn("[CRITICAL] - "+nodeId+" - the humidity is too high!");
					th.updateSensorState(nodeId, (short)0);
				} else if(value < getLowerBound("humidity")){
					reply = incCommand;
					if(!commandH.equals(incCommand))
						publish(pubTopic, reply, nodeId);
					logger.warn("[CRITICAL] - "+nodeId+" - the humidity is too low.");
				}

				commandH = reply;

			}
			if (sensorMessage.containsKey("brightness")) {

				Integer value = Integer.parseInt(sensorMessage.get("brightness").toString());
				String nodeId = sensorMessage.get("node").toString();
				Integer mode = Integer.parseInt(sensorMessage.get("mode").toString());

				if(!commandB.containsKey(nodeId))
					commandB.put(nodeId, " ");

				if(!th.checkSensorExistence(nodeId)) {
					th.addSensor(nodeId, "brightness");
				}
				ts = Parameters.adjustTime(15);
				DBService.addObservation(nodeId, value);
				String pubTopic1 = "brightness";
				int lux = 100;
				if(mode == 2)
				{
					reply = offCommand;
					if(!commandB.get(nodeId).equals("off"))
						publish(pubTopic1, reply, nodeId);

					logger.info("[MANUAL MODE] - "+nodeId+" - the brightness is controlled manually");
				}
				else if(mode == 1 && (value + lux) > getUpperBound("brightness"))
				{
					reply = decCommand;
					if(!commandB.get(nodeId).equals(reply))
						publish(pubTopic1, reply, nodeId);
					logger.info("[CRITICAL] - "+nodeId+" - the brightness is too high!");
				} else if(mode == 0 && value < getLowerBound("brightness")) {
					reply = incCommand;
					System.out.println("comm:" + reply + " commB: " + commandB.get(nodeId));
					if(!commandB.get(nodeId).equals(reply))
						publish(pubTopic1, reply,
								nodeId);

					logger.info("[CRITICAL] - "+nodeId+" - the brightness is too low.");
				}else{
					reply = goodCommand;
					if(!commandB.get(nodeId).equals(reply))
						publish(pubTopic1, reply, nodeId);
					logger.info("[NORMAL] - "+nodeId+" - the brightness is comfortable!");
				}
				commandB.put(nodeId, reply);

			}

		} catch (ParseException e) {
			logger.error("Parse exception", e);
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		logger.info("Delivery completed");
	}

}