package it.unipi.adii.iot.wsms.device;

import it.unipi.adii.iot.wsms.services.DBService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import it.unipi.adii.iot.wsms.services.resources.ResourceRegistration;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;


public class IoTDevice {
	private static final int LOWER_BOUND_TEMP = 19; // in °C
	private static final int UPPER_BOUND_TEMP = 27; // in °C
	private static final int LOWER_BOUND_HUM = 30; // in %
	private static final int UPPER_BOUND_HUM = 60; // in %

	private static final int LOWER_BOUND_NOISE = 10; // in dB
	private static final int UPPER_BOUND_NOISE = 60; // in dB

	private static final Logger logger = LogManager.getLogger(IoTDevice.class);
	private static final DBService db_Service = DBService.getInstance();

	private final String ip;
	private String mode = "auto";
	boolean recoverMode = false;

	private CoapClient resSensor;
	private CoapClient resSwitch;
	private boolean stopObserve = false;



	public static int getLowerBound(String dataType) {
		switch (dataType) {
			case "temperature":
				return LOWER_BOUND_TEMP;
			case "humidity":
				return LOWER_BOUND_HUM;
			case "noise":
				return LOWER_BOUND_NOISE;
			default:
				return -1;
		}
	}

	public static int getUpperBound(String dataType) {
		switch (dataType) {
			case "temperature":
				return UPPER_BOUND_TEMP;
			case "humidity":
				return UPPER_BOUND_HUM;
			case "noise":
				return UPPER_BOUND_NOISE;
			default:
				return -1;
		}
	}


	public IoTDevice(String ipAddress, String dataType) {

		this.ip = ipAddress;
		this.resSensor = new CoapClient("coap://[" + ipAddress + "]/sensor");
		this.resSwitch = new CoapClient("coap://[" + ipAddress + "]/switch");
		/*
		System.out.println("SENT");
		String payload = "ON";
    	Request req = new Request(Code.PUT);
		req.setPayload(payload);
    	req.setURI("coap://[" + ip + "]/switch");
    	req.send();*/

		CoapObserveRelation observeProperty = this.resSensor.observe(
				new CoapHandler() {
					public void onLoad(CoapResponse response) {

						int nodeId = 0;
						long timestamp = 0;
						int value = 0;
						int isAuto = 1;

						boolean success = true;

						if(response.getResponseText() == null || response.getResponseText().isEmpty())
							return;

						try {
							JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(response.getPayload()));

							nodeId = Integer.parseInt(sensorMessage.get("node_id").toString());
							timestamp = Integer.parseInt(sensorMessage.get("timestamp").toString());
							value = Integer.parseInt(sensorMessage.get("value").toString());
							isAuto = Integer.parseInt(sensorMessage.get("isAuto").toString());

						} catch (ParseException pe) {
							System.out.println(response.getResponseText());
							logger.error("Impossible to parse the response!", pe);
							success = false;
						}

						if(ip.endsWith(Integer.toString(nodeId))) {
							if(!DBService.addObservation(ip, value, timestamp)) {
								logger.warn("Impossible to add new observation!");
								success = false;
							}
						} else {
							logger.warn("Message destination is incorrect!");
						}

						if(!success)
							return;

						System.out.println("Received: " + value);

						// request for warn message
						if (value < getLowerBound(dataType)) {
							recoverMode = true;
							logger.warn(dataType + " too low! (" + value + ")");
							String payload = "recover=inc";
							Request req = new Request(Code.PUT);
							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch");
							req.send();
						} else if (value > getUpperBound(dataType)) {
							recoverMode = true;
							logger.warn(dataType + " too high! (" + value + ")");
							String payload = "recover=dec";
							Request req = new Request(Code.PUT);
							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch");
							req.send();

						} else if (recoverMode) {
							recoverMode = false;
							logger.info(dataType + " at normal level. (" + value + ")");
							String payload = "color=g";
							Request req = new Request(Code.PUT);
							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch");
							req.send();
						}
						DBService.addObservation(ip, value, timestamp);

						// request for mode changed
						if(isAuto == 1 && mode.equals("man") ) {
							logger.info(dataType + " mode changed to: " + mode);
							String payload = "mode=auto";
							Request req = new Request(Code.PUT);
							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch");
							mode = "auto";
							req.send();
						} else if (isAuto == 0 && mode.equals("auto")) {
							logger.info(dataType + " mode changed to: " + mode);
							String payload = "mode=man";
							Request req = new Request(Code.PUT);
							req.setPayload(payload);
							req.setURI("coap://[" + ip + "]/switch");
							mode = "man";
							req.send();
						}
					}

					public void onError() {
						stopObserve = true;
//						logger.error("OBSERVING FAILED with " + dataType + " sensor " + ip);

						if (ResourceRegistration.removeDevice(ip)) {
							db_Service.deleteSensor(ip, dataType);
							logger.error("OBSERVING FAILED with {} sensor {}", dataType, ip);
						} else {
							logger.error("The sensor {} is not registered", ip);
						}

					}
				}, MediaTypeRegistry.APPLICATION_JSON);

		if (stopObserve) {
			observeProperty.proactiveCancel();
		}
	}

	public String getIP() {
		return ip;
	}

}
